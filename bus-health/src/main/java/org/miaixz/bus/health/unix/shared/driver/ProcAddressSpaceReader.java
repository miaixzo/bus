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
package org.miaixz.bus.health.unix.shared.driver;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.LibCAPI.ssize_t;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NotThreadSafe;
import org.miaixz.bus.health.unix.shared.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

/**
 * Reads pointer-sized values and null-terminated strings from a process address space.
 * <p>
 * Each instance owns one file descriptor and one reusable page buffer, so callers must confine an instance to one
 * thread and close it when finished.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@NotThreadSafe
public final class ProcAddressSpaceReader implements AutoCloseable {

    /**
     * The libc binding used for address-space reads.
     */
    private final CLibrary libc;

    /**
     * The open address-space file descriptor.
     */
    private final int fd;

    /**
     * The memory page size in bytes.
     */
    private final long pageSize;

    /**
     * The reusable page buffer.
     */
    private final Memory buffer;

    /**
     * The reusable page buffer size.
     */
    private final size_t bufSize;

    /**
     * The start address currently represented by the buffer.
     */
    private long bufStart;

    /**
     * Whether the buffer currently contains a valid page.
     */
    private boolean bufValid;

    /**
     * Creates a new address-space reader.
     *
     * @param libc     the C library binding
     * @param fd       the open address-space file descriptor
     * @param pageSize the memory page size in bytes
     */
    private ProcAddressSpaceReader(CLibrary libc, int fd, long pageSize) {
        this.libc = libc;
        this.fd = fd;
        this.pageSize = pageSize;
        this.buffer = new Memory(pageSize * 2);
        this.bufSize = new size_t(this.buffer.size());
        this.bufStart = 0L;
        this.bufValid = false;
    }

    /**
     * Opens the process address space for reading.
     *
     * @param libc     the C library binding
     * @param pid      the process id
     * @param pageSize the memory page size in bytes
     * @return a reader, or {@code null} if the address space cannot be opened
     */
    public static ProcAddressSpaceReader open(CLibrary libc, int pid, long pageSize) {
        String procas = "/proc/" + pid + "/as";
        int fd = libc.open(procas, 0);
        if (fd < 0) {
            Logger.trace(false, "Health", "No permission to read file: {} ", procas);
            return null;
        }
        return new ProcAddressSpaceReader(libc, fd, pageSize);
    }

    /**
     * Reads a pointer-sized value at the requested address.
     *
     * @param addr      the address to read
     * @param increment the pointer size in bytes
     * @return the pointer value, or {@code 0} if the page cannot be read
     */
    public long readPointer(long addr, long increment) {
        if (!ensurePage(addr)) {
            return 0L;
        }
        return decodePointer(this.buffer, addr - this.bufStart, increment);
    }

    /**
     * Reads a null-terminated string at the requested address.
     *
     * @param addr the address to read
     * @return the string, or an empty string if the page cannot be read
     */
    public String readString(long addr) {
        if (!ensurePage(addr)) {
            return Normal.EMPTY;
        }
        return this.buffer.getString(addr - this.bufStart);
    }

    /**
     * Ensures the buffer contains the page for the requested address.
     *
     * @param addr the address whose page should be buffered
     * @return {@code true} if the page is available, otherwise {@code false}
     */
    private boolean ensurePage(long addr) {
        if (this.bufValid && addr >= this.bufStart && addr - this.bufStart <= this.pageSize) {
            return true;
        }
        this.bufValid = false;
        long newStart = Math.floorDiv(addr, this.pageSize) * this.pageSize;
        ssize_t result = this.libc.pread(this.fd, this.buffer, this.bufSize, new NativeLong(newStart));
        if (result.longValue() < this.pageSize) {
            Logger.debug(false, "Health", "Failed to read page from address space: {} bytes read", result.longValue());
            return false;
        }
        this.bufStart = newStart;
        this.bufValid = true;
        return true;
    }

    /**
     * Decodes a pointer-sized value from a buffer.
     *
     * @param buffer    the buffer to read from
     * @param offset    the offset within the buffer
     * @param increment the pointer size in bytes
     * @return the decoded pointer value
     */
    private static long decodePointer(Memory buffer, long offset, long increment) {
        return increment == 8 ? buffer.getLong(offset) : Integer.toUnsignedLong(buffer.getInt(offset));
    }

    /**
     * Closes the address-space file descriptor and releases the page buffer.
     */
    @Override
    public void close() {
        this.libc.close(this.fd);
        this.buffer.close();
    }

}
