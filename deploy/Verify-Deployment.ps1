param(
    [string]$BaseUrl = 'http://localhost:8080/trade-mis',
    [string]$Username = 'admin'
)
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($env:TRADE_AUTH_PASSWORD)) {
    throw 'Set TRADE_AUTH_PASSWORD to the deployment test account password.'
}
$base = $BaseUrl.TrimEnd('/')
$web = Invoke-WebRequest "$base/demo/index.html"
if ($web.StatusCode -ne 200 -or $web.Content -notmatch '商品台账') {
    throw 'Demo entry page was not returned.'
}
$credentials = @{ username = $Username; password = $env:TRADE_AUTH_PASSWORD } | ConvertTo-Json
$login = Invoke-RestMethod "$base/auth/login" -Method Post -ContentType 'application/json' -Body $credentials -SessionVariable session
if ($login.code -ne 200) { throw 'Login failed.' }
foreach ($path in '/products', '/catalog/brands', '/catalog/orders', '/catalog/orders/1/lines') {
    $result = Invoke-RestMethod "$base$path" -WebSession $session
    if ($result.code -ne 200) { throw "Endpoint failed: $path" }
    Write-Output "OK $path"
}
$logout = Invoke-RestMethod "$base/auth/logout" -Method Post -WebSession $session
if ($logout.code -ne 200) { throw 'Logout failed.' }
