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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class BulkData implements Value, Serializable {

    public static final int MAGIC_LEN = 0xfbfb;
    private static final long serialVersionUID = -1L;
    private String uuid;
    private String uri;
    private int uriPathEnd;
    private boolean bigEndian;
    private long offset = 0;
    private long length = -1;

    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    public BulkData(String uri, long offset, long length, boolean bigEndian) {
        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = uri + "?offset=" + offset + "&length=" + length;
        this.offset = offset;
        this.length = length;
        this.bigEndian = bigEndian;
    }

    public String getUUID() {
        return uuid;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
        this.offset = 0;
        this.length = -1;
        this.uriPathEnd = 0;
        if (uri == null)
            return;

        int pathEnd = uri.indexOf('?');
        if (pathEnd < 0) {
            this.uriPathEnd = uri.length();
            return;
        }

        this.uriPathEnd = pathEnd;
        for (String qparam : Builder.split(uri.substring(pathEnd + 1), '&')) {
            try {
                if (qparam.startsWith("offset=")) {
                    this.offset = Long.parseLong(qparam.substring(7));
                } else if (qparam.startsWith("length=")) {
                    this.length = Long.parseLong(qparam.substring(7));
                }
            } catch (NumberFormatException ignore) {
            }
        }
    }

    public boolean bigEndian() {
        return bigEndian;
    }

    public int length() {
        return (int) length;
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
        return "BulkData[uuid=" + uuid + ", uri=" + uri + ", bigEndian=" + bigEndian + "]";
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
        if (uri == null)
            throw new IllegalStateException("uri: null");

        return uri.substring(0, uriPathEnd);
    }

    public InputStream openStream() throws IOException {
        if (uri == null)
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

        return (int) (length + 1) & ~1;
    }

    @Override
    public int getEncodedLength(ImageEncodingOptions encOpts, boolean explicitVR, VR vr) {
        return (int) ((length == -1) ? -1 : ((length + 1) & ~1));
    }

    @Override
    public byte[] toBytes(VR vr, boolean bigEndian) throws IOException {
        int intLength = (int) length;
        if (intLength < 0)
            throw new UnsupportedOperationException();

        if (intLength == 0)
            return new byte[] {};

        InputStream in = openStream();
        try {
            byte[] b = new byte[intLength];
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

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(Builder.maskNull(uuid, ""));
        oos.writeUTF(Builder.maskNull(uri, ""));
        oos.writeBoolean(bigEndian);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uuid = Builder.maskEmpty(ois.readUTF(), null);
        setURI(Builder.maskEmpty(ois.readUTF(), null));
        bigEndian = ois.readBoolean();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BulkData other = (BulkData) obj;
        if (bigEndian != other.bigEndian)
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        if (uuid == null) {
            return other.uuid == null;
        } else
            return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (bigEndian ? 1231 : 1237);
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    /**
     * Returns the index after the segment ends
     */
    public long getSegmentEnd() {
        if (length == -1)
            return -1;
        return offset() + (length & 0xFFFFFFFFL);
    }

    /**
     * Gets the actual length as a long so it can represent the 2 gb to 4 gb range of lengths
     */
    public long longLength() {
        return length;
    }

    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + offset + "&length=" + length;
    }

    public void setLength(long length) {
        if (length < -1 || length > 0xFFFFFFFEL) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-2 but was " + length);
        }
        this.length = length;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + this.offset + "&length=" + length;
    }

    @FunctionalInterface
    public interface Creator {
        BulkData create(String uuid, String uri, boolean bigEndian);
    }

}
