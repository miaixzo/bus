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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.bodys.MultipartBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * PUT参数构造器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PutBuilder extends RequestBuilder<PutBuilder> {

    private List<PostRequest.FileInfo> fileInfos;
    private String body;
    private MultipartBody multipartBody;

    public PutBuilder(Httpd httpd) {
        super(httpd);
        fileInfos = new ArrayList<>();
    }

    @Override
    public RequestCall build() {
        return new PutRequest(
                url,
                tag,
                params,
                headers,
                fileInfos,
                body,
                multipartBody,
                id).
                build(httpd);
    }

    public PutBuilder body(String body) {
        this.body = body;
        return this;
    }

    public PutBuilder multipartBody(MultipartBody multipartBody) {
        this.multipartBody = multipartBody;
        return this;
    }

    public PutBuilder addFile(String partName, String fileName, byte[] content) {
        PostRequest.FileInfo fileInfo = new PostRequest.FileInfo();
        fileInfo.partName = partName;
        fileInfo.fileName = fileName;
        fileInfo.fileContent = content;
        fileInfos.add(fileInfo);
        return this;
    }

    public PutBuilder addFile(String partName, String fileName, File file) {
        PostRequest.FileInfo fileInfo = new PostRequest.FileInfo();
        fileInfo.partName = partName;
        fileInfo.fileName = fileName;
        fileInfo.file = file;
        fileInfos.add(fileInfo);
        return this;
    }

    public PutBuilder addFile(String partName, String fileName, String content)
            throws UnsupportedEncodingException {
        return addFile(partName, fileName, content, Charset.DEFAULT_UTF_8);
    }

    public PutBuilder addFile(String partName, String fileName, String content, String charsetName)
            throws UnsupportedEncodingException {
        return addFile(partName, fileName, content.getBytes(charsetName));
    }

    public PutBuilder addFile(String partName, String fileName, byte[] content, String charsetName) {
        return addFile(partName, fileName, content);
    }

}
