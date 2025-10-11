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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;

import java.io.IOException;

/**
 * An abstract {@link Interceptor} that adds a download progress listener to the response body. Subclasses must
 * implement the {@link #updateProgress} method to handle progress updates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FileInterceptor implements Interceptor, ProgressListener {

    /**
     * Intercepts the network response to wrap the original response body with a progress-monitoring body.
     *
     * @param chain The interceptor chain.
     * @return The modified {@link Response} with a progress-aware body.
     * @throws IOException if an I/O error occurs during the request.
     */
    @Override
    public Response intercept(NewChain chain) throws IOException {
        Response rsp = chain.proceed(chain.request());
        return rsp.newBuilder().body(new DownloadFileProgressResponseBody(rsp.body(), this)).build();
    }

    /**
     * An abstract callback method that is invoked with download progress updates.
     *
     * @param downloadLenth The number of bytes downloaded so far.
     * @param totalLength   The total size of the file in bytes.
     * @param isFinish      {@code true} if the download is complete, {@code false} otherwise.
     */
    public abstract void updateProgress(long downloadLenth, long totalLength, boolean isFinish);

    /**
     * A {@link ResponseBody} decorator that reports download progress as the body is being read.
     */
    public static class DownloadFileProgressResponseBody extends ResponseBody {

        /**
         * The original response body.
         */
        private final ResponseBody body;
        /**
         * The listener to notify of progress updates.
         */
        private final ProgressListener progressListener;
        /**
         * A buffered source to efficiently read from the response body.
         */
        private BufferSource bufferSource;

        /**
         * Constructs a new progress-reporting response body.
         *
         * @param body             The original response body to wrap.
         * @param progressListener The listener for progress updates.
         */
        public DownloadFileProgressResponseBody(ResponseBody body, ProgressListener progressListener) {
            this.body = body;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return body.contentType();
        }

        @Override
        public long contentLength() {
            return body.contentLength();
        }

        @Override
        public BufferSource source() {
            if (null == bufferSource) {
                bufferSource = IoKit.buffer(source(body.source()));
            }
            return bufferSource;
        }

        /**
         * Wraps the original source with a progress-monitoring source.
         *
         * @param source The original source from the response body.
         * @return A new {@link Source} that tracks read progress.
         */
        private Source source(Source source) {
            return new AssignSource(source) {

                long downloadLenth = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    boolean isFinish = (bytesRead == -1);
                    if (!isFinish) {
                        downloadLenth += bytesRead;
                    }
                    progressListener.updateProgress(downloadLenth, body.contentLength(), isFinish);
                    return bytesRead;
                }
            };
        }

    }

}
