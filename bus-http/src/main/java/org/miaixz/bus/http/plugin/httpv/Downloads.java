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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Callback;

import java.io.*;

/**
 * Manages the process of downloading content from an {@link InputStream} to a {@link File}. This class provides
 * controls for pausing, resuming, and canceling the download, and supports callbacks for success and failure events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Downloads {

    /**
     * Lock object for synchronizing access to the download status.
     */
    private final Object lock = new Object();
    /**
     * A temporary flag to indicate that the next callback should be executed on an I/O thread.
     */
    protected boolean nextOnIO = false;
    /**
     * The target file where the content will be saved.
     */
    private File file;
    /**
     * The source input stream providing the download content.
     */
    private InputStream input;
    /**
     * Callback to be invoked upon successful completion of the download.
     */
    private Callback<File> onSuccess;
    /**
     * Callback to be invoked if the download fails.
     */
    private Callback<Failure> onFailure;
    /**
     * The executor responsible for running the download task and callbacks.
     */
    private CoverTasks.Executor executor;
    /**
     * The number of bytes that have been successfully downloaded and written to the file.
     */
    private long doneBytes;
    /**
     * The size of the buffer used for reading from the input stream.
     */
    private int buffSize = 0;
    /**
     * The offset in the file from where to start writing. Used for resuming downloads.
     */
    private long seekBytes = 0;
    /**
     * A flag indicating whether to append to the file instead of overwriting.
     */
    private boolean appended;
    /**
     * The current status of the download (e.g., DOWNLOADING, PAUSED). Volatile for visibility across threads.
     */
    private volatile int status;
    /**
     * Flag indicating if the onSuccess callback should run on an I/O thread.
     */
    private boolean sOnIO;
    /**
     * Flag indicating if the onFailure callback should run on an I/O thread.
     */
    private boolean fOnIO;

    /**
     * The control object for managing the download lifecycle.
     */
    private Control control;

    /**
     * Constructs a new download handler.
     *
     * @param file      The destination file.
     * @param input     The source input stream.
     * @param executor  The task executor.
     * @param skipBytes The number of bytes to skip from the beginning of the input stream (used for resuming).
     */
    public Downloads(File file, InputStream input, CoverTasks.Executor executor, long skipBytes) {
        this.file = file;
        this.input = input;
        this.executor = executor;
        this.seekBytes = skipBytes;
        this.control = new Control();
    }

    /**
     * Sets the buffer size for reading from the input stream. The default size is 2048 bytes.
     *
     * @param buffSize The buffer size in bytes.
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads setBuffSize(int buffSize) {
        if (buffSize > 0) {
            this.buffSize = buffSize;
        }
        return this;
    }

    /**
     * Enables append mode. When set, the download will append data to the file, which is useful for resuming downloads
     * or chunked downloading.
     *
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads setAppended() {
        this.appended = true;
        return this;
    }

    /**
     * Sets the file pointer to a specific position. Writing will start from this byte offset.
     *
     * @param seekBytes The number of bytes to skip from the beginning of the file.
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads setFilePointer(long seekBytes) {
        this.seekBytes = seekBytes;
        return this;
    }

    /**
     * Specifies that the next callback to be set (e.g., onSuccess, onFailure) should be executed on an I/O thread.
     *
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads nextOnIO() {
        nextOnIO = true;
        return this;
    }

    /**
     * Sets the callback to be executed when the download completes successfully.
     *
     * @param onSuccess The success callback.
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads setOnSuccess(Callback<File> onSuccess) {
        this.onSuccess = onSuccess;
        sOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * Sets the callback to be executed when the download fails.
     *
     * @param onFailure The failure callback.
     * @return this {@code Downloads} instance for chaining.
     */
    public Downloads setOnFailure(Callback<Failure> onFailure) {
        this.onFailure = onFailure;
        fOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * Starts the download process asynchronously on an I/O thread.
     *
     * @return A {@link Control} object to manage the download (pause, resume, cancel).
     */
    public Control start() {
        if (buffSize == 0) {
            buffSize = Progress.DEFAULT_STEP_BYTES;
        }
        RandomAccessFile raFile = randomAccessFile();
        status = Control.STATUS__DOWNLOADING;
        executor.execute(() -> doDownload(raFile), true);
        return control;
    }

    /**
     * Gets the controller for this download task.
     *
     * @return The {@link Control} object.
     */
    public Control getCtrl() {
        return control;
    }

    /**
     * Opens the destination file in read-write mode.
     *
     * @return A {@link RandomAccessFile} instance.
     * @throws InternalException if the file cannot be opened.
     */
    private RandomAccessFile randomAccessFile() {
        try {
            return new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            status = Control.STATUS__ERROR;
            IoKit.close(input);
            throw new InternalException("Can't get file [" + file.getAbsolutePath() + "] Input stream", e);
        }
    }

    /**
     * The core download loop. Reads from the input stream and writes to the file, handling state changes like pausing
     * and canceling.
     *
     * @param raFile The file to write to.
     */
    private void doDownload(RandomAccessFile raFile) {
        try {
            if (appended && seekBytes > 0) {
                long length = raFile.length();
                if (seekBytes <= length) {
                    raFile.seek(seekBytes);
                    doneBytes = seekBytes;
                } else {
                    raFile.seek(length);
                    doneBytes = length;
                }
            }
            while (status != Control.STATUS__CANCELED && status != Control.STATUS__DONE) {
                if (status == Control.STATUS__DOWNLOADING) {
                    byte[] buff = new byte[buffSize];
                    int len;
                    while ((len = input.read(buff)) != -1) {
                        raFile.write(buff, 0, len);
                        doneBytes += len;
                        if (status == Control.STATUS__CANCELED || status == Control.STATUS__PAUSED) {
                            break;
                        }
                    }
                    if (len == -1) {
                        synchronized (lock) {
                            status = Control.STATUS__DONE;
                        }
                    }
                }
            }
        } catch (IOException e) {
            synchronized (lock) {
                status = Control.STATUS__ERROR;
            }
            if (null != onFailure) {
                executor.execute(() -> onFailure.on(new Failure(e)), fOnIO);
            } else {
                throw new InternalException("Streaming failed!", e);
            }
        } finally {
            IoKit.close(raFile);
            IoKit.close(input);
            if (status == Control.STATUS__CANCELED) {
                file.delete();
            }
        }
        if (status == Control.STATUS__DONE && null != onSuccess) {
            executor.execute(() -> onSuccess.on(file), sOnIO);
        }
    }

    /**
     * A listener interface for globally intercepting download events.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public interface Listener {

        /**
         * Listens for the creation of a new download task.
         *
         * @param http      The {@link CoverHttp} task that initiated the download.
         * @param downloads The {@link Downloads} event handler.
         */
        void listen(CoverHttp<?> http, Downloads downloads);

    }

    /**
     * Provides methods to control the state of an ongoing download.
     */
    public class Control {

        /**
         * Status indicating the download has been canceled. The partial file will be deleted.
         */
        public static final int STATUS__CANCELED = -1;

        /**
         * Status indicating the download is currently in progress.
         */
        public static final int STATUS__DOWNLOADING = 1;

        /**
         * Status indicating the download has been paused by the user.
         */
        public static final int STATUS__PAUSED = 2;

        /**
         * Status indicating the download has completed successfully.
         */
        public static final int STATUS__DONE = 3;

        /**
         * Status indicating an error occurred during the download.
         */
        public static final int STATUS__ERROR = 4;

        /**
         * Gets the current status of the download.
         *
         * @return The current status code.
         * @see #STATUS__CANCELED
         * @see #STATUS__DOWNLOADING
         * @see #STATUS__PAUSED
         * @see #STATUS__DONE
         * @see #STATUS__ERROR
         */
        public int status() {
            return status;
        }

        /**
         * Pauses the download if it is currently in progress.
         */
        public void pause() {
            synchronized (lock) {
                if (status == STATUS__DOWNLOADING) {
                    status = STATUS__PAUSED;
                }
            }
        }

        /**
         * Resumes the download if it is currently paused.
         */
        public void resume() {
            synchronized (lock) {
                if (status == STATUS__PAUSED) {
                    status = STATUS__DOWNLOADING;
                }
            }
        }

        /**
         * Cancels the download if it is in progress or paused. The partially downloaded file will be deleted.
         */
        public void cancel() {
            synchronized (lock) {
                if (status == STATUS__PAUSED || status == STATUS__DOWNLOADING) {
                    status = STATUS__CANCELED;
                }
            }
        }

    }

    /**
     * Encapsulates information about a download failure.
     */
    public class Failure {

        /**
         * The exception that caused the failure.
         */
        private final IOException exception;

        /**
         * Constructs a new Failure object.
         *
         * @param exception The IOException that occurred.
         */
        Failure(IOException exception) {
            this.exception = exception;
        }

        /**
         * @return The target download file.
         */
        public File getFile() {
            return file;
        }

        /**
         * @return The number of bytes successfully downloaded before the failure.
         */
        public long getDoneBytes() {
            return doneBytes;
        }

        /**
         * @return The exception that caused the failure.
         */
        public IOException getException() {
            return exception;
        }

    }

}
