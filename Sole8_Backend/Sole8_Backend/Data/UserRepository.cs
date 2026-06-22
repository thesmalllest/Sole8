using Dapper;
using Npgsql;
using Sole8_Backend.Domain;

namespace Sole8_Backend.Data;

public class UserRepository
{
    private readonly string _connectionString;

    public UserRepository(IConfiguration config)
    {
        _connectionString = config.GetConnectionString("DefaultConnection")!;
    }

    private NpgsqlConnection GetConn() => new(_connectionString);

    // REGISTER NEW USER
    public async Task<int> CreateUserAsync(User user)
    {
        const string sql = @"
            INSERT INTO users
            (first_name, last_name, birth_date, gender, username, email, password_hash, password_salt, created_at)
            VALUES
            (@FirstName, @LastName, @BirthDate, @Gender, @Username, @Email, @PasswordHash, @PasswordSalt, NOW())
            RETURNING id;
        ";

        using var conn = GetConn();
        return await conn.ExecuteScalarAsync<int>(sql, user);
    }

    // LOGIN — find user by email
    public async Task<User?> GetByEmailAsync(string email)
    {
        const string sql = @"
            SELECT
                id,
                first_name AS FirstName,
                last_name AS LastName,
                birth_date AS BirthDate,
                gender AS Gender,
                username AS Username,
                email AS Email,
                password_hash AS PasswordHash,
                password_salt AS PasswordSalt,
                created_at AS CreatedAt,
                updated_at AS UpdatedAt
            FROM users
            WHERE email = @email;
        ";

        using var conn = GetConn();
        return await conn.QueryFirstOrDefaultAsync<User>(sql, new { email });
    }

    // GET PROFILE BY ID
    public async Task<User?> GetByIdAsync(int id)
    {
        const string sql = @"
            SELECT
                id,
                first_name AS FirstName,
                last_name AS LastName,
                birth_date AS BirthDate,
                gender AS Gender,
                username AS Username,
                email AS Email,
                password_hash AS PasswordHash,
                password_salt AS PasswordSalt,
                created_at AS CreatedAt,
                updated_at AS UpdatedAt
            FROM users
            WHERE id = @id;
        ";

        using var conn = GetConn();
        return await conn.QueryFirstOrDefaultAsync<User>(sql, new { id });
    }
    
    // UPDATE PROFILE
    public async Task UpdateProfileAsync(User user)
    {
        const string sql = @"
        UPDATE users SET
            first_name = @FirstName,
            last_name = @LastName,
            birth_date = @BirthDate,
            gender = @Gender,
            username = @Username,
            email = @Email,
            updated_at = NOW()
        WHERE id = @Id;
    ";

        using var conn = GetConn();
        await conn.ExecuteAsync(sql, user);
    }

}
