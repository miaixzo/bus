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

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.logger.Logger;

import java.io.*;

/**
 * An abstract {@link Callback} implementation for handling responses that should be saved to a file. It processes the
 * response body and streams it to a file or provides an {@link InputStream} to the appropriate callback.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FileCallback implements Callback {

    /**
     * The absolute path to the destination file. If null, the response is provided as an InputStream.
     */
    private final String fileAbsolutePath;

    /**
     * Default constructor. When used, the response body will be delivered as an InputStream to the
     * {@link #onSuccess(NewCall, InputStream, String)} callback.
     */
    public FileCallback() {
        this.fileAbsolutePath = null;
    }

    /**
     * Constructor that specifies a destination file path. When used, the response body will be saved to this file, and
     * the {@link #onSuccess(NewCall, File, String)} callback will be invoked.
     *
     * @param fileAbsolutePath The absolute path of the file to save the response body to.
     */
    public FileCallback(String fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }

    /**
     * Handles the successful HTTP response. This method either saves the response body to the specified file or
     * provides it as an {@link InputStream} to the appropriate {@code onSuccess} callback.
     *
     * @param call     The {@link NewCall} that resulted in this response.
     * @param response The HTTP {@link Response}.
     * @param id       The unique ID of the request.
     */
    @Override
    public void onResponse(NewCall call, Response response, String id) {
        try {
            if (fileAbsolutePath != null && !fileAbsolutePath.isEmpty()) {
                File file = new File(fileAbsolutePath);
                try (FileOutputStream fos = new FileOutputStream(file);
                        ByteArrayInputStream bis = new ByteArrayInputStream(response.body().bytes())) {
                    IoKit.copy(bis, fos);
                    onSuccess(call, file, id);
                }
            } else {
                onSuccess(call, response.body().byteStream(), id);
            }
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /**
     * Callback invoked when the response body has been successfully saved to a file. Subclasses should override this
     * method to handle the downloaded file.
     *
     * @param call The {@link NewCall} that resulted in this response.
     * @param file The {@link File} where the response body was saved.
     * @param id   The unique ID of the request.
     */
    public void onSuccess(NewCall call, File file, String id) {
        // Default implementation does nothing.
    }

    /**
     * Callback invoked when no destination file is specified. The response body is provided as an {@link InputStream}.
     * Subclasses should override this method to process the stream. Note: The caller is responsible for closing the
     * stream.
     *
     * @param call       The {@link NewCall} that resulted in this response.
     * @param fileStream An {@link InputStream} of the response body.
     * @param id         The unique ID of the request.
     */
    public void onSuccess(NewCall call, InputStream fileStream, String id) {
        // Default implementation does nothing.
    }

}
