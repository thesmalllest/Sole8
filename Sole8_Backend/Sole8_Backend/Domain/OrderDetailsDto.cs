namespace Sole8_Backend.Domain;

public class OrderDetailsDto
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public decimal TotalPrice { get; set; }
    public string Status { get; set; } = "";
    public DateTime CreatedAt { get; set; }
    public string DeliveryAddress { get; set; } = "";
    public string PhoneNumber { get; set; } = "";
    public List<OrderItemDto> Items { get; set; } = new();
}