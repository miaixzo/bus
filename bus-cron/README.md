# ⏰ Bus Cron: Lightweight Scheduled Task Framework

<p align="center">
<strong>High-Performance, Flexible Scheduled Task Scheduling Framework</strong>
</p>

-----

## 📖 Project Introduction

**Bus Cron** is a lightweight, high-performance scheduled task framework based on cron expressions. It provides a simple yet powerful API for task scheduling, supporting standard Linux crontab-style syntax while offering enterprise-grade features like task listeners, flexible scheduling patterns, and thread pool management.

-----

## ✨ Core Features

### 🎯 Basic Capabilities

* **Cron Expression Support**: Full support for standard 5-field cron expressions (minute, hour, day of month, month, day of week)
* **Quartz Compatibility**: Supports 6-field (with seconds) and 7-field (with year) expressions for Quartz compatibility
* **Flexible Scheduling**: Schedule tasks using cron patterns, Runnable tasks, or custom Crontab implementations
* **Thread Pool Management**: Configurable thread pool with daemon/non-daemon options
* **Task Listeners**: Monitor task lifecycle with start, success, and failure callbacks

### ⚡ Advanced Features

| Feature | Description | Benefit |
| :--- | :--- | :--- |
| **Second-Level Precision** | Optional second matching mode | Sub-minute scheduling accuracy |
| **Time Zone Support** | Per-scheduler timezone configuration | Multi-region deployment support |
| **Trigger Queue Mode** | Pre-calculated trigger times | Improved scheduling performance |
| **Task Metadata** | ID-based task management | Dynamic task control |
| **Configuration Loading** | Load tasks from config files | Externalized task management |

### 🌍 Cron Expression Format

**Standard 5-Field Format**:
```
* * * * *
│ │ │ │ │
│ │ │ │ └─ Day of Week (0-6, Sunday=0 or 7)
│ │ │ └─── Month (1-12)
│ │ └───── Day of Month (1-31)
│ └─────── Hour (0-23)
└───────── Minute (0-59)
```

**6-Field Format** (Quartz-compatible, includes seconds):
```
* * * * * *
│ │ │ │ │ │
│ │ │ │ │ └─ Day of Week
│ │ │ │ └─── Month
│ │ │ └───── Day of Month
│ │ └─────── Hour
│ └───────── Minute
└─────────── Second (0-59)
```

**7-Field Format** (includes year):
```
* * * * * * *
│ │ │ │ │ │ │
│ │ │ │ │ │ └─ Year (1970-2099)
│ │ │ │ │ └─── (other fields same as 6-field)
```

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-cron</artifactId>
    <version>8.5.0</version>
</dependency>
```

### Basic Usage

#### 1. Simple Scheduled Task

```java
import org.miaixz.bus.cron.Builder;

// Start the scheduler (loads config/cron.setting if present)
Builder.start();

// Schedule a task to run every 5 minutes
Builder.schedule("*/5 * * * *", () -> {
    System.out.println("Task executed at: " + new Date());
});

// Stop the scheduler
Builder.stop();
```

#### 2. Schedule with Custom ID

```java
String taskId = Builder.schedule("my-task-id", "0 * * * *", () -> {
    System.out.println("Hourly task execution");
});

// Remove the task
Builder.remove(taskId);
```

#### 3. Using Scheduler Directly

```java
import org.miaixz.bus.cron.Scheduler;

// Create a custom scheduler
Scheduler scheduler = new Scheduler();

// Configure and start
scheduler.setMatchSecond(true)  // Enable second-level precision
          .start(true);         // Start as daemon thread

// Schedule a task
scheduler.schedule("task1", "0 */2 * * *", () -> {
    System.out.println("Task runs every 2 hours");
});

// Stop the scheduler
scheduler.stop();
```

-----

## 📝 Usage Examples

### 1. Cron Pattern Examples

```java
// Every minute
"* * * * *"

// Every 5 minutes
"*/5 * * * *"

// At minute 5 of every hour
"5 * * * *"

// At 11:59 on Monday and Tuesday
"59 11 * * 1,2"

// Every 5 minutes between 3 and 18 (inclusive)
"3-18/5 * * * *"

// At 2:00 AM every Monday
"0 2 * * 1"

// Every second (requires matchSecond=true)
"* * * * * *"

// At 12:00 PM on the last day of every month
"0 12 L * *"

// At midnight on January 1st every year
"0 0 1 1 *"
```

### 2. Runnable Task Scheduling

```java
Scheduler scheduler = new Scheduler();
scheduler.start();

// Schedule a Runnable
scheduler.schedule("cleanup", "0 0 3 * * *", new Runnable() {
    @Override
    public void run() {
        System.out.println("Running daily cleanup at 3 AM");
        // Cleanup logic here
    }
});
```

### 3. Custom Crontab Implementation

```java
import org.miaixz.bus.cron.crontab.Crontab;

public class MyCustomTask implements Crontab {
    @Override
    public void execute() {
        System.out.println("Executing custom task");
        // Your task logic here
    }
}

// Schedule the custom task
Scheduler scheduler = new Scheduler();
scheduler.schedule("custom", "0 * * * *", new MyCustomTask());
scheduler.start();
```

### 4. Task Listeners

```java
import org.miaixz.bus.cron.listener.TaskListener;
import org.miaixz.bus.cron.Executor;

Scheduler scheduler = new Scheduler();

// Add a task listener
scheduler.addListener(new TaskListener() {
    @Override
    public void onStart(Executor executor) {
        System.out.println("Task started: " + executor.task());
    }

    @Override
    public void onSucceeded(Executor executor) {
        System.out.println("Task completed successfully");
    }

    @Override
    public void onFailed(Executor executor, Exception e) {
        System.err.println("Task failed: " + e.getMessage());
    }
});

scheduler.start();
```

### 5. Configuration File Scheduling

Create `config/cron.setting`:

```properties
# Schedule tasks using class.method syntax
com.example.TaskScheduler.cleanup = 0 0 3 * * *
com.example.TaskScheduler.backup = 0 0 4 * * *

# Grouped tasks
[com.example.jobs]
dataSync = */30 * * * *
reportGeneration = 0 9 * * 1-5
```

Load and execute:

```java
import org.miaixz.bus.cron.Builder;
import org.miaixz.bus.setting.Setting;

// Load from default locations (config/cron.setting or cron.setting)
Builder.start();

// Or load from custom path
Setting cronSetting = new Setting("path/to/custom-cron.setting");
Scheduler scheduler = new Scheduler();
scheduler.schedule(cronSetting).start();
```

### 6. Dynamic Task Management

```java
Scheduler scheduler = new Scheduler();
scheduler.start();

// Add a task
String taskId = scheduler.schedule("temp-task", "*/10 * * * *", () -> {
    System.out.println("Temporary task");
});

// Update the pattern
scheduler.updatePattern(taskId, new org.miaixz.bus.cron.pattern.CronPattern("*/5 * * * *"));

// Remove the task
scheduler.deschedule(taskId);

// Get task info
Crontab task = scheduler.getTask(taskId);
CronPattern pattern = scheduler.getPattern(taskId);

// Check scheduler status
boolean isEmpty = scheduler.isEmpty();
int taskCount = scheduler.size();
```

### 7. Advanced Configuration

```java
import org.miaixz.bus.cron.Configure;
import org.miaixz.bus.cron.Scheduler;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Create custom configuration
Configure config = Configure.of()
    .setTimeZone(TimeZone.getTimeZone("America/New_York"))
    .setMatchSecond(true)
    .setDaemon(false)
    .setUseTriggerQueue(true);

// Create scheduler with config
Scheduler scheduler = new Scheduler(config);

// Set custom thread pool
ExecutorService executor = Executors.newFixedThreadPool(10);
scheduler.setThreadExecutor(executor);

// Add listener
scheduler.addListener(new TaskListener() {
    // Implement listener methods
});

// Start
scheduler.start();
```

### 8. Cron Pattern Matching

```java
import org.miaixz.bus.cron.pattern.CronPattern;
import java.util.Date;

// Create a pattern
CronPattern pattern = new CronPattern("0 * * * *");

// Check if a time matches
boolean matches = pattern.match(new Date(), false);

// Get next matching time
long nextMatch = pattern.nextMatchFromNow();

// Find all matches in a range
Date start = new Date();
Date end = new Date(start.getTime() + 86400000); // 24 hours later
List<Date> matches = CronPattern.matchedDates("0 * * * *", start, end, 24);

// Validate an expression
boolean isValid = Builder.isValidExpression("0 * * * *");
```

-----

## 💡 Best Practices

### 1. Choose Appropriate Precision

```java
// For most cases, minute precision is sufficient
Builder.setMatchSecond(false);
Builder.schedule("*/5 * * * *", task);

// Only enable second precision if needed
Builder.setMatchSecond(true);
Builder.schedule("*/30 * * * * *", task);  // Every 30 seconds
```

### 2. Handle Exceptions in Tasks

```java
// Good: Handle exceptions within the task
Builder.schedule("0 * * * *", () -> {
    try {
        // Task logic
        performOperation();
    } catch (Exception e) {
        logger.error("Task failed", e);
    }
});

// Better: Use task listeners
scheduler.addListener(new TaskListener() {
    @Override
    public void onFailed(Executor executor, Exception e) {
        logger.error("Task failed: {}", executor.task(), e);
    }
});
```

### 3. Use Task IDs for Management

```java
// Always specify IDs for tasks that might be managed dynamically
String taskId = scheduler.schedule("backup-task", "0 2 * * *", backupTask);

// Later, you can update or remove it
scheduler.updatePattern(taskId, newPattern);
scheduler.deschedule(taskId);
```

### 4. Configure Thread Pool Appropriately

```java
// For CPU-intensive tasks
ExecutorService cpuPool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
scheduler.setThreadExecutor(cpuPool);

// For I/O-intensive tasks
ExecutorService ioPool = Executors.newCachedThreadPool();
scheduler.setThreadExecutor(ioPool);
```

### 5. Consider Daemon vs Non-Daemon Threads

```java
// Daemon: Tasks terminate immediately when scheduler stops
scheduler.start(true);

// Non-Daemon: Tasks complete execution even after scheduler stops
scheduler.start(false);
```

-----

## ❓ Frequently Asked Questions

### Q1: How do I schedule a task at a specific time every day?

```java
// Schedule at 9:30 AM every day
Scheduler scheduler = new Scheduler();
scheduler.schedule("daily-930", "30 9 * * *", () -> {
    System.out.println("Task runs at 9:30 AM daily");
});
scheduler.start();
```

### Q2: Can I schedule tasks to run on specific days of the week?

```java
// Run only on weekdays (Monday-Friday)
scheduler.schedule("weekdays", "0 9 * * 1-5", task);

// Run on Monday, Wednesday, and Friday
scheduler.schedule("mwf", "0 10 * * 1,3,5", task);

// Run on weekends
scheduler.schedule("weekends", "0 12 * * 6,0", task);
```

### Q3: How do I enable second-level precision?

```java
// Using Builder
Builder.setMatchSecond(true);
Builder.start();

// Using Scheduler
Scheduler scheduler = new Scheduler();
scheduler.setMatchSecond(true);
scheduler.start();
```

### Q4: How can I stop a running scheduler gracefully?

```java
// Stop and keep tasks
scheduler.stop();

// Stop and clear all tasks
scheduler.stop(true);

// Wait for running tasks to complete
scheduler.stop(false);
```

### Q5: How do I handle task failures?

```java
scheduler.addListener(new SimpleTaskListener() {
    @Override
    public void onFailed(Executor executor, Exception e) {
        // Log the error
        logger.error("Task failed: {}", executor.task(), e);

        // Send alert
        alertService.send("Task execution failed", e);

        // Retry logic
        if (isRetryable(e)) {
            scheduleRetry(executor.task());
        }
    }
});
```

### Q6: Can I use cron expressions with named months/days?

```java
// Yes! Month names (case-insensitive)
"0 9 * Jan * "      // 9 AM every day in January
"0 12 * Apr-Jun *"  // Noon every day from April to June

// Day of week names
"0 9 * * Mon"       // 9 AM every Monday
"0 9 * * Mon,Fri"   // 9 AM every Monday and Friday
```

### Q7: How do I schedule a task on the last day of the month?

```java
// Use 'L' for last day of month
scheduler.schedule("last-day", "0 0 L * *", () -> {
    System.out.println("Last day of the month");
});
```

### Q8: What is the difference between daemon and non-daemon mode?

```java
// Daemon mode: JVM exits even if tasks are running
scheduler.start(true);

// Non-daemon mode: JVM waits for tasks to complete
scheduler.start(false);
```

Use daemon mode for background tasks that can be interrupted, and non-daemon mode for critical tasks that must complete.

-----

## 🔄 Version Compatibility

| Bus Cron Version | JDK Version |
| :--- | :--- |
| 8.x | 17+ |
| 7.x | 11+ |

-----

## 📋 Configuration Reference

### Scheduler Configuration Options

| Option | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `timezone` | `TimeZone` | `TimeZone.getDefault()` | Time zone for task scheduling |
| `matchSecond` | `boolean` | `false` | Whether to enable second-level precision |
| `daemon` | `boolean` | `false` | Whether to use daemon threads |
| `useTriggerQueue` | `boolean` | `false` | Whether to use trigger queue mode |

### Cron Expression Special Characters

| Character | Description | Example |
| :--- | :--- | :--- |
| `*` | Match all values | `* * * * *` (every minute) |
| `?` | Same as `*` | `? * ? * ?` |
| `/` | Step values | `*/5 * * * *` (every 5 minutes) |
| `-` | Range | `0 9-17 * * *` (9 AM to 5 PM) |
| `,` | List | `0 9,12,18 * * *` (9 AM, noon, 6 PM) |
| `L` | Last day | `0 0 L * *` (last day of month) |

-----

## 🔧 Advanced Topics

### Task Isolation and Concurrency

```java
// By default, tasks run independently
// To prevent overlapping executions:

public class IsolatedTask implements Crontab {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override
    public void execute() {
        if (!isRunning.compareAndSet(false, true)) {
            logger.warn("Previous execution still running, skipping");
            return;
        }

        try {
            // Task logic
        } finally {
            isRunning.set(false);
        }
    }
}
```

### Time Zone-Aware Scheduling

```java
// Create scheduler for specific timezone
Configure config = Configure.of()
    .setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

Scheduler scheduler = new Scheduler(config);

// Pattern matches in Shanghai time
scheduler.schedule("shanghai-task", "0 9 * * *", task);
```

### Cron Expression Validation

```java
// Validate before scheduling
String expression = "0 0 12 * * *";  // Invalid: has seconds but matchSecond=false

if (Builder.isValidExpression(expression)) {
    Builder.schedule(expression, task);
} else {
    logger.error("Invalid cron expression: {}", expression);
}
```

-----

## 📚 API Reference

### Builder Class (Static Methods)

```java
void start()                                              // Start scheduler
void start(boolean isDaemon)                              // Start with daemon setting
void stop()                                               // Stop scheduler
void restart()                                            // Restart scheduler
String schedule(String pattern, Crontab task)            // Schedule task
String schedule(String id, String pattern, Crontab task) // Schedule with ID
boolean remove(String schedulerId)                        // Remove task
void setMatchSecond(boolean isMatchSecond)                // Set second precision
```

### Scheduler Class

```java
Scheduler start()                                         // Start scheduler
Scheduler start(boolean isDaemon)                         // Start with daemon setting
Scheduler stop()                                          // Stop scheduler
Scheduler stop(boolean clearTasks)                        // Stop and optionally clear
Scheduler schedule(String id, String pattern, Crontab)    // Schedule task
Scheduler deschedule(String id)                           // Remove task
Scheduler updatePattern(String id, CronPattern)           // Update pattern
Crontab getTask(String id)                                // Get task
CronPattern getPattern(String id)                         // Get pattern
int size()                                                // Get task count
boolean isEmpty()                                         // Check if empty
Scheduler clear()                                         // Clear all tasks
```

### CronPattern Class

```java
boolean match(long millis, boolean isMatchSecond)        // Check if time matches
boolean match(Calendar calendar, boolean isMatchSecond)  // Check if calendar matches
long nextMatchFromNow()                                  // Get next match time
Calendar nextMatchAfter(Calendar calendar)               // Get next match after time
static List<Date> matchedDates(...)                      // Find all matches in range
```

-----

For more information and updates, visit: [https://github.com/818000/bus](https://github.com/818000/bus)
