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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Callback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

public class CoverTasks {
    /**
     * 任务监听接口
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static interface Listener<T> {

        /**
         * 全局任务监听
         *
         * @param task 所属的 CoverHttp
         * @param data 监听内容
         * @return 是否继续执行 task 对应的回调函数
         */
        boolean listen(CoverHttp<?> task, T data);

    }

    /**
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Executor {

        private java.util.concurrent.Executor ioExecutor;
        private java.util.concurrent.Executor mainExecutor;
        private Downloads.Listener downloadListener;
        private Listener<CoverResult> responseListener;
        private Listener<IOException> exceptionListener;
        private Listener<CoverResult.State> completeListener;
        private Convertor[] convertors;

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

        public java.util.concurrent.Executor getExecutor(boolean onIo) {
            if (onIo || null == mainExecutor) {
                return ioExecutor;
            }
            return mainExecutor;
        }

        public Downloads download(CoverHttp<?> coverHttp, File file, InputStream input, long skipBytes) {
            Downloads downloads = new Downloads(file, input, this, skipBytes);
            if (null != coverHttp && null != downloadListener) {
                downloadListener.listen(coverHttp, downloads);
            }
            return downloads;
        }

        public void execute(Runnable command, boolean onIo) {
            java.util.concurrent.Executor executor = ioExecutor;
            if (null != mainExecutor && !onIo) {
                executor = mainExecutor;
            }
            executor.execute(command);
        }

        public void executeOnResponse(CoverHttp<?> task, Callback<CoverResult> onResponse, CoverResult result,
                boolean onIo) {
            if (null != responseListener) {
                if (responseListener.listen(task, result) && null != onResponse) {
                    execute(() -> onResponse.on(result), onIo);
                }
            } else if (null != onResponse) {
                execute(() -> onResponse.on(result), onIo);
            }
        }

        public boolean executeOnException(CoverHttp<?> task, Callback<IOException> onException, IOException error,
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

        public void executeOnComplete(CoverHttp<?> task, Callback<CoverResult.State> onComplete,
                CoverResult.State state, boolean onIo) {
            if (null != completeListener) {
                if (completeListener.listen(task, state) && null != onComplete) {
                    execute(() -> onComplete.on(state), onIo);
                }
            } else if (null != onComplete) {
                execute(() -> onComplete.on(state), onIo);
            }
        }

        public <V> V doMsgConvert(ConvertFunc<V> callable) {
            return doMsgConvert(null, callable).data;
        }

        public <V> Data<V> doMsgConvert(String type, ConvertFunc<V> callable) {
            Throwable cause = null;
            for (int i = convertors.length - 1; i >= 0; i--) {
                Convertor convertor = convertors[i];
                String mediaType = convertor.mediaType();
                if (null != type && (null == mediaType || !mediaType.contains(type))) {
                    continue;
                }
                if (null == callable && null != mediaType) {
                    return new Data<>(null, mediaType);
                }
                try {
                    assert null != callable;
                    return new Data<>(callable.apply(convertor), mediaType);
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

            throw new InternalException("No match[" + type + "]Type converter！");
        }

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

        private void initRootCause(Throwable throwable, Throwable cause) {
            Throwable lastCause = throwable.getCause();
            if (null != lastCause) {
                initRootCause(lastCause, cause);
            } else {
                throwable.initCause(cause);
            }
        }

        /**
         * 关闭线程池
         */
        public void shutdown() {
            if (null != ioExecutor && ioExecutor instanceof ExecutorService) {
                ((ExecutorService) ioExecutor).shutdown();
            }
            if (null != mainExecutor && mainExecutor instanceof ExecutorService) {
                ((ExecutorService) mainExecutor).shutdown();
            }
        }

        public java.util.concurrent.Executor getIoExecutor() {
            return ioExecutor;
        }

        public java.util.concurrent.Executor getMainExecutor() {
            return mainExecutor;
        }

        public Downloads.Listener getDownloadListener() {
            return downloadListener;
        }

        public Listener<CoverResult> getResponseListener() {
            return responseListener;
        }

        public Listener<IOException> getExceptionListener() {
            return exceptionListener;
        }

        public Listener<CoverResult.State> getCompleteListener() {
            return completeListener;
        }

        public Convertor[] getConvertors() {
            return convertors;
        }

        public interface ConvertFunc<T> {

            T apply(Convertor convertor);

        }

        public static class Data<T> {

            public T data;
            public String mediaType;

            public Data(T data, String mediaType) {
                this.data = data;
                this.mediaType = mediaType;
            }
        }

    }

}
