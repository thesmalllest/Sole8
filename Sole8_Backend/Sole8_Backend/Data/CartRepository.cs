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
        const string sql = @"
            INSERT INTO cart_items (user_id, product_id, size_id, quantity, added_at)
            VALUES (@UserId, @ProductId, @SizeId, @Quantity, NOW())
            ON CONFLICT (user_id, product_id, size_id)
            DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity;
        ";

        using var conn = GetConn();
        await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId,
            SizeId = sizeId,
            Quantity = quantity
        });
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
        const string sql = @"
            UPDATE cart_items
            SET quantity = @Quantity
            WHERE user_id = @UserId
              AND product_id = @ProductId
              AND size_id = @SizeId;
        ";

        using var conn = GetConn();
        await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId,
            SizeId = sizeId,
            Quantity = quantity
        });
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