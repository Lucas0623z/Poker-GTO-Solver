@echo off
REM River GTO Solver Demo 运行脚本
REM 首次运行会自动下载 Maven（需要联网）

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo 错误: 未找到 JDK 17，请确认安装路径或修改本脚本中的 JAVA_HOME
    pause
    exit /b 1
)

cd /d "%~dp0"
call mvnw.cmd compile exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RiverGTOSolverDemo"
pause
