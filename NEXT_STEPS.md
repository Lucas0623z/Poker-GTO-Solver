# 下一步行动计划

**更新日期**: 2026-03-06
**当前阶段**: Milestone 2 扫尾 → Milestone 3 准备

---

## 📋 Milestone 2 剩余任务 (1-2周完成)

### 阶段1: 性能优化 (3-5天)

#### 1.1 InfoSet Key缓存 ⭐ 高优先级
**目标**: CFR迭代速度提升10-20%

**任务**:
- [ ] 在RiverDecisionNode中添加缓存字段
- [ ] 修改getInfoSetKey()方法为懒加载
- [ ] 运行性能对比测试
- [ ] 更新基准测试数据

**实现位置**: `src/main/java/com/poker/gto/core/tree/RiverDecisionNode.java`

**验收标准**:
- ✅ CFR迭代速度提升 > 10%
- ✅ 所有测试通过
- ✅ 内存占用增加 < 5%

**预计时间**: 1天

---

#### 1.2 策略存储优化 ⭐ 中优先级
**目标**: 内存占用减少30-50%

**任务**:
- [ ] 实现SparseStrategy类 (只存储>1%概率的动作)
- [ ] 使用float代替double存储概率
- [ ] 添加策略压缩序列化
- [ ] 内存对比测试

**实现位置**: `src/main/java/com/poker/gto/solver/cfr/SparseStrategy.java`

**验收标准**:
- ✅ 内存占用减少 > 30%
- ✅ 策略精度损失 < 1%
- ✅ 序列化/反序列化正常

**预计时间**: 2天

---

#### 1.3 性能基准测试 ⭐ 中优先级
**目标**: 建立性能回归测试基线

**任务**:
- [ ] 添加JMH依赖
- [ ] 编写HandEvaluator基准测试
- [ ] 编写CFR迭代基准测试
- [ ] 编写TreeBuilder基准测试
- [ ] 生成性能报告

**实现位置**: `tests/benchmark/`

**预计时间**: 1-2天

---

### 阶段2: 测试完善 (2-3天)

#### 2.1 运行集成测试 ⭐ 高优先级
**任务**:
- [x] 编写RiverSolverIntegrationTest.java ✅
- [ ] 运行所有测试场景
- [ ] 验证测试覆盖率 > 75%
- [ ] 修复失败的测试

**运行命令**:
```bash
mvnw.cmd test
```

**预计时间**: 1天

---

#### 2.2 收敛性验证 ⭐ 高优先级
**目标**: 验证CFR算法正确性

**任务**:
- [ ] 实现Exploitability计算
- [ ] 测试多个场景的收敛曲线
- [ ] 验证Exploitability < 0.05
- [ ] 生成收敛性报告

**实现位置**: `src/main/java/com/poker/gto/solver/metrics/Exploitability.java`

**预计时间**: 1-2天

---

### 阶段3: 文档完善 (2-3天)

#### 3.1 API文档 ⭐ 中优先级
**任务**:
- [ ] 为所有public类添加Javadoc
- [ ] 为关键方法添加注释
- [ ] 生成Javadoc HTML
- [ ] 发布到docs/api/

**命令**:
```bash
mvnw.cmd javadoc:javadoc
```

**预计时间**: 1天

---

#### 3.2 使用教程 ⭐ 高优先级
**任务**:
- [ ] 编写快速开始指南
- [ ] 编写API使用示例
- [ ] 编写自定义配置指南
- [ ] 编写性能调优指南

**文档位置**: `docs/`
- `QUICK_START.md` - 快速开始
- `API_GUIDE.md` - API使用
- `CONFIGURATION.md` - 配置说明
- `TUNING.md` - 性能调优

**预计时间**: 1-2天

---

#### 3.3 性能报告 ⭐ 低优先级
**任务**:
- [ ] 汇总性能基准数据
- [ ] 绘制性能对比图表
- [ ] 编写性能分析报告
- [ ] 发布到docs/PERFORMANCE_REPORT.md

**预计时间**: 1天

---

## 🚀 Milestone 3 准备 (1周)

### 阶段1: 技术调研 (2-3天)

#### 1.1 Chance Node设计 ⭐ 高优先级
**任务**:
- [ ] 研究Chance Node实现方案
- [ ] 设计发牌随机节点接口
- [ ] 设计概率加权算法
- [ ] 编写设计文档

**参考资料**:
- CFR算法论文
- Poker-AI研究论文
- 现有开源求解器代码

**输出**: `docs/CHANCE_NODE_DESIGN.md`

**预计时间**: 1-2天

---

#### 1.2 MCCFR算法调研 ⭐ 高优先级
**任务**:
- [ ] 研究MCCFR变种 (External Sampling, Chance Sampling)
- [ ] 对比不同抽样策略
- [ ] 设计MCCFR接口
- [ ] 编写算法文档

**输出**: `docs/MCCFR_DESIGN.md`

**预计时间**: 1-2天

---

#### 1.3 状态抽象方案 ⭐ 中优先级
**任务**:
- [ ] 研究Hand Bucketing方法
- [ ] 研究Flop/Turn抽象策略
- [ ] 设计可扩展的抽象框架
- [ ] 编写抽象设计文档

**输出**: `docs/ABSTRACTION_DESIGN.md`

**预计时间**: 1天

---

### 阶段2: 原型实现 (3-4天)

#### 2.1 ChanceNode原型 ⭐ 高优先级
**任务**:
- [ ] 实现ChanceNode类
- [ ] 实现简单的Turn发牌
- [ ] 测试Chance节点遍历
- [ ] 验证概率计算正确性

**实现位置**: `src/main/java/com/poker/gto/core/tree/ChanceNode.java`

**预计时间**: 1-2天

---

#### 2.2 MCCFR原型 ⭐ 高优先级
**任务**:
- [ ] 实现External Sampling CFR
- [ ] 在River场景测试
- [ ] 对比与Vanilla CFR的性能
- [ ] 验证收敛性

**实现位置**: `src/main/java/com/poker/gto/solver/cfr/MCCFR.java`

**预计时间**: 2-3天

---

#### 2.3 Turn求解器原型 ⭐ 中优先级
**任务**:
- [ ] 实现TurnState
- [ ] 实现TurnTreeBuilder
- [ ] 集成ChanceNode
- [ ] 运行简单Turn场景

**实现位置**: `src/main/java/com/poker/gto/core/game_state/TurnState.java`

**预计时间**: 2天

---

## 📅 时间线

### Week 1-2: Milestone 2扫尾
```
Day 1-2:   性能优化 (InfoSet缓存 + 策略优化)
Day 3-4:   测试完善 (集成测试 + 收敛性验证)
Day 5-7:   文档完善 (API文档 + 使用教程)
Day 8-10:  Milestone 3调研 (Chance Node + MCCFR)
Day 11-14: Milestone 3原型 (ChanceNode + MCCFR实现)
```

### Week 3-6: Milestone 3开发
```
Week 3:    Turn求解器 (TurnState + TurnTreeBuilder)
Week 4:    Turn CFR求解 + 测试
Week 5:    Flop求解器 (Hand Bucketing + 抽象)
Week 6:    Flop CFR求解 + 集成测试
```

---

## ✅ 验收标准

### Milestone 2完成标准
- [x] River求解器100%可用 ✅
- [ ] 性能优化完成 (迭代速度<0.1ms)
- [ ] 测试覆盖率 > 75%
- [ ] 文档完整 (API + 教程)
- [ ] 性能报告发布

### Milestone 3启动标准
- [ ] Chance Node设计完成
- [ ] MCCFR算法设计完成
- [ ] 抽象方案设计完成
- [ ] 原型验证通过

---

## 🎯 当前行动项 (本周)

### 今日任务
1. ✅ 创建Milestone 2总结文档
2. ✅ 创建集成测试文件
3. ✅ 创建性能优化指南
4. ✅ 创建下一步行动计划
5. [ ] 开始InfoSet Key缓存优化

### 本周任务
1. [ ] 完成性能优化 (InfoSet + 策略存储)
2. [ ] 运行并修复所有测试
3. [ ] 编写API文档和使用教程
4. [ ] 开始Milestone 3技术调研

---

## 📞 需要决策的问题

### 1. 性能优化优先级
**问题**: 是否要花时间实现并行CFR?

**选项**:
- A. 现在实现 (预期提升2-4x, 但增加复杂度)
- B. 推迟到Milestone 3 (专注Turn/Flop求解)
- C. 推迟到Milestone 4 (完整系统优化阶段)

**建议**: 选择B, 专注Milestone 3核心功能

---

### 2. 测试覆盖率目标
**问题**: 测试覆盖率要达到多少?

**选项**:
- A. 60% (快速迭代)
- B. 75% (平衡质量和速度) ⭐
- C. 90+ (高质量,但耗时)

**建议**: 选择B, 核心模块80%+, 辅助模块60%+

---

### 3. Milestone 3范围
**问题**: Milestone 3是只做Turn还是Turn+Flop?

**选项**:
- A. 只做Turn (更稳健, 2-3周)
- B. Turn + Flop (更激进, 4-6周) ⭐

**建议**: 选择B, 但分两个子阶段

---

## 📚 参考资料

### 必读论文
- [Regret Minimization in Games with Incomplete Information](http://poker.cs.ualberta.ca/publications/NIPS07-cfr.pdf)
- [Monte Carlo Sampling for Regret Minimization](https://poker.cs.ualberta.ca/publications/NIPS09-cfr-sampling.pdf)
- [DeepStack: Expert-Level Artificial Intelligence in Poker](https://www.deepstack.ai/s/DeepStack.pdf)

### 开源参考
- [PokerRL](https://github.com/TinkeringCode/PokerRL)
- [OpenCFR](https://github.com/tansey/pycfr)
- [PyPokerEngine](https://github.com/ishikota/PyPokerEngine)

### 工具
- JMH (性能基准测试)
- VisualVM (性能分析)
- JaCoCo (代码覆盖率)

---

## 🎉 总结

**Milestone 2进展**: 95% → 100% (预计1-2周)
**下一里程碑**: Milestone 3 - Turn/Flop扩展
**预计完成时间**: 4-6周

**当前重点**:
1. 完成性能优化
2. 补充测试和文档
3. 准备Milestone 3技术方案

**长期目标**:
- Milestone 4: Preflop + 完整流程 (2-3周)
- MVP发布: 3个月内

---

**文档维护者**: Architect Agent
**审核者**: 全体Agents
**最后更新**: 2026-03-06
