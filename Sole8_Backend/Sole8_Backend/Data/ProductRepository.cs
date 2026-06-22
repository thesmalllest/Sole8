using Dapper;
using Npgsql;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Data;

public class ProductRepository
{
    private readonly string _conn;

    public ProductRepository(IConfiguration config)
    {
        _conn = config.GetConnectionString("DefaultConnection")!;
    }

    private NpgsqlConnection GetConn() => new(_conn);

    // LIST OF PRODUCTS
    public async Task<IEnumerable<ProductListDto>> GetAllAsync()
    {
        const string sql = @"
        SELECT DISTINCT
            p.id,
            p.name,
            p.brand,
            p.price,
            p.model_3d_url AS Model3DUrl,
            (SELECT image_url FROM product_images WHERE product_id = p.id LIMIT 1) AS ThumbnailUrl
        FROM products p
        ORDER BY p.id;
        ";

        using var conn = GetConn();
        return await conn.QueryAsync<ProductListDto>(sql);
    }

    // PRODUCT DETAILS
    public async Task<ProductDetailsDto?> GetDetailsAsync(int id)
    {
        const string sqlProduct = @"
            SELECT
                id,
                name,
                brand,
                price,
                description,
                model_3d_url AS Model3DUrl
            FROM products
            WHERE id = @id;
        ";

        using var conn = GetConn();

        var product = await conn.QueryFirstOrDefaultAsync<ProductDetailsDto>(sqlProduct, new { id });
        if (product == null) return null;

        const string sqlImages = @"
            SELECT image_url FROM product_images WHERE product_id = @id;
        ";

        const string sqlSizes = @"
            SELECT 
                MIN(id) AS Id, -- Берем любой ID для маппинга в DTO
                size_value AS SizeValue, 
                SUM(stock) AS Stock -- Схлопываем дубликаты размеров и суммируем их остатки
            FROM product_sizes
            WHERE product_id = @id
            GROUP BY size_value
            ORDER BY size_value ASC;
        ";
        
        product.Images = (await conn.QueryAsync<string>(sqlImages, new { id })).ToList();
        product.Sizes = (await conn.QueryAsync<ProductSizeDto>(sqlSizes, new { id })).ToList();

        return product;
    }
}