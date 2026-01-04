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
package org.miaixz.bus.http.plugin.httpv;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.ResponseBody;

/**
 * An implementation of {@link CoverResult.Body} that wraps an HTTP {@link Response}. It provides a rich API for
 * consuming the response body in various formats (e.g., String, byte array, deserialized objects) and supports download
 * progress monitoring.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResultBody implements CoverResult.Body {

    /**
     * The underlying Httpd response.
     */
    private final Response response;
    /**
     * The executor for handling callbacks and data conversions.
     */
    protected CoverTasks.Executor executor;
    /**
     * The character set of the response body.
     */
    protected java.nio.charset.Charset charset;
    /**
     * Flag to determine if the next callback should be executed on an I/O thread.
     */
    private boolean onIO = false;
    /**
     * The callback for download progress updates.
     */
    private Callback<Progress> onProcess;
    /**
     * The progress update interval in bytes.
     */
    private long stepBytes = 0;
    /**
     * The progress update interval as a percentage of the total size (0.0 to 1.0).
     */
    private double stepRate = -1;
    /**
     * Flag to ignore the HTTP Range header when calculating total download size.
     */
    private boolean rangeIgnored = false;
    /**
     * The original HTTP task that produced this result.
     */
    private CoverHttp<?> coverHttp;
    /**
     * Flag indicating if the response body should be cached in memory after the first read.
     */
    private boolean cached = false;
    /**
     * The byte array used to cache the response body.
     */
    private byte[] data;

    /**
     * Constructs a new ResultBody.
     *
     * @param coverHttp The HTTP task that received the response.
     * @param response  The raw HTTP response.
     * @param executor  The task executor.
     */
    public ResultBody(CoverHttp<?> coverHttp, Response response, CoverTasks.Executor executor) {
        this.executor = executor;
        this.charset = coverHttp.charset(response);
        this.coverHttp = coverHttp;
        this.response = response;
    }

    /**
     * Converts the response body to a generic map/object wrapper.
     *
     * @return The wrapped object.
     */
    @Override
    public CoverWapper toWapper() {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return executor.doMsgConvert((Convertor c) -> c.toMapper(toByteStream(), charset));
    }

    /**
     * Converts the response body to an array.
     *
     * @return The array.
     */
    @Override
    public CoverArray toArray() {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return executor.doMsgConvert((Convertor c) -> c.toArray(toByteStream(), charset));
    }

    /**
     * Converts the response body to a bean of the specified type.
     *
     * @param <T>  The type of the bean.
     * @param type The class of the bean.
     * @return The bean instance.
     */
    @Override
    public <T> T toBean(Class<T> type) {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return executor.doMsgConvert((Convertor c) -> c.toBean(type, toByteStream(), charset));
    }

    /**
     * Converts the response body to a list of beans of the specified type.
     *
     * @param <T>  The type of the list items.
     * @param type The class of the list items.
     * @return The list of beans.
     */
    @Override
    public <T> List<T> toList(Class<T> type) {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return executor.doMsgConvert((Convertor c) -> c.toList(type, toByteStream(), charset));
    }

    /**
     * Returns the media type of the response body.
     *
     * @return The media type, or null if unknown.
     */
    @Override
    public MediaType getType() {
        ResponseBody body = response.body();
        if (null != body) {
            return body.contentType();
        }
        return null;
    }

    /**
     * Returns the length of the response body.
     *
     * @return The content length in bytes.
     */
    @Override
    public long getLength() {
        ResponseBody body = response.body();
        if (null != body) {
            return body.contentLength();
        }
        return 0;
    }

    /**
     * Specifies that the next callback should be executed on an I/O thread.
     *
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body nextOnIO() {
        onIO = true;
        return this;
    }

    /**
     * Sets a callback for download progress updates.
     *
     * @param onProcess The progress callback.
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body setOnProcess(Callback<Progress> onProcess) {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        if (cached) {
            throw new IllegalStateException(
                    "After the cache is turned on, you cannot set a download progress callback!");
        }
        this.onProcess = onProcess;
        return this;
    }

    /**
     * Sets the progress update interval in bytes.
     *
     * @param stepBytes The step size in bytes.
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body stepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return this;
    }

    /**
     * Sets the progress update interval as a percentage of the total size.
     *
     * @param stepRate The step rate (0.0 to 1.0).
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body stepRate(double stepRate) {
        this.stepRate = stepRate;
        return this;
    }

    /**
     * Ignores the HTTP Range header when calculating the total download size.
     *
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body setRangeIgnored() {
        this.rangeIgnored = true;
        return this;
    }

    /**
     * Returns the response body as an input stream.
     *
     * @return The input stream.
     */
    @Override
    public InputStream toByteStream() {
        InputStream input;
        if (cached) {
            input = new ByteArrayInputStream(cacheBytes());
        } else {
            ResponseBody body = response.body();
            if (null != body) {
                input = body.byteStream();
            } else {
                input = new ByteArrayInputStream(new byte[0]);
            }
        }
        if (null != onProcess) {
            long rangeStart = getRangeStart();
            long totalBytes = getLength();
            if (!rangeIgnored) {
                totalBytes += rangeStart;
            }
            if (stepRate > 0 && stepRate <= 1) {
                stepBytes = (long) (totalBytes * stepRate);
            }
            if (stepBytes <= 0) {
                stepBytes = Progress.DEFAULT_STEP_BYTES;
            }
            return new ProgressStream(input, onProcess, totalBytes, stepBytes, rangeIgnored ? 0 : rangeStart,
                    executor.getExecutor(onIO));
        }
        return input;
    }

    /**
     * Returns the response body as a byte array.
     *
     * @return The byte array.
     */
    @Override
    public byte[] toBytes() {
        if (cached) {
            return cacheBytes();
        }
        return bodyToBytes();
    }

    /**
     * Returns the response body as a character stream.
     *
     * @return The reader.
     */
    @Override
    public Reader toCharStream() {
        if (cached || null != onProcess) {
            return new InputStreamReader(toByteStream());
        }
        ResponseBody body = response.body();
        if (null != body) {
            return body.charStream();
        }
        return new CharArrayReader(new char[] {});
    }

    /**
     * Returns the response body as a string.
     *
     * @return The string.
     */
    @Override
    public String toString() {
        if (cached || null != onProcess) {
            return new String(toBytes(), charset);
        }
        try {
            ResponseBody body = response.body();
            if (null != body) {
                return new String(body.bytes(), charset);
            }
        } catch (IOException e) {
            throw new InternalException("Error in converting the body of the message!", e);
        }
        return null;
    }

    /**
     * Returns the response body as a ByteString.
     *
     * @return The ByteString.
     */
    @Override
    public ByteString toByteString() {
        return ByteString.of(toBytes());
    }

    /**
     * Downloads the response body to a file.
     *
     * @param filePath The file path.
     * @return A Downloads object for tracking the download.
     */
    @Override
    public Downloads toFile(String filePath) {
        return toFile(new File(filePath));
    }

    /**
     * Downloads the response body to a file.
     *
     * @param file The file to download to.
     * @return A Downloads object for tracking the download.
     */
    @Override
    public Downloads toFile(File file) {
        if (null == executor) {
            throw new IllegalStateException("Task executor is null!");
        }
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                response.close();
                throw new InternalException("Cannot create file [" + file.getAbsolutePath() + "]", e);
            }
        }
        return executor.download(coverHttp, file, toByteStream(), getRangeStart());
    }

    /**
     * Downloads the response body to a folder, using the filename from the response.
     *
     * @param dirPath The directory path.
     * @return A Downloads object for tracking the download.
     */
    @Override
    public Downloads toFolder(String dirPath) {
        String fileName = resolveFileName();
        String filePath = resolveFilePath(dirPath, fileName);
        int index = 0;
        File file = new File(filePath);
        while (file.exists()) {
            String indexFileName = indexFileName(fileName, index++);
            filePath = resolveFilePath(dirPath, indexFileName);
            file = new File(filePath);
        }
        return toFile(file);
    }

    /**
     * Downloads the response body to a folder, using the filename from the response.
     *
     * @param dir The directory.
     * @return A Downloads object for tracking the download.
     */
    @Override
    public Downloads toFolder(File dir) {
        if (dir.exists() && !dir.isDirectory()) {
            response.close();
            throw new InternalException(
                    "File download failed：[" + dir.getAbsolutePath() + "] Already exists and is not a directory !");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return toFolder(dir.getAbsolutePath());
    }

    /**
     * Enables caching of the response body in memory after the first read.
     *
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body cache() {
        if (null != onProcess) {
            throw new IllegalStateException("Cannot set a download progress callback after enabling cache!");
        }
        cached = true;
        return this;
    }

    /**
     * Closes the response and releases any resources.
     *
     * @return This body instance for chaining.
     */
    @Override
    public CoverResult.Body close() {
        response.close();
        data = null;
        return this;
    }

    /**
     * Reads the entire response body into a byte array and caches it. Subsequent calls will return the cached data.
     *
     * @return The cached byte array of the response body.
     */
    private byte[] cacheBytes() {
        synchronized (response) {
            if (null == data) {
                data = bodyToBytes();
            }
        }
        return data;
    }

    /**
     * Reads the response body into a byte array. This method consumes the body.
     *
     * @return The byte array of the response body.
     */
    private byte[] bodyToBytes() {
        if (null != onProcess) {
            try (Buffer buffer = new Buffer()) {
                return buffer.readFrom(toByteStream()).readByteArray();
            } catch (IOException e) {
                throw new InternalException("Error in converting byte array of message body!", e);
            } finally {
                response.close();
            }
        }
        ResponseBody body = response.body();
        if (null != body) {
            try {
                return body.bytes();
            } catch (IOException e) {
                throw new InternalException("Error in converting byte array of message body!", e);
            }
        }
        return new byte[0];
    }

    /**
     * Parses the 'Content-Range' header to determine the starting byte of a partial response.
     *
     * @return The starting byte offset, or 0 if not a partial response.
     */
    private long getRangeStart() {
        long rangeStart = 0;
        if (response.code() != HttpURLConnection.HTTP_PARTIAL) {
            return rangeStart;
        }
        String range = response.header(HTTP.CONTENT_RANGE);
        if (null != range && range.startsWith("bytes")) {
            int index = range.indexOf(Symbol.C_MINUS);
            if (index > 5) {
                String start = range.substring(5, index).trim();
                try {
                    rangeStart = Long.parseLong(start);
                } catch (Exception ignore) {
                    // Ignore parsing errors
                }
            }
        }
        return rangeStart;
    }

    /**
     * Joins a directory path and a file name into a full file path.
     *
     * @param dirPath  The directory path.
     * @param fileName The file name.
     * @return The resolved full file path.
     */
    private String resolveFilePath(String dirPath, String fileName) {
        if (dirPath.endsWith(Symbol.BACKSLASH) || dirPath.endsWith(Symbol.SLASH)) {
            return dirPath + fileName;
        }
        return dirPath + File.separator + fileName;
    }

    /**
     * Appends an index to a filename to avoid conflicts, e.g., "file.txt" -> "file(1).txt".
     *
     * @param fileName The original file name.
     * @param index    The index to append.
     * @return The new, indexed file name.
     */
    private String indexFileName(String fileName, int index) {
        int i = fileName.lastIndexOf(Symbol.C_DOT);
        if (i < 0) {
            return fileName + Symbol.PARENTHESE_LEFT + index + Symbol.PARENTHESE_RIGHT;
        }
        String ext = fileName.substring(i);
        if (i > 0) {
            String name = fileName.substring(0, i);
            return name + Symbol.PARENTHESE_LEFT + index + Symbol.PARENTHESE_RIGHT + ext;
        }
        return Symbol.PARENTHESE_LEFT + index + Symbol.PARENTHESE_RIGHT + ext;
    }

    /**
     * Resolves the download filename from the 'Content-Disposition' header or the request URL.
     *
     * @return The resolved filename.
     */
    private String resolveFileName() {
        String fileName = response.header("Content-Disposition");
        // Try to get filename from Content-Disposition
        if (null == fileName || fileName.length() < 1) {
            fileName = response.request().url().encodedPath();
            fileName = fileName.substring(fileName.lastIndexOf(Symbol.SLASH) + 1);
        } else {
            try {
                // Example: attachment; filename="filename.jpg"
                fileName = URLDecoder
                        .decode(fileName.substring(fileName.indexOf("filename=") + 9), Charset.DEFAULT_UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new InternalException("Failed to decode file name", e);
            }
            // The filename might be enclosed in quotes
            fileName = fileName.replaceAll("\"", Normal.EMPTY);
        }
        return fileName;
    }

}
