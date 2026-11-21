Set-Location "C:\Users\SARIKA PANDEY\Desktop\project-college"
$jarExe = "C:\Users\SARIKA PANDEY\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\jar.exe"

New-Item -ItemType Directory -Force -Path "BOOT-INF\classes\static" | Out-Null
New-Item -ItemType Directory -Force -Path "BOOT-INF\classes\com\cabpool\api\service" | Out-Null
New-Item -ItemType Directory -Force -Path "BOOT-INF\classes\com\cabpool\matcher\service" | Out-Null

Copy-Item "src\main\resources\static\*" "BOOT-INF\classes\static\" -Recurse -Force
Copy-Item "target\classes\com\cabpool\api\service\RideService*.class" "BOOT-INF\classes\com\cabpool\api\service\" -Force
Copy-Item "target\classes\com\cabpool\matcher\service\MatcherAlgorithmService*.class" "BOOT-INF\classes\com\cabpool\matcher\service\" -Force

$files = (Get-ChildItem -Path "BOOT-INF" -Recurse -File | ForEach-Object { Resolve-Path -Relative $_.FullName })
& $jarExe uf "target\cab-sharing-service-0.0.1-SNAPSHOT.jar" $files

Remove-Item -Recurse -Force "BOOT-INF"
Write-Host "JAR UPDATED SUCCESSFUL"
