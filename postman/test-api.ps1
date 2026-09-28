param(
    [ValidatePattern('^https?://')]
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$collection = Join-Path $PSScriptRoot 'etudiant-api.postman_collection.json'
$report = Join-Path $PSScriptRoot '../target/postman/results.xml'

if (-not (Get-Command npx.cmd -ErrorAction SilentlyContinue)) {
    throw 'Node.js et npm sont necessaires pour executer Newman. La collection peut aussi etre importee dans Postman.'
}

# Verifier la connexion avant d'installer/lancer Newman.
try {
    $null = Invoke-WebRequest -Uri ($BaseUrl.TrimEnd('/') + '/actuator/health') -UseBasicParsing -TimeoutSec 15
} catch {
    throw "Backend indisponible ou en mauvaise sante sur $BaseUrl. Demarrez Docker Desktop puis le backend. Detail : $($_.Exception.Message)"
}

$null = New-Item -ItemType Directory -Force -Path (Split-Path $report)
Write-Host "Tests de l'API sur $BaseUrl (creation d'un compte de test et d'un etudiant temporaire)."
# Version fixee ; npx utilise son cache ou telecharge Newman au premier lancement.
# Pas de --bail : les assertions en echec ne doivent pas empecher le nettoyage.
& npx.cmd --yes --package=newman@6.2.1 newman run $collection `
    --env-var "baseUrl=$($BaseUrl.TrimEnd('/'))" `
    --timeout-request 15000 `
    --timeout-script 10000 `
    --reporters cli,junit `
    --reporter-junit-export $report

$result = $LASTEXITCODE
Write-Host "Rapport JUnit : $report"
exit $result
