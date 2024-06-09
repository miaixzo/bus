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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.galaxy.Property;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BulkData implements Value {

    private final String uuid;
    private final boolean bigEndian;
    private String uri;
    private int uriPathEnd;
    private long offset = 0;
    private int length = -1;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    public BulkData(String uri, long offset, int length, boolean bigEndian) {
        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = uri + "?offset=" + offset + "&length=" + length;
        this.offset = offset;
        this.length = length;
        this.bigEndian = bigEndian;
    }

    public static Value deserializeFrom(ObjectInputStream ois)
            throws IOException {
        return new BulkData(
                Property.maskEmpty(ois.readUTF(), null),
                Property.maskEmpty(ois.readUTF(), null),
                ois.readBoolean());
    }

    public String getUUID() {
        return uuid;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
        this.uriPathEnd = uri.length();
        this.offset = 0;
        this.length = -1;
        int pathEnd = uri.indexOf(Symbol.C_QUESTION_MARK);
        if (pathEnd < 0)
            return;

        this.uriPathEnd = pathEnd;
        if (!uri.startsWith("?offset=", pathEnd))
            return;

        int offsetEnd = uri.indexOf("&length=", pathEnd + 8);
        if (offsetEnd < 0)
            return;

        try {
            this.offset = Integer.parseInt(uri.substring(pathEnd + 8, offsetEnd));
            this.length = Integer.parseInt(uri.substring(offsetEnd + 8));
        } catch (NumberFormatException ignore) {
        }
    }

    public boolean bigEndian() {
        return bigEndian;
    }

    public int length() {
        return length;
    }

    public long offset() {
        return offset;
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return "BulkData[uuid=" + uuid
                + ", uri=" + uri
                + ", bigEndian=" + bigEndian
                + "]";
    }

    public File getFile() {
        try {
            return new File(new URI(uriWithoutOffsetAndLength()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("uri: " + uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + uri);
        }
    }

    public String uriWithoutOffsetAndLength() {
        if (null == uri)
            throw new IllegalStateException("uri: null");

        return uri.substring(0, uriPathEnd);
    }

    public InputStream openStream() throws IOException {
        if (null == uri)
            throw new IllegalStateException("uri: null");

        if (!uri.startsWith("file:"))
            return new URL(uri).openStream();

        InputStream in = new FileInputStream(getFile());
        StreamKit.skipFully(in, offset);
        return in;

    }

    @Override
    public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        if (length == -1)
            throw new UnsupportedOperationException();

        return (length + 1) & ~1;
    }

    @Override
    public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return (length == -1) ? -1 : ((length + 1) & ~1);
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        if (length == -1)
            throw new UnsupportedOperationException();

        if (length == 0)
            return new byte[]{};

        InputStream in = openStream();
        try {
            byte[] b = new byte[length];
            StreamKit.readFully(in, b, 0, b.length);
            if (this.bigEndian != bigEndian) {
                vr.toggleEndian(b, false);
            }
            return b;
        } finally {
            in.close();
        }

    }

    @Override
    public void writeTo(ImageOutputStream out, VR vr) throws IOException {
        InputStream in = openStream();
        try {
            if (this.bigEndian != out.isBigEndian())
                IoKit.copy(in, out, length, vr.numEndianBytes());
            else
                IoKit.copy(in, out, length);
            if ((length & 1) != 0)
                out.write(vr.paddingByte());
        } finally {
            in.close();
        }
    }

    public void serializeTo(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(Property.maskNull(uuid, Normal.EMPTY));
        oos.writeUTF(Property.maskNull(uri, Normal.EMPTY));
        oos.writeBoolean(bigEndian);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (null == object)
            return false;
        if (getClass() != object.getClass())
            return false;
        BulkData other = (BulkData) object;
        if (bigEndian != other.bigEndian)
            return false;
        if (null == uri) {
            if (null != other.uri)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        if (null == uuid) {
            return null == other.uuid;
        } else return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (bigEndian ? 1231 : 1237);
        result = prime * result + ((null == uri) ? 0 : uri.hashCode());
        result = prime * result + ((null == uuid) ? 0 : uuid.hashCode());
        return result;
    }

    public long getSegmentEnd() {
        if (length == -1) return -1;
        return offset() + longLength();
    }

    public long longLength() {
        if (length == -1) return -1;
        return length & 0xFFFFFFFFl;
    }

    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + offset + "&length=" + this.length;
    }

    public void setLength(long longLength) {
        if (longLength < -1 || longLength > 0xFFFFFFF0l) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-16 but was " + longLength);
        }
        this.length = (int) longLength;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + this.offset + "&length=" + this.length;
    }

}
