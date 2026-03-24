/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractOSFileStore;

/**
 * OSFileStore implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class FreeBsdOSFileStore extends AbstractOSFileStore {

    private String logicalVolume;
    private String description;
    private String fsType;

    private long freeSpace;
    private long usableSpace;
    private long totalSpace;
    private long freeInodes;
    private long totalInodes;

    public FreeBsdOSFileStore(String name, String volume, String label, String mount, String options, String uuid,
            boolean local, String logicalVolume, String description, String fsType, long freeSpace, long usableSpace,
            long totalSpace, long freeInodes, long totalInodes) {
        super(name, volume, label, mount, options, uuid, local);
        this.logicalVolume = logicalVolume;
        this.description = description;
        this.fsType = fsType;
        this.freeSpace = freeSpace;
        this.usableSpace = usableSpace;
        this.totalSpace = totalSpace;
        this.freeInodes = freeInodes;
        this.totalInodes = totalInodes;
    }

    @Override
    public String getLogicalVolume() {
        return this.logicalVolume;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getType() {
        return this.fsType;
    }

    @Override
    public long getFreeSpace() {
        return this.freeSpace;
    }

    @Override
    public long getUsableSpace() {
        return this.usableSpace;
    }

    @Override
    public long getTotalSpace() {
        return this.totalSpace;
    }

    @Override
    public long getFreeInodes() {
        return this.freeInodes;
    }

    @Override
    public long getTotalInodes() {
        return this.totalInodes;
    }

    @Override
    public boolean updateAttributes() {
        for (OSFileStore fileStore : new FreeBsdFileSystem().getFileStores(isLocal())) {
            if (getName().equals(fileStore.getName()) && getVolume().equals(fileStore.getVolume())
                    && getMount().equals(fileStore.getMount())) {
                this.logicalVolume = fileStore.getLogicalVolume();
                this.description = fileStore.getDescription();
                this.fsType = fileStore.getType();
                this.freeSpace = fileStore.getFreeSpace();
                this.usableSpace = fileStore.getUsableSpace();
                this.totalSpace = fileStore.getTotalSpace();
                this.freeInodes = fileStore.getFreeInodes();
                this.totalInodes = fileStore.getTotalInodes();
                return true;
            }
        }
        return false;
    }

}
