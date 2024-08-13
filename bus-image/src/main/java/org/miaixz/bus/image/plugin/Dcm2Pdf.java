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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Pdf {

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

    private void convert(Path src, Path dest, boolean destIsDir) {
        try (ImageInputStream dis = new ImageInputStream(src.toFile())) {
            Attributes attributes = dis.readDataset();
            String sopCUID = attributes.getString(Tag.SOPClassUID);
            String ext = FileType.getFileExt(sopCUID);
            if (ext == null) {
                Logger.info("DICOM file {} with {} SOP Class cannot be converted to bulkdata file", src,
                        UID.nameOf(sopCUID));
                return;
            }
            File destFile = destIsDir ? dest.resolve(src.getFileName() + ext).toFile() : dest.toFile();
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] value = (byte[]) attributes.getValue(Tag.EncapsulatedDocument);
            fos.write(value, 0, value.length - 1);
            byte lastByte = value[value.length - 1];
            if (lastByte != 0)
                fos.write(lastByte);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    enum FileType {
        PDF(UID.EncapsulatedPDFStorage.uid, ".pdf"), CDA(UID.EncapsulatedCDAStorage.uid, ".xml"),
        MTL(UID.EncapsulatedMTLStorage.uid, ".mtl"), OBJ(UID.EncapsulatedOBJStorage.uid, ".obj"),
        STL(UID.EncapsulatedSTLStorage.uid, ".stl"), GENOZIP(UID.PrivateEncapsulatedGenozipStorage.uid, ".genozip"),
        VCF_BZIP2(UID.PrivateEncapsulatedBzip2VCFStorage.uid, ".vcfbz2"),
        DOC_BZIP2(UID.PrivateEncapsulatedBzip2DocumentStorage.uid, ".bz2");

        private final String sopClass;
        private final String fileExt;

        FileType(String sopClass, String fileExt) {
            this.sopClass = sopClass;
            this.fileExt = fileExt;
        }

        public static String getFileExt(String sopCUID) {
            for (FileType fileType : values())
                if (fileType.getSOPClass().equals(sopCUID))
                    return fileType.getFileExt();
            return null;
        }

        private String getSOPClass() {
            return sopClass;
        }

        private String getFileExt() {
            return fileExt;
        }
    }

    class Dcm2PdfFileVisitor extends SimpleFileVisitor<Path> {
        private final Path srcPath;
        private final Path destPath;
        private final boolean destIsDir;

        Dcm2PdfFileVisitor(Path srcPath, Path destPath, boolean destIsDir) {
            this.srcPath = srcPath;
            this.destPath = destPath;
            this.destIsDir = destIsDir;
        }

        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            convert(srcFilePath, destFilePath, destIsDir);
            return FileVisitResult.CONTINUE;
        }

        private Path resolveDestFilePath(Path srcFilePath) {
            int srcPathNameCount = srcPath.getNameCount();
            int srcFilePathNameCount = srcFilePath.getNameCount() - 1;
            if (srcPathNameCount == srcFilePathNameCount)
                return destPath;

            return destPath.resolve(srcFilePath.subpath(srcPathNameCount, srcFilePathNameCount));
        }
    }

}
