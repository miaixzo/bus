/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.magic;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.bodys.FormBody;
import org.aoju.bus.http.bodys.MultipartBody;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * PUT请求处理
 *
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
public class PutRequest extends HttpRequest {

    public PutRequest(String url,
                      Object tag,
                      Map<String, String> params,
                      Map<String, String> headers,
                      List<PostRequest.FileInfo> fileInfos,
                      String body,
                      MultipartBody multipartBody, int id) {
        super(url, tag, params, headers, fileInfos, body, multipartBody, id);
    }

    public static String getMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = null;
        try {
            contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, Charset.DEFAULT_UTF_8));
        } catch (UnsupportedEncodingException e) {
            Logger.error(e.getMessage(), e);
        }
        if (contentTypeFor == null) {
            contentTypeFor = MediaType.APPLICATION_OCTET_STREAM;
        }
        return contentTypeFor;
    }

    @Override
    protected RequestBody buildRequestBody() {
        if (multipartBody != null) {
            return multipartBody;
        } else if (fileInfos != null && fileInfos.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
            addParams(builder);
            fileInfos.forEach(fileInfo -> {
                RequestBody fileBody;
                if (fileInfo.file != null) {
                    fileBody = RequestBody.create(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.file);
                } else if (fileInfo.fileInputStream != null) {
                    fileBody = createRequestBody(MediaType.APPLICATION_OCTET_STREAM_TYPE, fileInfo.fileInputStream);
                } else {
                    fileBody = RequestBody.create(MediaType.valueOf(getMimeType(fileInfo.fileName)),
                            fileInfo.fileContent);
                }
                builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
            });
            if (body != null && body.length() > 0) {
                builder.addPart(RequestBody.create(MediaType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (body != null && body.length() > 0) {
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
        return builder.put(requestBody).build();
    }

    private void addParams(FormBody.Builder builder) {
        if (params != null) {
            params.forEach((k, v) -> builder.add(k, v));
        }
        if (encodedParams != null) {
            encodedParams.forEach((k, v) -> builder.addEncoded(k, v));
        }
    }

    private void addParams(MultipartBody.Builder builder) {
        if (params != null && !params.isEmpty()) {
            params.forEach((k, v) ->
                    builder.addPart(Headers.of(
                            Header.CONTENT_DISPOSITION,
                            "form-data; name=" + k + Symbol.DOUBLE_QUOTES),
                            RequestBody.create(null, v)
                    )
            );
        }
    }

}
