using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Sole8_Backend.Data;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Controllers;

[Authorize]
[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
{
    private readonly OrderRepository _repo;

    public OrdersController(OrderRepository repo)
    {
        _repo = repo;
    }

    // CREATE ORDER
    [HttpPost("create")]
    public async Task<IActionResult> Create([FromBody] CheckoutRequest req)
    {
        int userId = this.GetUserId();

        int orderId = await _repo.CreateOrderAsync(
            userId,
            req.DeliveryAddress,
            req.PhoneNumber
        );

        return Ok(new { orderId });
    }

    // GET USER ORDERS
    [HttpGet]
    public async Task<IActionResult> GetUserOrders()
    {
        int userId = this.GetUserId();
        var orders = await _repo.GetOrdersAsync(userId);
        return Ok(orders);
    }

    // GET ORDER DETAILS
    [HttpGet("details/{orderId}")]
    public async Task<IActionResult> GetDetails(int orderId)
    {
        var order = await _repo.GetDetailsAsync(orderId);
        if (order == null) return NotFound();

        int currentUserId = this.GetUserId();

        if (order.UserId != currentUserId)
            return Forbid();

        return Ok(order);
    }
}