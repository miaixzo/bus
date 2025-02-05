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
package org.miaixz.bus.image.nimble.codec;

import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.*;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.BufferedImages;
import org.miaixz.bus.image.nimble.Overlays;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLS;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLSOutputStream;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Compressor extends Decompressor implements Closeable {

    private final VR.Holder pixeldataVR = new VR.Holder();
    private BulkData pixeldata;
    private ImageWriter compressor;
    private ImageReader verifier;
    private PatchJPEGLS patchJPEGLS;
    private ImageWriteParam compressParam;
    private ImageInputStream iis;
    private IOException ex;
    private int[] embeddedOverlays;
    private int maxPixelValueError = -1;
    private int avgPixelValueBlockSize = 1;
    private int bitsCompressed;
    private BufferedImage bi2;

    private ImageReadParam verifyParam;

    public Compressor(Attributes dataset, String from) {
        super(dataset, from);

        Object pixeldata = dataset.getValue(Tag.PixelData, pixeldataVR);
        if (pixeldata == null)
            return;

        if (pixeldata instanceof BulkData) {
            this.pixeldata = (BulkData) pixeldata;
            if (pmi.isSubSampled())
                throw new UnsupportedOperationException("Unsupported Photometric Interpretation: " + pmi);
            if (this.pixeldata.length() < length)
                throw new IllegalArgumentException(
                        "Pixel data too short: " + this.pixeldata.length() + " instead " + length + " bytes");
        }
        embeddedOverlays = Overlays.getEmbeddedOverlayGroupOffsets(dataset);
    }

    public boolean compress(String tsuid, Property... params) throws IOException {

        if (tsuid == null)
            throw new NullPointerException("desttsuid");

        if (frames == 0)
            return false;

        ImageWriterFactory.ImageWriterParam param = ImageWriterFactory.getImageWriterParam(tsuid);
        if (param == null)
            throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);

        this.compressor = ImageWriterFactory.getImageWriter(param);
        Logger.debug("Compressor: {}", compressor.getClass().getName());
        this.patchJPEGLS = param.patchJPEGLS;

        this.compressParam = compressor.getDefaultWriteParam();
        for (Property property : cat(param.getImageWriteParams(), params)) {
            String name = property.getName();
            if (name.equals("maxPixelValueError"))
                this.maxPixelValueError = ((Number) property.getValue()).intValue();
            else if (name.equals("avgPixelValueBlockSize"))
                this.avgPixelValueBlockSize = ((Number) property.getValue()).intValue();
            else if (name.equals("bitsCompressed"))
                this.bitsCompressed = ((Number) property.getValue()).intValue();
            else {
                if (compressParam.getCompressionMode() != ImageWriteParam.MODE_EXPLICIT)
                    compressParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                property.setAt(compressParam);
            }
        }

        if (maxPixelValueError >= 0) {
            ImageReaderFactory.ImageReaderParam readerParam = ImageReaderFactory.getImageReaderParam(tsuid);
            if (readerParam == null)
                throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);

            this.verifier = ImageReaderFactory.getImageReader(readerParam);
            this.verifyParam = verifier.getDefaultReadParam();
            Logger.debug("Verifier: {}", verifier.getClass().getName());
        }

        TransferSyntaxType tstype = TransferSyntaxType.forUID(tsuid);
        if (decompressor == null || super.tstype == TransferSyntaxType.RLE)
            bi = createBufferedImage(Math.min(bitsStored, tstype.getMaxBitsStored()),
                    super.tstype == TransferSyntaxType.RLE || banded, signed && tstype.canEncodeSigned());
        Fragments compressedPixeldata = dataset.newFragments(Tag.PixelData, VR.OB, frames + 1);
        compressedPixeldata.add(Value.NULL);
        for (int i = 0; i < frames; i++) {
            CompressedFrame frame = new CompressedFrame(i);
            if (embeddedOverlays.length != 0)
                frame.compress();
            compressedPixeldata.add(frame);
        }
        if (samples > 1) {
            dataset.setString(Tag.PhotometricInterpretation, VR.CS, pmiAfterDecompression.compress(tsuid).toString());
            dataset.setInt(Tag.PlanarConfiguration, VR.US, tstype.getPlanarConfiguration());
        }
        for (int gg0000 : embeddedOverlays) {
            dataset.setInt(Tag.OverlayBitsAllocated | gg0000, VR.US, 1);
            dataset.setInt(Tag.OverlayBitPosition | gg0000, VR.US, 0);
        }
        return true;
    }

    private Property[] cat(Property[] a, Property[] b) {
        if (a.length == 0)
            return b;
        if (b.length == 0)
            return a;
        Property[] c = new Property[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public void close() {
        if (iis != null)
            try {
                iis.close();
            } catch (IOException ignore) {
            }
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (compressor != null)
            compressor.dispose();

        if (verifier != null)
            verifier.dispose();

        compressor = null;
        verifier = null;
    }

    public BufferedImage readFrame(int frameIndex) throws IOException {
        if (iis == null)
            iis = new FileImageInputStream(file);

        if (decompressor != null)
            return decompressFrame(iis, frameIndex);

        iis.setByteOrder(pixeldata.bigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        iis.seek(pixeldata.offset() + (long) frameLength * frameIndex);
        DataBuffer db = bi.getRaster().getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            byte[][] data = ((DataBufferByte) db).getBankData();
            for (byte[] bs : data)
                iis.readFully(bs);
            if (pixeldata.bigEndian() && pixeldataVR.vr == VR.OW)
                ByteKit.swapShorts(data);
            break;
        case DataBuffer.TYPE_USHORT:
            readFully(((DataBufferUShort) db).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            readFully(((DataBufferShort) db).getData());
            break;
        default:
            throw new UnsupportedOperationException("Unsupported Datatype: " + db.getDataType());
        }
        return bi;
    }

    private void verify(MemoryCacheImageOutputStream cache, int index) throws IOException {
        if (verifier == null)
            return;

        cache.seek(0);
        verifier.setInput(cache);
        verifyParam.setDestination(bi2);
        long start = System.currentTimeMillis();
        bi2 = verifier.read(0, verifyParam);
        int maxDiff = maxDiff(bi.getRaster(), bi2.getRaster());
        long end = System.currentTimeMillis();
        if (Logger.isDebugEnabled())
            Logger.debug("Verified compressed frame #{} in {} ms - max pixel value error: {}", index + 1, end - start,
                    maxDiff);
        if (maxDiff > maxPixelValueError)
            throw new InternalException(
                    "Decompressed pixel data differs up to " + maxDiff + " from original pixel data");

    }

    private int maxDiff(WritableRaster raster, WritableRaster raster2) {
        ComponentSampleModel csm = (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 = (ComponentSampleModel) raster2.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        DataBuffer db2 = raster2.getDataBuffer();
        int blockSize = avgPixelValueBlockSize;
        if (blockSize > 1) {
            int w = csm.getWidth();
            int h = csm.getHeight();
            int maxY = (h / blockSize - 1) * blockSize;
            int maxX = (w / blockSize - 1) * blockSize;
            int[] samples = new int[blockSize * blockSize];
            int diff, maxDiff = 0;
            for (int b = 0; b < csm.getNumBands(); b++)
                for (int y = 0; y < maxY; y += blockSize) {
                    for (int x = 0; x < maxX; x += blockSize) {
                        if (maxDiff < (diff = Math.abs(sum(csm.getSamples(x, y, blockSize, blockSize, b, samples, db))
                                - sum(csm2.getSamples(x, y, blockSize, blockSize, b, samples, db2)))))
                            maxDiff = diff;
                    }
                }
            return maxDiff / samples.length;
        }
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            return maxDiff(csm, ((DataBufferByte) db).getBankData(), csm2, ((DataBufferByte) db2).getBankData());
        case DataBuffer.TYPE_USHORT:
            return maxDiff(csm, ((DataBufferUShort) db).getData(), csm2, ((DataBufferUShort) db2).getData());
        case DataBuffer.TYPE_SHORT:
            return maxDiff(csm, ((DataBufferShort) db).getData(), csm2, ((DataBufferShort) db2).getData());
        default:
            throw new UnsupportedOperationException("Unsupported Datatype: " + db.getDataType());
        }
    }

    private int sum(int[] samples) {
        int sum = 0;
        for (int sample : samples)
            sum += sample;
        return sum;
    }

    private int maxDiff(ComponentSampleModel csm, short[] data, ComponentSampleModel csm2, short[] data2) {
        int w = csm.getWidth() * csm.getPixelStride();
        int h = csm.getHeight();
        int stride = csm.getScanlineStride();
        int stride2 = csm2.getScanlineStride();
        int diff, maxDiff = 0;
        for (int y = 0; y < h; y++) {
            for (int j = w, i = y * stride, i2 = y * stride2; j-- > 0; i++, i2++) {
                if (maxDiff < (diff = Math.abs(data[i] - data2[i2])))
                    maxDiff = diff;
            }
        }
        return maxDiff;
    }

    private int maxDiff(ComponentSampleModel csm, byte[][] banks, ComponentSampleModel csm2, byte[][] banks2) {
        int w = csm.getWidth();
        int h = csm.getHeight();
        int bands = csm.getNumBands();
        int stride = csm.getScanlineStride();
        int pixelStride = csm.getPixelStride();
        int[] bankIndices = csm.getBankIndices();
        int[] bandOffsets = csm.getBandOffsets();
        int stride2 = csm2.getScanlineStride();
        int pixelStride2 = csm2.getPixelStride();
        int[] bankIndices2 = csm2.getBankIndices();
        int[] bandOffsets2 = csm2.getBandOffsets();
        int diff, maxDiff = 0;
        for (int b = 0; b < bands; b++) {
            byte[] bank = banks[bankIndices[b]];
            byte[] bank2 = banks2[bankIndices2[b]];
            int off = bandOffsets[b];
            int off2 = bandOffsets2[b];
            for (int y = 0; y < h; y++) {
                for (int x = w, i = y * stride + off,
                        i2 = y * stride2 + off2; x-- > 0; i += pixelStride, i2 += pixelStride2) {
                    if (maxDiff < (diff = Math.abs(bank[i] - bank2[i2])))
                        maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }

    private void nullifyUnusedBits(int bitsStored, BufferedImage bi) {
        DataBuffer db = bi.getRaster().getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_USHORT:
            nullifyUnusedBits(bitsStored, ((DataBufferUShort) db).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            nullifyUnusedBits(bitsStored, ((DataBufferShort) db).getData());
            break;
        }
    }

    private void nullifyUnusedBits(int bitsStored, short[] data) {
        int mask = (1 << bitsStored) - 1;
        for (int i = 0; i < data.length; i++)
            data[i] &= mask;
    }

    private void extractEmbeddedOverlays(int frameIndex, BufferedImage bi) {
        for (int gg0000 : embeddedOverlays) {
            int ovlyRow = dataset.getInt(Tag.OverlayRows | gg0000, 0);
            int ovlyColumns = dataset.getInt(Tag.OverlayColumns | gg0000, 0);
            int ovlyBitPosition = dataset.getInt(Tag.OverlayBitPosition | gg0000, 0);
            int mask = 1 << ovlyBitPosition;
            int ovlyLength = ovlyRow * ovlyColumns;
            byte[] ovlyData = dataset.getSafeBytes(Tag.OverlayData | gg0000);
            if (ovlyData == null) {
                ovlyData = new byte[(((ovlyLength * frames + 7) >>> 3) + 1) & (~1)];
                dataset.setBytes(Tag.OverlayData | gg0000, VR.OB, ovlyData);
            }
            Overlays.extractFromPixeldata(bi.getRaster(), mask, ovlyData, ovlyLength * frameIndex, ovlyLength);
            Logger.debug("Extracted embedded overlay #{} from bit #{} of frame #{}", (gg0000 >>> 17) + 1,
                    ovlyBitPosition, frameIndex + 1);
        }
    }

    private void readFully(short[] data) throws IOException {
        iis.readFully(data, 0, data.length);
    }

    private static class CacheOutputStream extends FilterOutputStream {

        public CacheOutputStream() {
            super(null);
        }

        public void set(OutputStream out) {
            this.out = out;
        }
    }

    private static class FlushlessMemoryCacheImageOutputStream extends MemoryCacheImageOutputStream
            implements BytesWithImageImageDescriptor {

        private final ImageDescriptor imageDescriptor;

        public FlushlessMemoryCacheImageOutputStream(OutputStream stream, ImageDescriptor imageDescriptor) {
            super(stream);
            this.imageDescriptor = imageDescriptor;
        }

        @Override
        public void flush() throws IOException {
            // defer flush to writeTo()
            Logger.debug("Ignore invoke of MemoryCacheImageOutputStream.flush()");
        }

        @Override
        public ByteBuffer getBytes() throws IOException {
            byte[] array = new byte[8192];
            int length = 0;
            int read;
            while ((read = this.read(array, length, array.length - length)) > 0) {
                if ((length += read) == array.length)
                    array = Arrays.copyOf(array, array.length << 1);
            }
            return ByteBuffer.wrap(array, 0, length);
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            return imageDescriptor;
        }
    }

    private class CompressedFrame implements Value {

        private final int frameIndex;
        private final CacheOutputStream cacheout = new CacheOutputStream();
        private int streamLength;
        private MemoryCacheImageOutputStream cache;

        public CompressedFrame(int frameIndex) throws IOException {
            this.frameIndex = frameIndex;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeTo(out);
            return out.toByteArray();
        }

        @Override
        public void writeTo(ImageOutputStream out, VR vr) throws IOException {
            writeTo(out);
        }

        @Override
        public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            return getEncodedLength(encOpts, explicitVR, vr);
        }

        @Override
        public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
            try {
                compress();
            } catch (IOException e) {
                return -1;
            }
            return (streamLength + 1) & ~1;
        }

        private void writeTo(OutputStream out) throws IOException {
            compress();
            try {
                cacheout.set(out);
                long start = System.currentTimeMillis();
                cache.seek(streamLength);
                cache.flushBefore(streamLength);
                if ((streamLength & 1) != 0)
                    out.write(0);
                long end = System.currentTimeMillis();
                Logger.debug("Flushed frame #{} from memory in {} ms", frameIndex + 1, end - start);
            } finally {
                try {
                    cache.close();
                } catch (IOException ignore) {
                }
                cache = null;
            }
        }

        private void compress() throws IOException {
            if (cache != null)
                return;

            if (ex != null)
                throw ex;

            try {
                BufferedImage bi = Compressor.this.readFrame(frameIndex);
                Compressor.this.extractEmbeddedOverlays(frameIndex, bi);
                if (bitsStored < bitsAllocated)
                    Compressor.this.nullifyUnusedBits(bitsStored, bi);
                if (imageDescriptor.is16BitsAllocated8BitsStored()) {
                    bi = BufferedImages.convertShortsToBytes(bi, null);
                }
                cache = new FlushlessMemoryCacheImageOutputStream(cacheout, imageDescriptor);
                compressor.setOutput(patchJPEGLS != null ? new PatchJPEGLSOutputStream(cache, patchJPEGLS) : cache);
                long start = System.currentTimeMillis();
                compressor.write(null, new IIOImage(bi, null, null), compressParam);
                long end = System.currentTimeMillis();
                streamLength = (int) cache.getStreamPosition();
                if (Logger.isDebugEnabled())
                    Logger.debug("Compressed frame #{} {}:1 in {} ms", frameIndex + 1,
                            (float) sizeOf(bi) / streamLength, end - start);
                Compressor.this.verify(cache, frameIndex);
            } catch (IOException ex) {
                cache = null;
                Compressor.this.ex = ex;
                throw ex;
            }
        }

    }

}
