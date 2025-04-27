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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Httpv;
import org.miaixz.bus.http.Response;

/**
 * 执行结果
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverResult {

    /**
     * 构造一个 Results 此方法构造的 Results 不可设置进度回调，不可进行下载操作！ 若需要，请使用方法： {@link #of(Response, CoverTasks.Executor)}
     *
     * @param response Response
     * @return Results
     */
    static CoverResult of(Response response) {
        return of(response, null);
    }

    /**
     * 构造一个 Results
     *
     * @param response Response
     * @param executor 任务执行器, 可通过方法 {@link Httpv#executor()} 获得
     * @return Results
     */
    static CoverResult of(Response response, CoverTasks.Executor executor) {
        if (null != response) {
            return new Real(null, response, executor);
        }
        throw new IllegalArgumentException("Response cannot be empty!");
    }

    /**
     * @return 执行状态
     */
    State getState();

    /**
     * @return HTTP状态码
     */
    int getStatus();

    /**
     * @return 是否响应成功，状态码在 [200..300) 之间
     */
    boolean isSuccessful();

    /**
     * @return 响应头
     */
    Headers getHeaders();

    /**
     * @param name 头名称
     * @return 响应头
     */
    List<String> getHeaders(String name);

    /**
     * @param name 头名称
     * @return 响应头
     */
    String getHeader(String name);

    /**
     * 获取响应报文体长度（从请求头内提取） 在 HEAD 请求时，该方法返回不为 0，但{@link Body#getLength()} 将返回 0
     *
     * @return 长度
     */
    long getContentLength();

    /**
     * @return 响应报文体
     */
    Body getBody();

    /**
     * @return 执行中发生的异常
     */
    IOException getError();

    /**
     * 关闭报文 未对报文体做任何消费时使用，比如只读取报文头
     *
     * @return HttpResult
     */
    CoverResult close();

    enum State {

        /**
         * 执行异常
         */
        EXCEPTION,

        /**
         * 请求被取消
         */
        CANCELED,

        /**
         * 请求已响应
         */
        RESPONSED,

        /**
         * 网络超时
         */
        TIMEOUT,

        /**
         * 网络出错
         */
        NETWORK_ERROR

    }

    /**
     * HTTP响应报文体
     */
    interface Body {

        /**
         * @return 消息体转字节流
         */
        InputStream toByteStream();

        /**
         * @return 消息体转字节数组
         */
        byte[] toBytes();

        /**
         * @return ByteString
         */
        ByteString toByteString();

        /**
         * @return 消息体转字符流
         */
        Reader toCharStream();

        /**
         * @return 消息体转字符串
         */
        String toString();

        /**
         * @return 消息体转 Mapper 对象（不想定义 Java Bean 时使用）
         */
        CoverWapper toWapper();

        /**
         * @return 消息体转 Array 数组（不想定义 Java Bean 时使用）
         */
        CoverArray toArray();

        /**
         * @param <T>  目标泛型
         * @param type 目标类型
         * @return 报文体Json文本转JavaBean
         */
        <T> T toBean(Class<T> type);

        /**
         * @param <T>  目标泛型
         * @param type 目标类型
         * @return 报文体Json文本转JavaBean列表
         */
        <T> List<T> toList(Class<T> type);

        /**
         * @return 媒体类型
         */
        MediaType getType();

        /**
         * @return 报文体字节长度
         */
        long getLength();

        /**
         * 在IO线程执行
         *
         * @return Body
         */
        Body nextOnIO();

        /**
         * 设置报文体接收进度回调
         *
         * @param onProcess 进度回调函数
         * @return Body
         */
        Body setOnProcess(Callback<Progress> onProcess);

        /**
         * 设置进度回调的步进字节，默认 8K（8192） 表示每接收 stepBytes 个字节，执行一次进度回调
         *
         * @param stepBytes 步进字节
         * @return Body
         */
        Body stepBytes(long stepBytes);

        /**
         * 设置进度回调的步进比例 表示每接收 stepRate 比例，执行一次进度回调
         *
         * @param stepRate 步进比例
         * @return Body
         */
        Body stepRate(double stepRate);

        /**
         * 设置进度回调忽略响应的Range头信息，即进度回调会从0开始
         *
         * @return Body
         */
        Body setRangeIgnored();

        /**
         * 下载到指定路径 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param filePath 目标路径
         * @return 下载过程 #Download
         */
        Downloads toFile(String filePath);

        /**
         * 下载到指定文件 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param file 目标文件
         * @return 下载过程 #Download
         */
        Downloads toFile(File file);

        /**
         * 下载到指定文件夹 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param dirPath 目标目录
         * @return 下载过程 #Download
         */
        Downloads toFolder(String dirPath);

        /**
         * 下载到指定文件夹 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
         *
         * @param dir 目标目录
         * @return 下载过程 #Download
         */
        Downloads toFolder(File dir);

        /**
         * 缓存自己，缓存后可 重复使用 toXXX 类方法
         *
         * @return Body
         */
        Body cache();

        /**
         * 关闭报文体 未对报文体做任何消费时使用，比如只读取长度
         *
         * @return Body
         */
        Body close();

    }

    /**
     * @author Kimi Liu
     * @since Java 17+
     */
    class Real implements CoverResult {

        private State state;
        private Response response;
        private IOException error;
        private CoverTasks.Executor executor;
        private CoverHttp<?> coverHttp;
        private Body body;

        public Real(CoverHttp<?> coverHttp, State state) {
            this.coverHttp = coverHttp;
            this.state = state;
        }

        public Real(CoverHttp<?> coverHttp, Response response, CoverTasks.Executor executor) {
            this(coverHttp, executor);
            response(response);
        }

        public Real(CoverHttp<?> coverHttp, CoverTasks.Executor executor) {
            this.coverHttp = coverHttp;
            this.executor = executor;
        }

        public Real(CoverHttp<?> coverHttp, State state, IOException error) {
            this.coverHttp = coverHttp;
            exception(state, error);
        }

        public void exception(State state, IOException error) {
            this.state = state;
            this.error = error;
        }

        public void response(Response response) {
            this.state = State.RESPONSED;
            this.response = response;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public int getStatus() {
            if (null != response) {
                return response.code();
            }
            return 0;
        }

        @Override
        public boolean isSuccessful() {
            if (null != response) {
                return response.isSuccessful();
            }
            return false;
        }

        @Override
        public Headers getHeaders() {
            if (null != response) {
                return response.headers();
            }
            return null;
        }

        @Override
        public List<String> getHeaders(String name) {
            if (null != response) {
                return response.headers(name);
            }
            return Collections.emptyList();
        }

        @Override
        public String getHeader(String name) {
            if (null != response) {
                return response.header(name);
            }
            return null;
        }

        @Override
        public long getContentLength() {
            String length = getHeader("Content-Length");
            if (null != length) {
                try {
                    return Long.parseLong(length);
                } catch (Exception ignore) {
                }
            }
            return 0;
        }

        @Override
        public synchronized Body getBody() {
            if (null == body && null != response) {
                body = new ResultBody(coverHttp, response, executor);
            }
            return body;
        }

        @Override
        public IOException getError() {
            return error;
        }

        public Response getResponse() {
            return response;
        }

        @Override
        public String toString() {
            Body body = getBody();
            String text = "RealResult [\n  state: " + state + ",\n  status: " + getStatus() + ",\n  headers: "
                    + getHeaders();
            if (null != body) {
                text += ",\n  contentType: " + body.getType();
            }
            return text + ",\n  error: " + error + "\n]";
        }

        @Override
        public CoverResult close() {
            if (null != response) {
                response.close();
            }
            return this;
        }

    }
}
