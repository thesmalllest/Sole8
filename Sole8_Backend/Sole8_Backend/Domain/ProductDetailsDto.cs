namespace Sole8_Backend.Domain;
public class ProductDetailsDto
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public string Brand { get; set; } = "";
    public decimal Price { get; set; }
    public string Description { get; set; } = "";

    public string? Model3DUrl { get; set; }

    public List<string> Images { get; set; } = new();
    public List<ProductSizeDto> Sizes { get; set; } = new();
}