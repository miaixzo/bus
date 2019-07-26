package org.aoju.bus.core.utils;

import java.io.*;
import java.net.URL;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class StreamUtils {

    private static final int COPY_BUFFER_SIZE = 2048;

    public static int readAvailable(InputStream in, byte[] b, int off, int len)
            throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0)
                break;
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    public static void readFully(InputStream in, byte[] b, int off, int len)
            throws IOException {
        if (readAvailable(in, b, off, len) < len)
            throw new EOFException();
    }

    public static void skipFully(InputStream in, long n) throws IOException {
        while (n > 0) {
            long count = in.skip(n);
            if (count == 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                count = 1;
            }
            n -= count;
        }
    }

    public static void copy(InputStream in, OutputStream out, byte[] buf)
            throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0)
            if (out != null)
                out.write(buf, 0, count);
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        copy(in, out, new byte[COPY_BUFFER_SIZE]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            byte[] buf) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int count = in.read(buf, 0, Math.min(len, buf.length));
            if (count < 0)
                throw new EOFException();
            out.write(buf, 0, count);
            len -= count;
        }
    }

    public static void copy(InputStream in, OutputStream out, int len)
            throws IOException {
        copy(in, out, len, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            int swapBytes, byte[] buf) throws IOException {
        if (swapBytes == 1) {
            copy(in, out, len, buf);
            return;
        }
        if (!(swapBytes == 2 || swapBytes == 4))
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        if (len < 0 || (len % swapBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
        int off = 0;
        while (len > 0) {
            int count = in.read(buf, off, Math.min(len, buf.length - off));
            if (count < 0)
                throw new EOFException();
            len -= count;
            count += off;
            off = count % swapBytes;
            count -= off;
            switch (swapBytes) {
                case 2:
                    ByteUtils.swapShorts(buf, 0, count);
                    break;
                case 4:
                    ByteUtils.swapInts(buf, 0, count);
                    break;
                case 8:
                    ByteUtils.swapLongs(buf, 0, count);
                    break;
            }
            out.write(buf, 0, count);
            if (off > 0)
                System.arraycopy(buf, count, buf, 0, off);
        }
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            int swapBytes) throws IOException {
        copy(in, out, len, swapBytes, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = ResourceUtils.getResource(name.substring(9), StreamUtils.class);
            if (url == null)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(':') < 2)
            return new FileInputStream(name);
        return new URL(name).openStream();
    }

}