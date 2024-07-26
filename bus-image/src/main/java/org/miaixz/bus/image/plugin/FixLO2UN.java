/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class FixLO2UN extends SimpleFileVisitor<Path> {

    private final ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x55, 0x4e, 0, 0, 0, 0, 0, 0 })
            .order(ByteOrder.LITTLE_ENDIAN);
    private final Path srcPath;
    private final Path destPath;
    private final Dest dest;

    private FixLO2UN(Path srcPath, Path destPath, Dest dest) {
        this.srcPath = srcPath;
        this.destPath = destPath;
        this.dest = dest;
    }

    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {
        Path dstFile = dest.dstFile(srcFile, srcPath, destPath);
        Path dstDir = dstFile.getParent();
        if (dstDir != null)
            Files.createDirectories(dstDir);
        try (FileChannel ifc = (FileChannel) Files.newByteChannel(srcFile, EnumSet.of(StandardOpenOption.READ));
                FileChannel ofc = (FileChannel) Files.newByteChannel(dstFile,
                        EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
            MappedByteBuffer mbb = ifc.map(FileChannel.MapMode.READ_ONLY, 0, ifc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            mbb.mark();
            int length;
            while ((length = correctLength(mbb)) > 0) {
                int position = mbb.position();
                Logger.info("  %d: (%02X%02X,%02X%02X) LO #%d -> UN #%d%n", position - 6, mbb.get(position - 5),
                        mbb.get(position - 6), mbb.get(position - 3), mbb.get(position - 4), length & 0xfff, length);
                mbb.reset().limit(position - 2);
                ofc.write(mbb);
                buffer.putInt(4, length).rewind();
                ofc.write(buffer);
                mbb.limit(position + 2 + length).position(position + 2);
                ofc.write(mbb);
                mbb.limit((int) ifc.size()).mark();
            }
            mbb.reset();
            ofc.write(mbb);
        }
        return FileVisitResult.CONTINUE;
    }

    private int correctLength(MappedByteBuffer mbb) {
        int length;
        while (mbb.remaining() > 8) {
            if (mbb.getShort() == 0x4f4c && mbb.get(mbb.position() - 3) == 0 && mbb.get(mbb.position() - 6) % 2 != 0
                    && !isVRCode(mbb.getShort(mbb.position() + 6 + (length = mbb.getShort(mbb.position()) & 0xffff))))
                return correctLength(mbb, length);
        }
        return 0;
    }

    private boolean isVRCode(int code) {
        switch (code) {
        case 0x4541:
        case 0x5341:
        case 0x5441:
        case 0x5343:
        case 0x4144:
        case 0x5344:
        case 0x5444:
        case 0x4446:
        case 0x4c46:
        case 0x5349:
        case 0x4f4c:
        case 0x544c:
        case 0x424f:
        case 0x444f:
        case 0x464f:
        case 0x4c4f:
        case 0x574f:
        case 0x4e50:
        case 0x4853:
        case 0x4c53:
        case 0x5153:
        case 0x5353:
        case 0x5453:
        case 0x4d54:
        case 0x4355:
        case 0x4955:
        case 0x4c55:
        case 0x4e55:
        case 0x5255:
        case 0x5355:
        case 0x5455:
            return true;
        }
        return false;
    }

    private int correctLength(MappedByteBuffer mbb, int length) {
        do {
            length += 0x10000;
        } while (!isVRCode(mbb.getShort(mbb.position() + 6 + length)));
        return length;
    }

    private enum Dest {
        FILE, DIRECTORY {
            @Override
            Path dstFile(Path srcFile, Path srcPath, Path destPath) {
                return destPath.resolve(srcFile == srcPath ? srcFile.getFileName() : srcPath.relativize(srcFile));
            }
        };

        static Dest of(Path destPath) {
            return Files.isDirectory(destPath) ? DIRECTORY : FILE;
        }

        Path dstFile(Path srcFile, Path srcPath, Path destPath) {
            return destPath;
        }
    }

}
