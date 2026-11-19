@echo off
setlocal
cd /d "%~dp0.."
java -jar AjedrezEstructurado.jar
if errorlevel 1 (
 echo No se pudo iniciar. Asegurate de tener Java 17 o superior instalado.
 pause
)
