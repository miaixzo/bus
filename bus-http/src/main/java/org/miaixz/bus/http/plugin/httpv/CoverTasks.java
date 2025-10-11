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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Callback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * A container class for components related to task execution, lifecycle listeners, and data conversion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CoverTasks {

    /**
     * A generic listener interface for intercepting task lifecycle events.
     *
     * @param <T> The type of data associated with the event.
     * @author Kimi Liu
     * @since Java 17+
     */
    public interface Listener<T> {

        /**
         * A global listener method that is called when a specific task event occurs.
         *
         * @param task The HTTP task that triggered the event.
         * @param data The data associated with the event (e.g., a response, an exception).
         * @return {@code true} to allow the task's own callback to be executed, {@code false} to prevent it.
         */
        boolean listen(CoverHttp<?> task, T data);

    }

    /**
     * Manages the execution of asynchronous tasks, callbacks, and data conversions. This class orchestrates the use of
     * I/O and main-thread executors, global listeners, and a chain of data converters.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Executor {

        /**
         * Executor for I/O-bound operations.
         */
        private final java.util.concurrent.Executor ioExecutor;
        /**
         * Executor for main-thread (e.g., UI) operations. Can be null.
         */
        private final java.util.concurrent.Executor mainExecutor;
        /**
         * Global listener for download progress events.
         */
        private final Downloads.Listener downloadListener;
        /**
         * Global listener for successful HTTP response events.
         */
        private final Listener<CoverResult> responseListener;
        /**
         * Global listener for exceptions that occur during HTTP tasks.
         */
        private final Listener<IOException> exceptionListener;
        /**
         * Global listener for task completion events (both success and failure).
         */
        private final Listener<CoverResult.State> completeListener;
        /**
         * An array of converters for serializing and deserializing data.
         */
        private final Convertor[] convertors;

        /**
         * Constructs a new Executor.
         *
         * @param ioExecutor        The executor for I/O tasks.
         * @param mainExecutor      The executor for main-thread tasks.
         * @param downloadListener  The global download listener.
         * @param responseListener  The global response listener.
         * @param exceptionListener The global exception listener.
         * @param completeListener  The global completion listener.
         * @param convertors        The array of data converters.
         */
        public Executor(java.util.concurrent.Executor ioExecutor, java.util.concurrent.Executor mainExecutor,
                Downloads.Listener downloadListener, Listener<CoverResult> responseListener,
                Listener<IOException> exceptionListener, Listener<CoverResult.State> completeListener,
                Convertor[] convertors) {
            this.ioExecutor = ioExecutor;
            this.mainExecutor = mainExecutor;
            this.downloadListener = downloadListener;
            this.responseListener = responseListener;
            this.exceptionListener = exceptionListener;
            this.completeListener = completeListener;
            this.convertors = convertors;
        }

        /**
         * Gets the appropriate executor based on the {@code onIo} flag.
         *
         * @param onIo If {@code true}, returns the I/O executor; otherwise, returns the main executor.
         * @return The selected executor.
         */
        public java.util.concurrent.Executor getExecutor(boolean onIo) {
            if (onIo || null == mainExecutor) {
                return ioExecutor;
            }
            return mainExecutor;
        }

        /**
         * Creates a new download task handler.
         *
         * @param coverHttp The HTTP task associated with the download.
         * @param file      The destination file.
         * @param input     The input stream of the data to be downloaded.
         * @param skipBytes The number of bytes to skip from the beginning.
         * @return A new {@link Downloads} handler.
         */
        public Downloads download(CoverHttp<?> coverHttp, File file, InputStream input, long skipBytes) {
            Downloads downloads = new Downloads(file, input, this, skipBytes);
            if (null != coverHttp && null != downloadListener) {
                downloadListener.listen(coverHttp, downloads);
            }
            return downloads;
        }

        /**
         * Executes a command on the appropriate thread pool.
         *
         * @param command The command to execute.
         * @param onIo    If {@code true}, the command is executed on the I/O thread pool; otherwise, on the main thread
         *                pool.
         */
        public void execute(Runnable command, boolean onIo) {
            java.util.concurrent.Executor executor = ioExecutor;
            if (null != mainExecutor && !onIo) {
                executor = mainExecutor;
            }
            executor.execute(command);
        }

        /**
         * Executes the response callback for a task, respecting the global response listener.
         *
         * @param task       The HTTP task.
         * @param onResponse The specific callback for this task.
         * @param result     The successful result.
         * @param onIo       If {@code true}, the callback is executed on the I/O thread pool.
         */
        public void executeOnResponse(
                CoverHttp<?> task,
                Callback<CoverResult> onResponse,
                CoverResult result,
                boolean onIo) {
            if (null != responseListener) {
                if (responseListener.listen(task, result) && null != onResponse) {
                    execute(() -> onResponse.on(result), onIo);
                }
            } else if (null != onResponse) {
                execute(() -> onResponse.on(result), onIo);
            }
        }

        /**
         * Executes the exception callback for a task, respecting the global exception listener.
         *
         * @param task        The HTTP task.
         * @param onException The specific callback for this task.
         * @param error       The exception that occurred.
         * @param onIo        If {@code true}, the callback is executed on the I/O thread pool.
         * @return {@code true} if an exception handler (either global or specific) was invoked.
         */
        public boolean executeOnException(
                CoverHttp<?> task,
                Callback<IOException> onException,
                IOException error,
                boolean onIo) {
            if (null != exceptionListener) {
                if (exceptionListener.listen(task, error) && null != onException) {
                    execute(() -> onException.on(error), onIo);
                }
            } else if (null != onException) {
                execute(() -> onException.on(error), onIo);
            } else {
                return false;
            }
            return true;
        }

        /**
         * Executes the completion callback for a task, respecting the global completion listener.
         *
         * @param task       The HTTP task.
         * @param onComplete The specific callback for this task.
         * @param state      The final state of the task.
         * @param onIo       If {@code true}, the callback is executed on the I/O thread pool.
         */
        public void executeOnComplete(
                CoverHttp<?> task,
                Callback<CoverResult.State> onComplete,
                CoverResult.State state,
                boolean onIo) {
            if (null != completeListener) {
                if (completeListener.listen(task, state) && null != onComplete) {
                    execute(() -> onComplete.on(state), onIo);
                }
            } else if (null != onComplete) {
                execute(() -> onComplete.on(state), onIo);
            }
        }

        /**
         * Performs a data conversion by applying the given function to a suitable converter.
         *
         * @param callable The conversion function to apply.
         * @param <V>      The type of the converted data.
         * @return The converted data.
         */
        public <V> V doMsgConvert(ConvertFunc<V> callable) {
            return doMsgConvert(null, callable).data;
        }

        /**
         * Performs a data conversion by applying the given function to a suitable converter, optionally filtered by
         * type.
         *
         * @param type     The content type to match (e.g., "json"). Can be null.
         * @param callable The conversion function to apply.
         * @param <V>      The type of the converted data.
         * @return A {@link Data} object containing the converted data and its content type.
         * @throws InternalException if no matching converter is found or if conversion fails.
         */
        public <V> Data<V> doMsgConvert(String type, ConvertFunc<V> callable) {
            Throwable cause = null;
            for (int i = convertors.length - 1; i >= 0; i--) {
                Convertor convertor = convertors[i];
                String contentType = convertor.contentType();
                if (null != type && (null == contentType || !contentType.contains(type))) {
                    continue;
                }
                if (null == callable && null != contentType) {
                    return new Data<>(null, contentType);
                }
                try {
                    assert null != callable;
                    return new Data<>(callable.apply(convertor), contentType);
                } catch (Exception e) {
                    if (null != cause) {
                        initRootCause(e, cause);
                    }
                    cause = e;
                }
            }
            if (null == callable) {
                return new Data<>(null, toMediaType(type));
            }
            if (null != cause) {
                throw new InternalException("Conversion failed", cause);
            }

            throw new InternalException("No match[" + type + "]Type converter found!");
        }

        /**
         * Converts a simple type string to a standard MIME type.
         *
         * @param type The simple type (e.g., "json").
         * @return The corresponding MIME type string.
         */
        private String toMediaType(String type) {
            if (type != null) {
                String lower = type.toLowerCase();
                if (lower.contains(HTTP.JSON)) {
                    return MediaType.APPLICATION_JSON;
                }
                if (lower.contains(HTTP.XML)) {
                    return MediaType.APPLICATION_XML;
                }
                if (lower.contains(HTTP.PROTOBUF)) {
                    return MediaType.APPLICATION_X_PROTOBUF;
                }
            }
            return MediaType.APPLICATION_FORM_URLENCODED;
        }

        /**
         * Traverses the cause chain of a throwable to set the root cause.
         *
         * @param throwable The throwable to modify.
         * @param cause     The root cause to set.
         */
        private void initRootCause(Throwable throwable, Throwable cause) {
            Throwable lastCause = throwable.getCause();
            if (null != lastCause) {
                initRootCause(lastCause, cause);
            } else {
                throwable.initCause(cause);
            }
        }

        /**
         * Shuts down the I/O and main thread pools if they are instances of {@link ExecutorService}.
         */
        public void shutdown() {
            if (null != ioExecutor && ioExecutor instanceof ExecutorService) {
                ((ExecutorService) ioExecutor).shutdown();
            }
            if (null != mainExecutor && mainExecutor instanceof ExecutorService) {
                ((ExecutorService) mainExecutor).shutdown();
            }
        }

        /**
         * @return The I/O executor.
         */
        public java.util.concurrent.Executor getIoExecutor() {
            return ioExecutor;
        }

        /**
         * @return The main-thread executor.
         */
        public java.util.concurrent.Executor getMainExecutor() {
            return mainExecutor;
        }

        /**
         * @return The global download listener.
         */
        public Downloads.Listener getDownloadListener() {
            return downloadListener;
        }

        /**
         * @return The global response listener.
         */
        public Listener<CoverResult> getResponseListener() {
            return responseListener;
        }

        /**
         * @return The global exception listener.
         */
        public Listener<IOException> getExceptionListener() {
            return exceptionListener;
        }

        /**
         * @return The global completion listener.
         */
        public Listener<CoverResult.State> getCompleteListener() {
            return completeListener;
        }

        /**
         * @return The array of configured data converters.
         */
        public Convertor[] getConvertors() {
            return convertors;
        }

        /**
         * A functional interface for applying a conversion operation.
         *
         * @param <T> The return type of the conversion.
         */
        public interface ConvertFunc<T> {

            /**
             * Applies the conversion logic using the given converter.
             *
             * @param convertor The converter to use.
             * @return The result of the conversion.
             */
            T apply(Convertor convertor);

        }

        /**
         * A container for holding converted data along with its content type.
         *
         * @param <T> The type of the data.
         */
        public static class Data<T> {

            /**
             * The converted data.
             */
            public T data;
            /**
             * The content type of the data.
             */
            public String contentType;

            /**
             * Constructs a new Data object.
             *
             * @param data        The data.
             * @param contentType The content type.
             */
            public Data(T data, String contentType) {
                this.data = data;
                this.contentType = contentType;
            }
        }

    }

}
