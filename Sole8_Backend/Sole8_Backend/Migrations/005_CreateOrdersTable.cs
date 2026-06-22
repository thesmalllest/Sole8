using FluentMigrator;

namespace Sole8_Backend.Migrations
{
    [Migration(5)]
    public class CreateOrdersTable : Migration
    {
        public override void Up()
        {
            Create.Table("orders")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("user_id").AsInt32().NotNullable()
                .WithColumn("total_price").AsDecimal(10, 2).NotNullable()
                .WithColumn("status").AsString(20).NotNullable()
                .WithColumn("created_at").AsDateTime().NotNullable()
                .WithColumn("delivery_address").AsString(255).NotNullable()
                .WithColumn("phone_number").AsString(50).NotNullable();

            Create.ForeignKey("fk_orders_users")
                .FromTable("orders").ForeignColumn("user_id")
                .ToTable("users").PrimaryColumn("id");
        }

        public override void Down()
        {
            Delete.ForeignKey("fk_orders_users").OnTable("orders");
            Delete.Table("orders");
        }
    }
}