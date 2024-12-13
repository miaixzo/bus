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
package org.miaixz.bus.http.plugin.httpz;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.bodys.MultipartBody;

/**
 * POST参数构造器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostBuilder extends RequestBuilder<PostBuilder> {

    private String body;
    private MultipartBody multipartBody;
    private List<MultipartFile> list;

    public PostBuilder(Httpd httpd) {
        super(httpd);
        list = new ArrayList<>();
    }

    @Override
    public RequestCall build() {
        return new PostRequest(url, tag, params, encoded, headers, list, body, multipartBody, id).build(httpd);
    }

    public PostBuilder body(String body) {
        this.body = body;
        return this;
    }

    public PostBuilder multipartBody(MultipartBody multipartBody) {
        this.multipartBody = multipartBody;
        return this;
    }

    public PostBuilder addFile(String partName, String fileName, byte[] content) {
        MultipartFile multipartFile = new MultipartFile();
        multipartFile.part = partName;
        multipartFile.name = fileName;
        multipartFile.content = content;
        list.add(multipartFile);
        return this;
    }

    public PostBuilder addFile(String partName, String fileName, InputStream is) {
        MultipartFile multipartFile = new MultipartFile();
        multipartFile.part = partName;
        multipartFile.name = fileName;
        multipartFile.in = is;
        list.add(multipartFile);
        return this;
    }

    public PostBuilder addFile(String partName, String fileName, File file) {
        MultipartFile multipartFile = new MultipartFile();
        multipartFile.part = partName;
        multipartFile.name = fileName;
        multipartFile.file = file;
        list.add(multipartFile);
        return this;
    }

    public PostBuilder addFile(String partName, String fileName, String content) throws UnsupportedEncodingException {
        return addFile(partName, fileName, content, Charset.DEFAULT_UTF_8);
    }

    public PostBuilder addFile(String partName, String fileName, String content, String charsetName)
            throws UnsupportedEncodingException {
        return addFile(partName, fileName, content.getBytes(charsetName));
    }

    public PostBuilder addFile(String partName, String fileName, byte[] content, String charsetName) {
        return addFile(partName, fileName, content);
    }

}
