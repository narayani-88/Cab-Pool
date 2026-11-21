$proj = "C:\Users\SARIKA PANDEY\Desktop\project-college"
$javac = "C:\Users\SARIKA PANDEY\.vscode\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\javac.exe"

$jars = (Get-ChildItem -Path "C:\Users\SARIKA PANDEY\.m2\repository" -Recurse -Filter "*.jar" | ForEach-Object { $_.FullName })
$cp = "$proj\target\classes;" + ($jars -join ";")

& $javac --release 21 -cp $cp -d "$proj\target\classes" "$proj\src\main\java\com\cabpool\api\service\RideService.java" "$proj\src\main\java\com\cabpool\matcher\service\MatcherAlgorithmService.java"
Write-Host "Result: $LASTEXITCODE"
