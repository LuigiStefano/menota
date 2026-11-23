$ErrorActionPreference = 'Stop'
$localRoot = Join-Path $PSScriptRoot '.local'
$secretPath = Join-Path $localRoot 'gemini-key.dpapi'
Write-Host 'Configure uma chave do Google AI Studio com acesso gratuito. Nao ative cobranca para esta demonstracao.'
Write-Host 'A chave nao sera exibida nem enviada pelo configurador. Ela sera protegida pelo Windows para este usuario.'
$secureKey = Read-Host 'Cole a chave Gemini e pressione Enter' -AsSecureString
if ($secureKey.Length -lt 20) { throw 'Chave muito curta. Confira a chave no Google AI Studio.' }
New-Item -ItemType Directory -Path $localRoot -Force | Out-Null
$secureKey | ConvertFrom-SecureString | Set-Content -LiteralPath $secretPath -Encoding ASCII
$secureKey.Dispose()
Write-Host 'Chave salva. Reinicie o MeNota para utiliza-la. Nao compartilhe a pasta .local.'
