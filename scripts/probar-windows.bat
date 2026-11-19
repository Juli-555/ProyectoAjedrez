@echo off
setlocal
cd /d "%~dp0.."
if not exist build\classes mkdir build\classes
if not exist build\tests mkdir build\tests
REM Eliminar el listado anterior para evitar rutas duplicadas.
if exist build\sources.txt del build\sources.txt
for /r src\main\java %%f in (*.java) do @echo "%%f">>build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 goto :error
if exist build\test-sources.txt del build\test-sources.txt
for /r src\test\java %%f in (*.java) do @echo "%%f">>build\test-sources.txt
javac --release 17 -encoding UTF-8 -cp build\classes -d build\tests @build\test-sources.txt
if errorlevel 1 goto :error
java -cp "build\classes;build\tests" co.edu.ajedrez.PruebasProyecto
if errorlevel 1 goto :error
pause
exit /b 0
:error
echo Hubo un error. Comprueba java -version y javac -version.
pause
exit /b 1
