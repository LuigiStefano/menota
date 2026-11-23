param(
    [string]$JavaExe,
    [string]$PgBin,
    [switch]$PararBanco,
    [switch]$Celular
)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$localRoot = Join-Path $projectRoot '.local'
$clusterPath = Join-Path $localRoot 'postgres'
$settingsPath = Join-Path $localRoot 'config.json'

if (-not $PgBin) {
    $pgRoot = Join-Path $env:ProgramFiles 'PostgreSQL'
    if (Test-Path -LiteralPath $pgRoot) {
        $PgBin = Get-ChildItem -LiteralPath $pgRoot -Directory | Sort-Object Name -Descending |
            ForEach-Object { Join-Path $_.FullName 'bin' } |
            Where-Object { Test-Path -LiteralPath (Join-Path $_ 'pg_ctl.exe') } | Select-Object -First 1
    }
}
if (-not $PgBin -or -not (Test-Path -LiteralPath (Join-Path $PgBin 'pg_ctl.exe'))) {
    throw 'Informe -PgBin com a pasta bin do PostgreSQL instalado.'
}
if ($PararBanco) {
    if (Test-Path -LiteralPath (Join-Path $clusterPath 'PG_VERSION')) {
        & (Join-Path $PgBin 'pg_ctl.exe') -D $clusterPath -m fast -w stop
    }
    exit
}
if (-not $JavaExe) {
    $javaOptions = @()
    if ($env:JAVA_HOME) { $javaOptions += Join-Path $env:JAVA_HOME 'bin\java.exe' }
    $command = Get-Command java -ErrorAction SilentlyContinue
    if ($command) { $javaOptions += $command.Source }
    $jdkRoot = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path -LiteralPath $jdkRoot) {
        $javaOptions += Get-ChildItem -LiteralPath $jdkRoot -Directory |
            ForEach-Object { Join-Path $_.FullName 'bin\java.exe' }
    }
    $ideaRoot = Join-Path $env:ProgramFiles 'JetBrains'
    if (Test-Path -LiteralPath $ideaRoot) {
        $javaOptions += Get-ChildItem -LiteralPath $ideaRoot -Directory -Filter '*IntelliJ*' |
            ForEach-Object { Join-Path $_.FullName 'jbr\bin\java.exe' }
    }
    foreach ($candidate in $javaOptions) {
        if (-not (Test-Path -LiteralPath $candidate)) { continue }
        $previousErrorAction = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try { $versionText = & $candidate -version 2>&1 | Out-String }
        finally { $ErrorActionPreference = $previousErrorAction }
        if ($versionText -match 'version "(\d+)\.' -and [int]$Matches[1] -ge 21) {
            $JavaExe = $candidate
            break
        }
    }
}
if (-not $JavaExe -or -not (Test-Path -LiteralPath $JavaExe)) {
    throw 'Instale Java 21+ ou informe -JavaExe com o caminho completo de java.exe.'
}
$jarPath = Join-Path $projectRoot 'target/menota.jar'
if (-not (Test-Path -LiteralPath $jarPath)) { throw 'target/menota.jar ausente. Compile conforme o README.' }

function NovaChave {
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    return [Convert]::ToBase64String($bytes)
}
New-Item -ItemType Directory -Path $localRoot -Force | Out-Null
if (-not (Test-Path -LiteralPath $settingsPath)) {
    if (Test-Path -LiteralPath (Join-Path $clusterPath 'PG_VERSION')) {
        throw 'O banco existe, mas config.json está ausente. Restaure a configuração original; não gere outra chave.'
    }
    @{senha=(NovaChave); chave=(NovaChave)} | ConvertTo-Json | Set-Content -LiteralPath $settingsPath -Encoding UTF8
}
$settings = Get-Content -LiteralPath $settingsPath -Raw | ConvertFrom-Json
if (-not (Test-Path -LiteralPath (Join-Path $clusterPath 'PG_VERSION'))) {
    $passwordFile = Join-Path $localRoot 'password.tmp'
    [IO.File]::WriteAllText($passwordFile, $settings.senha)
    try {
        & (Join-Path $PgBin 'initdb.exe') -D $clusterPath -U menota_diario_demo --pwfile=$passwordFile --auth=scram-sha-256 --encoding=UTF8 --locale=C
        if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar o banco de demonstração.' }
    } finally { Remove-Item -LiteralPath $passwordFile -ErrorAction SilentlyContinue }
}
& (Join-Path $PgBin 'pg_ctl.exe') -D $clusterPath status | Out-Null
if ($LASTEXITCODE -ne 0) {
    $pgLog = Join-Path $localRoot 'postgres.log'
    $arguments = @('-D', ('"' + $clusterPath + '"'), '-l', ('"' + $pgLog + '"'), '-o', '"-p 55438 -h 127.0.0.1"', '-w', 'start')
    $process = Start-Process -FilePath (Join-Path $PgBin 'pg_ctl.exe') -ArgumentList $arguments -WindowStyle Hidden -PassThru
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) { throw 'Não foi possível iniciar o banco. Confira .local/postgres.log e a porta 55438.' }
}
$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = $settings.senha
    $exists = & (Join-Path $PgBin 'psql.exe') -h 127.0.0.1 -p 55438 -U menota_diario_demo -d postgres -w -tAc "SELECT 1 FROM pg_database WHERE datname='menota_diario_demo'"
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível conectar ao banco de demonstração.' }
    if ($exists -ne '1') {
        & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p 55438 -U menota_diario_demo -w menota_diario_demo
        if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar menota_diario_demo.' }
    }
} finally { $env:PGPASSWORD = $previousPassword }
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:55438/menota_diario_demo'
$env:DB_USER = 'menota_diario_demo'
$env:DB_PASSWORD = $settings.senha
$env:DIARIO_KEY = $settings.chave
$apiKeyPath = Join-Path $localRoot 'gemini-key.dpapi'
if (Test-Path -LiteralPath $apiKeyPath) {
    try {
        $secureKey = (Get-Content -LiteralPath $apiKeyPath -Raw).Trim() | ConvertTo-SecureString
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
        try { $env:GEMINI_API_KEY = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
            $secureKey.Dispose()
        }
    } catch { throw 'Nao foi possivel abrir a chave Gemini. Execute configurar-gemini.ps1 com este usuario do Windows.' }
}
$portaHttp = if ($env:PORT) { $env:PORT } else { '8080' }
$env:SERVER_ADDRESS = if ($Celular) { '0.0.0.0' } else { '127.0.0.1' }
if ($Celular) {
    Write-Host 'No celular, conectado ao mesmo Wi-Fi, abra um dos enderecos abaixo:'
    Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway } |
        ForEach-Object { $_.IPv4Address.IPAddress } |
        Where-Object { $_ -and $_ -notlike '169.254.*' } |
        ForEach-Object { Write-Host ("http://" + $_ + ":" + $portaHttp) }
    Write-Host 'Use este modo somente na sua rede local com dados ficticios. Ctrl+C encerra a aplicacao.'
} else {
    Write-Host "Abra http://127.0.0.1:$portaHttp e crie uma conta fictícia. Ctrl+C encerra a aplicação."
}
& $JavaExe -jar $jarPath
