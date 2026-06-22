using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Sole8_Backend.Data;

namespace Sole8_Backend.Controllers;

[Authorize] 
[ApiController]
[Route("api/cart")]
public class CartController : ControllerBase
{
    private readonly CartRepository _repo;

    public CartController(CartRepository repo)
    {
        _repo = repo;
    }

    [HttpPost("add")]
    public async Task<IActionResult> Add(int productId, int sizeId, int quantity)
    {
        int userId = this.GetUserId(); 
        await _repo.AddAsync(userId, productId, sizeId, quantity);
        return Ok(new { message = "added" });
    }

    [HttpPost("remove")]
    public async Task<IActionResult> Remove(int productId, int sizeId)
    {
        int userId = this.GetUserId();
        await _repo.RemoveAsync(userId, productId, sizeId);
        return Ok(new { message = "removed" });
    }

    [HttpPost("update")]
    public async Task<IActionResult> Update(int productId, int sizeId, int quantity)
    {
        int userId = this.GetUserId();
        await _repo.UpdateQuantityAsync(userId, productId, sizeId, quantity);
        return Ok(new { message = "updated" });
    }

    [HttpGet]
    public async Task<IActionResult> Get()
    {
        int userId = this.GetUserId();
        var items = await _repo.GetUserCartAsync(userId);
        return Ok(items);
    }

    [HttpPost("clear")]
    public async Task<IActionResult> Clear()
    {
        int userId = this.GetUserId();
        await _repo.ClearCartAsync(userId);
        return Ok(new { message = "cleared" });
    }
}