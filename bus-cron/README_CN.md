# ⏰ Bus Cron: 轻量级定时任务框架

<p align="center">
<strong>高性能、灵活的定时任务调度框架</strong>
</p>

-----

## 📖 项目介绍

**Bus Cron** 是一个基于 cron 表达式的轻量级、高性能定时任务框架。它为任务调度提供了简单而强大的 API，支持标准 Linux crontab 风格的语法，同时提供企业级特性，如任务监听器、灵活的调度模式和线程池管理。

-----

## ✨ 核心特性

### 🎯 基础能力

* **Cron 表达式支持**: 完整支持标准 5 字段 cron 表达式（分钟、小时、日、月、周）
* **Quartz 兼容性**: 支持 6 字段（带秒）和 7 字段（带年）表达式以兼容 Quartz
* **灵活调度**: 使用 cron 模式、Runnable 任务或自定义 Crontab 实现调度任务
* **线程池管理**: 可配置的线程池，支持守护/非守护选项
* **任务监听器**: 使用开始、成功和失败回调监控任务生命周期

### ⚡ 高级特性

| 特性 | 描述 | 优势 |
| :--- | :--- | :--- |
| **秒级精度** | 可选秒匹配模式 | 亚分钟调度精度 |
| **时区支持** | 每个调度器时区配置 | 支持多区域部署 |
| **触发队列模式** | 预计算触发时间 | 提高调度性能 |
| **任务元数据** | 基于 ID 的任务管理 | 动态任务控制 |
| **配置加载** | 从配置文件加载任务 | 外部化任务管理 |

### 🌍 Cron 表达式格式

**标准 5 字段格式**:
```
* * * * *
│ │ │ │ │
│ │ │ │ └─ 星期几 (0-6, 星期日=0 或 7)
│ │ │ └─── 月份 (1-12)
│ │ └───── 日期 (1-31)
│ └─────── 小时 (0-23)
└───────── 分钟 (0-59)
```

**6 字段格式**（Quartz 兼容，包含秒）:
```
* * * * * *
│ │ │ │ │ │
│ │ │ │ │ └─ 星期几
│ │ │ │ └─── 月份
│ │ │ └───── 日期
│ │ └─────── 小时
│ └───────── 分钟
└─────────── 秒 (0-59)
```

**7 字段格式**（包含年）:
```
* * * * * * *
│ │ │ │ │ │ │
│ │ │ │ │ │ └─ 年份 (1970-2099)
│ │ │ │ │ └─── (其他字段同 6 字段)
```

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-cron</artifactId>
    <version>8.5.2</version>
</dependency>
```

### 基础用法

#### 1. 简单定时任务

```java
import org.miaixz.bus.cron.Builder;

// 启动调度器（如果存在则加载 config/cron.setting）
Builder.start();

// 调度每 5 分钟运行的任务
Builder.schedule("*/5 * * * *", () -> {
    System.out.println("任务执行时间: " + new Date());
});

// 停止调度器
Builder.stop();
```

#### 2. 使用自定义 ID 调度

```java
String taskId = Builder.schedule("my-task-id", "0 * * * *", () -> {
    System.out.println("每小时任务执行");
});

// 删除任务
Builder.remove(taskId);
```

#### 3. 直接使用调度器

```java
import org.miaixz.bus.cron.Scheduler;

// 创建自定义调度器
Scheduler scheduler = new Scheduler();

// 配置并启动
scheduler.setMatchSecond(true)  // 启用秒级精度
          .start(true);         // 作为守护线程启动

// 调度任务
scheduler.schedule("task1", "0 */2 * * *", () -> {
    System.out.println("任务每 2 小时运行一次");
});

// 停止调度器
scheduler.stop();
```

-----

## 📝 使用示例

### 1. Cron 模式示例

```java
// 每分钟
"* * * * *"

// 每 5 分钟
"*/5 * * * *"

// 每小时的第 5 分钟
"5 * * * *"

// 星期一和星期二的 11:59
"59 11 * * 1,2"

// 在 3 和 18（含）之间每 5 分钟
"3-18/5 * * * *"

// 每星期一凌晨 2:00
"0 2 * * 1"

// 每秒（需要 matchSecond=true）
"* * * * * *"

// 每月最后一天的中午 12:00
"0 12 L * *"

// 每年 1 月 1 日午夜
"0 0 1 1 *"
```

### 2. Runnable 任务调度

```java
Scheduler scheduler = new Scheduler();
scheduler.start();

// 调度 Runnable
scheduler.schedule("cleanup", "0 0 3 * * *", new Runnable() {
    @Override
    public void run() {
        System.out.println("凌晨 3 点运行每日清理");
        // 清理逻辑
    }
});
```

### 3. 自定义 Crontab 实现

```java
import org.miaixz.bus.cron.crontab.Crontab;

public class MyCustomTask implements Crontab {
    @Override
    public void execute() {
        System.out.println("执行自定义任务");
        // 您的任务逻辑
    }
}

// 调度自定义任务
Scheduler scheduler = new Scheduler();
scheduler.schedule("custom", "0 * * * *", new MyCustomTask());
scheduler.start();
```

### 4. 任务监听器

```java
import org.miaixz.bus.cron.listener.TaskListener;
import org.miaixz.bus.cron.Executor;

Scheduler scheduler = new Scheduler();

// 添加任务监听器
scheduler.addListener(new TaskListener() {
    @Override
    public void onStart(Executor executor) {
        System.out.println("任务开始: " + executor.task());
    }

    @Override
    public void onSucceeded(Executor executor) {
        System.out.println("任务成功完成");
    }

    @Override
    public void onFailed(Executor executor, Exception e) {
        System.err.println("任务失败: " + e.getMessage());
    }
});

scheduler.start();
```

### 5. 配置文件调度

创建 `config/cron.setting`:

```properties
# 使用 class.method 语法调度任务
com.example.TaskScheduler.cleanup = 0 0 3 * * *
com.example.TaskScheduler.backup = 0 0 4 * * *

# 分组任务
[com.example.jobs]
dataSync = */30 * * * *
reportGeneration = 0 9 * * 1-5
```

加载并执行:

```java
import org.miaixz.bus.cron.Builder;
import org.miaixz.bus.setting.Setting;

// 从默认位置加载（config/cron.setting 或 cron.setting）
Builder.start();

// 或从自定义路径加载
Setting cronSetting = new Setting("path/to/custom-cron.setting");
Scheduler scheduler = new Scheduler();
scheduler.schedule(cronSetting).start();
```

### 6. 动态任务管理

```java
Scheduler scheduler = new Scheduler();
scheduler.start();

// 添加任务
String taskId = scheduler.schedule("temp-task", "*/10 * * * *", () -> {
    System.out.println("临时任务");
});

// 更新模式
scheduler.updatePattern(taskId, new org.miaixz.bus.cron.pattern.CronPattern("*/5 * * * *"));

// 删除任务
scheduler.deschedule(taskId);

// 获取任务信息
Crontab task = scheduler.getTask(taskId);
CronPattern pattern = scheduler.getPattern(taskId);

// 检查调度器状态
boolean isEmpty = scheduler.isEmpty();
int taskCount = scheduler.size();
```

### 7. 高级配置

```java
import org.miaixz.bus.cron.Configure;
import org.miaixz.bus.cron.Scheduler;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 创建自定义配置
Configure config = Configure.of()
    .setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
    .setMatchSecond(true)
    .setDaemon(false)
    .setUseTriggerQueue(true);

// 使用配置创建调度器
Scheduler scheduler = new Scheduler(config);

// 设置自定义线程池
ExecutorService executor = Executors.newFixedThreadPool(10);
scheduler.setThreadExecutor(executor);

// 添加监听器
scheduler.addListener(new TaskListener() {
    // 实现监听器方法
});

// 启动
scheduler.start();
```

### 8. Cron 模式匹配

```java
import org.miaixz.bus.cron.pattern.CronPattern;
import java.util.Date;

// 创建模式
CronPattern pattern = new CronPattern("0 * * * *");

// 检查时间是否匹配
boolean matches = pattern.match(new Date(), false);

// 获取下一个匹配时间
long nextMatch = pattern.nextMatchFromNow();

// 在范围内查找所有匹配
Date start = new Date();
Date end = new Date(start.getTime() + 86400000); // 24 小时后
List<Date> matches = CronPattern.matchedDates("0 * * * *", start, end, 24);

// 验证表达式
boolean isValid = Builder.isValidExpression("0 * * * *");
```

-----

## 💡 最佳实践

### 1. 选择适当的精度

```java
// 对于大多数情况，分钟精度就足够了
Builder.setMatchSecond(false);
Builder.schedule("*/5 * * * *", task);

// 仅在需要时启用秒级精度
Builder.setMatchSecond(true);
Builder.schedule("*/30 * * * * *", task);  // 每 30 秒
```

### 2. 在任务中处理异常

```java
// 好: 在任务内处理异常
Builder.schedule("0 * * * *", () -> {
    try {
        // 任务逻辑
        performOperation();
    } catch (Exception e) {
        logger.error("任务失败", e);
    }
});

// 更好: 使用任务监听器
scheduler.addListener(new TaskListener() {
    @Override
    public void onFailed(Executor executor, Exception e) {
        logger.error("任务失败: {}", executor.task(), e);
    }
});
```

### 3. 使用任务 ID 进行管理

```java
// 始终为可能需要动态管理的任务指定 ID
String taskId = scheduler.schedule("backup-task", "0 2 * * *", backupTask);

// 之后，您可以更新或删除它
scheduler.updatePattern(taskId, newPattern);
scheduler.deschedule(taskId);
```

### 4. 适当配置线程池

```java
// 对于 CPU 密集型任务
ExecutorService cpuPool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
scheduler.setThreadExecutor(cpuPool);

// 对于 I/O 密集型任务
ExecutorService ioPool = Executors.newCachedThreadPool();
scheduler.setThreadExecutor(ioPool);
```

### 5. 考虑守护与非守护线程

```java
// 守护: 调度器停止时任务立即终止
scheduler.start(true);

// 非守护: 调度器停止后任务完成执行
scheduler.start(false);
```

-----

## ❓ 常见问题

### Q1: 如何安排每天特定时间的任务？

```java
// 安排在每天上午 9:30
Scheduler scheduler = new Scheduler();
scheduler.schedule("daily-930", "30 9 * * *", () -> {
    System.out.println("任务每天上午 9:30 运行");
});
scheduler.start();
```

### Q2: 可以安排任务在一周的特定几天运行吗？

```java
// 仅在工作日（周一至周五）运行
scheduler.schedule("weekdays", "0 9 * * 1-5", task);

// 在周一、周三和周五运行
scheduler.schedule("mwf", "0 10 * * 1,3,5", task);

// 在周末运行
scheduler.schedule("weekends", "0 12 * * 6,0", task);
```

### Q3: 如何启用秒级精度？

```java
// 使用 Builder
Builder.setMatchSecond(true);
Builder.start();

// 使用 Scheduler
Scheduler scheduler = new Scheduler();
scheduler.setMatchSecond(true);
scheduler.start();
```

### Q4: 如何优雅地停止运行中的调度器？

```java
// 停止并保持任务
scheduler.stop();

// 停止并清除所有任务
scheduler.stop(true);

// 等待运行任务完成
scheduler.stop(false);
```

### Q5: 如何处理任务失败？

```java
scheduler.addListener(new SimpleTaskListener() {
    @Override
    public void onFailed(Executor executor, Exception e) {
        // 记录错误
        logger.error("任务失败: {}", executor.task(), e);

        // 发送警报
        alertService.send("任务执行失败", e);

        // 重试逻辑
        if (isRetryable(e)) {
            scheduleRetry(executor.task());
        }
    }
});
```

### Q6: 可以使用带有命名月份/日期的 cron 表达式吗？

```java
// 可以！月份名称（不区分大小写）
"0 9 * Jan * "      // 一月每天上午 9 点
"0 12 * Apr-Jun *"  // 四月至六月每天中午

// 星期几名称
"0 9 * * Mon"       // 每周一上午 9 点
"0 9 * * Mon,Fri"   // 每周一和周五上午 9 点
```

### Q7: 如何安排每月最后一天的任务？

```java
// 使用 'L' 表示每月最后一天
scheduler.schedule("last-day", "0 0 L * *", () -> {
    System.out.println("每月最后一天");
});
```

### Q8: 守护模式和非守护模式有什么区别？

```java
// 守护模式: 即使任务正在运行，JVM 也会退出
scheduler.start(true);

// 非守护模式: JVM 等待任务完成
scheduler.start(false);
```

对可以中断的后台任务使用守护模式，对必须完成的关键任务使用非守护模式。

-----

## 🔄 版本兼容性

| Bus Cron 版本 | JDK 版本 |
| :--- | :--- |
| 8.x | 17+ |
| 7.x | 11+ |

-----

## 📋 配置参考

### 调度器配置选项

| 选项 | 类型 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- |
| `timezone` | `TimeZone` | `TimeZone.getDefault()` | 任务调度的时区 |
| `matchSecond` | `boolean` | `false` | 是否启用秒级精度 |
| `daemon` | `boolean` | `false` | 是否使用守护线程 |
| `useTriggerQueue` | `boolean` | `false` | 是否使用触发队列模式 |

### Cron 表达式特殊字符

| 字符 | 描述 | 示例 |
| :--- | :--- | :--- |
| `*` | 匹配所有值 | `* * * * *`（每分钟） |
| `?` | 与 `*` 相同 | `? * ? * ?` |
| `/` | 步长值 | `*/5 * * * *`（每 5 分钟） |
| `-` | 范围 | `0 9-17 * * *`（上午 9 点到下午 5 点） |
| `,` | 列表 | `0 9,12,18 * * *`（上午 9 点、中午、下午 6 点） |
| `L` | 最后一天 | `0 0 L * *`（每月最后一天） |

-----

## 🔧 高级主题

### 任务隔离和并发

```java
// 默认情况下，任务独立运行
// 防止重叠执行:

public class IsolatedTask implements Crontab {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override
    public void execute() {
        if (!isRunning.compareAndSet(false, true)) {
            logger.warn("上一次执行仍在运行，跳过");
            return;
        }

        try {
            // 任务逻辑
        } finally {
            isRunning.set(false);
        }
    }
}
```

### 时区感知调度

```java
// 为特定时区创建调度器
Configure config = Configure.of()
    .setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

Scheduler scheduler = new Scheduler(config);

// 模式在上海时间匹配
scheduler.schedule("shanghai-task", "0 9 * * *", task);
```

### Cron 表达式验证

```java
// 调度前验证
String expression = "0 0 12 * * *";  // 无效: 有秒但 matchSecond=false

if (Builder.isValidExpression(expression)) {
    Builder.schedule(expression, task);
} else {
    logger.error("无效的 cron 表达式: {}", expression);
}
```

-----

## 📚 API 参考

### Builder 类（静态方法）

```java
void start()                                              // 启动调度器
void start(boolean isDaemon)                              // 使用守护设置启动
void stop()                                               // 停止调度器
void restart()                                            // 重启调度器
String schedule(String pattern, Crontab task)            // 调度任务
String schedule(String id, String pattern, Crontab task) // 带ID调度
boolean remove(String schedulerId)                        // 删除任务
void setMatchSecond(boolean isMatchSecond)                // 设置秒精度
```

### Scheduler 类

```java
Scheduler start()                                         // 启动调度器
Scheduler start(boolean isDaemon)                         // 使用守护设置启动
Scheduler stop()                                          // 停止调度器
Scheduler stop(boolean clearTasks)                        // 停止并可选清除
Scheduler schedule(String id, String pattern, Crontab)    // 调度任务
Scheduler deschedule(String id)                           // 删除任务
Scheduler updatePattern(String id, CronPattern)           // 更新模式
Crontab getTask(String id)                                // 获取任务
CronPattern getPattern(String id)                         // 获取模式
int size()                                                // 获取任务数
boolean isEmpty()                                         // 检查是否为空
Scheduler clear()                                         // 清除所有任务
```

### CronPattern 类

```java
boolean match(long millis, boolean isMatchSecond)        // 检查时间是否匹配
boolean match(Calendar calendar, boolean isMatchSecond)  // 检查日历是否匹配
long nextMatchFromNow()                                  // 获取下一个匹配时间
Calendar nextMatchAfter(Calendar calendar)               // 获取指定时间后的匹配
static List<Date> matchedDates(...)                      // 在范围内查找所有匹配
```

-----

如需更多信息和更新，请访问: [https://github.com/818000/bus](https://github.com/818000/bus)
