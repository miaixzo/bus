/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.plugin.httpv.*;
import org.miaixz.bus.http.socket.WebSocket;
import org.miaixz.bus.http.socket.WebSocketListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Httpv 客户端接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpv {

    /**
     * Httpd
     */
    Httpd httpd;
    /**
     * 根URL
     */
    String baseUrl;

    /**
     * 媒体类型
     */
    Map<String, String> mediaTypes;
    /**
     * 执行器
     */
    CoverTasks.Executor executor;
    /**
     * 预处理器
     */
    Preprocessor[] preprocessors;
    /**
     * 持有标签的任务
     */
    List<TagTask> tagTasks;
    /**
     * 最大预处理时间倍数（相对于普通请求的超时时间）
     */
    int preprocTimeoutTimes;
    /**
     * 编码格式
     */
    Charset charset;
    /**
     * 默认的请求体类型
     */
    String bodyType;

    public Httpv() {

    }

    public Httpv(Builder builder) {
        this.httpd = builder.httpd();
        this.baseUrl = builder.baseUrl();
        this.mediaTypes = builder.getMediaTypes();
        this.executor = new CoverTasks.Executor(httpd.dispatcher().executorService(),
                builder.mainExecutor(), builder.downloadListener(),
                builder.responseListener(), builder.exceptionListener(),
                builder.completeListener(), builder.msgConvertors());
        this.preprocessors = builder.preprocessors();
        this.preprocTimeoutTimes = builder.preprocTimeoutTimes();
        this.charset = builder.charset();
        this.bodyType = builder.bodyType();
        this.tagTasks = new LinkedList<>();
    }

    /**
     * HTTP 构建器
     *
     * @return HTTP 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public CoverHttp.Async async(String url) {
        return new CoverHttp.Async(this, urlPath(url, false));
    }

    public CoverHttp.Sync sync(String url) {
        return new CoverHttp.Sync(this, urlPath(url, false));
    }

    public CoverCall.Client webSocket(String url) {
        return new CoverCall.Client(this, urlPath(url, true));
    }

    public int cancel(String tag) {
        if (null == tag) {
            return 0;
        }
        int count = 0;
        synchronized (tagTasks) {
            Iterator<TagTask> it = tagTasks.iterator();
            while (it.hasNext()) {
                TagTask tagCall = it.next();
                // 只要任务的标签包含指定的Tag就会被取消
                if (tagCall.tag.contains(tag)) {
                    if (tagCall.canceler.cancel()) {
                        count++;
                    }
                    it.remove();
                } else if (tagCall.isExpired()) {
                    it.remove();
                }
            }
        }
        return count;
    }

    public void cancelAll() {
        httpd.dispatcher().cancelAll();
        synchronized (tagTasks) {
            tagTasks.clear();
        }
    }

    public NewCall request(Request request) {
        return httpd.newCall(request);
    }

    public WebSocket webSocket(Request request, WebSocketListener listener) {
        return httpd.newWebSocket(request, listener);
    }

    public Httpd httpd() {
        return httpd;
    }

    public int preprocTimeoutMillis() {
        return preprocTimeoutTimes * (httpd.connectTimeoutMillis()
                + httpd.writeTimeoutMillis()
                + httpd.readTimeoutMillis());
    }

    public int getTagTaskCount() {
        return tagTasks.size();
    }

    public TagTask addTagTask(String tag, Cancelable canceler, CoverHttp<?> task) {
        TagTask tagTask = new TagTask(tag, canceler, task);
        synchronized (tagTasks) {
            tagTasks.add(tagTask);
        }
        return tagTask;
    }

    public void removeTagTask(CoverHttp<?> task) {
        synchronized (tagTasks) {
            Iterator<TagTask> it = tagTasks.iterator();
            while (it.hasNext()) {
                TagTask tagCall = it.next();
                if (tagCall.task == task) {
                    it.remove();
                    break;
                }
                if (tagCall.isExpired()) {
                    it.remove();
                }
            }
        }
    }

    public MediaType mediaType(String type) {
        String mediaType = mediaTypes.get(type);
        if (null != mediaType) {
            return MediaType.valueOf(mediaType);
        }
        return MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM);
    }

    public CoverTasks.Executor executor() {
        return executor;
    }

    public void preprocess(CoverHttp<? extends CoverHttp<?>> coverHttp, Runnable request,
                           boolean skipPreproc, boolean skipSerialPreproc) {
        if (preprocessors.length == 0 || skipPreproc) {
            request.run();
            return;
        }
        int index = 0;
        if (skipSerialPreproc) {
            while (index < preprocessors.length
                    && preprocessors[index] instanceof SerialPreprocessor) {
                index++;
            }
        }
        if (index < preprocessors.length) {
            RealPreChain chain = new RealPreChain(preprocessors,
                    coverHttp, request, index + 1,
                    skipSerialPreproc);
            preprocessors[index].doProcess(chain);
        } else {
            request.run();
        }
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    private String urlPath(String urlPath, boolean websocket) {
        String fullUrl;
        if (null == urlPath) {
            if (null != baseUrl) {
                fullUrl = baseUrl;
            } else {
                throw new InternalException("Before setting BaseUrl, you must specify a specific path to initiate a request!");
            }
        } else {
            boolean isFullPath = urlPath.startsWith(Http.HTTPS_PREFIX)
                    || urlPath.startsWith(Http.HTTP_PREFIX)
                    || urlPath.startsWith(Http.WSS_PREFIX)
                    || urlPath.startsWith(Http.WS_PREFIX);
            if (isFullPath) {
                fullUrl = urlPath;
            } else if (null != baseUrl) {
                fullUrl = baseUrl + urlPath;
            } else {
                throw new InternalException("Before setting BaseUrl, you must use the full path URL to initiate the request. The current URL is：" + urlPath);
            }
        }
        if (websocket && fullUrl.startsWith(Http.HTTP)) {
            return fullUrl.replaceFirst(Http.HTTP, Http.WS);
        }
        if (!websocket && fullUrl.startsWith(Http.WS)) {
            return fullUrl.replaceFirst(Http.WS, Http.HTTP);
        }
        return fullUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Map<String, String> mediaTypes() {
        return mediaTypes;
    }

    public Preprocessor[] preprocessors() {
        return preprocessors;
    }

    public List<TagTask> tagTasks() {
        return tagTasks;
    }

    public int preprocTimeoutTimes() {
        return preprocTimeoutTimes;
    }

    public Charset charset() {
        return charset;
    }

    public String bodyType() {
        return bodyType;
    }

    /**
     * Http 配置器
     */
    public interface HttpvConfig {

        /**
         * 使用 builder 配置 Httpc
         *
         * @param builder Httpd 构建器
         */
        void config(Httpd.Builder builder);

    }

    /**
     * 串行预处理器
     */
    public static class SerialPreprocessor implements Preprocessor {

        // 预处理器
        private Preprocessor preprocessor;
        // 待处理的任务队列
        private Queue<PreChain> pendings;
        // 是否有任务正在执行
        private boolean running = false;

        public SerialPreprocessor(Preprocessor preprocessor) {
            this.preprocessor = preprocessor;
            this.pendings = new LinkedList<>();
        }

        @Override
        public void doProcess(PreChain chain) {
            boolean should = true;
            synchronized (this) {
                if (running) {
                    pendings.add(chain);
                    should = false;
                } else {
                    running = true;
                }
            }
            if (should) {
                preprocessor.doProcess(chain);
            }
        }

        public void afterProcess() {
            PreChain chain = null;
            synchronized (this) {
                if (pendings.size() > 0) {
                    chain = pendings.poll();
                } else {
                    running = false;
                }
            }
            if (null != chain) {
                preprocessor.doProcess(chain);
            }
        }

    }

    public static class Builder {

        private Httpd httpd;

        private String baseUrl;

        private Map<String, String> mediaTypes;

        private HttpvConfig config;

        private java.util.concurrent.Executor mainExecutor;

        private List<Preprocessor> preprocessors;

        private Downloads.Listener downloadListener;

        private CoverTasks.Listener<CoverResult> responseListener;

        private CoverTasks.Listener<IOException> exceptionListener;

        private CoverTasks.Listener<CoverResult.State> completeListener;

        private List<Convertor> convertors;

        private int preprocTimeoutTimes = 10;

        private Charset charset = org.miaixz.bus.core.lang.Charset.UTF_8;

        private String bodyType = Http.FORM;

        public Builder() {
            mediaTypes = new HashMap<>();
            mediaTypes.put("*", MediaType.APPLICATION_OCTET_STREAM);
            mediaTypes.put("png", "image/png");
            mediaTypes.put("jpg", "image/jpeg");
            mediaTypes.put("jpeg", "image/jpeg");
            mediaTypes.put("wav", "audio/wav");
            mediaTypes.put("mp3", "audio/mp3");
            mediaTypes.put("mp4", "video/mpeg4");
            mediaTypes.put("txt", "text/plain");
            mediaTypes.put("xls", "application/x-xls");
            mediaTypes.put("xml", "text/xml");
            mediaTypes.put("apk", "application/vnd.android.package-archive");
            mediaTypes.put("doc", "application/msword");
            mediaTypes.put("pdf", "application/pdf");
            mediaTypes.put("html", "text/html");
            preprocessors = new ArrayList<>();
            convertors = new ArrayList<>();
        }

        public Builder(Httpv httpv) {
            this.httpd = httpv.httpd();
            this.baseUrl = httpv.baseUrl();
            this.mediaTypes = httpv.mediaTypes();
            this.preprocessors = new ArrayList<>();
            Collections.addAll(this.preprocessors, httpv.preprocessors());
            CoverTasks.Executor executor = httpv.executor();
            this.downloadListener = executor.getDownloadListener();
            this.responseListener = executor.getResponseListener();
            this.exceptionListener = executor.getExceptionListener();
            this.completeListener = executor.getCompleteListener();
            this.convertors = new ArrayList<>();
            Collections.addAll(this.convertors, executor.getConvertors());
            this.preprocTimeoutTimes = httpv.preprocTimeoutTimes();
            this.charset = httpv.charset();
            this.bodyType = httpv.bodyType();
        }

        private static void addCopyInterceptor(Httpd.Builder builder) {
            builder.addInterceptor(chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);
                ResponseBody body = response.body();
                String type = response.header(Header.CONTENT_TYPE);
                if (null == body || null != type && (type.contains("octet-stream")
                        || type.contains("image") || type.contains("video")
                        || type.contains("archive") || type.contains("word")
                        || type.contains("xls") || type.contains("pdf"))) {
                    // 若是下载文件，则必须指定在 IO 线程操作
                    return response;
                }
                ResponseBody newBody = ResponseBody.create(body.mediaType(), body.bytes());
                return response.newBuilder().body(newBody).build();
            });
        }

        private static int androidSdkInt() {
            try {
                Class<?> versionClass = Class.forName("android.os.Build$VERSION");
                Field field = versionClass.getDeclaredField("SDK_INT");
                return field.getInt(field);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                return 0;
            }
        }

        /**
         * 配置 Httpd
         *
         * @param config 配置器
         * @return Builder
         */
        public Builder config(HttpvConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 baseUrl
         *
         * @param baseUrl 全局URL前缀
         * @return Builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * 配置媒体类型
         *
         * @param mediaTypes 媒体类型
         * @return Builder
         */
        public Builder mediaTypes(Map<String, String> mediaTypes) {
            if (null != mediaTypes) {
                this.mediaTypes.putAll(mediaTypes);
            }
            return this;
        }

        /**
         * 配置媒体类型
         *
         * @param key   媒体类型KEY
         * @param value 媒体类型VALUE
         * @return Builder
         */
        public Builder mediaTypes(String key, String value) {
            if (null != key && null != value) {
                this.mediaTypes.put(key, value);
            }
            return this;
        }

        /**
         * 设置回调执行器，例如实现切换线程功能，只对异步请求有效
         *
         * @param executor 回调执行器
         * @return Builder
         */
        public Builder callbackExecutor(java.util.concurrent.Executor executor) {
            this.mainExecutor = executor;
            return this;
        }

        /**
         * 添加可并行处理请求任务的预处理器
         *
         * @param preprocessor 预处理器
         * @return Builder
         */
        public Builder addPreprocessor(Preprocessor preprocessor) {
            if (null != preprocessor) {
                preprocessors.add(preprocessor);
            }
            return this;
        }

        /**
         * 添加预处理器
         *
         * @param preprocessor 预处理器
         * @return Builder
         */
        public Builder addSerialPreprocessor(Preprocessor preprocessor) {
            if (null != preprocessor) {
                preprocessors.add(new SerialPreprocessor(preprocessor));
            }
            return this;
        }

        /**
         * 最大预处理时间（倍数，相当普通请求的超时时间）
         *
         * @param times 普通超时时间的倍数，默认为 10
         * @return Builder
         */
        public Builder preprocTimeoutTimes(int times) {
            if (times > 0) {
                this.preprocTimeoutTimes = times;
            }
            return this;
        }

        /**
         * 设置全局响应监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder responseListener(CoverTasks.Listener<CoverResult> listener) {
            this.responseListener = listener;
            return this;
        }

        /**
         * 设置全局异常监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder exceptionListener(CoverTasks.Listener<IOException> listener) {
            this.exceptionListener = listener;
            return this;
        }

        /**
         * 设置全局完成监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder completeListener(CoverTasks.Listener<CoverResult.State> listener) {
            this.completeListener = listener;
            return this;
        }

        /**
         * 设置下载监听器
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder downloadListener(Downloads.Listener listener) {
            this.downloadListener = listener;
            return this;
        }

        /**
         * @param convertor JSON 服务
         * @return Builder
         * 添加消息转换器
         */
        public Builder addMsgConvertor(Convertor convertor) {
            if (null != convertor) {
                this.convertors.add(convertor);
            }
            return this;
        }

        /**
         * @param charset 编码
         * @return Builder
         * 设置默认编码格式
         */
        public Builder charset(Charset charset) {
            if (null != charset) {
                this.charset = charset;
            }
            return this;
        }

        /**
         * @param bodyType 请求体类型
         * @return Builder
         * 设置默认请求体类型
         */
        public Builder bodyType(String bodyType) {
            if (null != bodyType) {
                this.bodyType = bodyType;
            }
            return this;
        }

        /**
         * 构建 HTTP 实例
         *
         * @return HTTP
         */
        public Httpv build() {
            if (null != config || null == httpd) {
                Httpd.Builder builder = new Httpd.Builder();
                if (null != config) {
                    config.config(builder);
                }
                if (null != mainExecutor && androidSdkInt() > 24) {
                    addCopyInterceptor(builder);
                }
                httpd = builder.build();
            }
            return new Httpv(this);
        }

        public Httpd httpd() {
            return httpd;
        }

        public String baseUrl() {
            return baseUrl;
        }

        public Map<String, String> getMediaTypes() {
            return mediaTypes;
        }

        public java.util.concurrent.Executor mainExecutor() {
            return mainExecutor;
        }

        public Preprocessor[] preprocessors() {
            return preprocessors.toArray(new Preprocessor[0]);
        }

        public Downloads.Listener downloadListener() {
            return downloadListener;
        }

        public CoverTasks.Listener<CoverResult> responseListener() {
            return responseListener;
        }

        public CoverTasks.Listener<IOException> exceptionListener() {
            return exceptionListener;
        }

        public CoverTasks.Listener<CoverResult.State> completeListener() {
            return completeListener;
        }

        public Convertor[] msgConvertors() {
            return convertors.toArray(new Convertor[0]);
        }

        public int preprocTimeoutTimes() {
            return preprocTimeoutTimes;
        }

        public Charset charset() {
            return charset;
        }

        public String bodyType() {
            return bodyType;
        }

    }

    public class TagTask {

        String tag;
        Cancelable canceler;
        CoverHttp<?> task;
        long createAt;

        TagTask(String tag, Cancelable canceler, CoverHttp<?> task) {
            this.tag = tag;
            this.canceler = canceler;
            this.task = task;
            this.createAt = System.nanoTime();
        }

        boolean isExpired() {
            // 生存时间大于10倍的总超时限值
            return System.nanoTime() - createAt > 1_000_000 * preprocTimeoutMillis();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

    }

    class RealPreChain implements Preprocessor.PreChain {

        private int index;

        private Preprocessor[] preprocessors;

        private CoverHttp<?> coverHttp;

        private Runnable request;

        private boolean noSerialPreprocess;

        public RealPreChain(Preprocessor[] preprocessors, CoverHttp<?> coverHttp, Runnable request,
                            int index, boolean noSerialPreprocess) {
            this.index = index;        // index 大于等于 1
            this.preprocessors = preprocessors;
            this.coverHttp = coverHttp;
            this.request = request;
            this.noSerialPreprocess = noSerialPreprocess;
        }

        @Override
        public CoverHttp<?> getTask() {
            return coverHttp;
        }

        @Override
        public Httpv getHttp() {
            return Httpv.this;
        }

        @Override
        public void proceed() {
            if (noSerialPreprocess) {
                while (index < preprocessors.length
                        && preprocessors[index] instanceof SerialPreprocessor) {
                    index++;
                }
            } else {
                Preprocessor last = preprocessors[index - 1];
                if (last instanceof SerialPreprocessor) {
                    ((SerialPreprocessor) last).afterProcess();
                }
            }
            if (index < preprocessors.length) {
                preprocessors[index++].doProcess(this);
            } else {
                request.run();
            }
        }

    }

}
