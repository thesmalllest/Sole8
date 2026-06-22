using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Sole8_Backend.Data;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Controllers;

[Authorize]
[ApiController]
[Route("api/favorites")]
public class FavoritesController : ControllerBase
{
    private readonly FavoritesRepository _repo;

    public FavoritesController(FavoritesRepository repo)
    {
        _repo = repo;
    }

    [HttpGet]
    public async Task<IActionResult> GetUserFavorites()
    {
        int userId = this.GetUserId();
        var result = await _repo.GetFavorites(userId);
        return Ok(result);
    }

    [HttpPost("add")]
    public async Task<IActionResult> Add([FromBody] FavoriteRequest req)
    {
        int userId = this.GetUserId();
        await _repo.AddAsync(userId, req.ProductId);
        return Ok(new { message = "Added to favorites" });
    }

    [HttpDelete("remove")]
    public async Task<IActionResult> Remove([FromBody] FavoriteRequest req)
    {
        int userId = this.GetUserId();
        await _repo.RemoveAsync(userId, req.ProductId);
        return Ok(new { message = "Removed from favorites" });
    }
}