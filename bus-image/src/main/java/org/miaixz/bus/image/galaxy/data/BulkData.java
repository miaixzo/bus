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
 * 大数据类，用于处理DICOM中的大数据对象。 该类实现了Value接口，表示DICOM属性的大数据值，通常用于存储像素数据或其他大型二进制数据。
 * 大数据对象可以通过URI引用外部文件，也可以包含偏移量和长度信息来定位文件中的特定数据段。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BulkData implements Value, Serializable {

    @Serial
    private static final long serialVersionUID = 2852261350109L;

    /**
     * 魔数长度
     */
    public static final int MAGIC_LEN = 0xfbfb;

    /**
     * UUID标识符
     */
    private String uuid;

    /**
     * URI引用
     */
    private String uri;

    /**
     * URI路径结束位置
     */
    private int uriPathEnd;

    /**
     * 是否大端序
     */
    private boolean bigEndian;

    /**
     * 数据偏移量
     */
    private long offset = 0;

    /**
     * 数据长度
     */
    private long length = -1;

    /**
     * 使用UUID、URI和字节序构造大数据对象
     *
     * @param uuid      UUID标识符
     * @param uri       URI引用
     * @param bigEndian 是否大端序
     */
    public BulkData(String uuid, String uri, boolean bigEndian) {
        this.uuid = uuid;
        setURI(uri);
        this.bigEndian = bigEndian;
    }

    /**
     * 使用URI、偏移量、长度和字节序构造大数据对象
     *
     * @param uri       URI引用
     * @param offset    数据偏移量
     * @param length    数据长度
     * @param bigEndian 是否大端序
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
     * 获取UUID标识符
     *
     * @return UUID标识符
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * 获取URI引用
     *
     * @return URI引用
     */
    public String getURI() {
        return uri;
    }

    /**
     * 设置URI引用
     *
     * @param uri URI引用
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
     * 检查是否为大端序
     *
     * @return 如果是大端序则返回true，否则返回false
     */
    public boolean bigEndian() {
        return bigEndian;
    }

    /**
     * 获取数据长度
     *
     * @return 数据长度
     */
    public int length() {
        return (int) length;
    }

    /**
     * 获取数据偏移量
     *
     * @return 数据偏移量
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
     * 获取文件对象
     *
     * @return 文件对象
     * @throws IllegalStateException 如果URI无效
     */
    public File getFile() {
        try {
            return new File(new URI(uriWithoutQuery()));
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IllegalStateException("uri: " + uri);
        }
    }

    /**
     * 获取不带查询参数的URI
     *
     * @return 不带查询参数的URI
     * @throws IllegalStateException 如果URI为null
     */
    public String uriWithoutQuery() {
        if (uri == null)
            throw new IllegalStateException("uri: null");
        return uri.substring(0, uriPathEnd);
    }

    /**
     * 打开输入流
     *
     * @return 输入流
     * @throws IOException           如果发生I/O错误
     * @throws IllegalStateException 如果URI为null
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
     * 序列化写入对象
     *
     * @param oos 对象输出流
     * @throws IOException 如果发生I/O错误
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(Builder.maskNull(uuid, ""));
        oos.writeUTF(Builder.maskNull(uri, ""));
        oos.writeBoolean(bigEndian);
    }

    /**
     * 序列化读取对象
     *
     * @param ois 对象输入流
     * @throws IOException            如果发生I/O错误
     * @throws ClassNotFoundException 如果类未找到
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
     * 返回段结束后的索引
     *
     * @return 段结束后的索引
     */
    public long getSegmentEnd() {
        if (length == -1)
            return -1;
        return offset() + (length & 0xFFFFFFFFL);
    }

    /**
     * 获取实际长度，以long类型表示，可以表示2GB到4GB范围的长度
     *
     * @return 实际长度
     */
    public long longLength() {
        return length;
    }

    /**
     * 设置偏移量
     *
     * @param offset 偏移量
     */
    public void setOffset(long offset) {
        this.offset = offset;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + offset + "&length=" + length;
    }

    /**
     * 设置长度
     *
     * @param length 长度
     * @throws IllegalArgumentException 如果长度超出范围
     */
    public void setLength(long length) {
        if (length < -1 || length > 0xFFFFFFFEL) {
            throw new IllegalArgumentException("BulkData length limited to -1..2^32-2 but was " + length);
        }
        this.length = length;
        this.uri = this.uri.substring(0, this.uriPathEnd) + "?offset=" + this.offset + "&length=" + length;
    }

    /**
     * 创建者函数式接口，用于创建BulkData对象
     */
    @FunctionalInterface
    public interface Creator {
        /**
         * 创建BulkData对象
         *
         * @param uuid      UUID标识符
         * @param uri       URI引用
         * @param bigEndian 是否大端序
         * @return BulkData对象
         */
        BulkData create(String uuid, String uri, boolean bigEndian);
    }

}