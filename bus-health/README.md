# 🏥 Bus Health: Cross-Platform System Health Monitoring Framework

<p align="center">
<strong>Cross-Platform, High-Performance System Health Monitoring Solution</strong>
</p>

-----

## 📖 Project Introduction

**Bus Health** is a cross-platform system health monitoring framework based on OSHI (Operating System and Hardware Information). It provides a unified API for monitoring system and hardware information across Windows, Linux, macOS, and Unix systems. Designed with simplicity and performance in mind, it enables developers to easily obtain comprehensive system health metrics without worrying about underlying platform differences.

-----

## ✨ Core Features

### 🎯 Platform Support

* **Cross-Platform**: Supports Windows, Linux, macOS, Unix (Solaris, FreeBSD, AIX, OpenBSD)
* **Unified API**: Single interface for all platforms, no platform-specific code required
* **Zero Configuration**: Works out of the box with automatic platform detection

### ⚡ System Information Monitoring

| Category | Features | Description |
| :--- | :--- | :--- |
| **CPU** | Usage, Load, Ticks | Real-time CPU load, per-processor stats, tick counters |
| **Memory** | Physical, Virtual, Swap | Memory usage, availability, and swap statistics |
| **Disk** | Storage, Partitions, Usage | Disk drives, partitions, file stores, I/O statistics |
| **Network** | Interfaces, Bandwidth | Network interfaces, IP addresses, traffic monitoring |
| **Process** | List, CPU, Memory | Process listing with CPU and memory usage per process |
| **Hardware** | Sensors, Battery, Display | Temperature, fans, voltage, battery status, display info |
| **System** | OS, Firmware, Baseboard | OS version, manufacturer, model, serial numbers |

### 🔍 Advanced Capabilities

* **Real-time Monitoring**: Track system metrics with configurable polling intervals
* **EDID Parsing**: Detailed display information including manufacturer, model, resolution
* **USB Device Enumeration**: List all connected USB devices with full details
* **File System Monitoring**: Mount points, file types, usage statistics
* **Network Traffic**: Real-time bandwidth monitoring (RX/TX bytes and packets)
* **Battery Status**: Capacity, remaining time, power usage rate, voltage, temperature
* **Sensor Data**: CPU temperature, fan speeds, voltage readings

### 🛡️ Enterprise Features

* **Lightweight**: Minimal memory footprint with lazy initialization using Memoizer pattern
* **Thread-Safe**: All operations are thread-safe for concurrent access
* **Flexible Querying**: Query specific metrics or get comprehensive system overview
* **Formatted Output**: Built-in formatting for human-readable metrics (KB, MB, GB, TB)
* **Custom Health Indicators**: Extend with custom health checks

-----

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-health</artifactId>
    <version>x.x.x</version>
</dependency>
```

### Basic Usage

#### 1. Get System Information

```java
import org.miaixz.bus.health.Collector;

public class HealthExample {
    public static void main(String[] args) {
        Collector collector = new Collector();

        // Get all system information
        Map<String, Object> allInfo = collector.getAll();
        System.out.println(allInfo);

        // Get specific information
        Map<String, Object> cpuInfo = collector.getSingle(TID.CPU);
        System.out.println(cpuInfo);
    }
}
```

#### 2. Collect CPU Usage

```java
Collector collector = new Collector();

// Get CPU information
Cpu cpu = collector.getCpu();
System.out.println("Physical Cores: " + cpu.getPhysicalCores());
System.out.println("Logical Cores: " + cpu.getLogicalCores());
System.out.println("Total Usage: " + cpu.getTotalUsage() + "%");
System.out.println("User Usage: " + cpu.getUserUsage() + "%");
System.out.println("System Usage: " + cpu.getSystemUsage() + "%");
System.out.println("IO Wait: " + cpu.getIoWait() + "%");
```

#### 3. Collect Memory Usage

```java
Collector collector = new Collector();

// Get memory information
Memory memory = collector.getMemory();
System.out.println("Total Memory: " + memory.getTotal());
System.out.println("Used Memory: " + memory.getUsed());
System.out.println("Free Memory: " + memory.getFree());
System.out.println("Usage: " + memory.getUsage() + "%");
```

#### 4. Collect Disk Usage

```java
Collector collector = new Collector();

// Get disk information
List<Disk> disks = collector.getDisk();
for (Disk disk : disks) {
    System.out.println("Device: " + disk.getDeviceName());
    System.out.println("Mount Point: " + disk.getMountPoint());
    System.out.println("Total Space: " + Formats.formatBytes(disk.getTotalSpace()));
    System.out.println("Used Space: " + Formats.formatBytes(disk.getUsedSpace()));
    System.out.println("Free Space: " + Formats.formatBytes(disk.getFreeSpace()));
    System.out.println("Usage: " + disk.getUsagePercent() + "%");
}
```

#### 5. Collect JVM Information

```java
Collector collector = new Collector();

// Get JVM information
Jvm jvm = collector.getJvm();
System.out.println("JDK Version: " + jvm.getJdkVersion());
System.out.println("JDK Home: " + jvm.getJdkHome());
System.out.println("JVM Name: " + jvm.getJdkName());
System.out.println("Total Memory: " + Formats.formatBytes(jvm.getTotalMemory()));
System.out.println("Max Memory: " + Formats.formatBytes(jvm.getMaxMemory()));
System.out.println("Free Memory: " + Formats.formatBytes(jvm.getFreeMemory()));
System.out.println("Usage: " + jvm.getUsagePercent() + "%");
System.out.println("Start Time: " + jvm.getStartTime());
System.out.println("Uptime: " + jvm.getUptime() + "ms");
```

#### 6. Collect Processes

```java
Collector collector = new Collector();

// Get top 10 processes by CPU usage
Map<String, Object> result = new HashMap<>();
collector.appendProcessList(10, TID.PROCESS, result);
List<Map<String, Object>> processes = (List<Map<String, Object>>) result.get(TID.PROCESS);

for (Map<String, Object> process : processes) {
    System.out.println("PID: " + process.get("pid"));
    System.out.println("Name: " + process.get("name"));
    System.out.println("CPU: " + process.get(TID.CPU) + "%");
}
```

#### 7. Get Host Information

```java
Collector collector = new Collector();

// Get host information (includes network statistics)
Host host = collector.getHost();
System.out.println("Host Name: " + host.getName());
System.out.println("IP Address: " + host.getIp());
System.out.println("OS: " + host.getOs());
System.out.println("OS Arch: " + host.getOsArch());
System.out.println("User Dir: " + host.getUserDir());
System.out.println("RX Bytes/s: " + host.getRxBytesPerSecond() + "KB");
System.out.println("TX Bytes/s: " + host.getTxBytesPerSecond() + "KB");
```

-----

## 📝 Advanced Usage

### 1. Access Hardware Abstraction Layer

```java
Collector collector = new Collector();
HardwareAbstractionLayer hal = collector.getHardware();

// Processor information
CentralProcessor processor = hal.getProcessor();
System.out.println("Processor ID: " + processor.getProcessorIdentifier());
System.out.println("Physical Cores: " + processor.getPhysicalProcessorCount());
System.out.println("Logical Cores: " + processor.getLogicalProcessorCount());

// Memory information
GlobalMemory globalMemory = hal.getMemory();
System.out.println("Total Memory: " + globalMemory.getTotal());

// Sensors
Sensors sensors = hal.getSensors();
System.out.println("CPU Temperature: " + sensors.getCpuTemperature());
System.out.println("Fan Speeds: " + Arrays.toString(sensors.getFanSpeeds()));
System.out.println("CPU Voltage: " + sensors.getCpuVoltage());

// Power sources
List<PowerSource> powerSources = hal.getPowerSources();
for (PowerSource ps : powerSources) {
    System.out.println("Name: " + ps.getName());
    System.out.println("Remaining Capacity: " + ps.getRemainingCapacity() + "%");
    System.out.println("Time Remaining: " + ps.getTimeRemaining());
}

// Network interfaces
List<NetworkIF> networkIFs = hal.getNetworkIFs();
for (NetworkIF net : networkIFs) {
    System.out.println("Name: " + net.getName());
    System.out.println("Display Name: " + net.getDisplayName());
    System.out.println("IPv4: " + Arrays.toString(net.getIPv4addr()));
}

// Disk stores
List<HWDiskStore> diskStores = hal.getDiskStores();
for (HWDiskStore disk : diskStores) {
    System.out.println("Model: " + disk.getModel());
    System.out.println("Serial: " + disk.getSerial());
    System.out.println("Size: " + disk.getSize());
}

// Displays
List<Display> displays = hal.getDisplays();
for (Display display : displays) {
    System.out.println("EDID: " + new String(display.getEdid()));
}
```

### 2. Access Operating System Information

```java
Collector collector = new Collector();
OperatingSystem os = collector.getOperatingSystem();

// OS info
System.out.println("Family: " + os.getFamily());
System.out.println("Manufacturer: " + os.getManufacturer());
System.out.println("Version: " + os.getVersionInfo());

// Processes
List<OSProcess> processes = os.getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, 10);
for (OSProcess process : processes) {
    System.out.println("PID: " + process.getProcessID());
    System.out.println("Name: " + process.getName());
    System.out.println("CPU Load: " + process.getProcessCpuLoadCumulative());
    System.out.println("Memory: " + process.getResidentSetSize());
}

// File system
FileSystem fileSystem = os.getFileSystem();
Iterable<OSFileStore> fileStores = fileSystem.getFileStores();
for (OSFileStore fs : fileStores) {
    System.out.println("Name: " + fs.getName());
    System.out.println("Mount: " + fs.getMount());
    System.out.println("Type: " + fs.getType());
    System.out.println("Total Space: " + fs.getTotalSpace());
}
```

### 3. Custom Health Check

```java
public class CustomHealthCheck {

    private final Collector collector = new Collector();

    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // CPU health check
        Cpu cpu = collector.getCpu();
        health.put("cpu_healthy", cpu.getTotalUsage() < 80.0);
        health.put("cpu_usage", cpu.getTotalUsage());

        // Memory health check
        Memory memory = collector.getMemory();
        health.put("memory_healthy", memory.getUsage() < 80.0);
        health.put("memory_usage", memory.getUsage());

        // Disk health check
        List<Disk> disks = collector.getDisk();
        boolean diskHealthy = true;
        for (Disk disk : disks) {
            if (disk.getUsagePercent() > 80.0) {
                diskHealthy = false;
                break;
            }
        }
        health.put("disk_healthy", diskHealthy);

        return health;
    }
}
```

### 4. Scheduled Monitoring

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

            System.out.println("CPU Usage: " + cpu.getTotalUsage() + "%");
            System.out.println("Memory Usage: " + memory.getUsage() + "%");

            // Alert if usage is high
            if (cpu.getTotalUsage() > 80.0) {
                System.out.println("WARNING: High CPU usage!");
            }
            if (memory.getUsage() > 80.0) {
                System.out.println("WARNING: High memory usage!");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        scheduler.shutdown();
    }
}
```

### 5. Query Multiple Metrics

```java
Collector collector = new Collector();

// Get multiple specific metrics
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

## 📋 Type Identifier Reference

### Available Metrics (TID)

| Identifier | Description | Return Type |
| :--- | :--- | :--- |
| `TID.HOST` | Host information | `Host` |
| `TID.CPU` | CPU usage and statistics | `Cpu` |
| `TID.MEMORY` | Memory usage | `Memory` |
| `TID.DISK` | Disk storage information | `List<Disk>` |
| `TID.JVM` | JVM runtime information | `Jvm` |
| `TID.ALL_DISK` | All disk usage percentage | `Double` |
| `TID.PROCESS` | Top processes by CPU | `List<Map<String, Object>>` |
| `TID.SYSTEM` | Computer system info | `ComputerSystem` |
| `TID.PROCESSOR` | Processor details | `CentralProcessor` |
| `TID.HARDWARE` | All hardware information | `Map<String, Object>` |
| `TID.POWERSOURCES` | Battery/power source info | `Map<String, Object>` |
| `TID.NETWORKIFS` | Network interface info | `Map<String, Object>` |

-----

## 💡 Best Practices

### 1. Use Singleton Collector

```java
// ✅ Recommended: Reuse Collector instance
private static final Collector COLLECTOR = new Collector();

public void checkHealth() {
    Cpu cpu = COLLECTOR.getCpu();
    // ...
}

// ❌ Not Recommended: Create new instance each time
public void checkHealth() {
    Collector collector = new Collector(); // Wasteful
    // ...
}
```

### 2. Handle Platform Differences

```java
Collector collector = new Collector();
OS osType = (OS) collector.type();

switch (osType) {
    case WINDOWS:
        // Windows-specific logic
        break;
    case LINUX:
        // Linux-specific logic
        break;
    case MACOS:
        // macOS-specific logic
        break;
    default:
        // Default logic
}
```

### 3. Collect Specific Metrics Only

```java
// ✅ Recommended: Query only what you need
Map<String, Object> result = collector.getSingle(TID.CPU);

// ❌ Not Recommended: Query all when only one metric is needed
Map<String, Object> result = collector.getAll(); // Wasteful
```

### 4. Use Scheduled Monitoring

```java
// ✅ Recommended: Use scheduled executor for periodic checks
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    Cpu cpu = collector.getCpu();
    // Log or alert based on cpu.getTotalUsage()
}, 0, 5, TimeUnit.SECONDS);

// ❌ Not Recommended: Busy waiting
while (true) {
    Cpu cpu = collector.getCpu();
    Thread.sleep(5000); // Blocks thread
}
```

### 5. Handle Exceptions Gracefully

```java
public Map<String, Object> safeGetCpu() {
    try {
        Cpu cpu = collector.getCpu();
        return Map.of("cpu", cpu);
    } catch (Exception e) {
        Logger.error("Failed to get CPU info", e);
        return Map.of("cpu", null, "error", e.getMessage());
    }
}
```

-----

## ❓ Frequently Asked Questions

### Q1: How to get real-time CPU usage?

**A**: Use the `getCpu()` method which calculates usage over a 600ms interval:

```java
Cpu cpu = collector.getCpu();
double totalUsage = cpu.getTotalUsage();
```

### Q2: Can I monitor remote systems?

**A**: Bus Health monitors the local system. For remote monitoring, consider:
- Exposing health endpoints via HTTP API
- Using a monitoring agent that pushes metrics to a central server
- Integrating with monitoring systems like Prometheus

### Q3: How to get battery information on servers?

**A**: Use the `getPowerSourceInfo()` method:

```java
Map<String, Object> powerInfo = collector.getPowerSourceInfo();
List<PowerSource> powerSources = collector.getHardware().getPowerSources();

for (PowerSource ps : powerSources) {
    if (!ps.isPowerOnLine()) {
        System.out.println("On battery: " + ps.getRemainingCapacity() + "%");
    }
}
```

### Q4: What permissions are required?

**A**:
- **Linux/Unix**: Most information works without special permissions
- **Windows**: Generally no special permissions needed
- **Some sensors**: May require elevated privileges depending on OS

### Q5: How to reduce monitoring overhead?

**A**:
- Query only necessary metrics
- Increase monitoring interval (e.g., 5-10 seconds instead of 1 second)
- Use singleton Collector instance
- Cache results when appropriate

### Q6: Can I integrate with Spring Boot Actuator?

**A**: Yes, create a custom health indicator:

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

### Q7: How to get network traffic statistics?

**A**: Network statistics are calculated over a 3-second interval in `getHost()`:

```java
Host host = collector.getHost();
System.out.println("RX: " + host.getRxBytesPerSecond() + " KB/s");
System.out.println("TX: " + host.getTxBytesPerSecond() + " KB/s");
```

### Q8: Is it thread-safe?

**A**: Yes, the Collector is thread-safe and can be safely used in multi-threaded environments.

-----

## 🔄 Version Compatibility

| Bus Health Version | JDK Version | OSHI Version |
| :--- | :--- | :--- |
| 8.x | 17+ | 6.x+ |
| 7.x | 11+ | 5.x+ |

-----

## 🚀 Performance Considerations

### 1. Initialization Overhead

- First access to each platform component (OS, Hardware) involves lazy initialization
- Subsequent accesses use cached instances (Memoizer pattern)
- Typical initialization: 50-200ms depending on platform

### 2. Query Performance

| Operation | Typical Time | Notes |
| :--- | :--- | :--- |
| `getCpu()` | 600ms | Includes measurement interval |
| `getMemory()` | <10ms | Cached data |
| `getDisk()` | 20-50ms | File system queries |
| `getJvm()` | <5ms | JVM MXBean access |
| `getHost()` | 3000ms | Includes network measurement |

### 3. Memory Footprint

- Collector instance: ~1KB
- Cached platform instances: ~50-100KB
- Query results: Variable depending on data volume

### 4. Optimization Tips

```java
// Cache results if frequent polling is needed
private Cpu cachedCpu;
private long lastCpuUpdate = 0;

public Cpu getCpuWithCache() {
    long now = System.currentTimeMillis();
    if (now - lastCpuUpdate > 5000) { // 5 second cache
        cachedCpu = collector.getCpu();
        lastCpuUpdate = now;
    }
    return cachedCpu;
}
```

-----

## 📊 Supported Platforms

### Operating Systems

| Platform | Status | Notes |
| :--- | :--- | :--- |
| **Windows** | ✅ Fully Supported | Windows 7/8/10/11, Server 2012+ |
| **Linux** | ✅ Fully Supported | All major distributions |
| **macOS** | ✅ Fully Supported | macOS 10.12+ |
| **FreeBSD** | ✅ Supported | FreeBSD 10+ |
| **OpenBSD** | ✅ Supported | OpenBSD 6+ |
| **Solaris** | ✅ Supported | Solaris 10+ |
| **AIX** | ✅ Supported | AIX 6+ |

### Hardware Information

| Category | Windows | Linux | macOS | Unix |
| :--- | :--- | :--- | :--- | :--- |
| **CPU** | ✅ | ✅ | ✅ | ✅ |
| **Memory** | ✅ | ✅ | ✅ | ✅ |
| **Disk** | ✅ | ✅ | ✅ | ✅ |
| **Network** | ✅ | ✅ | ✅ | ✅ |
| **Battery** | ✅ | ✅ | ✅ | ⚠️ |
| **Sensors** | ✅ | ⚠️ | ✅ | ⚠️ |
| **Display** | ✅ | ⚠️ | ✅ | ❌ |
| **USB** | ✅ | ✅ | ✅ | ⚠️ |

Legend: ✅ Full Support | ⚠️ Partial Support | ❌ Not Supported

-----

## 🔧 Configuration Examples

### Maven Configuration

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-health</artifactId>
    <version>8.x.x</version>
</dependency>

<!-- Optional: For better native library support -->
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.18.0</version>
</dependency>
```

### Gradle Configuration

```groovy
implementation 'org.miaixz:bus-health:8.x.x'

// Optional: For better native library support
implementation 'net.java.dev.jna:jna-platform:5.18.0'
```

-----

## 📚 Additional Resources

- **Documentation**: [https://www.miaixz.org](https://www.miaixz.org)
- **GitHub Repository**: [https://github.com/818000/bus](https://github.com/818000/bus)
- **Issue Tracker**: [https://github.com/818000/bus/issues](https://github.com/818000/bus/issues)
- **OSHI Project**: [https://github.com/oshi/oshi](https://github.com/oshi/oshi)

-----

## 🙏 Acknowledgments

Part of this project is based on [OSHI](https://github.com/oshi/oshi) (Operating System and Hardware Information), licensed under the MIT License. We thank the OSHI team for their excellent work.

-----

**Built with ❤️ by the Miaixz Team**
