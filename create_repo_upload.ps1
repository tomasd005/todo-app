param([string]$token)
$owner = 'tomasd005'
$repo = 'todo-app'
$root = '\\hdes.srsa.local\Redirected folders\User\hdes7218\Desktop\To-do-List'
$headers = @{ Authorization = "token $token"; 'User-Agent' = 'vscode' }
$body = @{ name = $repo; description = 'To-do List app generated from workspace'; private = $false } | ConvertTo-Json
try {
    Write-Output "Creating repository $owner/$repo..."
    $resp = Invoke-RestMethod -Method Post -Uri 'https://api.github.com/user/repos' -Headers $headers -Body $body -ContentType 'application/json'
    Write-Output ("Repo created: " + $resp.html_url)
} catch {
    Write-Error "Failed to create repo: $($_.Exception.Message)"
    if ($_.Exception.Response) { $_.Exception.Response.Content | Write-Error }
    exit 1
}

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
foreach ($f in $files) {
    Upload-File $f.FullName
}

Write-Output 'Adding LICENSE and .gitignore'
$licensePath = Join-Path $root 'LICENSE'
$mit = @'
MIT License

Copyright (c) 2026 tomasd005

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
'@
Set-Content -Path $licensePath -Value $mit -Encoding utf8
$gitignorePath = Join-Path $root '.gitignore'
Set-Content -Path $gitignorePath -Value "target/`n*.class`n*.log`n.idea/`n.vscode/" -Encoding utf8
Upload-File $licensePath
Upload-File $gitignorePath

Write-Output "Done. Repo URL: https://github.com/$owner/$repo.git"
