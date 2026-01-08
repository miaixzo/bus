/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.plugin.httpv.*;
import org.miaixz.bus.http.socket.WebSocket;
import org.miaixz.bus.http.socket.WebSocketListener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A high-level HTTP client that provides a fluent and expressive API for making HTTP requests. It is built on top of
 * {@link Httpd} and adds features like pre-processing, task management, and simplified request creation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpv {

    /**
     * The underlying {@link Httpd} client used for executing requests.
     */
    final Httpd httpd;
    /**
     * The base URL used for resolving relative request paths.
     */
    final String baseUrl;

    /**
     * A map of custom media type mappings.
     */
    final Map<String, String> mediaTypes;
    /**
     * The executor for handling asynchronous tasks and callbacks.
     */
    final CoverTasks.Executor executor;
    /**
     * An array of preprocessors that can modify requests before they are sent.
     */
    final Preprocessor[] preprocessors;
    /**
     * A list of tasks that are associated with a tag for cancellation.
     */
    final List<TagTask> tagTasks;
    /**
     * The timeout multiplier for pre-processing tasks, relative to the regular request timeout.
     */
    final int preprocTimeoutTimes;
    /**
     * The default character set for request bodies.
     */
    final Charset charset;
    /**
     * The default body type for requests (e.g., "form").
     */
    final String bodyType;

    /**
     * Default constructor.
     */
    public Httpv() {
        this(new Builder());
    }

    /**
     * Constructs an {@code Httpv} instance with the configuration from the given builder.
     *
     * @param builder The builder to configure this instance.
     */
    public Httpv(Builder builder) {
        this.httpd = builder.httpd();
        this.baseUrl = builder.baseUrl();
        this.mediaTypes = builder.getMediaTypes();
        this.executor = new CoverTasks.Executor(httpd.dispatcher().executorService(), builder.mainExecutor(),
                builder.downloadListener(), builder.responseListener(), builder.exceptionListener(),
                builder.completeListener(), builder.msgConvertors());
        this.preprocessors = builder.preprocessors();
        this.preprocTimeoutTimes = builder.preprocTimeoutTimes();
        this.charset = builder.charset();
        this.bodyType = builder.bodyType();
        this.tagTasks = new LinkedList<>();
    }

    /**
     * Creates a new builder for constructing an {@code Httpv} instance.
     *
     * @return a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Starts an asynchronous HTTP request.
     *
     * @param url The URL for the request. Can be a relative or absolute path.
     * @return An {@link CoverHttp.Async} instance for building and executing the request.
     */
    public CoverHttp.Async async(String url) {
        return new CoverHttp.Async(this, urlPath(url, false));
    }

    /**
     * Starts a synchronous HTTP request.
     *
     * @param url The URL for the request. Can be a relative or absolute path.
     * @return A {@link CoverHttp.Sync} instance for building and executing the request.
     */
    public CoverHttp.Sync sync(String url) {
        return new CoverHttp.Sync(this, urlPath(url, false));
    }

    /**
     * Starts a WebSocket connection.
     *
     * @param url The URL for the WebSocket. Can be a relative or absolute path.
     * @return A {@link CoverCall.Client} instance for establishing the WebSocket connection.
     */
    public CoverCall.Client webSocket(String url) {
        return new CoverCall.Client(this, urlPath(url, true));
    }

    /**
     * Cancels all ongoing requests that are associated with the given tag.
     *
     * @param tag The tag to identify which requests to cancel.
     * @return The number of requests that were successfully canceled.
     */
    public int cancel(String tag) {
        if (null == tag) {
            return 0;
        }
        int count = 0;
        synchronized (tagTasks) {
            Iterator<TagTask> it = tagTasks.iterator();
            while (it.hasNext()) {
                TagTask tagCall = it.next();
                // Any task whose tag contains the specified tag will be canceled.
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

    /**
     * Cancels all ongoing requests and clears all tagged tasks.
     */
    public void cancelAll() {
        httpd.dispatcher().cancelAll();
        synchronized (tagTasks) {
            tagTasks.clear();
        }
    }

    /**
     * Creates a new call for the given request using the underlying {@link Httpd} client.
     *
     * @param request The request to execute.
     * @return A new {@link NewCall}.
     */
    public NewCall request(Request request) {
        return httpd.newCall(request);
    }

    /**
     * Creates a new WebSocket connection using the underlying {@link Httpd} client.
     *
     * @param request  The WebSocket request.
     * @param listener The listener for WebSocket events.
     * @return A new {@link WebSocket}.
     */
    public WebSocket webSocket(Request request, WebSocketListener listener) {
        return httpd.newWebSocket(request, listener);
    }

    /**
     * Gets the underlying {@link Httpd} client.
     *
     * @return The {@link Httpd} instance.
     */
    public Httpd httpd() {
        return httpd;
    }

    /**
     * Calculates the timeout for pre-processing tasks in milliseconds.
     *
     * @return The pre-processing timeout in milliseconds.
     */
    public int preprocTimeoutMillis() {
        return preprocTimeoutTimes
                * (httpd.connectTimeoutMillis() + httpd.writeTimeoutMillis() + httpd.readTimeoutMillis());
    }

    /**
     * Gets the current number of tagged tasks.
     *
     * @return The number of tagged tasks.
     */
    public int getTagTaskCount() {
        return tagTasks.size();
    }

    /**
     * Adds a new tagged task to the list of ongoing tasks.
     *
     * @param tag      The tag for the task.
     * @param canceler The object that can cancel the task.
     * @param task     The task itself.
     * @return The newly created {@link TagTask}.
     */
    public TagTask addTagTask(String tag, Cancelable canceler, CoverHttp<?> task) {
        TagTask tagTask = new TagTask(tag, canceler, task);
        synchronized (tagTasks) {
            tagTasks.add(tagTask);
        }
        return tagTask;
    }

    /**
     * Removes a tagged task from the list of ongoing tasks.
     *
     * @param task The task to remove.
     */
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

    /**
     * Gets the {@link MediaType} for a given type string based on the configured media types.
     *
     * @param type The type string (e.g., "png", "json").
     * @return The corresponding {@link MediaType}, or {@code application/octet-stream} if not found.
     */
    public MediaType contentType(String type) {
        String contentType = mediaTypes.get(type);
        if (null != contentType) {
            return MediaType.valueOf(contentType);
        }
        return MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Gets the executor for handling asynchronous tasks.
     *
     * @return The {@link CoverTasks.Executor}.
     */
    public CoverTasks.Executor executor() {
        return executor;
    }

    /**
     * Executes the pre-processing chain for a given request.
     *
     * @param coverHttp         The HTTP task being processed.
     * @param request           The final request action to run after pre-processing.
     * @param skipPreproc       Whether to skip all pre-processors.
     * @param skipSerialPreproc Whether to skip serial pre-processors.
     */
    public void preprocess(
            CoverHttp<? extends CoverHttp<?>> coverHttp,
            Runnable request,
            boolean skipPreproc,
            boolean skipSerialPreproc) {
        if (preprocessors.length == 0 || skipPreproc) {
            request.run();
            return;
        }
        int index = 0;
        if (skipSerialPreproc) {
            while (index < preprocessors.length && preprocessors[index] instanceof SerialPreprocessor) {
                index++;
            }
        }
        if (index < preprocessors.length) {
            RealPreChain chain = new RealPreChain(preprocessors, coverHttp, request, index + 1, skipSerialPreproc);
            preprocessors[index].doProcess(chain);
        } else {
            request.run();
        }
    }

    /**
     * Creates a new builder initialized with the configuration of this {@code Httpv} instance.
     *
     * @return A new {@link Builder}.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Resolves a URL path against the base URL.
     *
     * @param urlPath   The URL path, which can be relative or absolute.
     * @param websocket Whether the URL is for a WebSocket connection.
     * @return The full URL.
     * @throws InternalException if a relative URL is provided without a base URL.
     */
    private String urlPath(String urlPath, boolean websocket) {
        String fullUrl;
        if (null == urlPath) {
            if (null != baseUrl) {
                fullUrl = baseUrl;
            } else {
                throw new InternalException(
                        "Before setting BaseUrl, you must specify a specific path to initiate a request!");
            }
        } else {
            boolean isFullPath = urlPath.startsWith(Protocol.HTTPS_PREFIX) || urlPath.startsWith(Protocol.HTTP_PREFIX)
                    || urlPath.startsWith(Protocol.WSS_PREFIX) || urlPath.startsWith(Protocol.WS_PREFIX);
            if (isFullPath) {
                fullUrl = urlPath;
            } else if (null != baseUrl) {
                fullUrl = baseUrl + urlPath;
            } else {
                throw new InternalException(
                        "Before setting BaseUrl, you must use the full path URL to initiate the request. The current URL is: "
                                + urlPath);
            }
        }
        if (websocket && fullUrl.startsWith(Protocol.HTTP.name)) {
            return fullUrl.replaceFirst(Protocol.HTTP.name, Protocol.WS.name);
        }
        if (!websocket && fullUrl.startsWith(Protocol.WS.name)) {
            return fullUrl.replaceFirst(Protocol.WS.name, Protocol.HTTP.name);
        }
        return fullUrl;
    }

    /**
     * Gets the base URL.
     *
     * @return The base URL.
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Gets the map of media types.
     *
     * @return The map of media types.
     */
    public Map<String, String> mediaTypes() {
        return mediaTypes;
    }

    /**
     * Gets the array of preprocessors.
     *
     * @return The array of preprocessors.
     */
    public Preprocessor[] preprocessors() {
        return preprocessors;
    }

    /**
     * Gets the list of tagged tasks.
     *
     * @return The list of tagged tasks.
     */
    public List<TagTask> tagTasks() {
        return tagTasks;
    }

    /**
     * Gets the pre-processing timeout multiplier.
     *
     * @return The pre-processing timeout multiplier.
     */
    public int preprocTimeoutTimes() {
        return preprocTimeoutTimes;
    }

    /**
     * Gets the default charset.
     *
     * @return The default charset.
     */
    public Charset charset() {
        return charset;
    }

    /**
     * Gets the default body type.
     *
     * @return The default body type.
     */
    public String bodyType() {
        return bodyType;
    }

    /**
     * An interface for configuring the underlying {@link Httpd.Builder}.
     */
    public interface HttpvConfig {

        /**
         * Configures the {@link Httpd.Builder}.
         *
         * @param builder The {@link Httpd.Builder} to configure.
         */
        void config(Httpd.Builder builder);

    }

    /**
     * A preprocessor that ensures serial execution of other preprocessors.
     */
    public static class SerialPreprocessor implements Preprocessor {

        /**
         * The wrapped preprocessor.
         */
        private final Preprocessor preprocessor;
        /**
         * The queue of pending tasks.
         */
        private final Queue<PreChain> pendings;
        /**
         * Whether a task is currently running.
         */
        private boolean running = false;

        /**
         * Constructs a new {@code SerialPreprocessor}.
         *
         * @param preprocessor The preprocessor to wrap.
         */
        public SerialPreprocessor(Preprocessor preprocessor) {
            this.preprocessor = preprocessor;
            this.pendings = new LinkedList<>();
        }

        @Override
        public void doProcess(PreChain chain) {
            boolean shouldRun;
            synchronized (this) {
                if (running) {
                    pendings.add(chain);
                    shouldRun = false;
                } else {
                    running = true;
                    shouldRun = true;
                }
            }
            if (shouldRun) {
                preprocessor.doProcess(chain);
            }
        }

        /**
         * Called after a process is finished to run the next pending task.
         */
        public void afterProcess() {
            PreChain chain = null;
            synchronized (this) {
                if (!pendings.isEmpty()) {
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

    /**
     * A builder for creating and configuring {@link Httpv} instances.
     */
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
        private String bodyType = HTTP.FORM;

        /**
         * Default constructor that initializes with default values.
         */
        public Builder() {
            mediaTypes = new HashMap<>();
            mediaTypes.put(Symbol.STAR, MediaType.APPLICATION_OCTET_STREAM);
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

        /**
         * Constructor that initializes the builder with the settings of an existing {@link Httpv} instance.
         *
         * @param httpv The {@link Httpv} instance to copy settings from.
         */
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
                String type = response.header(HTTP.CONTENT_TYPE);
                if (null == body || null != type && (type.contains("octet-stream") || type.contains("image")
                        || type.contains("video") || type.contains("archive") || type.contains("word")
                        || type.contains("xls") || type.contains("pdf"))) {
                    // If it is a file download, it must be specified to operate in the IO thread.
                    return response;
                }
                ResponseBody newBody = ResponseBody.of(body.contentType(), body.bytes());
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
         * Sets a configurer for the underlying {@link Httpd.Builder}.
         *
         * @param config The configurer.
         * @return This builder.
         */
        public Builder config(HttpvConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets the base URL for all requests.
         *
         * @param baseUrl The base URL.
         * @return This builder.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Adds multiple media type mappings.
         *
         * @param mediaTypes A map of media types.
         * @return This builder.
         */
        public Builder mediaTypes(Map<String, String> mediaTypes) {
            if (null != mediaTypes) {
                this.mediaTypes.putAll(mediaTypes);
            }
            return this;
        }

        /**
         * Adds a single media type mapping.
         *
         * @param key   The file extension or type key.
         * @param value The media type value.
         * @return This builder.
         */
        public Builder mediaTypes(String key, String value) {
            if (null != key && null != value) {
                this.mediaTypes.put(key, value);
            }
            return this;
        }

        /**
         * Sets the executor for callbacks, allowing thread switching for asynchronous requests.
         *
         * @param executor The callback executor.
         * @return This builder.
         */
        public Builder callbackExecutor(java.util.concurrent.Executor executor) {
            this.mainExecutor = executor;
            return this;
        }

        /**
         * Adds a preprocessor that can run in parallel with other preprocessors.
         *
         * @param preprocessor The preprocessor to add.
         * @return This builder.
         */
        public Builder addPreprocessor(Preprocessor preprocessor) {
            if (null != preprocessor) {
                preprocessors.add(preprocessor);
            }
            return this;
        }

        /**
         * Adds a preprocessor that will be executed serially.
         *
         * @param preprocessor The preprocessor to add.
         * @return This builder.
         */
        public Builder addSerialPreprocessor(Preprocessor preprocessor) {
            if (null != preprocessor) {
                preprocessors.add(new SerialPreprocessor(preprocessor));
            }
            return this;
        }

        /**
         * Sets the maximum pre-processing time multiplier relative to the normal request timeout.
         *
         * @param times The multiplier for the timeout, default is 10.
         * @return This builder.
         */
        public Builder preprocTimeoutTimes(int times) {
            if (times > 0) {
                this.preprocTimeoutTimes = times;
            }
            return this;
        }

        /**
         * Sets a global listener for successful responses.
         *
         * @param listener The response listener.
         * @return This builder.
         */
        public Builder responseListener(CoverTasks.Listener<CoverResult> listener) {
            this.responseListener = listener;
            return this;
        }

        /**
         * Sets a global listener for exceptions.
         *
         * @param listener The exception listener.
         * @return This builder.
         */
        public Builder exceptionListener(CoverTasks.Listener<IOException> listener) {
            this.exceptionListener = listener;
            return this;
        }

        /**
         * Sets a global listener for when a request is completed (either successfully or with an error).
         *
         * @param listener The completion listener.
         * @return This builder.
         */
        public Builder completeListener(CoverTasks.Listener<CoverResult.State> listener) {
            this.completeListener = listener;
            return this;
        }

        /**
         * Sets a listener for download progress.
         *
         * @param listener The download listener.
         * @return This builder.
         */
        public Builder downloadListener(Downloads.Listener listener) {
            this.downloadListener = listener;
            return this;
        }

        /**
         * Adds a message converter for handling different response formats.
         *
         * @param convertor The message converter.
         * @return This builder.
         */
        public Builder addMsgConvertor(Convertor convertor) {
            if (null != convertor) {
                this.convertors.add(convertor);
            }
            return this;
        }

        /**
         * Sets the default charset for request bodies.
         *
         * @param charset The charset.
         * @return This builder.
         */
        public Builder charset(Charset charset) {
            if (null != charset) {
                this.charset = charset;
            }
            return this;
        }

        /**
         * Sets the default body type for requests.
         *
         * @param bodyType The body type (e.g., "form", "json").
         * @return This builder.
         */
        public Builder bodyType(String bodyType) {
            if (null != bodyType) {
                this.bodyType = bodyType;
            }
            return this;
        }

        /**
         * Builds a new {@link Httpv} instance with the configured settings.
         *
         * @return A new {@link Httpv} instance.
         */
        public Httpv build() {
            if (null != config || null == httpd) {
                Httpd.Builder builder = (null != httpd) ? httpd.newBuilder() : new Httpd.Builder();
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

        /**
         * @return The underlying {@link Httpd} instance.
         */
        public Httpd httpd() {
            return httpd;
        }

        /**
         * @return The configured base URL.
         */
        public String baseUrl() {
            return baseUrl;
        }

        /**
         * @return The map of media types.
         */
        public Map<String, String> getMediaTypes() {
            return mediaTypes;
        }

        /**
         * @return The main executor for callbacks.
         */
        public java.util.concurrent.Executor mainExecutor() {
            return mainExecutor;
        }

        /**
         * @return An array of configured preprocessors.
         */
        public Preprocessor[] preprocessors() {
            return preprocessors.toArray(new Preprocessor[0]);
        }

        /**
         * @return The download listener.
         */
        public Downloads.Listener downloadListener() {
            return downloadListener;
        }

        /**
         * @return The response listener.
         */
        public CoverTasks.Listener<CoverResult> responseListener() {
            return responseListener;
        }

        /**
         * @return The exception listener.
         */
        public CoverTasks.Listener<IOException> exceptionListener() {
            return exceptionListener;
        }

        /**
         * @return The completion listener.
         */
        public CoverTasks.Listener<CoverResult.State> completeListener() {
            return completeListener;
        }

        /**
         * @return An array of configured message converters.
         */
        public Convertor[] msgConvertors() {
            return convertors.toArray(new Convertor[0]);
        }

        /**
         * @return The pre-processing timeout multiplier.
         */
        public int preprocTimeoutTimes() {
            return preprocTimeoutTimes;
        }

        /**
         * @return The default charset.
         */
        public Charset charset() {
            return charset;
        }

        /**
         * @return The default body type.
         */
        public String bodyType() {
            return bodyType;
        }

    }

    /**
     * Represents a task that is associated with a tag for easy cancellation.
     */
    public class TagTask {

        /**
         * The tag associated with the task.
         */
        String tag;
        /**
         * The object that can cancel the task.
         */
        final Cancelable canceler;
        /**
         * The HTTP task.
         */
        final CoverHttp<?> task;
        /**
         * The timestamp when the task was created.
         */
        final long createAt;

        TagTask(String tag, Cancelable canceler, CoverHttp<?> task) {
            this.tag = tag;
            this.canceler = canceler;
            this.task = task;
            this.createAt = System.nanoTime();
        }

        /**
         * Checks if the task has expired.
         *
         * @return {@code true} if the task has expired, {@code false} otherwise.
         */
        boolean isExpired() {
            // The lifetime is greater than 10 times the total timeout limit.
            return System.nanoTime() - createAt > 1_000_000L * preprocTimeoutMillis();
        }

        /**
         * Sets the tag for this task.
         *
         * @param tag The new tag.
         */
        public void setTag(String tag) {
            this.tag = tag;
        }

    }

    /**
     * An implementation of the preprocessor chain.
     */
    class RealPreChain implements Preprocessor.PreChain {

        private int index;
        private final Preprocessor[] preprocessors;
        private final CoverHttp<?> coverHttp;
        private final Runnable request;
        private final boolean noSerialPreprocess;

        public RealPreChain(Preprocessor[] preprocessors, CoverHttp<?> coverHttp, Runnable request, int index,
                boolean noSerialPreprocess) {
            this.index = index; // index is greater than or equal to 1
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
                while (index < preprocessors.length && preprocessors[index] instanceof SerialPreprocessor) {
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
