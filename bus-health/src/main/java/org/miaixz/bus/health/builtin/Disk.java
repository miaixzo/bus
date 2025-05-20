/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 磁盘信息
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
public class Disk {
    /**
     * 设备名称 (如 /dev/sda1)
     */
    private String deviceName;

    /**
     * 文件系统卷名
     */
    private String volumeName;

    /**
     * 卷标
     */
    private String label;

    /**
     * 逻辑卷名
     */
    private String logicalVolumeName;

    /**
     * 挂载点 (如 /mnt/data)
     */
    private String mountPoint;

    /**
     * 文件系统描述
     */
    private String description;

    /**
     * 挂载选项 (如 rw,ro)
     */
    private String mountOptions;

    /**
     * 文件系统类型 (如 ext4, xfs, vfat)
     */
    private String filesystemType;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 总空间
     */
    private Long totalSpace;

    /**
     * 已用空间
     */
    private Long usedSpace;

    /**
     * 可用空间
     */
    private Long freeSpace;

    /**
     * 使用率 (usedSpace / totalSpace * 100)
     */
    private double usagePercent;

}