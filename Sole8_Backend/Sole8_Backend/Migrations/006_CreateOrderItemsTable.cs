using FluentMigrator;

namespace Sole8_Backend.Migrations
{
    [Migration(6)]
    public class CreateOrderItemsTable : Migration
    {
        public override void Up()
        {
            Create.Table("order_items")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("order_id").AsInt32().NotNullable()
                .WithColumn("product_id").AsInt32().NotNullable()
                .WithColumn("size_id").AsInt32().NotNullable()
                .WithColumn("price").AsDecimal(10, 2).NotNullable()   // цена в момент покупки
                .WithColumn("quantity").AsInt32().NotNullable();

            Create.ForeignKey("fk_order_items_orders")
                .FromTable("order_items").ForeignColumn("order_id")
                .ToTable("orders").PrimaryColumn("id");

            Create.ForeignKey("fk_order_items_products")
                .FromTable("order_items").ForeignColumn("product_id")
                .ToTable("products").PrimaryColumn("id");

            Create.ForeignKey("fk_order_items_sizes")
                .FromTable("order_items").ForeignColumn("size_id")
                .ToTable("product_sizes").PrimaryColumn("id");
        }

        public override void Down()
        {
            Delete.ForeignKey("fk_order_items_sizes").OnTable("order_items");
            Delete.ForeignKey("fk_order_items_products").OnTable("order_items");
            Delete.ForeignKey("fk_order_items_orders").OnTable("order_items");

            Delete.Table("order_items");
        }
    }
}