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
 * A file visitor utility to fix a specific type of DICOM file corruption where a data element with Value Representation
 * (VR) of Long String (LO) has its length incorrectly stored as a 16-bit value instead of a 32-bit value, which is
 * required for explicit VRs with a length that exceeds 65534 bytes.
 * <p>
 * This class traverses a file tree, reads each file, and searches for LO attributes with lengths that appear to be
 * truncated. It corrects this by changing the VR to Unknown (UN) and writing the proper 32-bit length. This is a
 * heuristic-based fix for a known issue with certain DICOM creation toolkits.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FixLO2UN extends SimpleFileVisitor<Path> {

    /**
     * A pre-allocated ByteBuffer containing the VR for UN (0x554E) and a 32-bit length field.
     */
    private final ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x55, 0x4e, 0, 0, 0, 0, 0, 0 })
            .order(ByteOrder.LITTLE_ENDIAN);
    /**
     * The source path to be processed.
     */
    private final Path srcPath;
    /**
     * The destination path for the corrected files.
     */
    private final Path destPath;
    /**
     * An enum to manage destination path logic (file or directory).
     */
    private final Dest dest;

    /**
     * Private constructor for the file visitor.
     *
     * @param srcPath  The source path (file or directory).
     * @param destPath The destination path (file or directory).
     * @param dest     The destination type logic.
     */
    private FixLO2UN(Path srcPath, Path destPath, Dest dest) {
        this.srcPath = srcPath;
        this.destPath = destPath;
        this.dest = dest;
    }

    /**
     * Processes a single file to find and fix corrupted LO elements. It memory-maps the source file for reading and
     * writes the corrected content to the destination file.
     *
     * @param srcFile The source file to visit.
     * @param attrs   The basic attributes of the file.
     * @return {@link FileVisitResult#CONTINUE} to continue the file tree walk.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {
        Path dstFile = dest.dstFile(srcFile, srcPath, destPath);
        Path dstDir = dstFile.getParent();
        if (dstDir != null)
            Files.createDirectories(dstDir);
        try (FileChannel ifc = (FileChannel) Files.newByteChannel(srcFile, EnumSet.of(StandardOpenOption.READ));
                FileChannel ofc = (FileChannel) Files
                        .newByteChannel(dstFile, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
            MappedByteBuffer mbb = ifc.map(FileChannel.MapMode.READ_ONLY, 0, ifc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            mbb.mark();
            int length;
            while ((length = correctLength(mbb)) > 0) {
                int position = mbb.position();
                Logger.info(
                        "  %d: (%02X%02X,%02X%02X) LO #%d -> UN #%d%n",
                        position - 6,
                        mbb.get(position - 5),
                        mbb.get(position - 6),
                        mbb.get(position - 3),
                        mbb.get(position - 4),
                        length & 0xffff,
                        length);
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
        } catch (FileAlreadyExistsException e) {
            Logger.warn("Destination file {} already exists, skipping.", dstFile);
        } catch (Exception e) {
            Logger.error("Failed to process file {}: {}", srcFile, e.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Scans the buffer to find a potential malformed LO element.
     *
     * @param mbb The memory-mapped byte buffer of the DICOM file.
     * @return The corrected 32-bit length if a fix is needed, otherwise 0.
     */
    private int correctLength(MappedByteBuffer mbb) {
        int length;
        while (mbb.remaining() > 8) {
            // Check for VR = 'LO' (0x4F4C), odd group number, and length that needs correction.
            if (mbb.getShort() == 0x4f4c && mbb.get(mbb.position() - 3) == 0 && mbb.get(mbb.position() - 6) % 2 != 0
                    && !isVRCode(mbb.getShort(mbb.position() + 6 + (length = mbb.getShort(mbb.position()) & 0xffff))))
                return correctLength(mbb, length);
        }
        return 0;
    }

    /**
     * Checks if a 2-byte integer represents a valid DICOM VR code. This is used as a heuristic to determine the end of
     * a data element.
     *
     * @param code The 2-byte code to check.
     * @return {@code true} if the code is a valid VR, {@code false} otherwise.
     */
    private boolean isVRCode(int code) {
        switch (code) {
            case 0x4541: // AE
            case 0x5341: // AS
            case 0x5441: // AT
            case 0x5343: // CS
            case 0x4144: // DA
            case 0x5344: // DS
            case 0x5444: // DT
            case 0x4446: // FD
            case 0x4c46: // FL
            case 0x5349: // IS
            case 0x4f4c: // LO
            case 0x544c: // LT
            case 0x424f: // OB
            case 0x444f: // OD
            case 0x464f: // OF
            case 0x4c4f: // OL
            case 0x574f: // OW
            case 0x4e50: // PN
            case 0x4853: // SH
            case 0x4c53: // SL
            case 0x5153: // SQ
            case 0x5353: // SS
            case 0x5453: // ST
            case 0x4d54: // TM
            case 0x4355: // UC
            case 0x4955: // UI
            case 0x4c55: // UL
            case 0x4e55: // UN
            case 0x5255: // UR
            case 0x5355: // US
            case 0x5455: // UT
                return true;
        }
        return false;
    }

    /**
     * Corrects the length by iteratively adding 65536 until the position after the element points to what appears to be
     * a valid VR code.
     *
     * @param mbb    The memory-mapped byte buffer.
     * @param length The initial 16-bit length.
     * @return The corrected 32-bit length.
     */
    private int correctLength(MappedByteBuffer mbb, int length) {
        do {
            length += 0x10000;
        } while (!isVRCode(mbb.getShort(mbb.position() + 6 + length)));
        return length;
    }

    /**
     * An enum to handle the logic for determining the destination path, which can be either a single file or a
     * directory, preserving the source's subdirectory structure.
     */
    private enum Dest {

        /**
         * Destination is a single file.
         */
        FILE,
        /**
         * Destination is a directory.
         */
        DIRECTORY {

            @Override
            Path dstFile(Path srcFile, Path srcPath, Path destPath) {
                return destPath.resolve(srcFile == srcPath ? srcFile.getFileName() : srcPath.relativize(srcFile));
            }
        };

        /**
         * Determines the destination type based on the path.
         *
         * @param destPath The destination path.
         * @return {@code DIRECTORY} if the path is a directory, {@code FILE} otherwise.
         */
        static Dest of(Path destPath) {
            return Files.isDirectory(destPath) ? DIRECTORY : FILE;
        }

        /**
         * Calculates the destination file path.
         *
         * @param srcFile  The source file being processed.
         * @param srcPath  The root source path.
         * @param destPath The root destination path.
         * @return The resolved destination file path.
         */
        Path dstFile(Path srcFile, Path srcPath, Path destPath) {
            return destPath;
        }
    }

}
