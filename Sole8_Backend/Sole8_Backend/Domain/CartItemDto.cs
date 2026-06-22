namespace Sole8_Backend.Domain;

public class CartItemDto
{
    public int Id { get; set; }
    public int ProductId { get; set; }
    public int SizeId { get; set; }
    public int Quantity { get; set; }
    public string Name { get; set; } = "";
    public decimal Price { get; set; }
    public decimal SizeValue { get; set; }
    public string ImageUrl { get; set; } = ""; 
}