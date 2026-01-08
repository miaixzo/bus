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
package org.miaixz.bus.http.plugin.httpv;

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

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.bodys.ResponseBody;

/**
 * An abstract base class for building HTTP requests, providing a fluent interface. It supports synchronous,
 * asynchronous, and WebSocket requests.
 *
 * @param <C> The concrete subclass type, for method chaining.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CoverHttp<C extends CoverHttp<?>> implements Cancelable {

    /**
     * Regular expression to validate if a URL path still contains un-replaced path parameters.
     */
    private static final String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";

    /**
     * The core HTTP client instance.
     */
    public Httpv httpv;
    /**
     * If true, exceptions will not be thrown but will be available in the result object.
     */
    public boolean nothrow;
    /**
     * If true, the next callback will be executed on an I/O thread.
     */
    public boolean nextOnIO = false;
    /**
     * If true, all preprocessors (both serial and parallel) will be skipped for this request.
     */
    public boolean skipPreproc = false;
    /**
     * If true, serial preprocessors will be skipped for this request.
     */
    public boolean skipSerialPreproc = false;
    /**
     * The URL path for the request.
     */
    private String urlPath;
    /**
     * A tag for identifying or canceling the request.
     */
    private String tag;
    /**
     * A map of request headers.
     */
    private Map<String, String> headers;
    /**
     * A map of parameters to be substituted in the URL path (e.g., /users/{id}).
     */
    private Map<String, String> pathParams;
    /**
     * A map of parameters to be appended to the URL query string.
     */
    private Map<String, String> urlParams;
    /**
     * A map of parameters to be included in the request body.
     */
    private Map<String, String> bodyParams;
    /**
     * A map of file parameters for multipart requests.
     */
    private Map<String, FilePara> files;
    /**
     * The request body object, to be serialized by a message converter.
     */
    private Object requestBody;
    /**
     * A specific date format to be used during serialization.
     */
    private String dateFormat;
    /**
     * The type of the request body (e.g., "json", "xml", "form").
     */
    private String bodyType;
    /**
     * A callback for tracking request body upload progress.
     */
    private Callback<Progress> onProcess;
    /**
     * If true, the progress callback will be executed on an I/O thread.
     */
    private boolean processOnIO;
    /**
     * The progress callback will be triggered every `stepBytes` bytes.
     */
    private long stepBytes = 0;
    /**
     * The progress callback will be triggered at percentage increments defined by this rate.
     */
    private double stepRate = -1;
    /**
     * An arbitrary object that can be attached to this request.
     */
    private Object object;
    /**
     * The task associated with the tag, used for cancellation.
     */
    private Httpv.TagTask tagTask;
    /**
     * The object responsible for canceling this request.
     */
    private Cancelable canceler;
    /**
     * The character set for this request.
     */
    private Charset charset;

    /**
     * Constructs a new CoverHttp request builder.
     *
     * @param httpv The core Httpv client instance.
     * @param url   The base URL or URL path for the request.
     */
    public CoverHttp(Httpv httpv, String url) {
        this.urlPath = url;
        this.httpv = httpv;
        this.charset = httpv.charset();
        this.bodyType = httpv.bodyType();
    }

    /**
     * Gets the URL of this request task.
     *
     * @return The URL string.
     */
    public String getUrl() {
        return urlPath;
    }

    /**
     * Gets the tag of this request task.
     *
     * @return The tag string.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets the body type of this request task.
     *
     * @return The body type string (e.g., "json", "form").
     */
    public String getBodyType() {
        return bodyType;
    }

    /**
     * Checks if this request's tag matches (contains) the given tag.
     *
     * @param tag The tag to check against.
     * @return True if the request is tagged with the specified tag, false otherwise.
     */
    public boolean isTagged(String tag) {
        if (null != this.tag && null != tag) {
            return this.tag.contains(tag);
        }
        return false;
    }

    /**
     * Gets the headers of this request task.
     *
     * @return A map of headers.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the object bound to this request.
     *
     * @return The bound object.
     */
    public Object getBound() {
        return object;
    }

    /**
     * Configures the request to not throw exceptions on failure. Instead, the exception will be available in the
     * `CoverResult` object.
     *
     * @return This instance for chaining.
     */
    public C nothrow() {
        this.nothrow = true;
        return (C) this;
    }

    /**
     * Specifies that this request should skip all preprocessors (both serial and parallel).
     *
     * @return This instance for chaining.
     */
    public C skipPreproc() {
        this.skipPreproc = true;
        return (C) this;
    }

    /**
     * Specifies that this request should skip any serial preprocessors.
     *
     * @return This instance for chaining.
     */
    public C skipSerialPreproc() {
        this.skipSerialPreproc = true;
        return (C) this;
    }

    /**
     * Adds a tag to the request. Multiple tags can be added and will be concatenated with dots.
     *
     * @param tag The tag to add.
     * @return This instance for chaining.
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
     * Sets the character encoding for this request.
     *
     * @param charset The character set.
     * @return This instance for chaining.
     */
    public C charset(Charset charset) {
        if (null != charset) {
            this.charset = charset;
        }
        return (C) this;
    }

    /**
     * Sets the request body type, such as "form", "json", "xml", "protobuf", etc. This determines which message
     * converter will be used for serialization.
     *
     * @param type The body type.
     * @return This instance for chaining.
     */
    public C bodyType(String type) {
        if (null != type) {
            this.bodyType = type;
        }
        return (C) this;
    }

    /**
     * Specifies that the next callback should be executed on an I/O thread.
     *
     * @return This instance for chaining.
     */
    public C nextOnIO() {
        nextOnIO = true;
        return (C) this;
    }

    /**
     * Binds an arbitrary object to this request.
     *
     * @param object The object to bind.
     * @return This instance for chaining.
     */
    public C bind(Object object) {
        this.object = object;
        return (C) this;
    }

    /**
     * Adds a request header.
     *
     * @param name  The header name.
     * @param value The header value.
     * @return This instance for chaining.
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
     * Adds multiple request headers.
     *
     * @param headers A map of headers to add.
     * @return This instance for chaining.
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
     * Sets the Range header to resume a download.
     *
     * @param rangeStart The byte offset to start receiving data from (inclusive).
     * @return This instance for chaining.
     */
    public C setRange(long rangeStart) {
        return addHeader("Range", "bytes=" + rangeStart + Symbol.MINUS);
    }

    /**
     * Sets the Range header to download a specific chunk of a file.
     *
     * @param rangeStart The starting byte offset (inclusive).
     * @param rangeEnd   The ending byte offset (inclusive).
     * @return This instance for chaining.
     */
    public C setRange(long rangeStart, long rangeEnd) {
        return addHeader("Range", "bytes=" + rangeStart + Symbol.MINUS + rangeEnd);
    }

    /**
     * Sets a callback to monitor the progress of the request body upload.
     *
     * @param onProcess The progress callback function.
     * @return This instance for chaining.
     */
    public C setOnProcess(Callback<Progress> onProcess) {
        this.onProcess = onProcess;
        processOnIO = nextOnIO;
        nextOnIO = false;
        return (C) this;
    }

    /**
     * Sets the step size in bytes for progress callbacks. The callback will be triggered approximately every
     * `stepBytes` bytes are transferred. Defaults to 8KB.
     *
     * @param stepBytes The step size in bytes.
     * @return This instance for chaining.
     */
    public C stepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return (C) this;
    }

    /**
     * Sets the step rate for progress callbacks. The callback will be triggered at percentage increments defined by
     * this rate (e.g., 0.01 for every 1%).
     *
     * @param stepRate The step rate (between 0.0 and 1.0).
     * @return This instance for chaining.
     */
    public C stepRate(double stepRate) {
        this.stepRate = stepRate;
        return (C) this;
    }

    /**
     * Adds a path parameter to be replaced in the URL (e.g., for /api/user/{id}).
     *
     * @param name  The parameter name (without braces).
     * @param value The parameter value.
     * @return This instance for chaining.
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
     * Adds multiple path parameters.
     *
     * @param params A map of path parameters.
     * @return This instance for chaining.
     */
    public C addPathPara(Map<String, ?> params) {
        if (null == pathParams) {
            pathParams = new HashMap<>();
        }
        doAddParams(pathParams, params);
        return (C) this;
    }

    /**
     * Adds a URL parameter to be appended to the query string.
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance for chaining.
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
     * Adds multiple URL parameters.
     *
     * @param params A map of URL parameters.
     * @return This instance for chaining.
     */
    public C addUrlPara(Map<String, ?> params) {
        if (null == urlParams) {
            urlParams = new HashMap<>();
        }
        doAddParams(urlParams, params);
        return (C) this;
    }

    /**
     * Adds a parameter to be included in the request body (e.g., for form submissions).
     *
     * @param name  The parameter name.
     * @param value The parameter value.
     * @return This instance for chaining.
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
     * Adds multiple body parameters.
     *
     * @param params A map of body parameters.
     * @return This instance for chaining.
     */
    public C addBodyPara(Map<String, ?> params) {
        if (null == bodyParams) {
            bodyParams = new HashMap<>();
        }
        doAddParams(bodyParams, params);
        return (C) this;
    }

    /**
     * Helper method to add parameters to a map.
     *
     * @param taskParams The map to add parameters to.
     * @param params     The parameters to add.
     */
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
     * Sets the request body directly. The body can be a byte array, a string, or a Java object that will be serialized
     * by a configured message converter.
     *
     * @param body The request body.
     * @return This instance for chaining.
     */
    public C setBodyPara(Object body) {
        this.requestBody = body;
        return (C) this;
    }

    /**
     * Adds a file parameter for a multipart request.
     *
     * @param name     The parameter name.
     * @param filePath The path to the file.
     * @return This instance for chaining.
     */
    public C addFilePara(String name, String filePath) {
        return addFilePara(name, new File(filePath));
    }

    /**
     * Adds a file parameter for a multipart request.
     *
     * @param name The parameter name.
     * @param file The file object.
     * @return This instance for chaining.
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
     * Adds a file parameter from a byte array for a multipart request.
     *
     * @param name    The parameter name.
     * @param type    The file type/extension (e.g., "png", "jpg").
     * @param content The file content as a byte array.
     * @return This instance for chaining.
     */
    public C addFilePara(String name, String type, byte[] content) {
        return addFilePara(name, type, null, content);
    }

    /**
     * Adds a file parameter from a byte array for a multipart request.
     *
     * @param name     The parameter name.
     * @param type     The file type/extension (e.g., "png", "jpg").
     * @param fileName The name of the file.
     * @param content  The file content as a byte array.
     * @return This instance for chaining.
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

    /**
     * Cancels the request.
     *
     * @return True if the request was successfully canceled, false otherwise.
     */
    @Override
    public boolean cancel() {
        if (null != canceler) {
            return canceler.cancel();
        }
        return false;
    }

    /**
     * Registers this task with the tag manager.
     *
     * @param canceler The object that can cancel this task.
     */
    protected void registeTagTask(Cancelable canceler) {
        if (null != tag && null == tagTask) {
            tagTask = httpv.addTagTask(tag, canceler, this);
        }
        this.canceler = canceler;
    }

    /**
     * Updates the tag in the tag manager if it has changed.
     */
    private void updateTagTask() {
        if (null != tagTask) {
            tagTask.setTag(tag);
        } else if (null != canceler) {
            registeTagTask(canceler);
        }
    }

    /**
     * Removes this task from the tag manager.
     */
    protected void removeTagTask() {
        if (null != tag) {
            httpv.removeTagTask(this);
        }
    }

    /**
     * Prepares the Httpd Call object for execution.
     *
     * @param method The HTTP method (e.g., "GET", "POST").
     * @return A new Call instance.
     */
    protected NewCall prepareCall(String method) {
        Request request = prepareRequest(method);
        return httpv.request(request);
    }

    /**
     * Prepares the Httpd Request object.
     *
     * @param method The HTTP method.
     * @return A new Request instance.
     */
    protected Request prepareRequest(String method) {
        boolean bodyCanUsed = HTTP.permitsRequestBody(method);
        assertNotConflict(!bodyCanUsed);
        Request.Builder builder = new Request.Builder().url(buildUrlPath());
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
                reqBody = new ProgressBody(reqBody, onProcess, httpv.executor().getExecutor(processOnIO), contentLength,
                        stepBytes);
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

    /**
     * Gets the content length of a request body.
     *
     * @param reqBody The request body.
     * @return The content length in bytes.
     */
    private long contentLength(RequestBody reqBody) {
        try {
            return reqBody.contentLength();
        } catch (IOException e) {
            throw new InternalException("Cannot get the length of the request body", e);
        }
    }

    /**
     * Adds configured headers to the request builder.
     *
     * @param builder The request builder.
     */
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

    /**
     * Converts an IOException into a CoverResult.State.
     *
     * @param e The IOException.
     * @return The corresponding state.
     */
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

    /**
     * Builds the RequestBody based on the configured parameters (body, files, body params).
     *
     * @return The constructed RequestBody.
     */
    private RequestBody buildRequestBody() {
        if (null != files) {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
            if (null != bodyParams) {
                for (String name : bodyParams.keySet()) {
                    byte[] value = bodyParams.get(name).getBytes(charset);
                    RequestBody body = RequestBody.of(null, value);
                    builder.addPart(MultipartBody.Part.formData(name, null, body));
                }
            }
            for (String name : files.keySet()) {
                FilePara file = files.get(name);
                MediaType type = httpv.contentType(file.type);
                RequestBody bodyPart;
                if (null != file.file) {
                    bodyPart = RequestBody.of(type, file.file);
                } else {
                    bodyPart = RequestBody.of(type, file.content);
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
        if (HTTP.FORM.equalsIgnoreCase(bodyType)) {
            FormBody.Builder builder = new FormBody.Builder(charset);
            for (String name : bodyParams.keySet()) {
                String value = bodyParams.get(name);
                builder.add(name, value);
            }
            return builder.build();
        }
        return toRequestBody(bodyParams);
    }

    /**
     * Converts an object into a RequestBody using a message converter.
     *
     * @param object The object to convert.
     * @return The resulting RequestBody.
     */
    private RequestBody toRequestBody(Object object) {
        if (object instanceof byte[] || object instanceof String) {
            byte[] body = object instanceof byte[] ? (byte[]) object : ((String) object).getBytes(charset);
            return RequestBody.of(
                    MediaType.valueOf(
                            httpv.executor().doMsgConvert(bodyType, null).contentType + "; charset=" + charset.name()),
                    body);
        }
        CoverTasks.Executor.Data<byte[]> data = httpv.executor()
                .doMsgConvert(bodyType, (Convertor c) -> c.serialize(object, dateFormat, charset));
        return RequestBody.of(MediaType.valueOf(data.contentType + "; charset=" + charset.name()), data.data);
    }

    /**
     * Builds the final URL path, substituting path parameters and appending URL parameters.
     *
     * @return The final URL string.
     */
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
                    throw new InternalException(
                            "pathParameter [ " + name + " ] Does not exist in url [ " + urlPath + " ]");
                }
            }
        }
        if (url.matches(PATH_PARAM_REGEX)) {
            throw new InternalException(
                    "There is no setting for pathParameter in url, you must first call addPathParam to set it!");
        }
        if (null != urlParams) {
            url = buildUrl(url.trim());
        }
        return url;
    }

    /**
     * Appends URL parameters to a base URL.
     *
     * @param url The base URL.
     * @return The URL with appended parameters.
     */
    private String buildUrl(String url) {
        StringBuilder sb = new StringBuilder(url);
        if (url.contains(Symbol.QUESTION_MARK)) {
            if (!url.endsWith(Symbol.QUESTION_MARK)) {
                if (url.lastIndexOf(Symbol.EQUAL) < url.lastIndexOf(Symbol.QUESTION_MARK) + 2) {
                    throw new InternalException("URL format error, '?' Not found after '='");
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

    /**
     * Asserts that there are no conflicting body parameter settings.
     *
     * @param bodyCantUsed True if the HTTP method does not permit a request body.
     */
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
                throw new InternalException(
                        "The methods addBodyPara and setBodyPara cannot be called at the same time!");
            }
            if (MapKit.isNotEmpty(files)) {
                throw new InternalException(
                        "The methods addFilePara and setBodyPara cannot be called at the same time!");
            }
        }
    }

    /**
     * Awaits on a CountDownLatch with a timeout.
     *
     * @param latch The CountDownLatch to wait on.
     * @return False if a timeout occurred, true otherwise.
     */
    protected boolean timeoutAwait(CountDownLatch latch) {
        try {
            return latch.await(httpv.preprocTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("TimeOut " + CoverResult.State.TIMEOUT, e);
        }
    }

    /**
     * Creates a result object for a timeout event.
     *
     * @return A timeout CoverResult.
     */
    protected CoverResult timeoutResult() {
        if (nothrow) {
            return new CoverResult.Real(this, CoverResult.State.TIMEOUT);
        }
        throw new InternalException("Execution timeout " + CoverResult.State.TIMEOUT);
    }

    /**
     * Extracts the character set from an HTTP response.
     *
     * @param response The HTTP response.
     * @return The character set, or the default if not specified.
     */
    public Charset charset(Response response) {
        ResponseBody b = response.body();
        MediaType contentType = null != b ? b.contentType() : null;
        return null != contentType ? contentType.charset(charset) : charset;
    }

    /**
     * A container for file parameters used in multipart requests.
     */
    static class FilePara {

        /**
         * The file type or extension.
         */
        String type;
        /**
         * The name of the file.
         */
        String fileName;
        /**
         * The file content as a byte array.
         */
        byte[] content;
        /**
         * The file object.
         */
        File file;

        /**
         * Constructor for file from byte array.
         *
         * @param type     The file type.
         * @param fileName The file name.
         * @param content  The file content.
         */
        FilePara(String type, String fileName, byte[] content) {
            this.type = type;
            this.fileName = fileName;
            this.content = content;
        }

        /**
         * Constructor for file from a File object.
         *
         * @param type     The file type.
         * @param fileName The file name.
         * @param file     The file object.
         */
        FilePara(String type, String fileName, File file) {
            this.type = type;
            this.fileName = fileName;
            this.file = file;
        }

    }

    /**
     * A builder for synchronous HTTP requests.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Sync extends CoverHttp<Sync> {

        /**
         * Constructs a new synchronous request builder.
         *
         * @param client The Httpv instance.
         * @param url    The request URL.
         */
        public Sync(Httpv client, String url) {
            super(client, url);
        }

        /**
         * Executes a GET request (REST: retrieve a resource, idempotent).
         *
         * @return The request result.
         */
        public CoverResult get() {
            return request(HTTP.GET);
        }

        /**
         * Executes a HEAD request (REST: retrieve resource headers, idempotent).
         *
         * @return The request result.
         */
        public CoverResult head() {
            return request(HTTP.HEAD);
        }

        /**
         * Executes a POST request (REST: create a resource, not idempotent).
         *
         * @return The request result.
         */
        public CoverResult post() {
            return request(HTTP.POST);
        }

        /**
         * Executes a PUT request (REST: update/replace a resource, idempotent).
         *
         * @return The request result.
         */
        public CoverResult put() {
            return request(HTTP.PUT);
        }

        /**
         * Executes a PATCH request (REST: partially update a resource, idempotent).
         *
         * @return The request result.
         */
        public CoverResult patch() {
            return request(HTTP.PATCH);
        }

        /**
         * Executes a DELETE request (REST: delete a resource, idempotent).
         *
         * @return The request result.
         */
        public CoverResult delete() {
            return request(HTTP.DELETE);
        }

        /**
         * Executes an HTTP request with the specified method.
         *
         * @param method The HTTP method.
         * @return The request result.
         */
        public CoverResult request(String method) {
            if (null == method || method.isEmpty()) {
                throw new IllegalArgumentException("Request method method cannot be empty!");
            }
            CoverResult.Real result = new CoverResult.Real(this, httpv.executor());
            SyncHttpCall httpCall = new SyncHttpCall();
            // Register tag task
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
            // Remove tag task
            removeTagTask();
            if (timeout) {
                httpCall.cancel();
                return timeoutResult();
            }
            IOException e = result.getError();
            CoverResult.State state = result.getState();
            if (null != e && state != CoverResult.State.CANCELED && !nothrow) {
                throw new InternalException("Abnormal execution", e);
            }
            return result;
        }

        /**
         * A cancelable wrapper for a synchronous Httpd Call.
         */
        static class SyncHttpCall implements Cancelable {

            /**
             * The underlying Httpd call.
             */
            NewCall call;
            /**
             * Flag indicating if the call has completed.
             */
            boolean done = false;
            /**
             * Flag indicating if the call has been canceled.
             */
            boolean canceled = false;

            /**
             * Cancels the synchronous call.
             * 
             * @return True if cancellation was successful.
             */
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
     * A builder for asynchronous HTTP requests.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Async extends CoverHttp<Async> {

        /**
         * Callback for successful response.
         */
        private Callback<CoverResult> onResponse;
        /**
         * Callback for exceptions.
         */
        private Callback<IOException> onException;
        /**
         * Callback for completion (success, failure, or cancellation).
         */
        private Callback<CoverResult.State> onComplete;
        /**
         * Flag to execute onResponse on an I/O thread.
         */
        private boolean rOnIO;
        /**
         * Flag to execute onException on an I/O thread.
         */
        private boolean eOnIO;
        /**
         * Flag to execute onComplete on an I/O thread.
         */
        private boolean cOnIO;

        /**
         * Constructs a new asynchronous request builder.
         *
         * @param htttpv The Httpv instance.
         * @param url    The request URL.
         */
        public Async(Httpv htttpv, String url) {
            super(htttpv, url);
        }

        /**
         * Sets the callback for exceptions. When set, exceptions will not be thrown.
         *
         * @param onException The exception callback.
         * @return This Async instance for chaining.
         */
        public Async setOnException(Callback<IOException> onException) {
            this.onException = onException;
            eOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the callback for when the request is complete (will be called in all cases).
         *
         * @param onComplete The completion callback.
         * @return This Async instance for chaining.
         */
        public Async setOnComplete(Callback<CoverResult.State> onComplete) {
            this.onComplete = onComplete;
            cOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the callback for a successful HTTP response.
         *
         * @param onResponse The response callback.
         * @return This Async instance for chaining.
         */
        public Async setOnResponse(Callback<CoverResult> onResponse) {
            this.onResponse = onResponse;
            rOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Executes an asynchronous GET request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall get() {
            return request(HTTP.GET);
        }

        /**
         * Executes an asynchronous HEAD request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall head() {
            return request(HTTP.HEAD);
        }

        /**
         * Executes an asynchronous POST request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall post() {
            return request(HTTP.POST);
        }

        /**
         * Executes an asynchronous PUT request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall put() {
            return request(HTTP.PUT);
        }

        /**
         * Executes an asynchronous PATCH request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall patch() {
            return request(HTTP.PATCH);
        }

        /**
         * Executes an asynchronous DELETE request.
         *
         * @return A GiveCall object to control and get the result of the async call.
         */
        public GiveCall delete() {
            return request(HTTP.DELETE);
        }

        /**
         * Executes an asynchronous HTTP request with the specified method.
         *
         * @param method The HTTP method.
         * @return A GiveCall object to control and get the result of the async call.
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

        /**
         * Executes the Httpd call asynchronously.
         *
         * @param call The Httpd NewCall object.
         * @return A GiveCall representing the ongoing request.
         */
        private GiveCall executeCall(NewCall call) {
            OkGiveCall httpCall = new OkGiveCall(call);
            call.enqueue(new Callback<Response>() {

                @Override
                public void onFailure(NewCall call, IOException error) {
                    CoverResult.State state = toState(error);
                    CoverResult result = new CoverResult.Real(Async.this, state, error);
                    onCallback(httpCall, result, () -> {
                        CoverTasks.Executor executor = httpv.executor();
                        executor.executeOnComplete(Async.this, onComplete, state, cOnIO);
                        if (!executor.executeOnException(Async.this, onException, error, eOnIO) && !nothrow) {
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

        /**
         * Synchronized handler for async callbacks.
         *
         * @param httpCall The call wrapper.
         * @param result   The result of the call.
         * @param runnable The callback logic to run.
         */
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

        /**
         * A preliminary implementation of GiveCall used before the actual Httpd call is created.
         */
        class PreGiveCall implements GiveCall {

            /**
             * The actual GiveCall implementation.
             */
            GiveCall call;
            /**
             * Flag indicating if the call has been canceled.
             */
            boolean canceled = false;
            /**
             * Latch to wait for the actual call to be set.
             */
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

            /**
             * Sets the actual GiveCall once it's created.
             * 
             * @param call The actual call.
             */
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

        /**
         * The standard implementation of GiveCall, wrapping an Httpd NewCall.
         */
        class OkGiveCall implements GiveCall {

            /**
             * The underlying Httpd call.
             */
            NewCall call;
            /**
             * The result of the call.
             */
            CoverResult result;
            /**
             * Latch to wait for the result.
             */
            CountDownLatch latch = new CountDownLatch(1);

            /**
             * Constructs a new OkGiveCall.
             * 
             * @param call The Httpd call.
             */
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

            /**
             * Sets the result of the call and releases the latch.
             * 
             * @param result The call result.
             */
            void setResult(CoverResult result) {
                this.result = result;
                latch.countDown();
            }

        }

    }

}
