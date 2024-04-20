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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * POST请求处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostRequest extends HttpRequest {

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
                       List<FileInfo> fileInfos, String postBody, MultipartBody multipartBody, String id) {
        super(url, tag, params, headers, fileInfos, postBody, multipartBody, id);
    }

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> encodeParams,
                       Map<String, String> headers, List<FileInfo> fileInfos, String postBody, MultipartBody multipartBody,
                       String id) {
        super(url, tag, params, encodeParams, headers, fileInfos, postBody, multipartBody, id);
    }

    @Override
    protected RequestBody buildRequestBody() {
        if (null != multipartBody) {
            return multipartBody;
        } else if (null != fileInfos && fileInfos.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
            addParams(builder);
            fileInfos.forEach(fileInfo -> {
                RequestBody fileBody;
                if (null != fileInfo.file) {
                    fileBody = RequestBody.create(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.file);
                } else if (null != fileInfo.fileInputStream) {
                    fileBody = createRequestBody(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.fileInputStream);
                } else {
                    fileBody = RequestBody.create(MediaType.valueOf(ObjectKit.defaultIfNull(FileKit.getMediaType(fileInfo.fileName), MediaType.APPLICATION_OCTET_STREAM)),
                            fileInfo.fileContent);
                }
                builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
            });
            if (null != body && body.length() > 0) {
                builder.addPart(RequestBody.create(MediaType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (null != body && body.length() > 0) {
            MediaType mediaType;
            if (headers.containsKey(Header.CONTENT_TYPE)) {
                mediaType = MediaType.valueOf(headers.get(Header.CONTENT_TYPE));
            } else {
                mediaType = MediaType.TEXT_PLAIN_TYPE;
            }
            return RequestBody.create(mediaType, body);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder);
            return builder.build();
        }
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }

    private void addParams(FormBody.Builder builder) {
        if (null != params) {
            params.forEach((k, v) -> builder.add(k, v));
        }
        if (null != encodedParams) {
            encodedParams.forEach((k, v) -> builder.addEncoded(k, v));
        }
    }

    private void addParams(MultipartBody.Builder builder) {
        if (null != params && !params.isEmpty()) {
            params.forEach((k, v) -> builder.addPart(Headers.of(Header.CONTENT_DISPOSITION, "form-data; name=\"" + k + Symbol.DOUBLE_QUOTES),
                    RequestBody.create(null, v)));
        }
    }

    public static class FileInfo {
        public String partName;
        public String fileName;
        public byte[] fileContent;
        public File file;
        public InputStream fileInputStream;
    }

}
