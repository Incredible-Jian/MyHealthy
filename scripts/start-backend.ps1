$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
Get-Content -LiteralPath (Join-Path $projectRoot '.env') | ForEach-Object {
 if($_ -match '^([A-Z_]+)=(.*)$'){[Environment]::SetEnvironmentVariable($matches[1],$matches[2],'Process')}
}
Set-Location $projectRoot
& java -jar (Join-Path $projectRoot 'backend/ruoyi-admin/target/ruoyi-admin.jar') --spring.profiles.active=druid,course --spring.config.additional-location=optional:file:./backend/ruoyi-admin/src/main/resources/application-secrets.local.yml
