using Dapper;
using Npgsql;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Data;

public class FavoritesRepository
{
    private readonly string _connectionString;

    public FavoritesRepository(IConfiguration config)
    {
        _connectionString = config.GetConnectionString("DefaultConnection")!;
    }

    private NpgsqlConnection GetConn() => new(_connectionString);

    // =========================
    // GET USER FAVORITES
    // =========================
    public async Task<IEnumerable<ProductListDto>> GetFavorites(int userId)
    {
        const string sql = @"
            SELECT 
                p.id,
                p.name,
                p.brand,
                p.price,
                (
                    SELECT image_url 
                    FROM product_images 
                    WHERE product_id = p.id 
                    ORDER BY id 
                    LIMIT 1
                ) AS ThumbnailUrl
            FROM favorites f
            JOIN products p ON p.id = f.product_id
            WHERE f.user_id = @UserId
            ORDER BY f.id DESC;
        ";

        using var conn = GetConn();

        return await conn.QueryAsync<ProductListDto>(sql, new
        {
            UserId = userId
        });
    }

    // =========================
    // ADD TO FAVORITES
    // =========================
    public async Task AddAsync(int userId, int productId)
    {
        const string sql = @"
            INSERT INTO favorites (user_id, product_id, created_at)
            VALUES (@UserId, @ProductId, NOW())
            ON CONFLICT (user_id, product_id) DO NOTHING;
        ";

        using var conn = GetConn();

        await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId
        });
    }

    // =========================
    // REMOVE FROM FAVORITES
    // =========================
    public async Task RemoveAsync(int userId, int productId)
    {
        const string sql = @"
            DELETE FROM favorites
            WHERE user_id = @UserId
              AND product_id = @ProductId;
        ";

        using var conn = GetConn();

        await conn.ExecuteAsync(sql, new
        {
            UserId = userId,
            ProductId = productId
        });
    }
}