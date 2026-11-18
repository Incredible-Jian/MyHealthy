$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
Get-Content -LiteralPath (Join-Path $projectRoot '.env') | ForEach-Object {
 if($_ -match '^([A-Z_]+)=(.*)$'){[Environment]::SetEnvironmentVariable($matches[1],$matches[2],'Process')}
}
Set-Location $projectRoot
$java = 'java.exe'
if ($env:JAVA_HOME) {
    $java = Join-Path $env:JAVA_HOME 'bin/java.exe'
    if (-not (Test-Path -LiteralPath $java -PathType Leaf)) {
        throw 'JAVA_HOME does not point to a JDK with bin/java.exe.'
    }
}
& $java -jar (Join-Path $projectRoot 'backend/ruoyi-admin/target/ruoyi-admin.jar') --spring.profiles.active=druid,course --spring.config.additional-location=optional:file:./backend/ruoyi-admin/src/main/resources/application-secrets.local.yml

exit $LASTEXITCODE
