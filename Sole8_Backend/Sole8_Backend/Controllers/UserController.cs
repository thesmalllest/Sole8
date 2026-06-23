using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Sole8_Backend.Data;
using Sole8_Backend.Domain;
using System.Globalization;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;

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
        var validationError = ValidateRegistration(dto);

        if (validationError != null)
            return BadRequest(new { message = validationError });

        var firstName = dto.FirstName.Trim();
        var lastName = dto.LastName.Trim();
        var birthDate = dto.BirthDate.Trim();
        var gender = dto.Gender.Trim().ToUpperInvariant();
        var username = dto.Username.Trim();
        var email = dto.Email.Trim().ToLowerInvariant();
        var password = dto.Password;

        if (await _repo.IsEmailTakenAsync(email))
            return BadRequest(new { message = "Email is already registered." });

        if (await _repo.IsUsernameTakenAsync(username))
            return BadRequest(new { message = "Username is already taken." });

        var salt = Guid.NewGuid().ToString();
        var hash = ComputeHash(password + salt);

        var user = new User
        {
            FirstName = firstName,
            LastName = lastName,
            BirthDate = birthDate,
            Gender = gender,
            Username = username,
            Email = email,
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
        var email = req.Email?.Trim().ToLowerInvariant() ?? "";
        var password = req.Password ?? "";

        if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
            return BadRequest(new { message = "Email and password are required." });

        if (!IsValidEmail(email))
            return BadRequest(new { message = "Invalid email address." });

        var user = await _repo.GetByEmailAsync(email);

        if (user == null)
            return Unauthorized(new { message = "Email not found." });

        var computed = ComputeHash(password + user.PasswordSalt);

        if (user.PasswordHash != computed)
            return Unauthorized(new { message = "Wrong password." });

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
            return NotFound(new { message = "User not found." });

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
            return NotFound(new { message = "User not found." });

        var validationError = ValidateProfileUpdate(dto);

        if (validationError != null)
            return BadRequest(new { message = validationError });

        var firstName = dto.FirstName.Trim();
        var lastName = dto.LastName.Trim();
        var birthDate = dto.BirthDate.Trim();
        var gender = dto.Gender.Trim().ToUpperInvariant();
        var username = dto.Username.Trim();
        var email = dto.Email.Trim().ToLowerInvariant();

        if (await _repo.IsEmailTakenAsync(email, userId))
            return BadRequest(new { message = "Email is already used by another user." });

        if (await _repo.IsUsernameTakenAsync(username, userId))
            return BadRequest(new { message = "Username is already used by another user." });

        var updated = new User
        {
            Id = userId,
            FirstName = firstName,
            LastName = lastName,
            BirthDate = birthDate,
            Gender = gender,
            Username = username,
            Email = email,
            PasswordHash = existing.PasswordHash,
            PasswordSalt = existing.PasswordSalt,
            CreatedAt = existing.CreatedAt,
            UpdatedAt = DateTime.UtcNow
        };

        await _repo.UpdateProfileAsync(updated);

        return Ok(new { message = "Profile updated." });
    }

    // ---------------------- VALIDATION ----------------------
    private static string? ValidateRegistration(UserCreateDto dto)
    {
        var profileError = ValidateProfileFields(
            dto.FirstName,
            dto.LastName,
            dto.BirthDate,
            dto.Gender,
            dto.Username,
            dto.Email
        );

        if (profileError != null)
            return profileError;

        var password = dto.Password ?? "";

        if (string.IsNullOrWhiteSpace(password))
            return "Password is required.";

        if (!IsValidPassword(password))
            return "Password must be at least 8 characters long and contain both letters and numbers.";

        return null;
    }

    private static string? ValidateProfileUpdate(UserUpdateDto dto)
    {
        return ValidateProfileFields(
            dto.FirstName,
            dto.LastName,
            dto.BirthDate,
            dto.Gender,
            dto.Username,
            dto.Email
        );
    }

    private static string? ValidateProfileFields(
        string? firstName,
        string? lastName,
        string? birthDate,
        string? gender,
        string? username,
        string? email
    )
    {
        firstName = firstName?.Trim() ?? "";
        lastName = lastName?.Trim() ?? "";
        birthDate = birthDate?.Trim() ?? "";
        gender = gender?.Trim().ToUpperInvariant() ?? "";
        username = username?.Trim() ?? "";
        email = email?.Trim() ?? "";

        if (!IsValidName(firstName))
            return "First name must contain only letters, spaces or hyphens and be 2-50 characters long.";

        if (!IsValidName(lastName))
            return "Last name must contain only letters, spaces or hyphens and be 2-50 characters long.";

        if (gender != "M" && gender != "F")
            return "Gender must be M or F.";

        if (!IsValidUsername(username))
            return "Username must be 3-20 characters and contain only letters, numbers, dots or underscores.";

        if (!IsValidEmail(email))
            return "Invalid email address.";

        if (!IsValidBirthDate(birthDate))
            return "User must be at least 13 years old and birth date must be in dd/MM/yyyy format.";

        return null;
    }

    private static bool IsValidName(string value)
    {
        return Regex.IsMatch(value, @"^[A-Za-zА-Яа-яЁё\s\-]{2,50}$");
    }

    private static bool IsValidUsername(string value)
    {
        return Regex.IsMatch(value, @"^[A-Za-z0-9._]{3,20}$");
    }

    private static bool IsValidEmail(string value)
    {
        return Regex.IsMatch(value, @"^[^@\s]+@[^@\s]+\.[^@\s]+$");
    }

    private static bool IsValidBirthDate(string value)
    {
        if (!DateTime.TryParseExact(
                value,
                "dd/MM/yyyy",
                CultureInfo.InvariantCulture,
                DateTimeStyles.None,
                out var birthDate
            ))
        {
            return false;
        }

        var today = DateTime.Today;

        if (birthDate.Date > today)
            return false;

        var age = today.Year - birthDate.Year;

        if (birthDate.Date > today.AddYears(-age))
            age--;

        return age >= 13;
    }

    private static bool IsValidPassword(string value)
    {
        if (string.IsNullOrWhiteSpace(value))
            return false;

        if (value.Length < 8)
            return false;

        var hasLetter = value.Any(char.IsLetter);
        var hasDigit = value.Any(char.IsDigit);

        return hasLetter && hasDigit;
    }
}