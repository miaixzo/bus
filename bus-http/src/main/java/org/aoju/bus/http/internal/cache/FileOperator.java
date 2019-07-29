package org.aoju.bus.http.internal.cache;

import org.aoju.bus.core.io.Buffer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * <ul>
 * <li><strong>Read/write:</strong> read and write using the same operator.
 * <li><strong>Random access:</strong> access any position within the file.
 * <li><strong>Shared channels:</strong> read and write a file channel that's shared between
 * multiple operators. Note that although the underlying {@code FileChannel} may be shared,
 * each {@code FileOperator} should not be.
 * </ul>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
final class FileOperator {

    private final FileChannel fileChannel;

    FileOperator(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    /**
     * Write {@code byteCount} bytes from {@code source} to the file at {@code pos}.
     */
    public void write(long pos, Buffer source, long byteCount) throws IOException {
        if (byteCount < 0 || byteCount > source.size()) throw new IndexOutOfBoundsException();

        while (byteCount > 0L) {
            long bytesWritten = fileChannel.transferFrom(source, pos, byteCount);
            pos += bytesWritten;
            byteCount -= bytesWritten;
        }
    }

    /**
     * Copy {@code byteCount} bytes from the file at {@code pos} into to {@code source}. It is the
     * caller's responsibility to make sure there are sufficient bytes to read: if there aren't this
     * method throws an {@link EOFException}.
     */
    public void read(long pos, Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IndexOutOfBoundsException();

        while (byteCount > 0L) {
            long bytesRead = fileChannel.transferTo(pos, byteCount, sink);
            pos += bytesRead;
            byteCount -= bytesRead;
        }
    }
}
