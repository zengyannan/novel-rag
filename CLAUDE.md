\## 工作方式

\- 实现前先说明方法。

\- 需求有歧义、高风险或影响较大时，先澄清并等待批准。

\- Plan 只写方案，不写代码。

\- 坚持 Spec Coding，不做 Vibe Coding。

\- 优先迭代，使用 `/loop`。

\- 完成后执行 `/simplify`。

\- 先指定一个 Claude 产出 plan。

\- 基于 plan 拆分任务，并分配给不同的 Claude 并行或串行执行。

\- 子任务保持边界清晰、职责明确、便于独立验证。

\- 完成后指定一个 Claude 汇总结果并输出最终报告。



\## 编码规则

\- 代码中只允许使用英文。

\- 代码注释和文档使用中文。

\- Spec 不依赖行号定位代码。

\- 注释不要写开发过程式说明。

\- 优先用概念性描述定位代码，不用”文件路径 + 行号”。



\## 拆分原则

\- 拆分为低耦合、可独立验证的子任务，必要时使用 `/batch`。

\- 重复出现 3 次的流程应沉淀为 Skill。

\- 控制单个 Claude 的上下文范围，只提供完成任务所需的最小上下文。

\- 跨任务共享时，优先传递结论、约束和接口，不传递冗长过程。



\## 质量要求

\- 项目早期只保留最小必要质量标准：可运行、可验证、可回滚。

\- 优先保证关键路径和高风险改动可验证。

\- 修复 bug 时，先复现，再修复并验证。

\- 实现与审查分离，完成后独立复核。



\## 汇总要求

\- 最终报告至少包含：任务目标、子任务结果、验证结论、遗留风险、后续建议。



\## 禁止事项

\- 永远不要使用 `/init`。

\- `CLAUDE.md` 必须按项目实际需求编写，不要套用空泛模板。

\- Avoid terms to describe development progress (`FIXED`, `Step`, `Week`, `Section`, `Phase`, `AC-x`, etc) in code comments, commit messages, or PR bodies.

\- Avoid AI tool names (such as Codex, Claude, Grok, Gemini, etc.) in code comments, commit messages, or PR bodies.



##指导编码
@import ./CLAUDE.frontend.md
-前端代码使用CLAUDE.frontend.md做指导
@import ./CLAUDE.backend.md
-后端代码使用CLAUDE.backend.md做指导