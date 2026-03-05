/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
