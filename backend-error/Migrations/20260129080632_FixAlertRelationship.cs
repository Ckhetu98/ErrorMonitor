using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ErrorMonitoringAPI.Migrations
{
    /// <inheritdoc />
    public partial class FixAlertRelationship : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Alerts_Applications_ApplicationId",
                table: "Alerts");

            migrationBuilder.AlterColumn<int>(
                name: "ApplicationId",
                table: "Alerts",
                type: "int",
                nullable: true,
                oldClrType: typeof(int),
                oldType: "int");

            migrationBuilder.AddColumn<string>(
                name: "AlertLevel",
                table: "Alerts",
                type: "nvarchar(20)",
                maxLength: 20,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AlertMessage",
                table: "Alerts",
                type: "nvarchar(2000)",
                maxLength: 2000,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "AlertType",
                table: "Alerts",
                type: "nvarchar(50)",
                maxLength: 50,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ErrorLogId",
                table: "Alerts",
                type: "nvarchar(max)",
                nullable: true);

            migrationBuilder.AddColumn<bool>(
                name: "IsResolved",
                table: "Alerts",
                type: "bit",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddForeignKey(
                name: "FK_Alerts_Applications_ApplicationId",
                table: "Alerts",
                column: "ApplicationId",
                principalTable: "Applications",
                principalColumn: "Id",
                onDelete: ReferentialAction.SetNull);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Alerts_Applications_ApplicationId",
                table: "Alerts");

            migrationBuilder.DropColumn(
                name: "AlertLevel",
                table: "Alerts");

            migrationBuilder.DropColumn(
                name: "AlertMessage",
                table: "Alerts");

            migrationBuilder.DropColumn(
                name: "AlertType",
                table: "Alerts");

            migrationBuilder.DropColumn(
                name: "ErrorLogId",
                table: "Alerts");

            migrationBuilder.DropColumn(
                name: "IsResolved",
                table: "Alerts");

            migrationBuilder.AlterColumn<int>(
                name: "ApplicationId",
                table: "Alerts",
                type: "int",
                nullable: false,
                defaultValue: 0,
                oldClrType: typeof(int),
                oldType: "int",
                oldNullable: true);

            migrationBuilder.AddForeignKey(
                name: "FK_Alerts_Applications_ApplicationId",
                table: "Alerts",
                column: "ApplicationId",
                principalTable: "Applications",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
