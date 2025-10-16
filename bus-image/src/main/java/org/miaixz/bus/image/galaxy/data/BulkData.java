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
 * Represents a DICOM Bulk Data object, typically used for storing large binary data such as pixel data. This class
 * implements the {@link Value} interface and can reference external files via URI, or contain offset and length
 * information to locate specific data segments within a file.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class BulkData implements Value, Serializable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852261350109L;

    /**
     * Magic length constant.
     */
    public static final int MAGIC_LEN = 0xfbfb;

    /**
     * The UUID identifier for the bulk data.
     */
    private String uuid;

    /**
     * The URI reference to the bulk data.
     */
    private String uri;

    /**
     * The end index of the URI path, before any query parameters.
     */
    private int uriPathEnd;

    /**
     * Indicates if the bulk data is big-endian.
     */
    private boolean bigEndian;

    /**
     * The offset of the data within the referenced resource.
     */
    private long offset = 0;

    /**
     * The length of the data within the referenced resource.
     */
    private long length = -1;

    /**
     * Constructs a {@code BulkData} object with a UUID, URI, and endianness.
     * 
     * @param uuid      The UUID identifier.
     * @param uri       The URI reference to the bulk data.
     * @param bigEndian {@code true} if the data is big-endian, {@code false} otherwise.
     */
    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    /**
     * Constructs a {@code BulkData} object with a URI, offset, length, and endianness.
     * 
     * @param uri       The URI reference to the bulk data.
     * @param offset    The offset of the data within the resource.
     * @param length    The length of the data within the resource.
     * @param bigEndian {@code true} if the data is big-endian, {@code false} otherwise.
     */
    public BulkData(String uri, long offset, long length, boolean bigEndian) {
        this.uuid = null;
        this.uriPathEnd = uri.length();
        this.uri = uri + "?offset=" + offset + "&length=" + length;
        this.offset = offset;
        this.length = length;
        this.bigEndian = bigEndian;
    }

    /**
     * Returns the UUID identifier of the bulk data.
     * 
     * @return The UUID identifier.
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Returns the URI reference to the bulk data.
     * 
     * @return The URI reference.
     */
    public String getURI() {
        return uri;
    }

    /**
     * Sets the URI reference for the bulk data. This method also parses any offset and length parameters from the URI
     * query string.
     * 
     * @param uri The URI reference.
     */
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

    /**
     * Checks if the bulk data is big-endian.
     * 
     * @return {@code true} if big-endian, {@code false} otherwise.
     */
    public boolean bigEndian() {
        return bigEndian;
    }

    /**
     * Returns the length of the bulk data as an integer. Note that this may truncate if the length exceeds
     * {@code Integer.MAX_VALUE}.
     * 
     * @return The length of the data.
     */
    public int length() {
        return (int) length;
    }

    /**
     * Returns the offset of the bulk data within its resource.
     * 
     * @return The offset.
     */
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

    /**
     * Returns the {@link File} object referenced by the URI, if it's a file URI.
     * 
     * @return The {@link File} object.
     * @throws IllegalStateException if the URI is invalid or not a file URI.
     */
    public File getFile() {
        try {
            return new File(new URI(uriWithoutQuery()));
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + uri);
        }
    }

    /**
     * Returns the URI without any query parameters.
     * 
     * @return The URI string without query parameters.
     * @throws IllegalStateException if the URI is {@code null}.
     */
    public String uriWithoutQuery() {
        if (uri == null)
            throw new IllegalStateException("uri: null");
        return uri.substring(0, uriPathEnd);
    }

    /**
     * Opens an {@link InputStream} to the bulk data. Handles both file and URL URIs.
     * 
     * @return An {@link InputStream} for reading the bulk data.
     * @throws IOException           if an I/O error occurs.
     * @throws IllegalStateException if the URI is {@code null}.
     */
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

    /**
     * Custom serialization method to write the object's state.
     * 
     * @param oos The {@link ObjectOutputStream} to write to.
     * @throws IOException if an I/O error occurs during serialization.
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(Builder.maskNull(uuid, ""));
        oos.writeUTF(Builder.maskNull(uri, ""));
        oos.writeBoolean(bigEndian);
    }

    /**
     * Custom deserialization method to read the object's state.
     * 
     * @param ois The {@link ObjectInputStream} to read from.
     * @throws IOException            if an I/O error occurs during deserialization.
     * @throws ClassNotFoundException if the class of a serialized object cannot be found.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uuid = Builder.maskEmpty(ois.readUTF(), null);
        setURI(Builder.maskEmpty(ois.readUTF(), null));
        bigEndian = ois.readBoolean();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        BulkData other = (BulkData) object;
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
     * Returns the end position of the data segment, calculated as offset + length.
     * 
     * @return The end position of the segment, or -1 if length is -1.
     */
    public long getSegmentEnd() {
        if (length == -1)
            return -1;
        return offset() + (length & 0xFFFFFFFFL);
    }

    /**
     * Returns the actual length of the bulk data as a long, which can represent lengths beyond
     * {@code Integer.MAX_VALUE}.
     * 
     * @return The length of the data.
     */
    public long longLength() {
        return length;
    }

    /**
     * Sets the offset of the bulk data within its resource. This also updates the URI string.
     * 
     * @param offset The new offset.
     */
    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + offset + "&length=" + length;
    }

    /**
     * Sets the length of the bulk data. This also updates the URI string.
     * 
     * @param length The new length. Must be between -1 and 2^32-2 (inclusive).
     * @throws IllegalArgumentException if the length is out of the valid range.
     */
    public void setLength(long length) {
        if (length < -1 || length > 0xFFFFFFFEL) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-2 but was " + length);
        }
        this.length = length;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + this.offset + "&length=" + length;
    }

    /**
     * Functional interface for creating {@code BulkData} objects.
     */
    @FunctionalInterface
    public interface Creator {

        /**
         * Creates a {@code BulkData} object.
         * 
         * @param uuid      The UUID identifier.
         * @param uri       The URI reference.
         * @param bigEndian {@code true} if big-endian, {@code false} otherwise.
         * @return A new {@code BulkData} instance.
         */
        BulkData create(String uuid, String uri, boolean bigEndian);
    }

}
