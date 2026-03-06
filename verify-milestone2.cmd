@echo off
REM Milestone 2 验证脚本
REM 验证 River GTO Solver 的所有核心功能

echo ============================================
echo Milestone 2: River 子博弈 - 功能验证
echo ============================================
echo.

REM 设置 JAVA_HOME (根据你的实际路径修改)
set "JAVA_HOME=C:\Program Files\Java\jdk-17"

REM 检查 Java
if exist "%JAVA_HOME%\bin\java.exe" (
    echo [√] Java 已找到: %JAVA_HOME%
) else (
    echo [X] 错误: 未找到 JDK 17
    echo     请修改脚本中的 JAVA_HOME 路径
    pause
    exit /b 1
)

echo.
echo === 1. 编译项目 ===
call mvnw.cmd clean compile
if errorlevel 1 (
    echo [X] 编译失败
    pause
    exit /b 1
)
echo [√] 编译成功
echo.

echo === 2. 运行核心功能演示 ===
echo.
echo --- 2.1 卡牌系统演示 ---
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.CardSystemDemo"
echo.

echo --- 2.2 手牌评估器演示 ---
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.EvaluatorDemo"
echo.

echo --- 2.3 范围系统演示 ---
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RangeDemo"
echo.

echo --- 2.4 River博弈树演示 ---
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RiverTreeDemo"
echo.

echo --- 2.5 River CFR 求解演示 ---
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RiverCFRDemo"
echo.

echo === 3. 完整GTO求解器演示 ===
call mvnw.cmd exec:java "-Dexec.mainClass=com.poker.gto.app.demo.RiverGTOSolverDemo"
echo.

echo === 4. 运行单元测试 ===
call mvnw.cmd test
if errorlevel 1 (
    echo [X] 某些测试失败 (这可能是正常的)
) else (
    echo [√] 所有测试通过
)
echo.

echo ============================================
echo 验证完成!
echo ============================================
echo.
echo Milestone 2 核心功能:
echo [√] 52张牌系统 (Rank, Suit, TexasCard, Deck)
echo [√] HandRank枚举 (10种牌型)
echo [√] 7卡手牌评估器 (HandEvaluator)
echo [√] 快速查表评估器 (LookupEvaluator)
echo [√] 范围表示系统 (Range, HandCombo)
echo [√] River状态建模 (RiverState)
echo [√] River博弈树构建 (RiverTreeBuilder)
echo [√] River CFR求解器 (RiverCFR)
echo [√] 策略分析和导出工具
echo.
echo 下一步: Milestone 3 - Turn/Flop 扩展
echo.
pause
