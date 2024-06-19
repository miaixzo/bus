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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.bodys.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CoverHttp<C extends CoverHttp<?>> implements Cancelable {

    private static final String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";

    public Httpv httpv;
    public boolean nothrow;
    public boolean nextOnIO = false;
    public boolean skipPreproc = false;
    public boolean skipSerialPreproc = false;
    private String urlPath;
    private String tag;
    private Map<String, String> headers;
    private Map<String, String> pathParams;
    private Map<String, String> urlParams;
    private Map<String, String> bodyParams;
    private Map<String, FilePara> files;
    private Object requestBody;
    private String dateFormat;
    private String bodyType;
    private Callback<Progress> onProcess;
    private boolean processOnIO;
    private long stepBytes = 0;
    private double stepRate = -1;
    private Object object;
    private Httpv.TagTask tagTask;
    private Cancelable canceler;
    private Charset charset;

    public CoverHttp(Httpv httpv, String url) {
        this.urlPath = url;
        this.httpv = httpv;
        this.charset = httpv.charset();
        this.bodyType = httpv.bodyType();
    }

    /**
     * 获取请求任务的URL地址
     *
     * @return URL地址
     */
    public String getUrl() {
        return urlPath;
    }

    /**
     * 获取请求任务的标签
     *
     * @return 标签
     */
    public String getTag() {
        return tag;
    }

    public String getBodyType() {
        return bodyType;
    }

    /**
     * 标签匹配
     * 判断任务标签与指定的标签是否匹配（包含指定的标签）
     *
     * @param tag 标签
     * @return 是否匹配
     */
    public boolean isTagged(String tag) {
        if (null != this.tag && null != tag) {
            return this.tag.contains(tag);
        }
        return false;
    }

    /**
     * 获取请求任务的头信息
     *
     * @return 头信息
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 获得被绑定的对象
     *
     * @return Object
     */
    public Object getBound() {
        return object;
    }

    /**
     * 设置在发生异常时不向上抛出，设置后：
     * 异步请求可以在异常回调内捕获异常，同步请求在返回结果中找到该异常
     *
     * @return this 实例
     */
    public C nothrow() {
        this.nothrow = true;
        return (C) this;
    }

    /**
     * 指定该请求跳过任何预处理器（包括串行和并行）
     *
     * @return this 实例
     */
    public C skipPreproc() {
        this.skipPreproc = true;
        return (C) this;
    }

    /**
     * 指定该请求跳过任何串行预处理器
     *
     * @return this 实例
     */
    public C skipSerialPreproc() {
        this.skipSerialPreproc = true;
        return (C) this;
    }

    /**
     * @param tag 标签
     * @return this 实例
     * 为请求任务添加标签
     */
    public C tag(String tag) {
        if (null != tag) {
            if (null != this.tag) {
                this.tag = this.tag + Symbol.DOT + tag;
            } else {
                this.tag = tag;
            }
            updateTagTask();
        }
        return (C) this;
    }

    /**
     * @param charset 编码格式
     * @return CoverHttp 实例
     * 设置该请求的编码格式
     */
    public C charset(Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        return (C) this;
    }

    /**
     * @param type 请求类型
     * @return CoverHttp  实例
     * 设置请求体的类型，如：form、json、xml、protobuf 等
     */
    public C bodyType(String type) {
        if (null != type) {
            this.bodyType = type;
        }
        return (C) this;
    }

    /**
     * 下一个回调在IO线程执行
     *
     * @return this 实例
     */
    public C nextOnIO() {
        nextOnIO = true;
        return (C) this;
    }

    /**
     * 绑定一个对象
     *
     * @param object 对象
     * @return this 实例
     */
    public C bind(Object object) {
        this.object = object;
        return (C) this;
    }

    /**
     * 添加请求头
     *
     * @param name  请求头名
     * @param value 请求头值
     * @return this 实例
     */
    public C addHeader(String name, String value) {
        if (null != name && null != value) {
            if (null == headers) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
        }
        return (C) this;
    }

    /**
     * 添加请求头
     *
     * @param headers 请求头集合
     * @return this 实例
     */
    public C addHeader(Map<String, String> headers) {
        if (null != headers) {
            if (null == this.headers) {
                this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
        }
        return (C) this;
    }

    /**
     * 设置Range头信息
     * 表示接收报文体时跳过的字节数，用于断点续传
     *
     * @param rangeStart 表示从 rangeStart 个字节处开始接收，通常是已经下载的字节数，即上次的断点）
     * @return this 实例
     */
    public C setRange(long rangeStart) {
        return addHeader("Range", "bytes=" + rangeStart + Symbol.MINUS);
    }

    /**
     * 设置Range头信息
     * 设置接收报文体时接收的范围，用于分块下载
     *
     * @param rangeStart 表示从 rangeStart 个字节处开始接收
     * @param rangeEnd   表示接收到 rangeEnd 个字节处
     * @return this 实例
     */
    public C setRange(long rangeStart, long rangeEnd) {
        return addHeader("Range", "bytes=" + rangeStart + Symbol.MINUS + rangeEnd);
    }

    /**
     * 设置报文体发送进度回调
     *
     * @param onProcess 进度回调函数
     * @return this 实例
     */
    public C setOnProcess(Callback<Progress> onProcess) {
        this.onProcess = onProcess;
        processOnIO = nextOnIO;
        nextOnIO = false;
        return (C) this;
    }

    /**
     * 设置进度回调的步进字节，默认 8K（8192）
     * 表示每接收 stepBytes 个字节，执行一次进度回调
     *
     * @param stepBytes 步进字节
     * @return this 实例
     */
    public C stepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return (C) this;
    }

    /**
     * 设置进度回调的步进比例
     * 表示每接收 stepRate 比例，执行一次进度回调
     *
     * @param stepRate 步进比例
     * @return this 实例
     */
    public C stepRate(double stepRate) {
        this.stepRate = stepRate;
        return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     *
     * @param name  参数名
     * @param value 参数值
     * @return this 实例
     */
    public C addPathPara(String name, Object value) {
        if (null != name && null != value) {
            if (null == pathParams) {
                pathParams = new HashMap<>();
            }
            pathParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     *
     * @param params 参数集合
     * @return this 实例
     */
    public C addPathPara(Map<String, ?> params) {
        if (null == pathParams) {
            pathParams = new HashMap<>();
        }
        doAddParams(pathParams, params);
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     *
     * @param name  参数名
     * @param value 参数值
     * @return this 实例
     */
    public C addUrlPara(String name, Object value) {
        if (null != name && null != value) {
            if (null == urlParams) {
                urlParams = new HashMap<>();
            }
            urlParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     *
     * @param params 参数集合
     * @return this 实例
     */
    public C addUrlPara(Map<String, ?> params) {
        if (null == urlParams) {
            urlParams = new HashMap<>();
        }
        doAddParams(urlParams, params);
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     *
     * @param name  参数名
     * @param value 参数值
     * @return this 实例
     */
    public C addBodyPara(String name, Object value) {
        if (null != name && null != value) {
            if (null == bodyParams) {
                bodyParams = new HashMap<>();
            }
            bodyParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     *
     * @param params 参数集合
     * @return this 实例
     */
    public C addBodyPara(Map<String, ?> params) {
        if (null == bodyParams) {
            bodyParams = new HashMap<>();
        }
        doAddParams(bodyParams, params);
        return (C) this;
    }

    private void doAddParams(Map<String, String> taskParams, Map<String, ?> params) {
        if (null != params) {
            for (String name : params.keySet()) {
                Object value = params.get(name);
                if (null != name && null != value) {
                    taskParams.put(name, value.toString());
                }
            }
        }
    }

    /**
     * 设置 json 请求体
     *
     * @param body 请求体，字节数组、字符串 或 Java对象（由 MsgConvertor 来序列化）
     * @return this 实例
     */
    public C setBodyPara(Object body) {
        this.requestBody = body;
        return (C) this;
    }

    /**
     * 添加文件参数
     *
     * @param name     参数名
     * @param filePath 文件路径
     * @return this 实例
     */
    public C addFilePara(String name, String filePath) {
        return addFilePara(name, new File(filePath));
    }

    /**
     * 添加文件参数
     *
     * @param name 参数名
     * @param file 文件
     * @return this 实例
     */
    public C addFilePara(String name, File file) {
        if (null != name && null != file && file.exists()) {
            String fileName = file.getName();
            String type = fileName.substring(fileName.lastIndexOf(Symbol.DOT) + 1);
            if (null == files) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, fileName, file));
        }
        return (C) this;
    }

    /**
     * 添加文件参数
     *
     * @param name    参数名
     * @param type    文件类型: 如 png、jpg、jpeg 等
     * @param content 文件内容
     * @return this 实例
     */
    public C addFilePara(String name, String type, byte[] content) {
        return addFilePara(name, type, null, content);
    }

    /**
     * 添加文件参数
     *
     * @param name     参数名
     * @param type     文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param content  文件内容
     * @return this 实例
     */
    public C addFilePara(String name, String type, String fileName, byte[] content) {
        if (null != name && null != content) {
            if (null == files) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, fileName, content));
        }
        return (C) this;
    }

    @Override
    public boolean cancel() {
        if (null != canceler) {
            return canceler.cancel();
        }
        return false;
    }

    protected void registeTagTask(Cancelable canceler) {
        if (null != tag && null == tagTask) {
            tagTask = httpv.addTagTask(tag, canceler, this);
        }
        this.canceler = canceler;
    }

    private void updateTagTask() {
        if (null != tagTask) {
            tagTask.setTag(tag);
        } else if (null != canceler) {
            registeTagTask(canceler);
        }
    }

    protected void removeTagTask() {
        if (null != tag) {
            httpv.removeTagTask(this);
        }
    }

    protected NewCall prepareCall(String method) {
        Request request = prepareRequest(method);
        return httpv.request(request);
    }

    protected Request prepareRequest(String method) {
        boolean bodyCanUsed = Http.permitsRequestBody(method);
        assertNotConflict(!bodyCanUsed);
        Request.Builder builder = new Request.Builder()
                .url(buildUrlPath());
        buildHeaders(builder);
        if (bodyCanUsed) {
            RequestBody reqBody = buildRequestBody();
            if (null != onProcess) {
                long contentLength = contentLength(reqBody);
                if (stepRate > 0 && stepRate <= 1) {
                    stepBytes = (long) (contentLength * stepRate);
                }
                if (stepBytes <= 0) {
                    stepBytes = Progress.DEFAULT_STEP_BYTES;
                }
                reqBody = new ProgressBody(reqBody, onProcess,
                        httpv.executor().getExecutor(processOnIO),
                        contentLength, stepBytes);
            }
            builder.method(method, reqBody);
        } else {
            builder.method(method, null);
        }
        if (null != tag) {
            builder.tag(String.class, tag);
        }
        return builder.build();
    }

    private long contentLength(RequestBody reqBody) {
        try {
            return reqBody.length();
        } catch (IOException e) {
            throw new InternalException("Cannot get the length of the request body", e);
        }
    }

    private void buildHeaders(Request.Builder builder) {
        if (null != headers) {
            for (String name : headers.keySet()) {
                String value = headers.get(name);
                if (null != value) {
                    builder.addHeader(name, value);
                }
            }
        }
    }

    public CoverResult.State toState(IOException e) {
        if (e instanceof SocketTimeoutException) {
            return CoverResult.State.TIMEOUT;
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return CoverResult.State.NETWORK_ERROR;
        }
        String msg = e.getMessage();
        if (null != msg && ("Canceled".equals(msg) || e instanceof SocketException
                && (msg.startsWith("Socket operation on nonsocket") || "Socket closed".equals(msg)))) {
            return CoverResult.State.CANCELED;
        }
        return CoverResult.State.EXCEPTION;
    }

    private RequestBody buildRequestBody() {
        if (null != files) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
            if (null != bodyParams) {
                for (String name : bodyParams.keySet()) {
                    byte[] value = bodyParams.get(name).getBytes(charset);
                    RequestBody body = RequestBody.create(null, value);
                    builder.addPart(MultipartBody.Part.createFormData(name, null, body));
                }
            }
            for (String name : files.keySet()) {
                FilePara file = files.get(name);
                MediaType type = httpv.mediaType(file.type);
                RequestBody bodyPart;
                if (null != file.file) {
                    bodyPart = RequestBody.create(type, file.file);
                } else {
                    bodyPart = RequestBody.create(type, file.content);
                }
                builder.addFormDataPart(name, file.fileName, bodyPart);
            }
            return builder.build();
        }
        if (null != requestBody) {
            return toRequestBody(requestBody);
        }
        if (null == bodyParams) {
            return new FormBody.Builder(charset).build();
        }
        if (Http.FORM.equalsIgnoreCase(bodyType)) {
            FormBody.Builder builder = new FormBody.Builder(charset);
            for (String name : bodyParams.keySet()) {
                String value = bodyParams.get(name);
                builder.add(name, value);
            }
            return builder.build();
        }
        return toRequestBody(bodyParams);
    }

    private RequestBody toRequestBody(Object object) {
        if (object instanceof byte[] || object instanceof String) {
            String mediaType = httpv.executor().doMsgConvert(bodyType, null).mediaType;
            byte[] body = object instanceof byte[] ? (byte[]) object : ((String) object).getBytes(charset);
            return RequestBody.create(MediaType.valueOf(mediaType + "; charset=" + charset.name()), body);
        }
        CoverTasks.Executor.Data<byte[]> data = httpv.executor()
                .doMsgConvert(bodyType, (Convertor c) -> c.serialize(object, dateFormat, charset));
        return RequestBody.create(MediaType.valueOf(data.mediaType + "; charset=" + charset.name()), data.data);
    }

    private String buildUrlPath() {
        String url = urlPath;
        if (null == url || url.trim().isEmpty()) {
            throw new InternalException("Url cannot be empty!");
        }
        if (null != pathParams) {
            for (String name : pathParams.keySet()) {
                String target = "{" + name + "}";
                if (url.contains(target)) {
                    url = url.replace(target, pathParams.get(name));
                } else {
                    throw new InternalException("pathParameter [ " + name + " ] Does not exist in url [ " + urlPath + " ]");
                }
            }
        }
        if (url.matches(PATH_PARAM_REGEX)) {
            throw new InternalException("There is no setting for pathParameter in url, you must first call addPathParam to set it!");
        }
        if (null != urlParams) {
            url = buildUrl(url.trim());
        }
        return url;
    }

    private String buildUrl(String url) {
        StringBuilder sb = new StringBuilder(url);
        if (url.contains(Symbol.QUESTION_MARK)) {
            if (!url.endsWith(Symbol.QUESTION_MARK)) {
                if (url.lastIndexOf(Symbol.EQUAL) < url.lastIndexOf(Symbol.QUESTION_MARK) + 2) {
                    throw new InternalException("URL format error，'？' Not found after '='");
                }
                if (!url.endsWith(Symbol.AND)) {
                    sb.append(Symbol.C_AND);
                }
            }
        } else {
            sb.append(Symbol.C_QUESTION_MARK);
        }
        for (String name : urlParams.keySet()) {
            sb.append(name).append(Symbol.C_EQUAL).append(urlParams.get(name)).append(Symbol.C_AND);
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    protected void assertNotConflict(boolean bodyCantUsed) {
        if (bodyCantUsed) {
            if (ObjectKit.isNotEmpty(requestBody)) {
                throw new InternalException("GET | HEAD request The setBodyPara method cannot be called!");
            }
            if (MapKit.isNotEmpty(bodyParams)) {
                throw new InternalException("GET | HEAD request The addBodyPara method cannot be called!");
            }
            if (MapKit.isNotEmpty(files)) {
                throw new InternalException("GET | HEAD request The addFilePara method cannot be called!");
            }
        }
        if (ObjectKit.isNotEmpty(requestBody)) {
            if (MapKit.isNotEmpty(bodyParams)) {
                throw new InternalException("The methods addBodyPara and setBodyPara cannot be called at the same time!");
            }
            if (MapKit.isNotEmpty(files)) {
                throw new InternalException("The methods addFilePara and setBodyPara cannot be called at the same time!");
            }
        }
    }

    /**
     * @param latch CountDownLatch
     * @return 是否未超时：false 表示已超时
     */
    protected boolean timeoutAwait(CountDownLatch latch) {
        try {
            return latch.await(httpv.preprocTimeoutMillis(),
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new InternalException("TimeOut " + CoverResult.State.TIMEOUT);
        }
    }

    protected CoverResult timeoutResult() {
        if (nothrow) {
            return new CoverResult.Real(this, CoverResult.State.TIMEOUT);
        }
        throw new InternalException("Execution timeout " + CoverResult.State.TIMEOUT);
    }

    public Charset charset(Response response) {
        ResponseBody b = response.body();
        MediaType mediaType = null != b ? b.mediaType() : null;
        return null != mediaType ? mediaType.charset(charset) : charset;
    }

    static class FilePara {

        String type;
        String fileName;
        byte[] content;
        File file;

        FilePara(String type, String fileName, byte[] content) {
            this.type = type;
            this.fileName = fileName;
            this.content = content;
        }

        FilePara(String type, String fileName, File file) {
            this.type = type;
            this.fileName = fileName;
            this.file = file;
        }

    }

    /**
     * 同步 Http 请求任务
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Sync extends CoverHttp<Sync> {

        public Sync(Httpv client, String url) {
            super(client, url);
        }

        /**
         * 发起 GET 请求（Rest：获取资源，幂等）
         *
         * @return 请求结果
         */
        public CoverResult get() {
            return request(Http.GET);
        }

        /**
         * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
         *
         * @return 请求结果
         */
        public CoverResult head() {
            return request(Http.HEAD);
        }

        /**
         * 发起 POST 请求（Rest：创建资源，非幂等）
         *
         * @return 请求结果
         */
        public CoverResult post() {
            return request(Http.POST);
        }

        /**
         * 发起 PUT 请求（Rest：更新资源，幂等）
         *
         * @return 请求结果
         */
        public CoverResult put() {
            return request(Http.PUT);
        }

        /**
         * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
         *
         * @return HttpCall
         */
        public CoverResult patch() {
            return request(Http.PATCH);
        }

        /**
         * 发起 DELETE 请求（Rest：删除资源，幂等）
         *
         * @return 请求结果
         */
        public CoverResult delete() {
            return request(Http.DELETE);
        }

        /**
         * 发起 HTTP 请求
         *
         * @param method 请求方法
         * @return 请求结果
         */
        public CoverResult request(String method) {
            if (null == method || method.isEmpty()) {
                throw new IllegalArgumentException("Request method method cannot be empty!");
            }
            CoverResult.Real result = new CoverResult.Real(this, httpv.executor());
            SyncHttpCall httpCall = new SyncHttpCall();
            // 注册标签任务
            registeTagTask(httpCall);
            CountDownLatch latch = new CountDownLatch(1);
            httpv.preprocess(this, () -> {
                synchronized (httpCall) {
                    if (httpCall.canceled) {
                        result.exception(CoverResult.State.CANCELED, null);
                        latch.countDown();
                        return;
                    }
                    httpCall.call = prepareCall(method);
                }
                try {
                    result.response(httpCall.call.execute());
                    httpCall.done = true;
                } catch (IOException e) {
                    result.exception(toState(e), e);
                } finally {
                    latch.countDown();
                }
            }, skipPreproc, skipSerialPreproc);
            boolean timeout = false;
            if (null == result.getState()) {
                timeout = !timeoutAwait(latch);
            }
            // 移除标签任务
            removeTagTask();
            if (timeout) {
                httpCall.cancel();
                return timeoutResult();
            }
            IOException e = result.getError();
            CoverResult.State state = result.getState();
            if (null != e && state != CoverResult.State.CANCELED
                    && !nothrow) {
                throw new InternalException("Abnormal execution", e);
            }
            return result;
        }


        static class SyncHttpCall implements Cancelable {

            NewCall call;
            boolean done = false;
            boolean canceled = false;

            @Override
            public synchronized boolean cancel() {
                if (done) {
                    return false;
                }
                if (null != call) {
                    call.cancel();
                }
                canceled = true;
                return true;
            }

        }

    }

    /**
     * 异步 Http 请求任务
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Async extends CoverHttp<Async> {

        private Callback<CoverResult> onResponse;
        private Callback<IOException> onException;
        private Callback<CoverResult.State> onComplete;
        private boolean rOnIO;
        private boolean eOnIO;
        private boolean cOnIO;

        public Async(Httpv htttpv, String url) {
            super(htttpv, url);
        }

        /**
         * 设置请求执行异常后的回调函数，设置后，相关异常将不再向上抛出
         *
         * @param onException 请求异常回调
         * @return AsyncHttp 实例
         */
        public Async setOnException(Callback<IOException> onException) {
            this.onException = onException;
            eOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * 设置请求执行完成后的回调函数，无论成功|失败|异常 都会被执行
         *
         * @param onComplete 请求完成回调
         * @return AsyncHttp 实例
         */
        public Async setOnComplete(Callback<CoverResult.State> onComplete) {
            this.onComplete = onComplete;
            cOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * 设置请求得到响应后的回调函数
         *
         * @param onResponse 请求响应回调
         * @return AsyncHttp 实例
         */
        public Async setOnResponse(Callback<CoverResult> onResponse) {
            this.onResponse = onResponse;
            rOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * 发起 GET 请求（Rest：读取资源，幂等）
         *
         * @return GiveCall
         */
        public GiveCall get() {
            return request(Http.GET);
        }

        /**
         * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
         *
         * @return GiveCall
         */
        public GiveCall head() {
            return request(Http.HEAD);
        }

        /**
         * 发起 POST 请求（Rest：创建资源，非幂等）
         *
         * @return GiveCall
         */
        public GiveCall post() {
            return request(Http.POST);
        }

        /**
         * 发起 PUT 请求（Rest：更新资源，幂等）
         *
         * @return GiveCall
         */
        public GiveCall put() {
            return request(Http.PUT);
        }

        /**
         * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
         *
         * @return GiveCall
         */
        public GiveCall patch() {
            return request(Http.PATCH);
        }

        /**
         * 发起 DELETE 请求（Rest：删除资源，幂等）
         *
         * @return GiveCall
         */
        public GiveCall delete() {
            return request(Http.DELETE);
        }

        /**
         * 发起 HTTP 请求
         *
         * @param method 请求方法
         * @return GiveCall
         */
        public GiveCall request(String method) {
            if (null == method || method.isEmpty()) {
                throw new IllegalArgumentException("Request method method cannot be empty!");
            }
            PreGiveCall call = new PreGiveCall();
            registeTagTask(call);
            httpv.preprocess(this, () -> {
                synchronized (call) {
                    if (call.canceled) {
                        removeTagTask();
                    } else {
                        call.setCall(executeCall(prepareCall(method)));
                    }
                }
            }, skipPreproc, skipSerialPreproc);
            return call;
        }

        private GiveCall executeCall(NewCall call) {
            OkGiveCall httpCall = new OkGiveCall(call);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(NewCall call, IOException error) {
                    CoverResult.State state = toState(error);
                    CoverResult result = new CoverResult.Real(Async.this, state, error);
                    onCallback(httpCall, result, () -> {
                        CoverTasks.Executor executor = httpv.executor();
                        executor.executeOnComplete(Async.this, onComplete, state, cOnIO);
                        if (!executor.executeOnException(Async.this, onException, error, eOnIO)
                                && !nothrow) {
                            throw new InternalException(error.getMessage(), error);
                        }
                    });
                }

                @Override
                public void onResponse(NewCall call, Response response) {
                    CoverTasks.Executor executor = httpv.executor();
                    CoverResult result = new CoverResult.Real(Async.this, response, executor);
                    onCallback(httpCall, result, () -> {
                        executor.executeOnComplete(Async.this, onComplete, CoverResult.State.RESPONSED, cOnIO);
                        executor.executeOnResponse(Async.this, onResponse, result, rOnIO);
                    });
                }

            });
            return httpCall;
        }

        private void onCallback(OkGiveCall httpCall, CoverResult result, Runnable runnable) {
            synchronized (httpCall) {
                removeTagTask();
                if (httpCall.isCanceled() || result.getState() == CoverResult.State.CANCELED) {
                    httpCall.setResult(new CoverResult.Real(Async.this, CoverResult.State.CANCELED));
                    return;
                }
                httpCall.setResult(result);
                runnable.run();
            }
        }

        class PreGiveCall implements GiveCall {

            GiveCall call;
            boolean canceled = false;
            CountDownLatch latch = new CountDownLatch(1);

            @Override
            public synchronized boolean cancel() {
                canceled = null == call || call.cancel();
                latch.countDown();
                return canceled;
            }

            @Override
            public boolean isDone() {
                if (null != call) {
                    return call.isDone();
                }
                return canceled;
            }

            @Override
            public boolean isCanceled() {
                return canceled;
            }

            void setCall(GiveCall call) {
                this.call = call;
                latch.countDown();
            }

            @Override
            public CoverResult getResult() {
                if (!timeoutAwait(latch)) {
                    cancel();
                    return timeoutResult();
                }
                if (canceled || null == call) {
                    return new CoverResult.Real(Async.this, CoverResult.State.CANCELED);
                }
                return call.getResult();
            }

        }

        class OkGiveCall implements GiveCall {

            NewCall call;
            CoverResult result;
            CountDownLatch latch = new CountDownLatch(1);

            OkGiveCall(NewCall call) {
                this.call = call;
            }

            @Override
            public synchronized boolean cancel() {
                if (null == result) {
                    call.cancel();
                    return true;
                }
                return false;
            }

            @Override
            public boolean isDone() {
                return null != result;
            }

            @Override
            public boolean isCanceled() {
                return call.isCanceled();
            }

            @Override
            public CoverResult getResult() {
                if (null == result) {
                    if (!timeoutAwait(latch)) {
                        cancel();
                        return timeoutResult();
                    }
                }
                return result;
            }

            void setResult(CoverResult result) {
                this.result = result;
                latch.countDown();
            }

        }

    }
}
