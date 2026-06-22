using Microsoft.AspNetCore.Mvc;
using Sole8_Backend.Data;

namespace Sole8_Backend.Controllers;

[ApiController]
[Route("api/products")]
public class ProductController : ControllerBase
{
    private readonly ProductRepository _repo;

    public ProductController(ProductRepository repo)
    {
        _repo = repo;
    }

    // GET ALL PRODUCTS
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var list = await _repo.GetAllAsync();
        return Ok(list);
    }

    // GET PRODUCT DETAILS
    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(int id)
    {
        var product = await _repo.GetDetailsAsync(id);

        if (product == null)
            return NotFound("Product not found");

        return Ok(product);
    }
}