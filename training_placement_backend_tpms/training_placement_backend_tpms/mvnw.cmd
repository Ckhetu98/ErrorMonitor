@echo off
setlocal
set DIR=%~dp0
set WRAPPER_DIR=%DIR%.mvn\wrapper
set JAR=%WRAPPER_DIR%\maven-wrapper.jar
set TAKARI_URL=https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar

if not exist "%JAR%" (
  echo Maven wrapper jar not found; downloading...
  if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" (
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; if(-not (Test-Path -Path '%WRAPPER_DIR%')) { New-Item -ItemType Directory -Path '%WRAPPER_DIR%' | Out-Null }; Invoke-WebRequest -Uri '%TAKARI_URL%' -OutFile '%JAR%' -UseBasicParsing"
  ) else (
    echo PowerShell not found. Please download the maven-wrapper jar manually to %JAR%
    exit /b 1
  )
)

rem Verify the downloaded JAR contains a MANIFEST.MF; if not, attempt re-download once
powershell -Command "try { Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction Stop } catch {} ; $hasManifest = $false; if (Test-Path -Path '%JAR%') { try { $z=[System.IO.Compression.ZipFile]::OpenRead('%JAR%'); $e=$z.Entries | Where-Object { $_.FullName -ieq 'META-INF/MANIFEST.MF' }; if ($e) { $hasManifest = $true }; $z.Dispose() } catch { $hasManifest = $false } } ; if (-not $hasManifest) { Write-Host 'Wrapper JAR appears invalid (missing manifest). Re-downloading...'; Invoke-WebRequest -Uri '%TAKARI_URL%' -OutFile '%JAR%' -UseBasicParsing }"

rem Run the wrapper
java -jar "%JAR%" %*
