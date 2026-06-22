using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Sole8_Backend.Data;
using Sole8_Backend.Domain;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace Sole8_Backend.Controllers;

[ApiController]
[Route("api/user")]
public class UserController : ControllerBase
{
    private readonly UserRepository _repo;
    private readonly IConfiguration _config; 
    
    public UserController(UserRepository repo, IConfiguration config)
    {
        _repo = repo;
        _config = config;
    }

    // ---------------------- REGISTER ----------------------
    [HttpPost("register")]
    public async Task<IActionResult> Register(UserCreateDto dto)
    {
        var salt = Guid.NewGuid().ToString();
        var hash = ComputeHash(dto.Password + salt);

        var user = new User
        {
            FirstName = dto.FirstName,
            LastName = dto.LastName,
            BirthDate = dto.BirthDate,
            Gender = dto.Gender,
            Username = dto.Username,
            Email = dto.Email,
            PasswordSalt = salt,
            PasswordHash = hash
        };

        var id = await _repo.CreateUserAsync(user);

        return Ok(new { userId = id });
    }

    // ---------------------- LOGIN ----------------------
    [HttpPost("login")]
    public async Task<IActionResult> Login(LoginRequest req)
    {
        var user = await _repo.GetByEmailAsync(req.Email);

        if (user == null)
            return Unauthorized("Email not found");

        var computed = ComputeHash(req.Password + user.PasswordSalt);

        if (user.PasswordHash != computed)
            return Unauthorized("Wrong password");

        var token = GenerateToken(user);

        return Ok(new
        {
            token,
            user = new UserDto
            {
                Id = user.Id,
                FirstName = user.FirstName,
                LastName = user.LastName,
                BirthDate = user.BirthDate,
                Gender = user.Gender,
                Username = user.Username,
                Email = user.Email
            }
        });
    }

    // ---------------------- JWT ----------------------
    private string GenerateToken(User user)
    {
        // Используем единую константу ключа
        var key = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(JwtConfig.SecretKey)
        );

        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
            new Claim(ClaimTypes.Email, user.Email)
        };

        var token = new JwtSecurityToken(
            claims: claims,
            expires: DateTime.UtcNow.AddHours(2),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    // ---------------------- HASH ----------------------
    private static string ComputeHash(string input)
    {
        using var sha = SHA256.Create();
        return Convert.ToHexString(
            sha.ComputeHash(Encoding.UTF8.GetBytes(input))
        );
    }

    // ---------------------- PROFILE ----------------------
    [Authorize]
    [HttpGet("profile")]
    public async Task<IActionResult> GetMyProfile()
    {
        int userId = this.GetUserId();

        var user = await _repo.GetByIdAsync(userId);

        if (user == null)
            return NotFound("User not found");

        return Ok(new UserDto
        {
            Id = user.Id,
            FirstName = user.FirstName,
            LastName = user.LastName,
            BirthDate = user.BirthDate,
            Gender = user.Gender,
            Username = user.Username,
            Email = user.Email
        });
    }

    // ---------------------- UPDATE PROFILE ----------------------
    [Authorize]
    [HttpPut("profile")]
    public async Task<IActionResult> UpdateMyProfile(UserUpdateDto dto)
    {
        int userId = this.GetUserId();

        var existing = await _repo.GetByIdAsync(userId);

        if (existing == null)
            return NotFound("User not found");

        var updated = new User
        {
            Id = userId,
            FirstName = dto.FirstName,
            LastName = dto.LastName,
            BirthDate = dto.BirthDate,
            Gender = dto.Gender,
            Username = dto.Username,
            Email = dto.Email,
            PasswordHash = existing.PasswordHash,
            PasswordSalt = existing.PasswordSalt,
            CreatedAt = existing.CreatedAt,
            UpdatedAt = DateTime.UtcNow
        };

        await _repo.UpdateProfileAsync(updated);

        return Ok(new { message = "Profile updated" });
    }
}
