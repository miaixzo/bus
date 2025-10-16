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
import org.miaixz.bus.image.galaxy.data.Fragments;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.codec.Compressor;
import org.miaixz.bus.image.nimble.codec.Decompressor;
import org.miaixz.bus.image.nimble.codec.Transcoder;
import org.miaixz.bus.image.nimble.codec.TransferSyntaxType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code Dcm2Dcm} class provides functionality to transcode DICOM files from one transfer syntax to another. It
 * supports both modern transcoding via {@link Transcoder} and a legacy approach.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Dcm {

    /**
     * List of compression parameters.
     */
    private final List<Property> params = new ArrayList<>();
    /**
     * The target Transfer Syntax UID.
     */
    private String tsuid;
    /**
     * The target Transfer Syntax type.
     */
    private TransferSyntaxType tstype;
    /**
     * Flag to retain the original File Meta Information.
     */
    private boolean retainfmi;
    /**
     * Flag to exclude the File Meta Information from the output.
     */
    private boolean nofmi;
    /**
     * Flag to use the legacy transcoding method.
     */
    private boolean legacy;
    /**
     * Encoding options for writing the DICOM file.
     */
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    /**
     * The maximum number of threads to use for transcoding.
     */
    private int maxThreads = 1;

    /**
     * Converts a string to a more specific type (Double, Boolean, or String).
     *
     * @param s the input string.
     * @return the converted value.
     */
    private static Object toValue(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : s.equalsIgnoreCase("false") ? Boolean.FALSE : s;
        }
    }

    /**
     * Sets the destination Transfer Syntax UID.
     *
     * @param uid the Transfer Syntax UID string.
     * @throws IllegalArgumentException if the UID is not a supported Transfer Syntax.
     */
    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
        this.tstype = TransferSyntaxType.forUID(uid);
        if (tstype == null) {
            throw new IllegalArgumentException("Unsupported Transfer Syntax: " + tsuid);
        }
    }

    /**
     * Sets whether to retain the original File Meta Information. If true, the FMI is updated with the new Transfer
     * Syntax.
     *
     * @param retainfmi true to retain and update FMI, false otherwise.
     */
    public final void setRetainFileMetaInformation(boolean retainfmi) {
        this.retainfmi = retainfmi;
    }

    /**
     * Sets whether to write the output DICOM file without File Meta Information.
     *
     * @param nofmi true to exclude FMI, false otherwise.
     */
    public final void setWithoutFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    /**
     * Sets whether to use the legacy transcoding implementation.
     *
     * @param legacy true to use the legacy method, false to use the modern {@link Transcoder}.
     */
    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    /**
     * Sets the encoding options for writing the DICOM file.
     *
     * @param encOpts the encoding options.
     */
    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    /**
     * Adds a compression parameter.
     *
     * @param name  the parameter name.
     * @param value the parameter value.
     */
    public void addCompressionParam(String name, Object value) {
        params.add(new Property(name, value));
    }

    /**
     * Sets the maximum number of threads to use for concurrent transcoding.
     *
     * @param maxThreads the number of threads. Must be greater than 0.
     * @throws IllegalArgumentException if maxThreads is not positive.
     */
    public void setMaxThreads(int maxThreads) {
        if (maxThreads <= 0)
            throw new IllegalArgumentException("max-threads: " + maxThreads);
        this.maxThreads = maxThreads;
    }

    /**
     * Transcodes a list of source files or directories into a destination directory.
     *
     * @param srcList a list of source file/directory paths.
     * @param dest    the destination directory.
     */
    private void mtranscode(List<String> srcList, File dest) {
        ExecutorService executorService = maxThreads > 1 ? Executors.newFixedThreadPool(maxThreads) : null;
        for (String src : srcList) {
            mtranscode(new File(src), dest, executorService);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Recursively transcodes a source file or directory.
     *
     * @param src      the source file or directory.
     * @param dest     the destination file or directory.
     * @param executer the executor service for concurrent processing.
     */
    private void mtranscode(final File src, File dest, Executor executer) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mtranscode(file, new File(dest, file.getName()), executer);
            return;
        }
        final File finalDest = dest.isDirectory() ? new File(dest, src.getName()) : dest;
        if (executer != null) {
            executer.execute(() -> transcode(src, finalDest));
        } else {
            transcode(src, finalDest);
        }
    }

    /**
     * Transcodes a single source file to a destination file.
     *
     * @param src  the source file.
     * @param dest the destination file.
     */
    private void transcode(File src, File dest) {
        try {
            if (legacy)
                transcodeLegacy(src, dest);
            else
                transcodeWithTranscoder(src, dest);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Transcodes a DICOM file using a legacy approach involving manual decompression and compression.
     *
     * @param src  the source file.
     * @param dest the destination file.
     * @throws IOException if an I/O error occurs.
     */
    public void transcodeLegacy(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        ImageInputStream dis = new ImageInputStream(src);
        try {
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset();
        } finally {
            dis.close();
        }
        Object pixeldata = dataset.getValue(Tag.PixelData);
        Compressor compressor = null;
        ImageOutputStream dos = null;
        try {
            String tsuid = this.tsuid;
            if (pixeldata != null) {
                if (tstype.isPixeldataEncapsulated()) {
                    tsuid = adjustTransferSyntax(tsuid, dataset.getInt(Tag.BitsStored, 8));
                    compressor = new Compressor(dataset, dis.getTransferSyntax());
                    compressor.compress(tsuid, params.toArray(new Property[params.size()]));
                } else if (pixeldata instanceof Fragments)
                    Decompressor.decompress(dataset, dis.getTransferSyntax());
            }
            if (nofmi)
                fmi = null;
            else if (retainfmi && fmi != null)
                fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
            else
                fmi = dataset.createFileMetaInformation(tsuid);
            dos = new ImageOutputStream(dest);
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        } finally {
            IoKit.close(compressor);
            IoKit.close(dos);
        }
    }

    /**
     * Transcodes a DICOM file using the {@link Transcoder} class.
     *
     * @param src  the source file.
     * @param dest the destination file.
     * @throws IOException if an I/O error occurs or transcoding fails.
     */
    public void transcodeWithTranscoder(File src, final File dest) throws IOException {
        try (Transcoder transcoder = new Transcoder(src)) {
            transcoder.setIncludeFileMetaInformation(!nofmi);
            transcoder.setRetainFileMetaInformation(retainfmi);
            transcoder.setEncodingOptions(encOpts);
            transcoder.setDestinationTransferSyntax(tsuid);
            transcoder.setCompressParams(params.toArray(new Property[params.size()]));
            transcoder.transcode((transcoder1, dataset) -> new FileOutputStream(dest));
        } catch (Exception e) {
            Files.deleteIfExists(dest.toPath());
            throw e;
        }
    }

    /**
     * Adjusts the JPEG Transfer Syntax based on the bits stored value.
     *
     * @param tsuid      the proposed Transfer Syntax UID.
     * @param bitsStored the value of the Bits Stored (0028,0101) tag.
     * @return the adjusted or original Transfer Syntax UID.
     */
    private String adjustTransferSyntax(String tsuid, int bitsStored) {
        switch (tstype) {
            case JPEG_BASELINE:
                if (bitsStored > 8)
                    return UID.JPEGExtended12Bit.uid;
                break;

            case JPEG_EXTENDED:
                if (bitsStored <= 8)
                    return UID.JPEGBaseline8Bit.uid;
                break;

            default:
        }
        return tsuid;
    }

}
