# Register Admin User Script
$apiUrl = "http://localhost:52583/api/auth/register"

$adminUser = @{
    username = "khetu"
    email = "ckhetesh089@gmail.com"
    password = "Khetesh@8104"
    role = "ADMIN"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
}

try {
    Write-Host "Registering admin user..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri $apiUrl -Method POST -Body $adminUser -Headers $headers
    Write-Host "Admin user registered successfully!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Cyan
}
catch {
    Write-Host "Error registering admin user:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}