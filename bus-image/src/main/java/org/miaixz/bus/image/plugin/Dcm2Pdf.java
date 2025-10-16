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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * The {@code Dcm2Pdf} class provides functionality to extract encapsulated documents (such as PDF, CDA, etc.) from
 * DICOM files. It can process individual files or entire directories recursively.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Pdf {

    /**
     * Processes a list of source files/directories and extracts encapsulated documents to a destination.
     *
     * @param args A list of strings where the last element is the destination path, and the preceding elements are
     *             source paths.
     * @throws IOException if an I/O error occurs.
     */
    private void convert(List<String> args) throws IOException {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        boolean destIsDir = Files.isDirectory(destPath);
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath))
                Files.walkFileTree(srcPath, new Dcm2PdfFileVisitor(srcPath, destPath, destIsDir));
            else
                convert(srcPath, destPath, destIsDir);
        }
    }

    /**
     * Converts a single DICOM file by extracting its encapsulated document.
     *
     * @param src       The path to the source DICOM file.
     * @param dest      The path to the destination file or directory.
     * @param destIsDir A flag indicating if the destination path is a directory.
     */
    private void convert(Path src, Path dest, boolean destIsDir) {
        try (ImageInputStream dis = new ImageInputStream(src.toFile())) {
            Attributes attributes = dis.readDataset();
            String sopCUID = attributes.getString(Tag.SOPClassUID);
            String ext = FileType.getFileExt(sopCUID);
            if (ext == null) {
                Logger.info(
                        "DICOM file {} with {} SOP Class cannot be converted to bulkdata file",
                        src,
                        UID.nameOf(sopCUID));
                return;
            }
            File destFile = destIsDir ? dest.resolve(src.getFileName() + ext).toFile() : dest.toFile();
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] value = (byte[]) attributes.getValue(Tag.EncapsulatedDocument);
                fos.write(value, 0, value.length - 1);
                byte lastByte = value[value.length - 1];
                if (lastByte != 0)
                    fos.write(lastByte);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * An enumeration of supported encapsulated file types, mapping SOP Class UIDs to file extensions.
     */
    enum FileType {

        /** Encapsulated PDF Storage. */
        PDF(UID.EncapsulatedPDFStorage.uid, ".pdf"),
        /** Encapsulated CDA Storage. */
        CDA(UID.EncapsulatedCDAStorage.uid, ".xml"),
        /** Encapsulated MTL Storage. */
        MTL(UID.EncapsulatedMTLStorage.uid, ".mtl"),
        /** Encapsulated OBJ Storage. */
        OBJ(UID.EncapsulatedOBJStorage.uid, ".obj"),
        /** Encapsulated STL Storage. */
        STL(UID.EncapsulatedSTLStorage.uid, ".stl"),
        /** Private Encapsulated Genozip Storage. */
        GENOZIP(UID.PrivateEncapsulatedGenozipStorage.uid, ".genozip"),
        /** Private Encapsulated Bzip2 VCF Storage. */
        VCF_BZIP2(UID.PrivateEncapsulatedBzip2VCFStorage.uid, ".vcfbz2"),
        /** Private Encapsulated Bzip2 Document Storage. */
        DOC_BZIP2(UID.PrivateEncapsulatedBzip2DocumentStorage.uid, ".bz2");

        /** The SOP Class UID for the file type. */
        private final String sopClass;
        /** The file extension for the file type. */
        private final String fileExt;

        /**
         * Constructs a FileType enum constant.
         *
         * @param sopClass The SOP Class UID.
         * @param fileExt  The corresponding file extension.
         */
        FileType(String sopClass, String fileExt) {
            this.sopClass = sopClass;
            this.fileExt = fileExt;
        }

        /**
         * Gets the file extension for a given SOP Class UID.
         *
         * @param sopCUID The SOP Class UID to look up.
         * @return The file extension if a match is found, otherwise {@code null}.
         */
        public static String getFileExt(String sopCUID) {
            for (FileType fileType : values())
                if (fileType.getSOPClass().equals(sopCUID))
                    return fileType.getFileExt();
            return null;
        }

        /**
         * Gets the SOP Class UID of this file type.
         *
         * @return The SOP Class UID.
         */
        private String getSOPClass() {
            return sopClass;
        }

        /**
         * Gets the file extension of this file type.
         *
         * @return The file extension.
         */
        private String getFileExt() {
            return fileExt;
        }
    }

    /**
     * A {@link SimpleFileVisitor} that walks a file tree and converts DICOM files containing encapsulated documents.
     */
    class Dcm2PdfFileVisitor extends SimpleFileVisitor<Path> {

        /** The source root path. */
        private final Path srcPath;
        /** The destination root path. */
        private final Path destPath;
        /** Flag indicating if the destination is a directory. */
        private final boolean destIsDir;

        /**
         * Constructs a new file visitor.
         *
         * @param srcPath   The source directory being traversed.
         * @param destPath  The destination directory.
         * @param destIsDir True if the destination is a directory.
         */
        Dcm2PdfFileVisitor(Path srcPath, Path destPath, boolean destIsDir) {
            this.srcPath = srcPath;
            this.destPath = destPath;
            this.destIsDir = destIsDir;
        }

        /**
         * Called for each file visited. It triggers the conversion of the file.
         *
         * @param srcFilePath The path to the visited file.
         * @param attrs       The file's basic attributes.
         * @return {@link FileVisitResult#CONTINUE} to continue the walk.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            convert(srcFilePath, destFilePath, destIsDir);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Resolves the destination file path, preserving the relative directory structure from the source.
         *
         * @param srcFilePath The path of the source file.
         * @return The resolved destination path.
         */
        private Path resolveDestFilePath(Path srcFilePath) {
            int srcPathNameCount = srcPath.getNameCount();
            int srcFilePathNameCount = srcFilePath.getNameCount() - 1;
            if (srcPathNameCount == srcFilePathNameCount)
                return destPath;

            return destPath.resolve(srcFilePath.subpath(srcPathNameCount, srcFilePathNameCount));
        }
    }

}
