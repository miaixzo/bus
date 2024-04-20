#### 项目说明
--------------------------- 
Windows • Linux • Mac OS • Unix (Solaris, FreeBSD)

Supported features
--------------------------

* Computer System and firmware, baseboard
* Operating System and Version/Build
* Physical (core) and Logical (hyperthreaded) CPUs
* System and per-processor load % and tick counters
* CPU uptime, processes, and threads
* Process uptime, CPU, memory usage
* Physical and virtual memory used/available
* Mounted filesystems (type, usable and total space)
* Disk drives (model, serial, size) and partitions
* Network interfaces (IPs, bandwidth in/out)
* Battery state (% capacity, time remaining)
* Connected displays (with EDID info)
* USB Devices
* Sensors (temperature, fan speeds, voltage)

Output
-------------
General information about the operating system and computer system.

```
Apple macOS 10.14.6 (Mojave) build 18G84
Booted: 2019-07-28T20:27:49Z
Uptime: 10 days, 08:24:03
Running without elevated permissions.

manufacturer: Apple Inc.
model: MacBook Pro (MacBookPro15,1)
serialnumber: C03Z53B7LVDR
```

Processor identification.

```
Intel(R) Core(TM) i9-9880H CPU @ 2.30GHz
 1 physical CPU package(s)
 8 physical CPU core(s)
 16 logical CPU(s)
Identifier: Intel64 Family 6 Model 158 Stepping 13
ProcessorID: BFEBFBFF000906ED
```

By measuring ticks (user, nice, system, idle, iowait, and irq) between time intervals, percent usage can be calculated.
Per-processor information is also provided.

```
CPU, IOWait, and IRQ ticks @ 0 sec:[967282, 15484, 195343, 124216619], 6176, [4054, 2702]
CPU, IOWait, and IRQ ticks @ 1 sec:[967308, 15484, 195346, 124216790], 6177, [4057, 2705]
User: 13.0% Nice: 0.0% System: 1.5% Idle: 85.5%
CPU load: 8.8%
CPU load averages: 2.69 2.47 2.38
CPU load per processor: 23.6% 1.3% 18.2% 0.7% 12.9% 0.7% 12.1% 1.3%
Vendor Frequency: 2.3 GHz
Max Frequency: 2.3 GHz
Current Frequencies: 2.3 GHz, 2.3 GHz, 2.3 GHz, 2.3 GHz, 2.3 GHz, 2.3 GHz, 2.3 GHz, 2.3 GHz
```

Process information including CPU and memory per process is available.

```
Processes: 401, Threads: 1159
   PID  %CPU %MEM       VSZ       RSS Name
 55977  27.9  0.2   6.8 GiB  34.3 MiB java
 51820  18.7  5.6   6.3 GiB 919.2 MiB eclipse
 39272  11.2 17.8   7.1 GiB   2.8 GiB prl_vm_app
 85316   6.5  2.9   5.6 GiB 471.4 MiB thunderbird
 35301   5.4  0.5   1.7 GiB  89.8 MiB Microsoft Excel
```

Memory and swapfile information is available.

```
Memory: 11.6 GiB/32 GiB
Swap used: 3.6 GiB/5 GiB
```

Statistics for the system battery are provided.

```
Power Sources: 
 Name: InternalBattery-0, Device Name: bq20z451,
 RemainingCapacityPercent: 100.0%, Time Remaining: 5:42, Time Remaining Instant: 5:42,
 Power Usage Rate: -16045.216mW, Voltage: 12.694V, Amperage: -1264.0mA,
 Power OnLine: false, Charging: false, Discharging: true,
 Capacity Units: MAH, Current Capacity: 7213, Max Capacity: 7315, Design Capacity: 7336,
 Cycle Count: 6, Chemistry: LIon, Manufacture Date: 2019-06-11, Manufacturer: SMP,
 SerialNumber: D869243A2U3J65JAB, Temperature: 30.46°C
```

The EDID for each Display is provided. This can be parsed with various utilities for detailed information. health
provides a summary of selected data.

```
Displays:
 Display 0:
  Manuf. ID=SAM, Product ID=2ad, Analog, Serial=HA19, ManufDate=3/2008, EDID v1.3
  41 x 27 cm (16.1 x 10.6 in)
  Preferred Timing: Clock 106MHz, Active Pixels 3840x2880 
  Range Limits: Field Rate 56-75 Hz vertical, 30-81 Hz horizontal, Max clock: 140 MHz
  Monitor Name: SyncMaster
  Serial Number: H9FQ345476
 Display 1:
  Manuf. ID=SAM, Product ID=226, Analog, Serial=HA19, ManufDate=4/2007, EDID v1.3
  41 x 26 cm (16.1 x 10.2 in)
  Preferred Timing: Clock 106MHz, Active Pixels 3840x2880 
  Range Limits: Field Rate 56-75 Hz vertical, 30-81 Hz horizontal, Max clock: 140 MHz
  Monitor Name: SyncMaster
  Serial Number: HMCP431880
```

Disks and usage (reads, writes, transfer times) are shown, and partitions can be mapped to filesystems.

```
Disks:
 disk0: (model: SanDisk Ultra II 960GB - S/N: 161008800550) size: 960.2 GB, reads: 1053132 (23.0 GiB), writes: 243792 (11.1 GiB), xfer: 73424854 ms
 |-- disk0s1: EFI (EFI System Partition) Maj:Min=1:1, size: 209.7 MB
 |-- disk0s2: Macintosh HD (Macintosh SSD) Maj:Min=1:2, size: 959.3 GB @ /
 disk1: (model: Disk Image - S/N: ) size: 960.0 GB, reads: 3678 (60.0 MiB), writes: 281 (8.6 MiB), xfer: 213627 ms
 |-- disk1s1: EFI (EFI System Partition) Maj:Min=1:4, size: 209.7 MB
 |-- disk1s2: Dropbox (disk image) Maj:Min=1:5, size: 959.7 GB @ /Volumes/Dropbox

```

```
Sensors:
 CPU Temperature: 69.8°C
 Fan Speeds:[4685, 4687]
 CPU Voltage: 3.9V
```

Attached USB devices can be listed:

```
USB Devices:
 AppleUSBEHCI
 |-- Root Hub Simulation Simulation (Apple Inc.)
     |-- IOUSBHostDevice
         |-- IR Receiver (Apple Computer, Inc.)
         |-- USB Receiver (Logitech)
 AppleUSBEHCI
 |-- Root Hub Simulation Simulation (Apple Inc.)
     |-- FaceTime HD Camera (Built-in) (Apple Inc.) [s/n: DJHB1V077FDH5HL0]
     |-- IOUSBHostDevice
         |-- Apple Internal Keyboard / Trackpad (Apple Inc.)
         |-- BRCM2070 Hub (Apple Inc.)
             |-- Bluetooth USB Host Controller (Apple Inc.)
 AppleUSBEHCI
 |-- Root Hub Simulation Simulation (Apple Inc.)
     |-- IOUSBHostDevice
         |-- Apple Thunderbolt Display (Apple Inc.) [s/n: 162C0C25]
         |-- Display Audio (Apple Inc.) [s/n: 162C0C25]
         |-- FaceTime HD Camera (Display) (Apple Inc.) [s/n: CCGCAN000TDJ7DFX]
         |-- USB2.0 Hub
             |-- ANT USBStick2 (Dynastream Innovations) [s/n: 051]
             |-- Fitbit Base Station (Fitbit Inc.)
```

### Snapshots

* Snapshot releases may be deployed using `mvn clean deploy`
    * The version number in the pom.xml must end in -SNAPSHOT

### Prepare

* Make sure tests are green on [Travis CI](https://travis-ci.org/839128/bus).
* Run `mvn clean test` on every OS you have access to
* Choose an appropriate [version number](http://semver.org/) for the release
    * Proactively change version numbers in the download links on [README.md](README.md).
        * HTML-escape `&`, `<`, and `>` in any links in the site version
    * Move "Your contribution here." to a new empty "Next" section
    * Commit changes as a "prep for x.x release"

### Release

*

See [this page](http://central.sonatype.org/pages/apache-maven.html#performing-a-release-deployment-with-the-maven-release-plugin)
for a summary of the below steps

* `mvn clean deploy`
    * Do a final snapshot release and fix any errors in the javadocs
    * If license headers are rewritten as part of this deployment, commit the changes
* `mvn release:clean`
    * Takes a few seconds
* `mvn release:prepare`
    * Takes a few minutes
    * This will ask for the version being released, removing -SNAPSHOT
    * This will suggest the next version, increment appropriately
* `mvn release:perform`
    * Takes a few minutes.
    * This pushes the release to the [Nexus](https://oss.sonatype.org/) staging repository
* Log on to [Nexus](https://oss.sonatype.org/)
  and [release the deployment from OSSRH to the Central Repository](http://central.sonatype.org/pages/releasing-the-deployment.html)
  .

* Add a title and release notes [to the tag](https://github.com/839128/bus/tags) on GitHub and publish the release to make
  it current.

* As development progresses, update version in [pom.xml](pom.xml) using -SNAPSHOT appended to the new version
  using [Semantic Versioning](http://semver.org/) standards:
    * Increment major version (x.0) for API-breaking changes or additions
    * Increment minor version (x.1) for substantive additions, bugfixes and changes that are backwards compatible
    * Increment patch version (x.x.1) for minor bugfixes or changes that are backwards compatible

Thank you for OSHI
-------------------
Part of this article is from OSHI
