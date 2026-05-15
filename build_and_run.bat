@echo off
cd /d %~dp0
mkdir bin 2>nul
javac -d bin -cp bin ^
  src\model\*.java ^
  src\view\*.java ^
  src\controller\*.java ^
  src\utils\*.java

echo Compilation done.

rem Ensure images are in bin
if not exist "bin\UnoCards\wild" (
    echo Copying images to bin...
    robocopy "src\UnoCards" "bin\UnoCards" /E /NFL /NDL /NJH /NJS /NC /NS /NP /NFL >nul
)

java -cp bin utils.App
