/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.exception.InstrumentException;
import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.http.OnBack;
import org.aoju.bus.http.Process;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.Results.Body;
import org.aoju.bus.http.accord.ProcessStream;
import org.aoju.bus.http.metric.Download;
import org.aoju.bus.http.metric.TaskExecutor;
import org.aoju.bus.http.metric.http.CoverHttp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResultBody extends AbstractBody implements Body {

    private final Response response;
    private boolean onIO = false;
    private OnBack<Process> onProcess;
    private long stepBytes = 0;
    private double stepRate = -1;
    private boolean rangeIgnored = false;
    private CoverHttp<?> coverHttp;
    private boolean cached = false;
    private byte[] data;

    public ResultBody(CoverHttp<?> coverHttp, Response response, TaskExecutor taskExecutor) {
        super(taskExecutor, coverHttp.charset(response));
        this.coverHttp = coverHttp;
        this.response = response;
    }


    @Override
    public MediaType getType() {
        ResponseBody body = response.body();
        if (null != body) {
            return body.contentType();
        }
        return null;
    }

    @Override
    public long getLength() {
        ResponseBody body = response.body();
        if (null != body) {
            return body.contentLength();
        }
        return 0;
    }

    @Override
    public Body nextOnIO() {
        onIO = true;
        return this;
    }

    @Override
    public Body setOnProcess(OnBack<Process> onProcess) {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        if (cached) {
            throw new IllegalStateException("After the cache is turned on, you cannot set a download progress callback!");
        }
        this.onProcess = onProcess;
        return this;
    }

    @Override
    public Body stepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return this;
    }

    @Override
    public Body stepRate(double stepRate) {
        this.stepRate = stepRate;
        return this;
    }

    @Override
    public Body setRangeIgnored() {
        this.rangeIgnored = true;
        return this;
    }

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
                stepBytes = Process.DEFAULT_STEP_BYTES;
            }
            return new ProcessStream(input, onProcess, totalBytes, stepBytes,
                    rangeIgnored ? 0 : rangeStart, taskExecutor.getExecutor(onIO));
        }
        return input;
    }

    @Override
    public byte[] toBytes() {
        if (cached) {
            return cacheBytes();
        }
        return bodyToBytes();
    }

    @Override
    public Reader toCharStream() {
        if (cached || null != onProcess) {
            return new InputStreamReader(toByteStream());
        }
        ResponseBody body = response.body();
        if (null != body) {
            return body.charStream();
        }
        return new CharArrayReader(new char[]{});
    }

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
            throw new InstrumentException("Error in converting the body of the message!", e);
        }
        return null;
    }

    @Override
    public ByteString toByteString() {
        return ByteString.of(toBytes());
    }

    @Override
    public Download toFile(String filePath) {
        return toFile(new File(filePath));
    }

    @Override
    public Download toFile(File file) {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                response.close();
                throw new InstrumentException("Cannot create file [" + file.getAbsolutePath() + "]", e);
            }
        }
        return taskExecutor.download(coverHttp, file, toByteStream(),
                getRangeStart());
    }

    @Override
    public Download toFolder(String dirPath) {
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

    @Override
    public Download toFolder(File dir) {
        if (dir.exists() && !dir.isDirectory()) {
            response.close();
            throw new InstrumentException("File download failed：[" + dir.getAbsolutePath() + "] Already exists and is not a directory !");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return toFolder(dir.getAbsolutePath());
    }

    @Override
    public Body cache() {
        if (null != onProcess) {
            throw new IllegalStateException("After the cache is turned on, you cannot set a download progress callback!");
        }
        cached = true;
        return this;
    }

    @Override
    public Body close() {
        response.close();
        data = null;
        return this;
    }

    private byte[] cacheBytes() {
        synchronized (response) {
            if (null == data) {
                data = bodyToBytes();
            }
        }
        return data;
    }

    private byte[] bodyToBytes() {
        if (null != onProcess) {
            try (Buffer buffer = new Buffer()) {
                return buffer.readFrom(toByteStream()).readByteArray();
            } catch (IOException e) {
                throw new InstrumentException("Error in converting byte array of message body!", e);
            } finally {
                response.close();
            }
        }
        ResponseBody body = response.body();
        if (null != body) {
            try {
                return body.bytes();
            } catch (IOException e) {
                throw new InstrumentException("Error in converting byte array of message body!", e);
            }
        }
        return new byte[0];
    }

    private long getRangeStart() {
        long rangeStart = 0;
        if (response.code() != HttpURLConnection.HTTP_PARTIAL) {
            return rangeStart;
        }
        String range = response.header(Header.CONTENT_RANGE);
        if (null != range && range.startsWith("bytes")) {
            int index = range.indexOf(Symbol.C_MINUS);
            if (index > 5) {
                String start = range.substring(5, index).trim();
                try {
                    rangeStart = Long.parseLong(start);
                } catch (Exception ignore) {
                }
            }
        }
        return rangeStart;
    }

    private String resolveFilePath(String dirPath, String fileName) {
        if (dirPath.endsWith(Symbol.BACKSLASH) || dirPath.endsWith(Symbol.SLASH)) {
            return dirPath + fileName;
        }
        return dirPath + Symbol.BACKSLASH + fileName;
    }

    private String indexFileName(String fileName, int index) {
        int i = fileName.lastIndexOf(Symbol.C_DOT);
        if (i < 0) {
            return fileName + Symbol.PARENTHESE_LEFT + index + ")";
        }
        String ext = fileName.substring(i);
        if (i > 0) {
            String name = fileName.substring(0, i);
            return name + Symbol.PARENTHESE_LEFT + index + ")" + ext;
        }
        return Symbol.PARENTHESE_LEFT + index + ")" + ext;
    }

    private String resolveFileName() {
        String fileName = response.header("Content-Disposition");
        // 通过Content-Disposition获取文件名
        if (null == fileName || fileName.length() < 1) {
            fileName = response.request().url().encodedPath();
            fileName = fileName.substring(fileName.lastIndexOf(Symbol.SLASH) + 1);
        } else {
            try {
                fileName = URLDecoder.decode(fileName.substring(
                        fileName.indexOf("filename=") + 9), Charset.DEFAULT_UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new InstrumentException("Failed to decode file name", e);
            }
            // 去掉文件名会被包含""，不然无法读取文件后缀
            fileName = fileName.replaceAll("\"", Normal.EMPTY);
        }
        return fileName;
    }

}
