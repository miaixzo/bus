/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import org.aoju.bus.core.utils.StreamUtils;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.logger.Logger;

import java.io.*;

/**
 * 文件-异步回调
 *
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 */
public abstract class FileCallback extends AbsCallback {

    private String fileAbsolutePath;

    public FileCallback() {
    }

    public FileCallback(String fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }

    @Override
    public void onResponse(NewCall newCall, HttpResponse response, int id) {
        try {
            if (fileAbsolutePath != null && fileAbsolutePath.length() > 0) {
                File file = new File(fileAbsolutePath);
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayInputStream bis = new ByteArrayInputStream(response.body().bytes());
                StreamUtils.copy(bis, fos);
                onSuccess(newCall, file, id);
            } else {
                onSuccess(newCall, response.body().byteStream(), id);
            }
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    public void onSuccess(NewCall newCall, File file, int id) {

    }

    public void onSuccess(NewCall newCall, InputStream fileStream, int id) {

    }

}
