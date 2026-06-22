namespace Sole8_Backend.Domain;

public class OrderListItemDto
{
    public int Id { get; set; }
    public decimal TotalPrice { get; set; }
    public string Status { get; set; } = "";
    public DateTime CreatedAt { get; set; }
}