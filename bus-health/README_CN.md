# 🏥 Bus Health: 跨平台系统健康监控框架

<p align="center">
<strong>跨平台、高性能的系统健康监控解决方案</strong>
</p>

-----

## 📖 项目介绍

**Bus Health** 是基于 OSHI（Operating System and Hardware Information）的跨平台系统健康监控框架。它为 Windows、Linux、macOS 和
Unix 系统提供了统一的 API 来监控系统和硬件信息。设计简洁、性能优先,使开发者能够轻松获取全面的系统健康指标,无需担心底层平台差异。

-----

## ✨ 核心特性

### 🎯 平台支持

* **跨平台**: 支持 Windows、Linux、macOS、Unix (Solaris、FreeBSD、AIX、OpenBSD)
* **统一 API**: 所有平台使用单一接口,无需编写平台特定代码
* **零配置**: 开箱即用,自动检测平台

### ⚡ 系统信息监控

| 类别     | 功能                         | 描述                                    |
|:---------|:-----------------------------|:----------------------------------------|
| **CPU**  | 使用率、负载、时钟           | 实时 CPU 负载、每处理器统计、时钟计数器 |
| **内存** | 物理内存、虚拟内存、交换分区 | 内存使用量、可用性和交换分区统计        |
| **磁盘** | 存储、分区、使用率           | 磁盘驱动器、分区、文件存储、I/O 统计    |
| **网络** | 接口、带宽                   | 网络接口、IP 地址、流量监控             |
| **进程** | 列表、CPU、内存              | 进程列表及每进程 CPU 和内存使用情况     |
| **硬件** | 传感器、电池、显示器         | 温度、风扇、电压、电池状态、显示器信息  |
| **系统** | 操作系统、固件、主板         | 操作系统版本、制造商、型号、序列号      |

### 🔍 高级功能

* **实时监控**: 通过可配置的轮询间隔跟踪系统指标
* **EDID 解析**: 详细的显示器信息,包括制造商、型号、分辨率
* **USB 设备枚举**: 列出所有连接的 USB 设备及完整详细信息
* **文件系统监控**: 挂载点、文件类型、使用统计
* **网络流量**: 实时带宽监控 (RX/TX 字节数和包数)
* **电池状态**: 容量、剩余时间、功耗、电压、温度
* **传感器数据**: CPU 温度、风扇转速、电压读数

### 🛡️ 企业级特性

* **轻量级**: 最小内存占用,使用 Memoizer 模式实现延迟初始化
* **线程安全**: 所有操作都是线程安全的,支持并发访问
* **灵活查询**: 查询特定指标或获取完整的系统概览
* **格式化输出**: 内置格式化,提供人类可读的指标 (KB、MB、GB、TB)
* **自定义健康检查**: 通过自定义健康检查进行扩展

-----

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-health</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 基础用法

#### 1. 获取系统信息

```java
import org.miaixz.bus.health.Collector;

public class HealthExample {
    public static void main(String[] args) {
        Collector collector = new Collector();

        // 获取所有系统信息
        Map<String, Object> allInfo = collector.getAll();
        System.out.println(allInfo);

        // 获取特定信息
        Map<String, Object> cpuInfo = collector.getSingle(TID.CPU);
        System.out.println(cpuInfo);
    }
}
```

#### 2. 监控 CPU 使用率

```java
Collector collector = new Collector();

// 获取 CPU 信息
Cpu cpu = collector.getCpu();
System.out.println("物理核心数: " + cpu.getPhysicalCores());
System.out.println("逻辑核心数: " + cpu.getLogicalCores());
System.out.println("总使用率: " + cpu.getTotalUsage() + "%");
System.out.println("用户使用率: " + cpu.getUserUsage() + "%");
System.out.println("系统使用率: " + cpu.getSystemUsage() + "%");
System.out.println("IO 等待: " + cpu.getIoWait() + "%");
```

#### 3. 监控内存使用率

```java
Collector collector = new Collector();

// 获取内存信息
Memory memory = collector.getMemory();
System.out.println("总内存: " + memory.getTotal());
System.out.println("已用内存: " + memory.getUsed());
System.out.println("空闲内存: " + memory.getFree());
System.out.println("使用率: " + memory.getUsage() + "%");
```

#### 4. 监控磁盘使用率

```java
Collector collector = new Collector();

// 获取磁盘信息
List<Disk> disks = collector.getDisk();
for (Disk disk : disks) {
    System.out.println("设备: " + disk.getDeviceName());
    System.out.println("挂载点: " + disk.getMountPoint());
    System.out.println("总空间: " + Formats.formatBytes(disk.getTotalSpace()));
    System.out.println("已用空间: " + Formats.formatBytes(disk.getUsedSpace()));
    System.out.println("可用空间: " + Formats.formatBytes(disk.getFreeSpace()));
    System.out.println("使用率: " + disk.getUsagePercent() + "%");
}
```

#### 5. 监控 JVM 信息

```java
Collector collector = new Collector();

// 获取 JVM 信息
Jvm jvm = collector.getJvm();
System.out.println("JDK 版本: " + jvm.getJdkVersion());
System.out.println("JDK 主目录: " + jvm.getJdkHome());
System.out.println("JVM 名称: " + jvm.getJdkName());
System.out.println("总内存: " + Formats.formatBytes(jvm.getTotalMemory()));
System.out.println("最大内存: " + Formats.formatBytes(jvm.getMaxMemory()));
System.out.println("空闲内存: " + Formats.formatBytes(jvm.getFreeMemory()));
System.out.println("使用率: " + jvm.getUsagePercent() + "%");
System.out.println("启动时间: " + jvm.getStartTime());
System.out.println("运行时间: " + jvm.getUptime() + "ms");
```

#### 6. 监控进程

```java
Collector collector = new Collector();

// 获取 CPU 使用率前 10 的进程
Map<String, Object> result = new HashMap<>();
collector.appendProcessList(10, TID.PROCESS, result);
List<Map<String, Object>> processes = (List<Map<String, Object>>) result.get(TID.PROCESS);

for (Map<String, Object> process : processes) {
    System.out.println("进程 ID: " + process.get("pid"));
    System.out.println("名称: " + process.get("name"));
    System.out.println("CPU: " + process.get(TID.CPU) + "%");
}
```

#### 7. 获取主机信息

```java
Collector collector = new Collector();

// 获取主机信息(包括网络统计)
Host host = collector.getHost();
System.out.println("主机名: " + host.getName());
System.out.println("IP 地址: " + host.getIp());
System.out.println("操作系统: " + host.getOs());
System.out.println("操作系统架构: " + host.getOsArch());
System.out.println("用户目录: " + host.getUserDir());
System.out.println("接收字节/秒: " + host.getRxBytesPerSecond() + "KB");
System.out.println("发送字节/秒: " + host.getTxBytesPerSecond() + "KB");
```

-----

## 📝 高级用法

### 1. 访问硬件抽象层

```java
Collector collector = new Collector();
HardwareAbstractionLayer hal = collector.getHardware();

// 处理器信息
CentralProcessor processor = hal.getProcessor();
System.out.println("处理器标识: " + processor.getProcessorIdentifier());
System.out.println("物理核心数: " + processor.getPhysicalProcessorCount());
System.out.println("逻辑核心数: " + processor.getLogicalProcessorCount());

// 内存信息
GlobalMemory globalMemory = hal.getMemory();
System.out.println("总内存: " + globalMemory.getTotal());

// 传感器
Sensors sensors = hal.getSensors();
System.out.println("CPU 温度: " + sensors.getCpuTemperature());
System.out.println("风扇转速: " + Arrays.toString(sensors.getFanSpeeds()));
System.out.println("CPU 电压: " + sensors.getCpuVoltage());

// 电源
List<PowerSource> powerSources = hal.getPowerSources();
for (PowerSource ps : powerSources) {
    System.out.println("名称: " + ps.getName());
    System.out.println("剩余容量: " + ps.getRemainingCapacity() + "%");
    System.out.println("剩余时间: " + ps.getTimeRemaining());
}

// 网络接口
List<NetworkIF> networkIFs = hal.getNetworkIFs();
for (NetworkIF net : networkIFs) {
    System.out.println("名称: " + net.getName());
    System.out.println("显示名称: " + net.getDisplayName());
    System.out.println("IPv4: " + Arrays.toString(net.getIPv4addr()));
}

// 磁盘存储
List<HWDiskStore> diskStores = hal.getDiskStores();
for (HWDiskStore disk : diskStores) {
    System.out.println("型号: " + disk.getModel());
    System.out.println("序列号: " + disk.getSerial());
    System.out.println("大小: " + disk.getSize());
}

// 显示器
List<Display> displays = hal.getDisplays();
for (Display display : displays) {
    System.out.println("EDID: " + new String(display.getEdid()));
}
```

### 2. 访问操作系统信息

```java
Collector collector = new Collector();
OperatingSystem os = collector.getOperatingSystem();

// 操作系统信息
System.out.println("系列: " + os.getFamily());
System.out.println("制造商: " + os.getManufacturer());
System.out.println("版本: " + os.getVersionInfo());

// 进程
List<OSProcess> processes = os.getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, 10);
for (OSProcess process : processes) {
    System.out.println("进程 ID: " + process.getProcessID());
    System.out.println("名称: " + process.getName());
    System.out.println("CPU 负载: " + process.getProcessCpuLoadCumulative());
    System.out.println("内存: " + process.getResidentSetSize());
}

// 文件系统
FileSystem fileSystem = os.getFileSystem();
Iterable<OSFileStore> fileStores = fileSystem.getFileStores();
for (OSFileStore fs : fileStores) {
    System.out.println("名称: " + fs.getName());
    System.out.println("挂载点: " + fs.getMount());
    System.out.println("类型: " + fs.getType());
    System.out.println("总空间: " + fs.getTotalSpace());
}
```

### 3. 自定义健康检查

```java
public class CustomHealthCheck {

    private final Collector collector = new Collector();

    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // CPU 健康检查
        Cpu cpu = collector.getCpu();
        health.put("cpu_健康", cpu.getTotalUsage() < 80.0);
        health.put("cpu_使用率", cpu.getTotalUsage());

        // 内存健康检查
        Memory memory = collector.getMemory();
        health.put("memory_健康", memory.getUsage() < 80.0);
        health.put("memory_使用率", memory.getUsage());

        // 磁盘健康检查
        List<Disk> disks = collector.getDisk();
        boolean diskHealthy = true;
        for (Disk disk : disks) {
            if (disk.getUsagePercent() > 80.0) {
                diskHealthy = false;
                break;
            }
        }
        health.put("disk_健康", diskHealthy);

        return health;
    }
}
```

### 4. 定时监控

```java
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsScheduler {

    private final Collector collector = new Collector();
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            Cpu cpu = collector.getCpu();
            Memory memory = collector.getMemory();

            System.out.println("CPU 使用率: " + cpu.getTotalUsage() + "%");
            System.out.println("内存使用率: " + memory.getUsage() + "%");

            // 如果使用率高则发出警告
            if (cpu.getTotalUsage() > 80.0) {
                System.out.println("警告: CPU 使用率过高!");
            }
            if (memory.getUsage() > 80.0) {
                System.out.println("警告: 内存使用率过高!");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        scheduler.shutdown();
    }
}
```

### 5. 查询多个指标

```java
Collector collector = new Collector();

// 获取多个特定指标
List<String> metrics = Arrays.asList(
    TID.CPU,
    TID.MEMORY,
    TID.DISK,
    TID.JVM
);

Map<String, Object> result = collector.get(metrics);
result.forEach((key, value) -> System.out.println(key + ": " + value));
```

-----

## 📋 类型标识符参考

### 可用指标 (TID)

| 标识符             | 描述                 | 返回类型                    |
|:-------------------|:---------------------|:----------------------------|
| `TID.HOST`         | 主机信息             | `Host`                      |
| `TID.CPU`          | CPU 使用率和统计     | `Cpu`                       |
| `TID.MEMORY`       | 内存使用情况         | `Memory`                    |
| `TID.DISK`         | 磁盘存储信息         | `List<Disk>`                |
| `TID.JVM`          | JVM 运行时信息       | `Jvm`                       |
| `TID.ALL_DISK`     | 所有磁盘使用率百分比 | `Double`                    |
| `TID.PROCESS`      | CPU 使用率最高的进程 | `List<Map<String, Object>>` |
| `TID.SYSTEM`       | 计算机系统信息       | `ComputerSystem`            |
| `TID.PROCESSOR`    | 处理器详情           | `CentralProcessor`          |
| `TID.HARDWARE`     | 所有硬件信息         | `Map<String, Object>`       |
| `TID.POWERSOURCES` | 电池/电源信息        | `Map<String, Object>`       |
| `TID.NETWORKIFS`   | 网络接口信息         | `Map<String, Object>`       |

-----

## 💡 最佳实践

### 1. 使用单例 Collector

```java
// ✅ 推荐: 重用 Collector 实例
private static final Collector COLLECTOR = new Collector();

public void checkHealth() {
    Cpu cpu = COLLECTOR.getCpu();
    // ...
}

// ❌ 不推荐: 每次都创建新实例
public void checkHealth() {
    Collector collector = new Collector(); // 浪费资源
    // ...
}
```

### 2. 处理平台差异

```java
Collector collector = new Collector();
OS osType = (OS) collector.type();

switch (osType) {
    case WINDOWS:
        // Windows 特定逻辑
        break;
    case LINUX:
        // Linux 特定逻辑
        break;
    case MACOS:
        // macOS 特定逻辑
        break;
    default:
        // 默认逻辑
}
```

### 3. 仅监控特定指标

```java
// ✅ 推荐: 仅查询所需内容
Map<String, Object> result = collector.getSingle(TID.CPU);

// ❌ 不推荐: 只需一个指标却查询所有
Map<String, Object> result = collector.getAll(); // 浪费资源
```

### 4. 使用定时监控

```java
// ✅ 推荐: 使用定时执行器进行周期性检查
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    Cpu cpu = collector.getCpu();
    // 根据 cpu.getTotalUsage() 记录日志或发出警告
}, 0, 5, TimeUnit.SECONDS);

// ❌ 不推荐: 忙等待
while (true) {
    Cpu cpu = collector.getCpu();
    Thread.sleep(5000); // 阻塞线程
}
```

### 5. 优雅地处理异常

```java
public Map<String, Object> safeGetCpu() {
    try {
        Cpu cpu = collector.getCpu();
        return Map.of("cpu", cpu);
    } catch (Exception e) {
        Logger.error("获取 CPU 信息失败", e);
        return Map.of("cpu", null, "error", e.getMessage());
    }
}
```

-----

## ❓ 常见问题

### Q1: 如何获取实时 CPU 使用率?

**A**: 使用 `getCpu()` 方法,它会计算 600 毫秒间隔内的使用率:

```java
Cpu cpu = collector.getCpu();
double totalUsage = cpu.getTotalUsage();
```

### Q2: 可以监控远程系统吗?

**A**: Bus Health 监控本地系统。对于远程监控,可以考虑:

- 通过 HTTP API 暴露健康检查端点
- 使用监控代理将指标推送到中心服务器
- 集成 Prometheus 等监控系统

### Q3: 如何在服务器上获取电池信息?

**A**: 使用 `getPowerSourceInfo()` 方法:

```java
Map<String, Object> powerInfo = collector.getPowerSourceInfo();
List<PowerSource> powerSources = collector.getHardware().getPowerSources();

for (PowerSource ps : powerSources) {
    if (!ps.isPowerOnLine()) {
        System.out.println("使用电池: " + ps.getRemainingCapacity() + "%");
    }
}
```

### Q4: 需要什么权限?

**A**:

- **Linux/Unix**: 大部分信息无需特殊权限
- **Windows**: 通常无需特殊权限
- **某些传感器**: 可能需要根据操作系统提升权限

### Q5: 如何减少监控开销?

**A**:

- 仅查询必要的指标
- 增加监控间隔 (例如,5-10 秒而不是 1 秒)
- 使用单例 Collector 实例
- 适当缓存结果

### Q6: 可以与 Spring Boot Actuator 集成吗?

**A**: 可以,创建自定义健康指示器:

```java
@Component
public class SystemHealthIndicator implements HealthIndicator {

    private final Collector collector = new Collector();

    @Override
    public Health health() {
        Cpu cpu = collector.getCpu();
        Memory memory = collector.getMemory();

        if (cpu.getTotalUsage() > 90.0 || memory.getUsage() > 90.0) {
            return Health.down()
                .withDetail("cpu", cpu.getTotalUsage())
                .withDetail("memory", memory.getUsage())
                .build();
        }

        return Health.up()
            .withDetail("cpu", cpu.getTotalUsage())
            .withDetail("memory", memory.getUsage())
            .build();
    }
}
```

### Q7: 如何获取网络流量统计?

**A**: 网络统计在 `getHost()` 中通过 3 秒间隔计算:

```java
Host host = collector.getHost();
System.out.println("接收: " + host.getRxBytesPerSecond() + " KB/s");
System.out.println("发送: " + host.getTxBytesPerSecond() + " KB/s");
```

### Q8: 是否线程安全?

**A**: 是的,Collector 是线程安全的,可以在多线程环境中安全使用。

-----

## 🔄 版本兼容性

| Bus Health 版本 | JDK 版本 | OSHI 版本 |
|:----------------|:---------|:----------|
| 8.x             | 17+      | 6.x+      |
| 7.x             | 11+      | 5.x+      |

-----

## 🚀 性能考虑

### 1. 初始化开销

- 首次访问每个平台组件 (OS、Hardware)涉及延迟初始化
- 后续访问使用缓存实例 (Memoizer 模式)
- 典型初始化时间: 50-200ms (取决于平台)

### 2. 查询性能

| 操作          | 典型时间 | 说明            |
|:--------------|:---------|:----------------|
| `getCpu()`    | 600ms    | 包含测量间隔    |
| `getMemory()` | <10ms    | 缓存数据        |
| `getDisk()`   | 20-50ms  | 文件系统查询    |
| `getJvm()`    | <5ms     | JVM MXBean 访问 |
| `getHost()`   | 3000ms   | 包含网络测量    |

### 3. 内存占用

- Collector 实例: ~1KB
- 缓存的平台实例: ~50-100KB
- 查询结果: 根据数据量变化

### 4. 优化建议

```java
// 如果需要频繁轮询,缓存结果
private Cpu cachedCpu;
private long lastCpuUpdate = 0;

public Cpu getCpuWithCache() {
    long now = System.currentTimeMillis();
    if (now - lastCpuUpdate > 5000) { // 5 秒缓存
        cachedCpu = collector.getCpu();
        lastCpuUpdate = now;
    }
    return cachedCpu;
}
```

-----

## 📊 支持的平台

### 操作系统

| 平台        | 状态        | 说明                            |
|:------------|:------------|:--------------------------------|
| **Windows** | ✅ 完全支持 | Windows 7/8/10/11, Server 2012+ |
| **Linux**   | ✅ 完全支持 | 所有主流发行版                  |
| **macOS**   | ✅ 完全支持 | macOS 10.12+                    |
| **FreeBSD** | ✅ 支持     | FreeBSD 10+                     |
| **OpenBSD** | ✅ 支持     | OpenBSD 6+                      |
| **Solaris** | ✅ 支持     | Solaris 10+                     |
| **AIX**     | ✅ 支持     | AIX 6+                          |

### 硬件信息

| 类别       | Windows | Linux | macOS | Unix |
|:-----------|:--------|:------|:------|:-----|
| **CPU**    | ✅      | ✅    | ✅    | ✅   |
| **内存**   | ✅      | ✅    | ✅    | ✅   |
| **磁盘**   | ✅      | ✅    | ✅    | ✅   |
| **网络**   | ✅      | ✅    | ✅    | ✅   |
| **电池**   | ✅      | ✅    | ✅    | ⚠️   |
| **传感器** | ✅      | ⚠️    | ✅    | ⚠️   |
| **显示器** | ✅      | ⚠️    | ✅    | ❌   |
| **USB**    | ✅      | ✅    | ✅    | ⚠️   |

图例: ✅ 完全支持 | ⚠️ 部分支持 | ❌ 不支持

-----

## 🔧 配置示例

### Maven 配置

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-health</artifactId>
    <version>8.x.x</version>
</dependency>

<!-- 可选: 更好的本地库支持 -->
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.18.0</version>
</dependency>
```

### Gradle 配置

```groovy
implementation 'org.miaixz:bus-health:8.x.x'

// 可选: 更好的本地库支持
implementation 'net.java.dev.jna:jna-platform:5.18.0'
```

-----

## 📚 更多资源

- **文档**: [https://www.miaixz.org](https://www.miaixz.org)
- **GitHub 仓库**: [https://github.com/818000/bus](https://github.com/818000/bus)
- **问题跟踪**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
- **OSHI 项目**: [https://github.com/oshi/oshi](https://github.com/oshi/oshi)

-----

## 🙏 致谢

本项目部分基于 [OSHI](https://github.com/oshi/oshi)(Operating System and Hardware Information),采用 MIT 许可证。感谢 OSHI
团队的杰出工作。

-----

**由 Miaixz 团队用 ❤️ 构建**
