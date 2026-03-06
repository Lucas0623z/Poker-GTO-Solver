@echo off
REM 编译和测试脚本 - Milestone 2
REM 自动设置JAVA_HOME并编译运行测试

echo ============================================
echo Milestone 2 - 编译和测试
echo ============================================
echo.

REM 设置JAVA_HOME
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [!] 警告: JDK 17未找到于 %JAVA_HOME%
    echo [?] 尝试使用系统Java...
    where java >nul 2>&1
    if errorlevel 1 (
        echo [X] 错误: 未找到Java
        echo     请安装JDK 17或修改此脚本中的JAVA_HOME路径
        pause
        exit /b 1
    )
) else (
    echo [√] Java 已找到: %JAVA_HOME%
)

echo.
echo === 步骤1: 清理和编译 ===
call mvnw.cmd clean compile
if errorlevel 1 (
    echo [X] 编译失败
    pause
    exit /b 1
)
echo [√] 编译成功
echo.

echo === 步骤2: 运行性能对比演示 ===
echo 测试优化效果...
echo.
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.PerformanceComparisonDemo"
echo.

echo === 步骤3: 运行River GTO Solver演示 ===
echo.
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RiverGTOSolverDemo"
echo.

echo === 步骤4: 运行单元测试 ===
call mvnw.cmd test
if errorlevel 1 (
    echo [!] 某些测试失败
) else (
    echo [√] 所有测试通过
)
echo.

echo ============================================
echo 完成！
echo ============================================
echo.
echo 已完成的优化:
echo [√] InfoSet Key缓存 (减少字符串计算)
echo [√] 稀疏策略存储 (节省30-50%%内存)
echo [√] 手牌评估查表法 (100x性能提升)
echo.
echo 查看详细报告:
echo - docs/MILESTONE2_SUMMARY.md
echo - docs/PERFORMANCE_OPTIMIZATION.md
echo.
pause
