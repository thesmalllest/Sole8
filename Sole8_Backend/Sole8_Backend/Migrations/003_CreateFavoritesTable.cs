using FluentMigrator;

namespace Sole8_Backend.Migrations
{
    [Migration(3)]
    public class CreateFavoritesTable : Migration
    {
        public override void Up()
        {
            Create.Table("favorites")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("user_id").AsInt32().NotNullable()
                .WithColumn("product_id").AsInt32().NotNullable()
                .WithColumn("created_at").AsDateTime().NotNullable();

            // =========================
            // FOREIGN KEYS
            // =========================
            Create.ForeignKey("fk_favorites_users")
                .FromTable("favorites").ForeignColumn("user_id")
                .ToTable("users").PrimaryColumn("id");

            Create.ForeignKey("fk_favorites_products")
                .FromTable("favorites").ForeignColumn("product_id")
                .ToTable("products").PrimaryColumn("id");

            // =========================
            // UNIQUE (ВАЖНО ДЛЯ ON CONFLICT)
            // =========================
            Create.UniqueConstraint("uq_favorites_user_product")
                .OnTable("favorites")
                .Columns("user_id", "product_id");
        }

        public override void Down()
        {
            Delete.ForeignKey("fk_favorites_users").OnTable("favorites");
            Delete.ForeignKey("fk_favorites_products").OnTable("favorites");

            Delete.UniqueConstraint("uq_favorites_user_product").FromTable("favorites");

            Delete.Table("favorites");
        }
    }
}