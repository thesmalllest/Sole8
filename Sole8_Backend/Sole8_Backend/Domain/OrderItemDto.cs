namespace Sole8_Backend.Domain;

public class OrderItemDto
{
    public int ProductId { get; set; }
    public string ProductName { get; set; } = "";
    public string ImageUrl { get; set; } = "";
    public int SizeId { get; set; }
    public decimal SizeValue { get; set; }
    public decimal Price { get; set; }
    public int Quantity { get; set; }
}