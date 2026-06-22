using Dapper;
using Npgsql;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Data;

public class OrderRepository
{
    private readonly string _conn;

    public OrderRepository(IConfiguration config)
    {
        _conn = config.GetConnectionString("DefaultConnection")!;
    }

    private NpgsqlConnection GetConn() => new(_conn);

    // ============================================================
    // 1. CREATE ORDER
    // ============================================================
   public async Task<int> CreateOrderAsync(int userId, string deliveryAddress, string phoneNumber)
    {
        using var conn = GetConn();
        await conn.OpenAsync();
        using var tx = conn.BeginTransaction();

        try
        {
            // 1. Получаем элементы корзины напрямую из таблицы cart_items
            // Дополнительно джойним таблицы, чтобы узнать актуальную цену товара и его название
            const string sqlCart = @"
                SELECT 
                    c.product_id AS ProductId,
                    c.size_id AS SizeId,
                    c.quantity AS Quantity,
                    p.price AS Price,
                    p.name AS ProductName
                FROM cart_items c
                JOIN products p ON p.id = c.product_id
                WHERE c.user_id = @UserId;
            ";

            // Читаем как строго типизированный список промежуточного класса, чтобы Dapper не падал
            var cartItems = (await conn.QueryAsync<CartOrderTmp>(sqlCart, new { UserId = userId }, tx)).ToList();

            if (!cartItems.Any())
                throw new Exception("Корзина пуста. Невозможно оформить заказ.");

            // 2. ВАЛИДАЦИЯ СКЛАДСКИХ ОСТАТКОВ И СПИСАНИЕ
            foreach (var item in cartItems)
            {
                // Запрашиваем текущий stock из базы с блокировкой строки FOR UPDATE
                const string sqlCheckStock = @"
                    SELECT stock 
                    FROM product_sizes 
                    WHERE id = @SizeId FOR UPDATE;
                ";

                int currentStock = await conn.ExecuteScalarAsync<int>(sqlCheckStock, new { SizeId = item.SizeId }, tx);

                if (currentStock < item.Quantity)
                {
                    throw new Exception($"Недостаточно товара на складе для позиции ID размера: {item.SizeId}. Доступно: {currentStock} шт.");
                }

                // Уменьшаем остаток на складе
                const string sqlUpdateStock = @"
                    UPDATE product_sizes 
                    SET stock = stock - @Quantity 
                    WHERE id = @SizeId;
                ";

                await conn.ExecuteAsync(sqlUpdateStock, new { Quantity = item.Quantity, SizeId = item.SizeId }, tx);
            }

            // 3. Считаем общую стоимость заказа
            decimal total = cartItems.Sum(i => i.Price * i.Quantity);

            // 4. Создаем сам заказ в таблице orders
            const string sqlOrder = @"
                INSERT INTO orders 
                (user_id, total_price, status, created_at, delivery_address, phone_number)
                VALUES 
                (@UserId, @Total, 'created', NOW(), @DeliveryAddress, @PhoneNumber)
                RETURNING id;
            ";

            int orderId = await conn.ExecuteScalarAsync<int>(sqlOrder, new
            {
                UserId = userId,
                Total = total,
                DeliveryAddress = deliveryAddress,
                PhoneNumber = phoneNumber
            }, tx);

            // 5. Заполняем позиции заказа в таблице order_items
            const string sqlItems = @"
                INSERT INTO order_items 
                (order_id, product_id, size_id, price, quantity)
                VALUES 
                (@OrderId, @ProductId, @SizeId, @Price, @Quantity);
            ";

            foreach (var item in cartItems)
            {
                await conn.ExecuteAsync(sqlItems, new
                {
                    OrderId = orderId,
                    ProductId = item.ProductId,
                    SizeId = item.SizeId,
                    Price = item.Price,
                    Quantity = item.Quantity
                }, tx);
            }

            // 6. Полностью очищаем корзину пользователя после успешной покупки
            await conn.ExecuteAsync(
                "DELETE FROM cart_items WHERE user_id = @UserId",
                new { UserId = userId },
                tx
            );

            await tx.CommitAsync();
            return orderId;
        }
        catch (Exception ex)
        {
            await tx.RollbackAsync();
            // Выводим ошибку в консоль бэкенда, чтобы её можно было прочитать при отладке
            Console.WriteLine($"[Order Error] Критическая ошибка при создании заказа: {ex.Message}");
            throw;
        }
    }

    // Вспомогательный приватный класс для безопасного маппинга Dapper внутри репозитория
    private class CartOrderTmp
    {
        public int ProductId { get; set; }
        public int SizeId { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
        public string ProductName { get; set; } = "";
    }
    
    // ============================================================
    // 2. GET ORDERS LIST
    // ============================================================
    public async Task<IEnumerable<OrderListItemDto>> GetOrdersAsync(int userId)
    {
        const string sql = @"
            SELECT 
                id AS Id,
                total_price AS TotalPrice,
                status AS Status,
                created_at AS CreatedAt
            FROM orders
            WHERE user_id = @UserId
            ORDER BY created_at DESC;
        ";

        using var conn = GetConn();
        return await conn.QueryAsync<OrderListItemDto>(sql, new { UserId = userId });
    }

    // ============================================================
    // 3. GET ORDER DETAILS
    // ============================================================
    public async Task<OrderDetailsDto?> GetDetailsAsync(int orderId)
    {
        const string sqlOrder = @"
            SELECT 
                id AS Id,
                user_id AS UserId,
                total_price AS TotalPrice,
                status AS Status,
                created_at AS CreatedAt,
                delivery_address AS DeliveryAddress,
                phone_number AS PhoneNumber
            FROM orders
            WHERE id = @OrderId;
        ";

        const string sqlItems = @"
            SELECT
                oi.product_id AS ProductId,
                p.name AS ProductName,
                oi.size_id AS SizeId,
                s.size_value AS SizeValue,
                oi.price AS Price,
                oi.quantity AS Quantity,
                (SELECT image_url FROM product_images WHERE product_id = p.id LIMIT 1) AS ImageUrl
            FROM order_items oi
            JOIN products p ON p.id = oi.product_id
            JOIN product_sizes s ON s.id = oi.size_id
            WHERE oi.order_id = @OrderId;
        ";

        using var conn = GetConn();

        var order = await conn.QueryFirstOrDefaultAsync<OrderDetailsDto>(
            sqlOrder,
            new { OrderId = orderId }
        );

        if (order == null)
            return null;

        var items = await conn.QueryAsync<OrderItemDto>(
            sqlItems,
            new { OrderId = orderId }
        );

        order.Items = items.ToList();

        return order;
    }

    private class OrderCartItemDto
    {
        public int ProductId { get; set; }
        public int SizeId { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }
}