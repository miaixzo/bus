package org.aoju.bus.extra.servlet.multipart;

import java.io.*;

/**
 * Http请求解析流，提供了专门针对带文件的form表单的解析<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MultipartRequestInputStream extends BufferedInputStream {

    /**
     * part部分边界
     */
    protected byte[] boundary;
    protected UploadFileHeader lastHeader;

    public MultipartRequestInputStream(InputStream in) {
        super(in);
    }

    /**
     * 读取byte字节流，在末尾抛出异常
     *
     * @return byte
     * @throws IOException
     */
    public byte readByte() throws IOException {
        int i = super.read();
        if (i == -1) {
            throw new IOException("End of HTTP request stream reached");
        }
        return (byte) i;
    }

    /**
     * 跳过指定位数的 bytes.
     *
     * @param i 跳过的byte数
     */
    public void skipBytes(int i) throws IOException {
        long len = super.skip(i);
        if (len != i) {
            throw new IOException("Unable to skip data in HTTP request");
        }
    }

    /**
     * 输入流中读取边界
     *
     * @return 边界
     * @throws IOException
     */
    public byte[] readBoundary() throws IOException {
        ByteArrayOutputStream boundaryOutput = new ByteArrayOutputStream(1024);
        byte b;
        while ((b = readByte()) <= ' ') {
        }
        boundaryOutput.write(b);

        while ((b = readByte()) != '\r') {
            boundaryOutput.write(b);
        }
        if (boundaryOutput.size() == 0) {
            throw new IOException("Problems with parsing request: invalid boundary");
        }
        skipBytes(1);
        boundary = new byte[boundaryOutput.size() + 2];
        System.arraycopy(boundaryOutput.toByteArray(), 0, boundary, 2, boundary.length - 2);
        boundary[0] = '\r';
        boundary[1] = '\n';
        return boundary;
    }

    public UploadFileHeader getLastHeader() {
        return lastHeader;
    }

    /**
     * 从流中读取文件头部信息， 如果达到末尾则返回null
     *
     * @param encoding 字符集
     * @return 头部信息， 如果达到末尾则返回null
     * @throws IOException
     */
    public UploadFileHeader readDataHeader(String encoding) throws IOException {
        String dataHeader = readDataHeaderString(encoding);
        if (dataHeader != null) {
            lastHeader = new UploadFileHeader(dataHeader);
        } else {
            lastHeader = null;
        }
        return lastHeader;
    }

    protected String readDataHeaderString(String encoding) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte b;
        while (true) {
            if ((b = readByte()) != '\r') {
                data.write(b);
                continue;
            }
            mark(4);
            skipBytes(1);
            int i = read();
            if (i == -1) {
                return null;
            }
            if (i == '\r') {
                reset();
                break;
            }
            reset();
            data.write(b);
        }
        skipBytes(3);
        return encoding == null ? data.toString() : data.toString(encoding);
    }

    /**
     * 全部字节流复制到out
     *
     * @param out 输出流
     * @return 复制的字节数
     * @throws IOException
     */
    public int copy(OutputStream out) throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
        }
        return count;
    }

    /**
     * 复制字节流到out， 大于maxBytes或者文件末尾停止
     *
     * @param out   输出流
     * @param limit 最大字节数
     * @return 复制的字节数
     * @throws IOException
     */
    public int copy(OutputStream out, int limit) throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
            if (count > limit) {
                break;
            }
        }
        return count;
    }

    /**
     * 跳过边界表示
     *
     * @return 跳过的字节数
     */
    public int skipToBoundary() throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            count++;
            if (isBoundary(b)) {
                break;
            }
        }
        return count;
    }

    /**
     * @param b byte
     * @return 是否为边界的标志
     * @throws IOException
     */
    public boolean isBoundary(byte b) throws IOException {
        int boundaryLen = boundary.length;
        mark(boundaryLen + 1);
        int bpos = 0;
        while (b == boundary[bpos]) {
            b = readByte();
            bpos++;
            if (bpos == boundaryLen) {
                return true;
            }
        }
        reset();
        return false;
    }

}
