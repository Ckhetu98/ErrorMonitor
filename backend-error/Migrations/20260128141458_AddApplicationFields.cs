using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ErrorMonitoringAPI.Migrations
{
    /// <inheritdoc />
    public partial class AddApplicationFields : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "BaseUrl",
                table: "Applications",
                type: "nvarchar(200)",
                maxLength: 200,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "HealthCheckUrl",
                table: "Applications",
                type: "nvarchar(200)",
                maxLength: 200,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Technology",
                table: "Applications",
                type: "nvarchar(50)",
                maxLength: 50,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Version",
                table: "Applications",
                type: "nvarchar(20)",
                maxLength: 20,
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "BaseUrl",
                table: "Applications");

            migrationBuilder.DropColumn(
                name: "HealthCheckUrl",
                table: "Applications");

            migrationBuilder.DropColumn(
                name: "Technology",
                table: "Applications");

            migrationBuilder.DropColumn(
                name: "Version",
                table: "Applications");
        }
    }
}
