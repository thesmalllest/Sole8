using FluentMigrator;

namespace Sole8_Backend.Migrations
{
    [Migration(2)]
    public class CreateProductsTables : Migration
    {
        public override void Up()
        {
            // -----------------------------
            // PRODUCTS
            // -----------------------------
            Create.Table("products")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("name").AsString(150).NotNullable()
                .WithColumn("brand").AsString(50).NotNullable()
                .WithColumn("price").AsDecimal(10, 2).NotNullable()
                .WithColumn("description").AsString().Nullable()
                .WithColumn("model_3d_url").AsString().Nullable()
                .WithColumn("created_at").AsDateTime().NotNullable()
                .WithColumn("updated_at").AsDateTime().Nullable();

            // -----------------------------
            // PRODUCT IMAGES
            // -----------------------------
            Create.Table("product_images")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("product_id").AsInt32().NotNullable()
                .WithColumn("image_url").AsString().NotNullable();

            Create.ForeignKey("fk_product_images_products")
                .FromTable("product_images").ForeignColumn("product_id")
                .ToTable("products").PrimaryColumn("id");

            // -----------------------------
            // PRODUCT SIZES
            // -----------------------------
            Create.Table("product_sizes")
                .WithColumn("id").AsInt32().PrimaryKey().Identity()
                .WithColumn("product_id").AsInt32().NotNullable()
                .WithColumn("size_value").AsDecimal(4, 1).NotNullable()  // пример: 42.5
                .WithColumn("stock").AsInt32().NotNullable();           // остаток товара

            Create.ForeignKey("fk_product_sizes_products")
                .FromTable("product_sizes").ForeignColumn("product_id")
                .ToTable("products").PrimaryColumn("id");
        }

        public override void Down()
        {
            Delete.ForeignKey("fk_product_sizes_products").OnTable("product_sizes");
            Delete.ForeignKey("fk_product_images_products").OnTable("product_images");

            Delete.Table("product_sizes");
            Delete.Table("product_images");
            Delete.Table("products");
        }
    }
}
