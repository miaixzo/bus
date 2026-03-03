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
package org.miaixz.bus.health.builtin.software.common;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSFileStore;

/**
 * Common implementations for OSFileStore
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractOSFileStore implements OSFileStore {

    private String name;
    private String volume;
    private String label;
    private String mount;
    private String options;
    private String uuid;
    private boolean local;

    protected AbstractOSFileStore() {

    }

    protected AbstractOSFileStore(String name, String volume, String label, String mount, String options, String uuid,
            boolean local) {
        this.name = name;
        this.volume = volume;
        this.label = label;
        this.mount = mount;
        this.options = options;
        this.uuid = uuid;
        this.local = local;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVolume() {
        return this.volume;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getMount() {
        return this.mount;
    }

    @Override
    public String getOptions() {
        return options;
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

    @Override
    public boolean isLocal() {
        return this.local;
    }

    @Override
    public String toString() {
        return "OSFileStore [name=" + getName() + ", volume=" + getVolume() + ", label=" + getLabel()
                + ", logicalVolume=" + getLogicalVolume() + ", mount=" + getMount() + ", description="
                + getDescription() + ", fsType=" + getType() + ", options=\"" + getOptions() + "\", uuid=" + getUUID()
                + ", isLocal=" + isLocal() + ", freeSpace=" + getFreeSpace() + ", usableSpace=" + getUsableSpace()
                + ", totalSpace=" + getTotalSpace() + ", freeInodes=" + getFreeInodes() + ", totalInodes="
                + getTotalInodes() + "]";
    }

}
