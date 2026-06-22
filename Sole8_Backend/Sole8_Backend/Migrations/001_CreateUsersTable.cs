using FluentMigrator;

namespace Sole8_Backend.Migrations;

[Migration(1)]
public class CreateUsersTable : Migration
{
    public override void Up()
    {
        Create.Table("users")
            .WithColumn("id").AsInt32().PrimaryKey().Identity()
            .WithColumn("first_name").AsString(50).NotNullable()
            .WithColumn("last_name").AsString(50).NotNullable()
            .WithColumn("birth_date").AsString(20).NotNullable()
            .WithColumn("gender").AsString(1).NotNullable()   // "M" / "F"
            .WithColumn("username").AsString(50).NotNullable().Unique()
            .WithColumn("email").AsString(120).NotNullable().Unique()
            .WithColumn("password_hash").AsString(255).NotNullable()
            .WithColumn("created_at").AsDateTime().NotNullable()
            .WithColumn("updated_at").AsDateTime().Nullable()
            .WithColumn("password_salt").AsString(100).NotNullable();
    }

    public override void Down()
    {
        Delete.Table("users");
    }
}