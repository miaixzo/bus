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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.xml.sax.SAXException;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pdf2Dcm {

    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    private static final int[] IUID_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID };

    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime, Tag.AcquisitionDateTime };

    private static Attributes staticMetadata;
    private static FileContentType fileContentType;
    private static boolean encapsulatedDocLength;

    private static FileContentType fileContentType(String s) {
        switch (s.toLowerCase(Locale.ENGLISH)) {
        case "stl":
        case "model/stl":
        case "model/x.stl-binary":
        case "application/sla":
            return FileContentType.STL;
        case "pdf":
        case "application/pdf":
            return FileContentType.PDF;
        case "xml":
        case "application/xml":
            return FileContentType.CDA;
        case "mtl":
        case "model/mtl":
            return FileContentType.MTL;
        case "obj":
        case "model/obj":
            return FileContentType.OBJ;
        case "genozip":
        case "application/vnd.genozip":
            return FileContentType.GENOZIP;
        case "vcf.bz2":
        case "vcfbzip2":
        case "vcfbz2":
        case "application/prs.vcfbzip2":
            return FileContentType.VCF_BZIP2;
        case "boz":
        case "bz2":
        case "application/x-bzip2":
            return FileContentType.DOC_BZIP2;
        default:
            throw new IllegalArgumentException(s);
        }
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private Attributes createMetadata(FileContentType fileContentType, File srcFile) throws Exception {
        Attributes fileMetadata = SAXReader.parse(IoKit.openFileOrURL(fileContentType.getSampleMetadataFile()));
        fileMetadata.addAll(staticMetadata);
        if ((fileContentType == FileContentType.STL || fileContentType == FileContentType.OBJ)
                && !fileMetadata.containsValue(Tag.FrameOfReferenceUID))
            fileMetadata.setString(Tag.FrameOfReferenceUID, VR.UI, UID.createUID());
        if (encapsulatedDocLength)
            fileMetadata.setLong(Tag.EncapsulatedDocumentLength, VR.UL, srcFile.length());
        return fileMetadata;
    }

    private void convert(List<String> args) throws Exception {
        int argsSize = args.size();
        Path destPath = Paths.get(args.get(argsSize - 1));
        for (String src : args.subList(0, argsSize - 1)) {
            Path srcPath = Paths.get(src);
            if (Files.isDirectory(srcPath))
                Files.walkFileTree(srcPath, new Pdf2DcmFileVisitor(srcPath, destPath));
            else if (Files.isDirectory(destPath))
                convert(srcPath, destPath.resolve(srcPath.getFileName() + ".dcm"));
            else
                convert(srcPath, destPath);
        }
    }

    private void convert(Path srcFilePath, Path destFilePath) throws Exception {
        FileContentType fileContentType1 = fileContentType != null ? fileContentType
                : FileContentType.valueOf(srcFilePath);
        File srcFile = srcFilePath.toFile();
        File destFile = destFilePath.toFile();
        Attributes fileMetadata = createMetadata(fileContentType1, srcFile);
        long fileLength = srcFile.length();
        if (fileLength > MAX_FILE_SIZE)
            throw new IllegalArgumentException(srcFile.getPath());

        try (ImageOutputStream dos = new ImageOutputStream(destFile)) {
            dos.writeDataset(fileMetadata.createFileMetaInformation(UID.ExplicitVRLittleEndian.uid), fileMetadata);
            dos.writeAttribute(Tag.EncapsulatedDocument, VR.OB, Files.readAllBytes(srcFile.toPath()));
        }
    }

    enum FileContentType {
        PDF("resource:encapsulatedPDFMetadata.xml"), CDA("resource:encapsulatedCDAMetadata.xml"),
        STL("resource:encapsulatedSTLMetadata.xml"), MTL("resource:encapsulatedMTLMetadata.xml"),
        OBJ("resource:encapsulatedOBJMetadata.xml"), GENOZIP("resource:encapsulatedGenozipMetadata.xml"),
        VCF_BZIP2("resource:encapsulatedVCFBzip2Metadata.xml"),
        DOC_BZIP2("resource:encapsulatedDocumentBzip2Metadata.xml");

        private final String sampleMetadataFile;

        FileContentType(String sampleMetadataFile) {
            this.sampleMetadataFile = sampleMetadataFile;
        }

        static FileContentType valueOf(Path path) throws IOException {
            String fileName = path.toFile().getName();
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            String contentType = Files.probeContentType(path);
            return fileContentType(contentType != null ? contentType : ext);
        }

        public String getSampleMetadataFile() {
            return sampleMetadataFile;
        }
    }

    class Pdf2DcmFileVisitor extends SimpleFileVisitor<Path> {
        private final Path srcPath;
        private final Path destPath;

        Pdf2DcmFileVisitor(Path srcPath, Path destPath) {
            this.srcPath = srcPath;
            this.destPath = destPath;
        }

        @Override
        public FileVisitResult visitFile(Path srcFilePath, BasicFileAttributes attrs) throws IOException {
            Path destFilePath = resolveDestFilePath(srcFilePath);
            if (!Files.isDirectory(destFilePath))
                Files.createDirectories(destFilePath);
            try {
                convert(srcFilePath, destFilePath.resolve(srcFilePath.getFileName() + ".dcm"));
            } catch (SAXException | ParserConfigurationException e) {
                e.printStackTrace(System.out);
                return FileVisitResult.TERMINATE;
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
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
