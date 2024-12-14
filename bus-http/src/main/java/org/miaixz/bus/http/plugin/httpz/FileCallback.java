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

import java.io.*;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.logger.Logger;

/**
 * 文件-异步回调
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FileCallback implements Callback {

    private String fileAbsolutePath;

    public FileCallback() {

    }

    public FileCallback(String fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }

    @Override
    public void onResponse(NewCall call, Response response, String id) {
        try {
            if (fileAbsolutePath != null && fileAbsolutePath.length() > 0) {
                File file = new File(fileAbsolutePath);
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayInputStream bis = new ByteArrayInputStream(response.body().bytes());
                IoKit.copy(bis, fos);
                onSuccess(call, file, id);
            } else {
                onSuccess(call, response.body().byteStream(), id);
            }
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    public void onSuccess(NewCall call, File file, String id) {

    }

    public void onSuccess(NewCall call, InputStream fileStream, String id) {

    }

}
