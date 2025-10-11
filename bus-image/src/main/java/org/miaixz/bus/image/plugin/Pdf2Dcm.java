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

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;

/**
 * The {@code Pdf2Dcm} class provides functionality to encapsulate various file types (e.g., PDF, STL, CDA) into a DICOM
 * Part 10 file as an Encapsulated Document. It can process individual files or entire directories.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pdf2Dcm {

    /**
     * The maximum file size allowed for encapsulation (2GB - 2 bytes).
     */
    private static final long MAX_FILE_SIZE = 0x7FFFFFFE;
    /**
     * The standard DICOM element dictionary.
     */
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    /**
     * Tags for UIDs that need to be generated if missing.
     */
    private static final int[] IUID_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID };

    /**
     * Type 2 tags that need to be present, even if with a null value.
     */
    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime, Tag.AcquisitionDateTime };

    /**
     * A set of static metadata to be merged into every created DICOM object.
     */
    private static Attributes staticMetadata;
    /**
     * The content type of the source file (e.g., PDF, STL).
     */
    private static FileContentType fileContentType;
    /**
     * A flag to include the Encapsulated Document Length tag.
     */
    private static boolean encapsulatedDocLength;

    /**
     * Determines the {@link FileContentType} from a string identifier (MIME type or file extension).
     *
     * @param s The string identifier.
     * @return The corresponding {@code FileContentType}.
     * @throws IllegalArgumentException if the type is not supported.
     */
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
                throw new IllegalArgumentException("Unsupported file content type: " + s);
        }
    }

    /**
     * Supplements the metadata with UIDs for Study, Series, and SOP Instance if they are missing.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUID_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    /**
     * Ensures that all Type 2 tags are present in the metadata, setting them to null if absent.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    /**
     * Creates the metadata for the DICOM file by loading a template and merging static metadata.
     *
     * @param fileContentType The content type of the file being encapsulated.
     * @param srcFile         The source file.
     * @return The created DICOM attributes.
     * @throws Exception if an error occurs during metadata creation.
     */
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

    /**
     * Converts a list of source files/directories to DICOM files.
     *
     * @param args A list where the last element is the destination and the others are sources.
     * @throws Exception if an error occurs during conversion.
     */
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

    /**
     * Converts a single source file to a DICOM file.
     *
     * @param srcFilePath  The path to the source file.
     * @param destFilePath The path for the destination DICOM file.
     * @throws Exception if an error occurs during conversion.
     */
    private void convert(Path srcFilePath, Path destFilePath) throws Exception {
        FileContentType type = fileContentType != null ? fileContentType : FileContentType.valueOf(srcFilePath);
        File srcFile = srcFilePath.toFile();
        File destFile = destFilePath.toFile();
        Attributes fileMetadata = createMetadata(type, srcFile);
        long fileLength = srcFile.length();
        if (fileLength > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large: " + srcFile.getPath());
        }

        try (ImageOutputStream dos = new ImageOutputStream(destFile)) {
            dos.writeDataset(fileMetadata.createFileMetaInformation(UID.ExplicitVRLittleEndian.uid), fileMetadata);
            dos.writeAttribute(Tag.EncapsulatedDocument, VR.OB, Files.readAllBytes(srcFile.toPath()));
        }
    }

    /**
     * An enumeration of supported file content types for encapsulation, each associated with a sample metadata file.
     */
    enum FileContentType {

        /**
         * Encapsulated PDF.
         */
        PDF("resource:encapsulatedPDFMetadata.xml"),
        /**
         * Encapsulated CDA.
         */
        CDA("resource:encapsulatedCDAMetadata.xml"),
        /**
         * Encapsulated STL (3D model).
         */
        STL("resource:encapsulatedSTLMetadata.xml"),
        /**
         * Encapsulated MTL (3D model material).
         */
        MTL("resource:encapsulatedMTLMetadata.xml"),
        /**
         * Encapsulated OBJ (3D model).
         */
        OBJ("resource:encapsulatedOBJMetadata.xml"),
        /**
         * Encapsulated Genozip.
         */
        GENOZIP("resource:encapsulatedGenozipMetadata.xml"),
        /**
         * Encapsulated Bzip2 compressed VCF.
         */
        VCF_BZIP2("resource:encapsulatedVCFBzip2Metadata.xml"),
        /**
         * Encapsulated Bzip2 compressed document.
         */
        DOC_BZIP2("resource:encapsulatedDocumentBzip2Metadata.xml");

        /**
         * The path to the sample metadata XML file.
         */
        private final String sampleMetadataFile;

        /**
         * Constructs a FileContentType enum constant.
         *
         * @param sampleMetadataFile The resource path to the sample metadata file.
         */
        FileContentType(String sampleMetadataFile) {
            this.sampleMetadataFile = sampleMetadataFile;
        }

        /**
         * Determines the {@code FileContentType} from a file path by probing its content type or using its extension.
         *
         * @param path The path to the file.
         * @return The determined {@code FileContentType}.
         * @throws IOException if an I/O error occurs.
         */
        static FileContentType valueOf(Path path) throws IOException {
            String fileName = path.toFile().getName();
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            String contentType = Files.probeContentType(path);
            return fileContentType(contentType != null ? contentType : ext);
        }

        /**
         * Gets the resource path of the sample metadata file for this content type.
         *
         * @return The sample metadata file path.
         */
        public String getSampleMetadataFile() {
            return sampleMetadataFile;
        }
    }

    /**
     * A file visitor to recursively find and convert files in a directory.
     */
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
