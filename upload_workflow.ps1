param([string]$token)
$owner = 'tomasd005'
$repo = 'todo-app'
$filePath = '.github/workflows/maven.yml'
$root = '\\hdes.srsa.local\Redirected folders\User\hdes7218\Desktop\To-do-List'
$fullPath = Join-Path $root $filePath

if (-not (Test-Path $fullPath)) { Write-Error "Workflow file not found: $fullPath"; exit 1 }

$content = Get-Content -Path $fullPath -Raw -Encoding UTF8
$bytes = [System.Text.Encoding]::UTF8.GetBytes($content)
$b = [Convert]::ToBase64String($bytes)
$payload = @{ message = 'Add GitHub Actions workflow: maven.yml'; content = $b } | ConvertTo-Json -Depth 5
$url = "https://api.github.com/repos/$owner/$repo/contents/$filePath"
$headers = @{ Authorization = "token $token"; 'User-Agent' = 'vscode' }

try {
    $resp = Invoke-RestMethod -Method Put -Uri $url -Headers $headers -Body $payload -ContentType 'application/json'
    Write-Output "Workflow uploaded: $($resp.content.path)"
} catch {
    Write-Error "Upload failed: $($_.Exception.Message)"
    if ($_.Exception.Response) { $_.Exception.Response.Content | Write-Error }
    exit 1
}
