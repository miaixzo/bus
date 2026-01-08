/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.plugin.httpv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Httpv;
import org.miaixz.bus.http.Response;

/**
 * Represents the result of an HTTP execution. This interface provides access to the response status, headers, body, and
 * any errors that may have occurred during the request.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverResult {

    /**
     * Creates a {@code CoverResult} from a raw {@link Response}. Note: Results created this way cannot have progress
     * callbacks set or perform download operations, as they lack a task executor. This is suitable for simple,
     * synchronous use cases.
     *
     * @param response The raw {@link Response} from the HTTP client.
     * @return A new {@code CoverResult} instance.
     */
    static CoverResult of(Response response) {
        return of(response, null);
    }

    /**
     * Creates a fully functional {@code CoverResult} from a raw {@link Response} and a task executor.
     *
     * @param response The raw {@link Response} from the HTTP client.
     * @param executor The task executor, typically obtained from {@link Httpv#executor()}.
     * @return A new {@code CoverResult} instance.
     */
    static CoverResult of(Response response, CoverTasks.Executor executor) {
        if (null != response) {
            return new Real(null, response, executor);
        }
        throw new IllegalArgumentException("Response cannot be empty!");
    }

    /**
     * Retrieves the final state of the HTTP execution.
     *
     * @return The execution {@link State}.
     */
    State getState();

    /**
     * Retrieves the HTTP status code from the response.
     *
     * @return The HTTP status code (e.g., 200, 404). Returns 0 if no response was received.
     */
    int getStatus();

    /**
     * Checks if the response was successful (i.e., the status code is in the range [200..299]).
     *
     * @return {@code true} if the response was successful, {@code false} otherwise.
     */
    boolean isSuccessful();

    /**
     * Retrieves the headers from the HTTP response.
     *
     * @return The response {@link Headers}.
     */
    Headers getHeaders();

    /**
     * Retrieves all header values for a given name.
     *
     * @param name The case-insensitive header name.
     * @return A list of header values, or an empty list if the header is not present.
     */
    List<String> getHeaders(String name);

    /**
     * Retrieves the first header value for a given name.
     *
     * @param name The case-insensitive header name.
     * @return The header value, or {@code null} if the header is not present.
     */
    String getHeader(String name);

    /**
     * Gets the response body length as specified by the 'Content-Length' header. Note: For HEAD requests, this method
     * may return a non-zero value, but {@link Body#getLength()} will return 0 because there is no actual body content.
     *
     * @return The content length in bytes, or 0 if not specified.
     */
    long getContentLength();

    /**
     * Retrieves the response body handler.
     *
     * @return The {@link Body} object for consuming the response content.
     */
    Body getBody();

    /**
     * Retrieves the exception that occurred during execution, if any.
     *
     * @return The {@link IOException} that caused the failure, or {@code null} if the request was successful.
     */
    IOException getError();

    /**
     * Closes the underlying response. This should be called to release resources, especially if the response body is
     * not fully consumed.
     *
     * @return This {@code CoverResult} instance.
     */
    CoverResult close();

    /**
     * Represents the final state of an HTTP task.
     */
    enum State {

        /**
         * The task failed due to an exception during execution.
         */
        EXCEPTION,

        /**
         * The task was canceled before completion.
         */
        CANCELED,

        /**
         * The task completed successfully and a response was received.
         */
        RESPONSED,

        /**
         * The task failed due to a network timeout.
         */
        TIMEOUT,

        /**
         * The task failed due to a network connectivity issue.
         */
        NETWORK_ERROR

    }

    /**
     * An interface for consuming the HTTP response body in various formats.
     */
    interface Body {

        /**
         * Converts the response body to a byte stream.
         *
         * @return An {@link InputStream} of the response body.
         */
        InputStream toByteStream();

        /**
         * Reads the entire response body into a byte array. This method consumes the body.
         *
         * @return A byte array containing the response body.
         */
        byte[] toBytes();

        /**
         * Reads the entire response body into a ByteString. This method consumes the body.
         *
         * @return A {@link ByteString} containing the response body.
         */
        ByteString toByteString();

        /**
         * Converts the response body to a character stream using the response's charset.
         *
         * @return A {@link Reader} for the response body.
         */
        Reader toCharStream();

        /**
         * Reads the entire response body into a string using the response's charset. This method consumes the body.
         *
         * @return The response body as a {@link String}.
         */
        String toString();

        /**
         * Deserializes the response body into a schemaless map-like object.
         *
         * @return A {@link CoverWapper} representing the data structure.
         */
        CoverWapper toWapper();

        /**
         * Deserializes the response body into a schemaless list-like object.
         *
         * @return A {@link CoverArray} representing the data structure.
         */
        CoverArray toArray();

        /**
         * Deserializes the response body into an object of the specified type.
         *
         * @param <T>  The target generic type.
         * @param type The class of the target object.
         * @return An instance of the target type.
         */
        <T> T toBean(Class<T> type);

        /**
         * Deserializes the response body into a list of objects of the specified type.
         *
         * @param <T>  The target generic type for list elements.
         * @param type The class of the elements in the list.
         * @return A list of instances of the target type.
         */
        <T> List<T> toList(Class<T> type);

        /**
         * Gets the {@link MediaType} of the response body.
         *
         * @return The media type.
         */
        MediaType getType();

        /**
         * Gets the length of the response body in bytes.
         *
         * @return The length of the body.
         */
        long getLength();

        /**
         * Specifies that the next download-related callback should be executed on an I/O thread.
         *
         * @return This {@code Body} instance for chaining.
         */
        Body nextOnIO();

        /**
         * Sets a progress callback for monitoring the download of the response body.
         *
         * @param onProcess The progress callback function.
         * @return This {@code Body} instance for chaining.
         */
        Body setOnProcess(Callback<Progress> onProcess);

        /**
         * Sets the interval in bytes for progress callback invocations. Defaults to 8KB (8192).
         *
         * @param stepBytes The step size in bytes.
         * @return This {@code Body} instance for chaining.
         */
        Body stepBytes(long stepBytes);

        /**
         * Sets the interval as a rate (percentage) for progress callback invocations.
         *
         * @param stepRate The step rate, from 0.0 to 1.0.
         * @return This {@code Body} instance for chaining.
         */
        Body stepRate(double stepRate);

        /**
         * Configures the progress callback to ignore the HTTP Range header, calculating progress from 0. This is useful
         * when the total file size is known but the download is of a partial chunk.
         *
         * @return This {@code Body} instance for chaining.
         */
        Body setRangeIgnored();

        /**
         * Downloads the response body to a file at the specified path.
         *
         * @param filePath The absolute or relative path to the target file.
         * @return A {@link Downloads} object to control the download process.
         */
        Downloads toFile(String filePath);

        /**
         * Downloads the response body to the specified file.
         *
         * @param file The target file.
         * @return A {@link Downloads} object to control the download process.
         */
        Downloads toFile(File file);

        /**
         * Downloads the response body to the specified directory. The filename is automatically resolved from the
         * 'Content-Disposition' header or the URL.
         *
         * @param dirPath The path to the target directory.
         * @return A {@link Downloads} object to control the download process.
         */
        Downloads toFolder(String dirPath);

        /**
         * Downloads the response body to the specified directory. The filename is automatically resolved from the
         * 'Content-Disposition' header or the URL.
         *
         * @param dir The target directory.
         * @return A {@link Downloads} object to control the download process.
         */
        Downloads toFolder(File dir);

        /**
         * Caches the response body in memory after the first read. This allows multiple consumption methods (e.g.,
         * {@code toBytes()}, {@code toString()}) to be called. Note: Progress callbacks cannot be used when caching is
         * enabled.
         *
         * @return This {@code Body} instance for chaining.
         */
        Body cache();

        /**
         * Closes the response body without consuming it.
         *
         * @return This {@code Body} instance.
         */
        Body close();

    }

    /**
     * The concrete implementation of {@link CoverResult}.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    class Real implements CoverResult {

        /**
         * The final state of the task.
         */
        private State state;
        /**
         * The raw HTTP response, null if an error occurred before a response was received.
         */
        private Response response;
        /**
         * The exception that occurred, null if the task was successful.
         */
        private IOException error;
        /**
         * The executor for callbacks and conversions.
         */
        private CoverTasks.Executor executor;
        /**
         * The original HTTP task.
         */
        private CoverHttp<?> coverHttp;
        /**
         * The lazily-initialized body handler.
         */
        private Body body;

        /**
         * Constructs a result with a final state (e.g., CANCELED).
         * 
         * @param coverHttp The original HTTP task.
         * @param state     The final state.
         */
        public Real(CoverHttp<?> coverHttp, State state) {
            this.coverHttp = coverHttp;
            this.state = state;
        }

        /**
         * Constructs a result from a successful response.
         * 
         * @param coverHttp The original HTTP task.
         * @param response  The raw HTTP response.
         * @param executor  The task executor.
         */
        public Real(CoverHttp<?> coverHttp, Response response, CoverTasks.Executor executor) {
            this(coverHttp, executor);
            response(response);
        }

        /**
         * Constructs a result with an executor, to be populated later.
         * 
         * @param coverHttp The original HTTP task.
         * @param executor  The task executor.
         */
        public Real(CoverHttp<?> coverHttp, CoverTasks.Executor executor) {
            this.coverHttp = coverHttp;
            this.executor = executor;
        }

        /**
         * Constructs a result from a failure.
         * 
         * @param coverHttp The original HTTP task.
         * @param state     The failure state.
         * @param error     The exception that caused the failure.
         */
        public Real(CoverHttp<?> coverHttp, State state, IOException error) {
            this.coverHttp = coverHttp;
            exception(state, error);
        }

        /**
         * Sets the result to a failure state.
         * 
         * @param state The failure state.
         * @param error The exception.
         */
        public void exception(State state, IOException error) {
            this.state = state;
            this.error = error;
        }

        /**
         * Sets the result to a success state with a response.
         * 
         * @param response The raw HTTP response.
         */
        public void response(Response response) {
            this.state = State.RESPONSED;
            this.response = response;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public int getStatus() {
            if (null != response) {
                return response.code();
            }
            return 0;
        }

        @Override
        public boolean isSuccessful() {
            if (null != response) {
                return response.isSuccessful();
            }
            return false;
        }

        @Override
        public Headers getHeaders() {
            if (null != response) {
                return response.headers();
            }
            return null;
        }

        @Override
        public List<String> getHeaders(String name) {
            if (null != response) {
                return response.headers(name);
            }
            return Collections.emptyList();
        }

        @Override
        public String getHeader(String name) {
            if (null != response) {
                return response.header(name);
            }
            return null;
        }

        @Override
        public long getContentLength() {
            String length = getHeader("Content-Length");
            if (null != length) {
                try {
                    return Long.parseLong(length);
                } catch (Exception ignore) {
                    // Ignore parsing errors
                }
            }
            return 0;
        }

        @Override
        public synchronized Body getBody() {
            if (null == body && null != response) {
                body = new ResultBody(coverHttp, response, executor);
            }
            return body;
        }

        @Override
        public IOException getError() {
            return error;
        }

        /**
         * Gets the raw Httpd {@link Response}.
         *
         * @return The raw response object.
         */
        public Response getResponse() {
            return response;
        }

        @Override
        public String toString() {
            Body body = getBody();
            String text = "RealResult [\n  state: " + state + ",\n  status: " + getStatus() + ",\n  headers: "
                    + getHeaders();
            if (null != body) {
                text += ",\n  contentType: " + body.getType();
            }
            return text + ",\n  error: " + error + "\n]";
        }

        @Override
        public CoverResult close() {
            if (null != response) {
                response.close();
            }
            return this;
        }

    }

}
