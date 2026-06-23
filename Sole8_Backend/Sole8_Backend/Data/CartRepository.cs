using Dapper;
using Npgsql;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Data;

public class CartRepository
{
    private readonly string _connectionString;

    public CartRepository(IConfiguration config)
    {
        _connectionString = config.GetConnectionString("DefaultConnection")!;
    }

    private NpgsqlConnection GetConn() => new(_connectionString);

    // =========================
    // ADD ITEM
    // =========================
    public async Task AddAsync(int userId, int productId, int sizeId, int quantity)
    {
        if (quantity <= 0)
            throw new InvalidOperationException("Quantity must be greater than zero.");

        const string sql = @"
        INSERT INTO cart_items (user_id, product_id, size_id, quantity, added_at)
        SELECT @UserId, @ProductId, @SizeId, @Quantity, NOW()
        FROM product_sizes ps
        WHERE ps.id = @SizeId
          AND ps.product_id = @ProductId
          AND @Quantity <= ps.stock

        ON CONFLICT (user_id, product_id, size_id)
        DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity
        WHERE cart_items.quantity + EXCLUDED.quantity <= (
            SELECT stock
            FROM product_sizes
            WHERE id = @SizeId
              AND product_id = @ProductId
        );
    ";

        using var conn = GetConn();

        var rows = await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId,
            SizeId = sizeId,
            Quantity = quantity
        });

        if (rows == 0)
            throw new InvalidOperationException("Not enough stock.");
    }
    
    // =========================
    // REMOVE ITEM
    // =========================
    public async Task RemoveAsync(int userId, int productId, int sizeId)
    {
        const string sql = @"
            DELETE FROM cart_items
            WHERE user_id = @UserId
              AND product_id = @ProductId
              AND size_id = @SizeId;
        ";

        using var conn = GetConn();
        await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId,
            SizeId = sizeId
        });
    }

    // =========================
    // UPDATE QUANTITY
    // =========================
    public async Task UpdateQuantityAsync(int userId, int productId, int sizeId, int quantity)
    {
        if (quantity <= 0)
        {
            await RemoveAsync(userId, productId, sizeId);
            return;
        }

        const string sql = @"
        UPDATE cart_items ci
        SET quantity = @Quantity
        FROM product_sizes ps
        WHERE ci.user_id = @UserId
          AND ci.product_id = @ProductId
          AND ci.size_id = @SizeId
          AND ps.id = ci.size_id
          AND ps.product_id = ci.product_id
          AND @Quantity <= ps.stock;
    ";

        using var conn = GetConn();

        var rows = await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId,
            SizeId = sizeId,
            Quantity = quantity
        });

        if (rows == 0)
            throw new InvalidOperationException("Not enough stock.");
    }
    
    // =========================
    // GET USER CART
    // =========================
    public async Task<IEnumerable<CartItemDto>> GetUserCartAsync(int userId)
    {
        const string sql = @"
        SELECT 
            ci.id AS Id,
            ci.product_id AS ProductId,
            ci.size_id AS SizeId,
            ci.quantity AS Quantity,

            p.name AS Name,
            p.price AS Price,

            ps.size_value AS SizeValue,
            ps.stock AS Stock,

            (
                SELECT image_url
                FROM product_images
                WHERE product_id = p.id
                ORDER BY id
                LIMIT 1
            ) AS ImageUrl

        FROM cart_items ci
        JOIN products p ON p.id = ci.product_id
        JOIN product_sizes ps ON ps.id = ci.size_id
        WHERE ci.user_id = @UserId;
    ";

        using var conn = GetConn();
        return await conn.QueryAsync<CartItemDto>(sql, new { UserId = userId });
    }
    
    // =========================
    // CLEAR CART
    // =========================
    public async Task ClearCartAsync(int userId)
    {
        const string sql = @"
            DELETE FROM cart_items
            WHERE user_id = @UserId;
        ";

        using var conn = GetConn();
        await conn.ExecuteAsync(sql, new { UserId = userId });
    }
}