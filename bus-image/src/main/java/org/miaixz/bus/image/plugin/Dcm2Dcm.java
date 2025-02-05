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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dcm2Dcm {

    private final List<Property> params = new ArrayList<>();
    private String tsuid;
    private TransferSyntaxType tstype;
    private boolean retainfmi;
    private boolean nofmi;
    private boolean legacy;
    private ImageEncodingOptions encOpts = ImageEncodingOptions.DEFAULT;
    private int maxThreads = 1;

    private static Object toValue(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE : s.equalsIgnoreCase("false") ? Boolean.FALSE : s;
        }
    }

    public final void setTransferSyntax(String uid) {
        this.tsuid = uid;
        this.tstype = TransferSyntaxType.forUID(uid);
        if (tstype == null) {
            throw new IllegalArgumentException("Unsupported Transfer Syntax: " + tsuid);
        }
    }

    public final void setRetainFileMetaInformation(boolean retainfmi) {
        this.retainfmi = retainfmi;
    }

    public final void setWithoutFileMetaInformation(boolean nofmi) {
        this.nofmi = nofmi;
    }

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    public final void setEncodingOptions(ImageEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    public void addCompressionParam(String name, Object value) {
        params.add(new Property(name, value));
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads <= 0)
            throw new IllegalArgumentException("max-threads: " + maxThreads);
        this.maxThreads = maxThreads;
    }

    private void mtranscode(List<String> srcList, File dest) {
        ExecutorService executorService = maxThreads > 1 ? Executors.newFixedThreadPool(maxThreads) : null;
        for (String src : srcList) {
            mtranscode(new File(src), dest, executorService);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

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
