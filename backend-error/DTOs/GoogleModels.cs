using System.Text.Json.Serialization;

namespace ErrorMonitoringAPI.DTOs;

public class GoogleTokenResponse
{
    [JsonPropertyName("access_token")]
    public string AccessToken { get; set; } = "";
    
    [JsonPropertyName("expires_in")]
    public int ExpiresIn { get; set; }
}

public class GoogleUserInfo
{
    [JsonPropertyName("email")]
    public string Email { get; set; } = "";
    
    [JsonPropertyName("name")]
    public string Name { get; set; } = "";
    
    [JsonPropertyName("picture")]
    public string Picture { get; set; } = "";
}