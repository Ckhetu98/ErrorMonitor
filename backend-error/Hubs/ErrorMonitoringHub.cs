using Microsoft.AspNetCore.SignalR;
using Microsoft.AspNetCore.Authorization;

namespace ErrorMonitoringAPI.Hubs
{
    [Authorize]
    public class ErrorMonitoringHub : Hub
    {
        public async Task JoinApplicationGroup(string applicationId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, $"app_{applicationId}");
        }

        public async Task LeaveApplicationGroup(string applicationId)
        {
            await Groups.RemoveFromGroupAsync(Context.ConnectionId, $"app_{applicationId}");
        }

        public override async Task OnDisconnectedAsync(Exception exception)
        {
            await base.OnDisconnectedAsync(exception);
        }
    }
}