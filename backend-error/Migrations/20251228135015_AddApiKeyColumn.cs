using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ErrorMonitoringAPI.Migrations
{
    /// <inheritdoc />
    public partial class AddApiKeyColumn : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "ApiKey",
                table: "Applications",
                type: "nvarchar(100)",
                maxLength: 100,
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "ApiKey",
                table: "Applications");
        }
    }
}
