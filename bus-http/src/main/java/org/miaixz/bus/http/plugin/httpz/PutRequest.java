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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP PUT request. This class encapsulates parameters and builds the appropriate {@link RequestBody},
 * handling URL-encoded forms, multipart file uploads, and raw body content.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PutRequest extends HttpRequest {

    /**
     * Constructs a new {@code PutRequest}.
     *
     * @param url           The request URL.
     * @param tag           A tag for this request.
     * @param params        The form parameters.
     * @param headers       The request headers.
     * @param list          A list of files for multipart upload.
     * @param body          A raw string for the request body.
     * @param multipartBody A pre-built multipart body.
     * @param id            A unique identifier for this request.
     */
    public PutRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
            List<MultipartFile> list, String body, MultipartBody multipartBody, String id) {
        super(url, tag, params, headers, list, body, multipartBody, id);
    }

    /**
     * Builds the request body based on the provided parameters. The priority is as follows: 1. A pre-built
     * {@link MultipartBody} if provided. 2. A new {@link MultipartBody} if files are added. 3. A raw string body if
     * provided. 4. A URL-encoded {@link FormBody} as the default.
     *
     * @return The constructed {@link RequestBody}.
     */
    @Override
    protected RequestBody buildRequestBody() {
        if (null != multipartBody) {
            return multipartBody;
        } else if (null != list && !list.isEmpty()) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
            addParam(builder);
            list.forEach(file -> {
                RequestBody fileBody;
                if (null != file.file) {
                    fileBody = RequestBody.of(MediaType.APPLICATION_OCTET_STREAM_TYPE, file.file);
                } else if (null != file.in) {
                    fileBody = createRequestBody(MediaType.APPLICATION_OCTET_STREAM_TYPE, file.in);
                } else {
                    fileBody = RequestBody.of(MediaType.valueOf(FileKit.getMimeType(file.name)), file.content);
                }
                builder.addFormDataPart(file.part, file.name, fileBody);
            });
            if (null != body && !body.isEmpty()) {
                builder.addPart(RequestBody.of(MediaType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (null != body && !body.isEmpty()) {
            MediaType contentType;
            if (headers.containsKey(HTTP.CONTENT_TYPE)) {
                contentType = MediaType.valueOf(headers.get(HTTP.CONTENT_TYPE));
            } else {
                contentType = MediaType.TEXT_PLAIN_TYPE;
            }
            return RequestBody.of(contentType, body);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            addParam(builder);
            return builder.build();
        }
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.put(requestBody).build();
    }

    /**
     * Adds standard and encoded parameters to a {@link FormBody.Builder}.
     *
     * @param builder The form body builder.
     */
    private void addParam(FormBody.Builder builder) {
        if (null != params) {
            params.forEach(builder::add);
        }
        if (null != encodedParams) {
            encodedParams.forEach(builder::addEncoded);
        }
    }

    /**
     * Adds form parameters as parts to a {@link MultipartBody.Builder}.
     *
     * @param builder The multipart body builder.
     */
    private void addParam(MultipartBody.Builder builder) {
        if (null != params && !params.isEmpty()) {
            params.forEach(
                    (k, v) -> builder.addPart(
                            Headers.of(HTTP.CONTENT_DISPOSITION, "form-data; name=" + k + Symbol.DOUBLE_QUOTES),
                            RequestBody.of(null, v)));
        }
    }

}
