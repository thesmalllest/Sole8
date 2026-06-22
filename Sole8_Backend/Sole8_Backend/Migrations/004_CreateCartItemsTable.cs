using FluentMigrator;

namespace Sole8_Backend.Migrations
{
    [Migration(4)]
    public class CreateCartItemsTable : Migration
    {
        public override void Up()
        {
            Create.Table("cart_items")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("user_id").AsInt32().NotNullable()
                .WithColumn("product_id").AsInt32().NotNullable()
                .WithColumn("size_id").AsInt32().NotNullable()
                .WithColumn("quantity").AsInt32().NotNullable()
                .WithColumn("added_at").AsDateTime().NotNullable().WithDefault(SystemMethods.CurrentDateTime);

            Create.ForeignKey("fk_cart_items_users")
                .FromTable("cart_items").ForeignColumn("user_id")
                .ToTable("users").PrimaryColumn("id");

            Create.ForeignKey("fk_cart_items_products")
                .FromTable("cart_items").ForeignColumn("product_id")
                .ToTable("products").PrimaryColumn("id");

            Create.ForeignKey("fk_cart_items_sizes")
                .FromTable("cart_items").ForeignColumn("size_id")
                .ToTable("product_sizes").PrimaryColumn("id");
            
            Create.UniqueConstraint("uq_cart_user_product_size")
                .OnTable("cart_items")
                .Columns("user_id", "product_id", "size_id");
        }

        public override void Down()
        {
            Delete.ForeignKey("fk_cart_items_sizes").OnTable("cart_items");
            Delete.ForeignKey("fk_cart_items_products").OnTable("cart_items");
            Delete.ForeignKey("fk_cart_items_users").OnTable("cart_items");

            Delete.Table("cart_items");
        }
    }
}