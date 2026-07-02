param([string]$token)
$owner = 'tomasd005'
$repo = 'todo-app'
$root = '\\hdes.srsa.local\Redirected folders\User\hdes7218\Desktop\To-do-List'
$headers = @{ Authorization = "token $token"; 'User-Agent' = 'vscode' }

function Upload-File([string]$fullpath) {
    try {
        $rel = $fullpath.Substring($root.Length + 1).TrimStart('\') -replace '\\','/'
        $bytes = [System.IO.File]::ReadAllBytes($fullpath)
        $b = [Convert]::ToBase64String($bytes)
        $payload = @{ message = "Add $rel"; content = $b } | ConvertTo-Json -Depth 5
        $url = "https://api.github.com/repos/$owner/$repo/contents/$rel"
        Invoke-RestMethod -Method Put -Uri $url -Headers $headers -Body $payload -ContentType 'application/json' | Out-Null
        Write-Output "Uploaded: $rel"
    } catch {
        Write-Error "Failed upload: $rel - $($_.Exception.Message)"
    }
}

Write-Output 'Collecting files...'
$files = Get-ChildItem -Path $root -Recurse -File -Force | Where-Object { $_.FullName -notmatch '\\target\\' -and $_.FullName -notmatch '\\.git\\' -and $_.FullName -notmatch '\\.idea\\' }
Write-Output ("Found files: " + $files.Count)
foreach ($f in $files) { Upload-File $f.FullName }
Write-Output 'Done.'
