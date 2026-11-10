/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.hardware;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.*;
import com.sun.jna.platform.mac.DiskArbitration;
import com.sun.jna.platform.mac.DiskArbitration.DADiskRef;
import com.sun.jna.platform.mac.DiskArbitration.DASessionRef;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.IOReturnException;
import com.sun.jna.platform.mac.SystemB.Statfs;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.mac.CFKit;
import org.miaixz.bus.health.mac.driver.disk.Fsstat;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

/**
 * Mac hard disk implementation.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class MacHWDiskStore extends AbstractHWDiskStore {

    /**
     * The CF constant.
     */
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;

    /**
     * The DA constant.
     */
    private static final DiskArbitration DA = DiskArbitration.INSTANCE;

    /** IORegistry property naming a media object's BSD device. */
    private static final String BSD_NAME = "BSD Name";

    /** IORegistry property marking a whole disk. */
    private static final String WHOLE = "Whole";

    /** IOKit class of a block storage medium. */
    private static final String IO_MEDIA = "IOMedia";

    /** IORegistry property naming a disk image URL. */
    private static final String DISK_IMAGE_URL = "DiskImageURL";

    /** IOKit class of a modern disk image device. */
    private static final String DISK_IMAGE_DEVICE = "AppleDiskImageDevice";

    /** IOKit class of a legacy disk image device. */
    private static final String LEGACY_DISK_IMAGE_DEVICE = "IOHDIXHDDrive";

    /** IOKit class at the top of the IOService device tree. */
    private static final String PLATFORM_EXPERT = "IOPlatformExpertDevice";

    /**
     * The currentQueueLength value.
     */
    private final long currentQueueLength = 0L;

    /**
     * The reads value.
     */
    private long reads = 0L;

    /**
     * The readBytes value.
     */
    private long readBytes = 0L;

    /**
     * The writes value.
     */
    private long writes = 0L;

    /**
     * The writeBytes value.
     */
    private long writeBytes = 0L;

    /**
     * The transferTime value.
     */
    private long transferTime = 0L;

    /**
     * The timeStamp value.
     */
    private long timeStamp = 0L;

    /**
     * The partitionList value.
     */
    private List<HWPartition> partitionList = Collections.emptyList();

    /**
     * The stores this disk is built on.
     */
    private final Optional<List<String>> backingStores;

    /**
     * Constructs a {@code MacHWDiskStore} object.
     *
     * @param name          The name of the disk.
     * @param model         The model of the disk.
     * @param serial        The serial number of the disk.
     * @param size          The size of the disk in bytes.
     * @param diskType      The disk type.
     * @param session       The DiskArbitration session reference.
     * @param mountPointMap A map of partition BSD names to their mount points.
     * @param cfKeyMap      A map of {@link CFKey} enum values to their corresponding {@link CFStringRef}.
     */
    public MacHWDiskStore(String name, String model, String serial, long size, String diskType, DASessionRef session,
            Map<String, String> mountPointMap, Map<CFKey, CFStringRef> cfKeyMap) {
        this(name, model, serial, size, diskType, Optional.empty(), session, mountPointMap, cfKeyMap);
    }

    /**
     * Constructs a disk store with its backing stores.
     *
     * @param name          The disk name.
     * @param model         The disk model.
     * @param serial        The serial number.
     * @param size          The size in bytes.
     * @param diskType      The media type.
     * @param backingStores The backing stores.
     * @param session       The DiskArbitration session.
     * @param mountPointMap The partition mount points.
     * @param cfKeyMap      The Core Foundation keys.
     */
    private MacHWDiskStore(String name, String model, String serial, long size, String diskType,
            Optional<List<String>> backingStores, DASessionRef session, Map<String, String> mountPointMap,
            Map<CFKey, CFStringRef> cfKeyMap) {
        super(name, model, serial, size, backingStores.map(List::isEmpty).orElse(true) ? diskType : "Virtual");
        this.backingStores = backingStores;
        updateDiskStats(session, mountPointMap, cfKeyMap);
    }

    /**
     * Gets the disks on this machine.
     *
     * @return A list of {@link HWDiskStore} objects representing the disks.
     */
    public static List<HWDiskStore> getDisks() {
        Map<String, String> mountPointMap = Fsstat.queryPartitionToMountMap();
        Map<CFKey, CFStringRef> cfKeyMap = mapCFKeys();

        List<HWDiskStore> diskList = new ArrayList<>();

        try {
            // Open a DiskArbitration session
            DASessionRef session = DA.DASessionCreate(CF.CFAllocatorGetDefault());
            if (session == null) {
                Logger.error(false, "Health", "Unable to open session to DiskArbitration framework.");
                return Collections.emptyList();
            }
            try {
                // Get IOMedia objects representing whole drives
                List<String> bsdNames = new ArrayList<>();
                IOIterator iter = IOKitUtil.getMatchingServices(IO_MEDIA);
                if (iter != null) {
                    IORegistryEntry media = iter.next();
                    while (media != null) {
                        Boolean whole = media.getBooleanProperty(WHOLE);
                        if (whole != null && whole) {
                            DADiskRef disk = DA.DADiskCreateFromIOMedia(CF.CFAllocatorGetDefault(), session, media);
                            if (disk != null) {
                                String bsdName = DA.DADiskGetBSDName(disk);
                                if (bsdName != null) {
                                    bsdNames.add(bsdName);
                                }
                                disk.release();
                            }
                        }
                        media.release();
                        media = iter.next();
                    }
                    iter.release();
                }

                // Now iterate the bsdNames
                for (String bsdName : bsdNames) {
                    String model = Normal.EMPTY;
                    String serial = Normal.EMPTY;
                    long size = 0L;

                    // Get a reference to the disk - only matching /dev/disk*
                    String path = "/dev/" + bsdName;

                    // Get the DiskArbitration dictionary for this disk, which has model
                    // and size (capacity)
                    DADiskRef disk = DA.DADiskCreateFromBSDName(CF.CFAllocatorGetDefault(), session, path);
                    if (disk != null) {
                        CFDictionaryRef diskInfo = DA.DADiskCopyDescription(disk);
                        if (diskInfo != null) {
                            // Parse out model and size from their respective keys
                            Pointer result = diskInfo.getValue(cfKeyMap.get(CFKey.DA_DEVICE_MODEL));
                            model = CFKit.cfPointerToString(result);
                            result = diskInfo.getValue(cfKeyMap.get(CFKey.DA_MEDIA_SIZE));
                            CFNumberRef sizePtr = new CFNumberRef(result);
                            size = sizePtr.longValue();
                            diskInfo.release();

                            // Prefer this disk's own block-storage device serial before matching by model.
                            serial = queryDeviceSerial(bsdName);
                            if (serial.isEmpty() && !"Disk Image".equals(model)) {
                                CFStringRef modelNameRef = CFStringRef.createCFString(model);
                                CFMutableDictionaryRef propertyDict = CF.CFDictionaryCreateMutable(
                                        CF.CFAllocatorGetDefault(),
                                        new CFIndex(0),
                                        null,
                                        null);
                                propertyDict.setValue(cfKeyMap.get(CFKey.MODEL), modelNameRef);
                                CFMutableDictionaryRef matchingDict = CF.CFDictionaryCreateMutable(
                                        CF.CFAllocatorGetDefault(),
                                        new CFIndex(0),
                                        null,
                                        null);
                                matchingDict.setValue(cfKeyMap.get(CFKey.IO_PROPERTY_MATCH), propertyDict);

                                // search for all IOservices that match the model
                                IOIterator serviceIterator = IOKitUtil.getMatchingServices(matchingDict);
                                // getMatchingServices releases matchingDict
                                modelNameRef.release();
                                propertyDict.release();

                                if (serviceIterator != null) {
                                    IORegistryEntry sdService = serviceIterator.next();
                                    while (sdService != null) {
                                        // look up the serial number
                                        serial = sdService.getStringProperty("Serial Number");
                                        sdService.release();
                                        if (serial != null) {
                                            break;
                                        }
                                        sdService = serviceIterator.next();
                                    }
                                    serviceIterator.release();
                                }
                                if (serial == null) {
                                    serial = Normal.EMPTY;
                                }
                            }
                        }
                        disk.release();

                        // If empty, ignore
                        if (size <= 0) {
                            continue;
                        }
                        HWDiskStore diskStore = new MacHWDiskStore(bsdName, model.trim(), serial.trim(), size,
                                detectDiskType(bsdName), queryBackingStores(bsdName), session, mountPointMap, cfKeyMap);
                        diskList.add(diskStore);
                    }
                }
                return diskList;
            } finally {
                session.release();
            }
        } finally {
            for (CFTypeRef value : cfKeyMap.values()) {
                value.release();
            }
        }
    }

    /**
     * Temporarily caches pointers to keys. The values from this map must be released after use.
     *
     * @return A map of keys in the {@link CFKey} enum to corresponding {@link CFStringRef}.
     */
    private static Map<CFKey, CFStringRef> mapCFKeys() {
        Map<CFKey, CFStringRef> keyMap = new EnumMap<>(CFKey.class);
        for (CFKey cfKey : CFKey.values()) {
            keyMap.put(cfKey, CFStringRef.createCFString(cfKey.getKey()));
        }
        return keyMap;
    }

    /**
     * Finds what a disk is built on by walking its IOService ancestors.
     *
     * @param bsdName The disk's BSD name.
     * @return The backing stores, an empty list for a non-layered disk, or an empty optional when unavailable.
     */
    private static Optional<List<String>> queryBackingStores(String bsdName) {
        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
        if (matchingDict == null) {
            return Optional.empty();
        }
        IOIterator iter = IOKitUtil.getMatchingServices(matchingDict);
        if (iter == null) {
            return Optional.empty();
        }
        try {
            IORegistryEntry media = iter.next();
            if (media == null) {
                return Optional.empty();
            }
            IORegistryEntry entry;
            try {
                entry = media.getParentEntry("IOService");
            } finally {
                media.release();
            }
            while (entry != null) {
                IORegistryEntry current = entry;
                try {
                    if (current.conformsTo(IO_MEDIA)) {
                        return mediaBackingStore(
                                current.getStringProperty(BSD_NAME),
                                current.getBooleanProperty(WHOLE));
                    }
                    if (current.conformsTo(DISK_IMAGE_DEVICE)) {
                        String imageUrl = current.getStringProperty(DISK_IMAGE_URL);
                        return imageUrl == null ? Optional.empty()
                                : imageBackingStore(imageUrl, MacHWDiskStore::queryMountPoint);
                    }
                    if (current.conformsTo(LEGACY_DISK_IMAGE_DEVICE)) {
                        return Optional.empty();
                    }
                    if (current.conformsTo(PLATFORM_EXPERT)) {
                        return Optional.of(Collections.emptyList());
                    }
                    entry = current.getParentEntry("IOService");
                } finally {
                    current.release();
                }
            }
            return Optional.empty();
        } catch (IOReturnException e) {
            Logger.debug(
                    false,
                    "Health",
                    "Unable to walk the IORegistry for {}: {}",
                    bsdName,
                    e.getClass().getSimpleName());
            return Optional.empty();
        } finally {
            iter.release();
        }
    }

    /**
     * Builds the backing store of a disk layered on another media object.
     *
     * @param bsdName The ancestor's BSD name.
     * @param whole   Whether the ancestor is a whole disk.
     * @return The backing store, or an empty optional if no BSD name is available.
     */
    private static Optional<List<String>> mediaBackingStore(String bsdName, Boolean whole) {
        if (bsdName == null) {
            return Optional.empty();
        }
        StoreKind kind = Boolean.TRUE.equals(whole) ? StoreKind.DISK : StoreKind.PARTITION;
        return Optional.of(Collections.singletonList(kind.entry(bsdName)));
    }

    /**
     * Builds the file-store backing entry of a disk image.
     *
     * @param imageUrl The disk image URL.
     * @param mountOf  A function resolving a path to its mount point.
     * @return The backing store, or an empty optional when unavailable.
     */
    private static Optional<List<String>> imageBackingStore(String imageUrl, UnaryOperator<String> mountOf) {
        String path = imagePath(imageUrl);
        if (path.isEmpty()) {
            return Optional.empty();
        }
        String mount = mountOf.apply(path);
        return mount.isEmpty() ? Optional.empty() : Optional.of(Collections.singletonList(StoreKind.FILE.entry(mount)));
    }

    /**
     * Converts a local disk image URL to its decoded path.
     *
     * @param imageUrl The disk image URL.
     * @return The local path, or an empty string for a non-file or invalid URL.
     */
    private static String imagePath(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            return "file".equals(uri.getScheme()) && uri.getPath() != null ? uri.getPath() : Normal.EMPTY;
        } catch (URISyntaxException e) {
            return Normal.EMPTY;
        }
    }

    /**
     * Resolves the mount point containing a path.
     *
     * @param path The local path.
     * @return The mount point, or an empty string when unavailable.
     */
    private static String queryMountPoint(String path) {
        Statfs fs = new Statfs();
        return SystemB.INSTANCE.statfs64(path, fs) == Normal._0 ? Native.toString(fs.f_mntonname, Charset.UTF_8)
                : Normal.EMPTY;
    }

    /**
     * Detects the disk type from IOKit metadata.
     *
     * @param bsdName the BSD disk name
     * @return the detected disk type
     */
    private static String detectDiskType(String bsdName) {
        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
        if (matchingDict == null) {
            return "Unknown";
        }
        IOIterator iter = IOKitUtil.getMatchingServices(matchingDict);
        if (iter == null) {
            return "Unknown";
        }
        try {
            IORegistryEntry media = iter.next();
            if (media != null) {
                try {
                    Boolean removable = media.getBooleanProperty("Removable");
                    if (removable != null && removable) {
                        return "Removable";
                    }
                    IORegistryEntry driver = media.getParentEntry("IOService");
                    if (driver != null) {
                        try {
                            IORegistryEntry device = driver.getParentEntry("IOService");
                            if (device != null) {
                                try {
                                    String type = readDeviceCharacteristic(device, "Medium Type");
                                    if (type != null) {
                                        if (type.contains("Solid State") || type.contains("SSD")) {
                                            return "SSD";
                                        } else if (type.contains("Rotational")) {
                                            return "HDD";
                                        }
                                    }
                                } finally {
                                    device.release();
                                }
                            }
                        } finally {
                            driver.release();
                        }
                    }
                } finally {
                    media.release();
                }
            }
        } finally {
            iter.release();
        }
        return "Unknown";
    }

    /**
     * Reads the serial number from the block-storage device backing a whole disk.
     *
     * @param bsdName the BSD disk name
     * @return the serial number, or an empty string when unavailable
     */
    private static String queryDeviceSerial(String bsdName) {
        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
        if (matchingDict == null) {
            return Normal.EMPTY;
        }
        IOIterator iterator = IOKitUtil.getMatchingServices(matchingDict);
        if (iterator == null) {
            return Normal.EMPTY;
        }
        try {
            IORegistryEntry media = iterator.next();
            if (media == null) {
                return Normal.EMPTY;
            }
            try {
                IORegistryEntry driver = media.getParentEntry("IOService");
                if (driver == null) {
                    return Normal.EMPTY;
                }
                try {
                    IORegistryEntry device = driver.getParentEntry("IOService");
                    if (device == null) {
                        return Normal.EMPTY;
                    }
                    try {
                        return Parsing.getStringValueOrEmpty(readDeviceCharacteristic(device, "Serial Number"));
                    } finally {
                        device.release();
                    }
                } finally {
                    driver.release();
                }
            } finally {
                media.release();
            }
        } finally {
            iterator.release();
        }
    }

    /**
     * Reads an IOKit device characteristic from an IOBlockStorageDevice entry.
     *
     * @param device the IOKit device entry
     * @param key    the device-characteristic key
     * @return the characteristic string, or {@code null}
     */
    private static String readDeviceCharacteristic(IORegistryEntry device, String key) {
        CFMutableDictionaryRef props = device.createCFProperties();
        if (props == null) {
            return null;
        }
        CFStringRef deviceCharacteristics = CFStringRef.createCFString("Device Characteristics");
        CFStringRef characteristicKey = CFStringRef.createCFString(key);
        try {
            Pointer charDict = props.getValue(deviceCharacteristics);
            if (charDict != null) {
                CFDictionaryRef characteristics = new CFDictionaryRef(charDict);
                Pointer characteristic = characteristics.getValue(characteristicKey);
                if (characteristic != null) {
                    return CFKit.cfPointerToString(characteristic);
                }
            }
            return null;
        } finally {
            characteristicKey.release();
            deviceCharacteristics.release();
            props.release();
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getReads() {
        return reads;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getWrites() {
        return writes;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getWriteBytes() {
        return writeBytes;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getTransferTime() {
        return transferTime;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    /**
     * Returns the backing stores.
     *
     * @return The backing stores, or an empty optional when unavailable.
     */
    @Override
    public Optional<List<String>> getBackingStores() {
        return backingStores;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public synchronized boolean updateAttributes() {
        // Open a session and create CFStrings
        DASessionRef session = DA.DASessionCreate(CF.CFAllocatorGetDefault());
        if (session == null) {
            Logger.error(false, "Health", "Unable to open session to DiskArbitration framework.");
            return false;
        }
        try {
            Map<CFKey, CFStringRef> cfKeyMap = mapCFKeys();
            try {
                // Execute the update
                return updateDiskStats(session, Fsstat.queryPartitionToMountMap(), cfKeyMap);
            } finally {
                for (CFTypeRef value : cfKeyMap.values()) {
                    value.release();
                }
            }
        } finally {
            session.release();
        }
    }

    /**
     * Updates disk statistics for this disk store.
     *
     * @param session       The DiskArbitration session reference.
     * @param mountPointMap A map of partition BSD names to their mount points.
     * @param cfKeyMap      A map of {@link CFKey} enum values to their corresponding {@link CFStringRef}.
     * @return {@code true} if the disk statistics were successfully updated, {@code false} otherwise.
     */
    private boolean updateDiskStats(
            DASessionRef session,
            Map<String, String> mountPointMap,
            Map<CFKey, CFStringRef> cfKeyMap) {
        // Now look up the device using the BSD Name to get its
        // statistics
        String bsdName = getName();
        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
        if (matchingDict != null) {
            // search for all IOservices that match the bsd name
            IOIterator driveListIter = IOKitUtil.getMatchingServices(matchingDict);
            if (driveListIter != null) {
                // getMatchingServices releases matchingDict
                boolean updated = false;
                IORegistryEntry drive = driveListIter.next();
                // Should only match one drive
                if (drive != null) {
                    // Should be an IOMedia object with a parent
                    // IOBlockStorageDriver or AppleAPFSContainerScheme object
                    // Get the properties from the parent
                    if (drive.conformsTo(IO_MEDIA)) {
                        IORegistryEntry parent = drive.getParentEntry("IOService");
                        if (parent != null && (parent.conformsTo("IOBlockStorageDriver")
                                || parent.conformsTo("AppleAPFSContainerScheme"))) {
                            CFMutableDictionaryRef properties = parent.createCFProperties();
                            // We now have a properties object with the
                            // statistics we need on it. Fetch them
                            Pointer result = properties.getValue(cfKeyMap.get(CFKey.STATISTICS));
                            CFDictionaryRef statistics = new CFDictionaryRef(result);
                            this.timeStamp = System.currentTimeMillis();

                            // Now get the stats we want
                            result = statistics.getValue(cfKeyMap.get(CFKey.READ_OPS));
                            CFNumberRef stat = new CFNumberRef(result);
                            this.reads = stat.longValue();
                            result = statistics.getValue(cfKeyMap.get(CFKey.READ_BYTES));
                            stat.setPointer(result);
                            this.readBytes = stat.longValue();

                            result = statistics.getValue(cfKeyMap.get(CFKey.WRITE_OPS));
                            stat.setPointer(result);
                            this.writes = stat.longValue();
                            result = statistics.getValue(cfKeyMap.get(CFKey.WRITE_BYTES));
                            stat.setPointer(result);
                            this.writeBytes = stat.longValue();

                            // Total time is in nanoseconds. Add read+write
                            // and convert total to ms
                            final Pointer readTimeResult = statistics.getValue(cfKeyMap.get(CFKey.READ_TIME));
                            final Pointer writeTimeResult = statistics.getValue(cfKeyMap.get(CFKey.WRITE_TIME));
                            // AppleAPFSContainerScheme does not have timer statistics
                            if (readTimeResult != null && writeTimeResult != null) {
                                stat.setPointer(readTimeResult);
                                long xferTime = stat.longValue();
                                stat.setPointer(writeTimeResult);
                                xferTime += stat.longValue();
                                this.transferTime = xferTime / 1_000_000L;
                            }

                            properties.release();
                            updated = true;
                        } else {
                            // This is normal for FileVault drives, Fusion
                            // drives, and other virtual bsd names
                            Logger.debug(
                                    false,
                                    "Health",
                                    "Unable to find block storage driver properties for {}",
                                    bsdName);
                        }
                        // Now get partitions for this disk.
                        List<HWPartition> partitions = new ArrayList<>();

                        CFMutableDictionaryRef properties = drive.createCFProperties();
                        // Partitions will match BSD Unit property
                        Pointer result = properties.getValue(cfKeyMap.get(CFKey.BSD_UNIT));
                        CFNumberRef bsdUnit = new CFNumberRef(result);
                        // We need a CFBoolean that's false.
                        // Whole disk has 'true' for Whole and 'false'
                        // for leaf; store the boolean false
                        result = properties.getValue(cfKeyMap.get(CFKey.LEAF));
                        CFBooleanRef cfFalse = new CFBooleanRef(result);
                        // create a matching dict for BSD Unit
                        CFMutableDictionaryRef propertyDict = CF
                                .CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CFIndex(0), null, null);
                        propertyDict.setValue(cfKeyMap.get(CFKey.BSD_UNIT), bsdUnit);
                        propertyDict.setValue(cfKeyMap.get(CFKey.WHOLE), cfFalse);
                        matchingDict = CF
                                .CFDictionaryCreateMutable(CF.CFAllocatorGetDefault(), new CFIndex(0), null, null);
                        matchingDict.setValue(cfKeyMap.get(CFKey.IO_PROPERTY_MATCH), propertyDict);

                        // search for IOservices that match the BSD Unit
                        // with whole=false; these are partitions
                        IOIterator serviceIterator = IOKitUtil.getMatchingServices(matchingDict);
                        // getMatchingServices releases matchingDict
                        properties.release();
                        propertyDict.release();

                        if (serviceIterator != null) {
                            // Iterate disks
                            IORegistryEntry sdService = IOKit.INSTANCE.IOIteratorNext(serviceIterator);
                            while (sdService != null) {
                                // look up the BSD Name
                                String partBsdName = sdService.getStringProperty("BSD Name");
                                String name = partBsdName;
                                String type = Normal.EMPTY;
                                String label = Normal.EMPTY;
                                // Get the DiskArbitration dictionary for
                                // this partition
                                DADiskRef disk = DA
                                        .DADiskCreateFromBSDName(CF.CFAllocatorGetDefault(), session, partBsdName);
                                if (disk != null) {
                                    CFDictionaryRef diskInfo = DA.DADiskCopyDescription(disk);
                                    if (diskInfo != null) {
                                        // get volume name from its key
                                        result = diskInfo.getValue(cfKeyMap.get(CFKey.DA_MEDIA_NAME));
                                        type = CFKit.cfPointerToString(result);
                                        result = diskInfo.getValue(cfKeyMap.get(CFKey.DA_VOLUME_NAME));
                                        if (result == null) {
                                            name = type;
                                        } else {
                                            String volumeName = CFKit.cfPointerToString(result);
                                            name = volumeName;
                                            label = volumeName;
                                        }
                                        diskInfo.release();
                                    }
                                    disk.release();
                                }
                                String mountPoint = mountPointMap.getOrDefault(partBsdName, Normal.EMPTY);
                                Long size = sdService.getLongProperty("Size");
                                Integer bsdMajor = sdService.getIntegerProperty("BSD Major");
                                Integer bsdMinor = sdService.getIntegerProperty("BSD Minor");
                                String uuid = sdService.getStringProperty("UUID");
                                partitions.add(
                                        new HWPartition(partBsdName, name, type, uuid == null ? Normal.UNKNOWN : uuid,
                                                label, size == null ? 0L : size, bsdMajor == null ? 0 : bsdMajor,
                                                bsdMinor == null ? 0 : bsdMinor, mountPoint));
                                // iterate
                                // iterate
                                sdService.release();
                                sdService = IOKit.INSTANCE.IOIteratorNext(serviceIterator);
                            }
                            serviceIterator.release();
                        }
                        this.partitionList = Collections.unmodifiableList(
                                partitions.stream().sorted(Comparator.comparing(HWPartition::getName))
                                        .collect(Collectors.toList()));
                        if (parent != null) {
                            parent.release();
                        }
                    }
                    drive.release();
                }
                driveListIter.release();
                return updated;
            }
        }
        return false;
    }

    /**
     * Enum to hold CFStringRef keys for DiskArbitration and IOKit lookups.
     *
     * @author Kimi Liu
     */
    private enum CFKey {

        /**
         * Key for IOPropertyMatch.
         */
        IO_PROPERTY_MATCH("IOPropertyMatch"),

        /**
         * Key for Statistics.
         */
        STATISTICS("Statistics"),
        /**
         * Key for Read Operations.
         */
        READ_OPS("Operations (Read)"),
        /**
         * Key for Read Bytes.
         */
        READ_BYTES("Bytes (Read)"),
        /**
         * Key for Total Time (Read).
         */
        READ_TIME("Total Time (Read)"),
        /**
         * Key for Write Operations.
         */
        WRITE_OPS("Operations (Write)"),
        /**
         * Key for Write Bytes.
         */
        WRITE_BYTES("Bytes (Write)"),
        /**
         * Key for Total Time (Write).
         */
        WRITE_TIME("Total Time (Write)"),

        /**
         * Key for BSD Unit.
         */
        BSD_UNIT("BSD Unit"),
        /**
         * Key for Leaf.
         */
        LEAF("Leaf"),
        /**
         * Key for Whole.
         */
        WHOLE(MacHWDiskStore.WHOLE),

        /**
         * Key for DAMediaName.
         */
        DA_MEDIA_NAME("DAMediaName"),
        /**
         * Key for DAVolumeName.
         */
        DA_VOLUME_NAME("DAVolumeName"),
        /**
         * Key for DAMediaSize.
         */
        DA_MEDIA_SIZE("DAMediaSize"),
        /**
         * Key for DADeviceModel.
         */
        DA_DEVICE_MODEL("DADeviceModel"),
        /**
         * Key for Model.
         */
        MODEL("Model");

        /**
         * The key value.
         */
        private final String key;

        /**
         * Constructs a {@code CFKey} enum value.
         *
         * @param key The string representation of the key.
         */
        CFKey(String key) {
            this.key = key;
        }

        /**
         * Gets the string representation of the key.
         *
         * @return The key string.
         */
        public String getKey() {
            return this.key;
        }

    }

}
