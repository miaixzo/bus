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
package org.miaixz.bus.storage.metric;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.gitlab.GitLabApiClient;
import org.miaixz.bus.storage.Context;

/**
 * 存储服务-Gitlab
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GitlabFileProvider extends AbstractProvider {

    GitLabApiClient client;

    public GitlabFileProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getPrefix(), "[prefix] not defined");
        Assert.notBlank(this.context.getBucket(), "[bucket] not defined");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] not defined");
        Assert.notBlank(this.context.getSecretKey(), "[secure] not defined");

        this.client = new GitLabApiClient(this.context.getEndpoint(), this.context.getAccessKey());
    }

    @Override
    public Message download(String fileName) {
        return null;
    }

    @Override
    public Message download(String bucket, String fileName) {
        return null;
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        return null;
    }

    @Override
    public Message download(String fileName, File file) {
        return null;
    }

    @Override
    public Message rename(String oldName, String newName) {
        return null;
    }

    @Override
    public Message rename(String bucket, String oldName, String newName) {
        return null;
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return null;
    }

    @Override
    public Message upload(String bucket, String fileName, InputStream content) {
        return null;
    }

    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return null;
    }

    @Override
    public Message remove(String fileName) {
        return null;
    }

    @Override
    public Message remove(String bucket, String fileName) {
        return null;
    }

    @Override
    public Message remove(String bucket, Path path) {
        return null;
    }

}