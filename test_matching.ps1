$ErrorActionPreference = "Stop"

# 1. Login Priya
$pBody = '{"email":"priya.sharma@example.com","role":"RIDER"}'
$priya = Invoke-RestMethod -Uri 'http://localhost:8080/api/users/login' -Method Post -ContentType 'application/json' -Body $pBody
Write-Host "Priya Id:" $priya.id

# 2. Login Ananya
$aBody = '{"email":"ananya.iyer@example.com","role":"RIDER"}'
$ananya = Invoke-RestMethod -Uri 'http://localhost:8080/api/users/login' -Method Post -ContentType 'application/json' -Body $aBody
Write-Host "Ananya Id:" $ananya.id

# 3. Submit Priya request
$targetTime = (Get-Date).AddMinutes(15).ToString("yyyy-MM-ddTHH:mm:ssZ")
$priyaReq = @{
    userId = $priya.id
    venue = "KIA_AIRPORT"
    direction = "FROM_VENUE"
    eventIdentifier = "Flight 6E-204"
    targetTime = $targetTime
    maxWaitMinutes = 30
    dropAddress = "Outer Ring Road, HSR Layout"
    dropLat = 12.9121
    dropLng = 77.6446
    dropZone = "HSR"
    preference = "WOMEN_ONLY"
} | ConvertTo-Json

$res1 = Invoke-RestMethod -Uri 'http://localhost:8080/api/rides/requests' -Method Post -ContentType 'application/json' -Body $priyaReq
Write-Host "Priya Request 1:" $res1.id "Status:" $res1.status

Start-Sleep -Seconds 1

# 4. Submit Ananya request (Should trigger matching!)
$ananyaReq = @{
    userId = $ananya.id
    venue = "KIA_AIRPORT"
    direction = "FROM_VENUE"
    eventIdentifier = "Flight 6E-204"
    targetTime = $targetTime
    maxWaitMinutes = 30
    dropAddress = "Sector 2, HSR Layout"
    dropLat = 12.9150
    dropLng = 77.6480
    dropZone = "HSR"
    preference = "WOMEN_ONLY"
} | ConvertTo-Json

$res2 = Invoke-RestMethod -Uri 'http://localhost:8080/api/rides/requests' -Method Post -ContentType 'application/json' -Body $ananyaReq
Write-Host "Ananya Request 2:" $res2.id "Status:" $res2.status

Start-Sleep -Seconds 3
Write-Host "=== Matching Completed Successfully ==="
