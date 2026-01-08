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
package org.miaixz.bus.image.galaxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the progress of an image operation, including status, associated attributes, and cancellation
 * capabilities. This class also manages a list of {@link ProgressListener} to notify about progress updates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageProgress implements CancelListener {

    /**
     * A list of listeners to be notified of progress updates.
     */
    private final List<ProgressListener> listenerList;
    /**
     * The attributes associated with the image operation.
     */
    private Attributes attributes;
    /**
     * A volatile flag indicating whether the operation has been cancelled.
     */
    private volatile boolean cancel;
    /**
     * The file being processed.
     */
    private File processedFile;
    /**
     * A volatile flag indicating if the last sub-operation failed.
     */
    private volatile boolean lastFailed = false;

    /**
     * Constructs a new {@code ImageProgress} instance. Initializes the cancellation flag to {@code false} and creates
     * an empty list for progress listeners.
     */
    public ImageProgress() {
        this.cancel = false;
        this.listenerList = new ArrayList<>();
    }

    /**
     * Retrieves the attributes associated with the image operation.
     *
     * @return The {@link Attributes} object.
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the attributes for the image operation and fires a progress update. This method also updates the
     * {@code lastFailed} flag based on the number of failed sub-operations.
     *
     * @param attributes The {@link Attributes} to set.
     */
    public void setAttributes(Attributes attributes) {
        synchronized (this) {
            int failed = getNumberOfFailedSuboperations();
            failed = Math.max(failed, 0);
            this.attributes = attributes;
            lastFailed = failed < getNumberOfFailedSuboperations();
        }

        fireProgress();
    }

    /**
     * Checks if the last sub-operation failed.
     *
     * @return {@code true} if the last sub-operation failed, {@code false} otherwise.
     */
    public boolean isLastFailed() {
        return lastFailed;
    }

    /**
     * Retrieves the file currently being processed.
     *
     * @return The {@link File} being processed.
     */
    public synchronized File getProcessedFile() {
        return processedFile;
    }

    /**
     * Sets the file being processed.
     *
     * @param processedFile The {@link File} to set as the processed file.
     */
    public synchronized void setProcessedFile(File processedFile) {
        this.processedFile = processedFile;
    }

    /**
     * Adds a {@link ProgressListener} to the list of listeners.
     *
     * @param listener The {@link ProgressListener} to add. Will not be added if null or already present.
     */
    public void addProgressListener(ProgressListener listener) {
        if (listener != null && !listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    /**
     * Removes a {@link ProgressListener} from the list of listeners.
     *
     * @param listener The {@link ProgressListener} to remove. Will do nothing if null.
     */
    public void removeProgressListener(ProgressListener listener) {
        if (listener != null) {
            listenerList.remove(listener);
        }
    }

    /**
     * Notifies all registered {@link ProgressListener}s about a progress update.
     */
    private void fireProgress() {
        for (ProgressListener progressListener : listenerList) {
            progressListener.handle(this);
        }
    }

    /**
     * Cancels the current image operation. Sets the cancellation flag to {@code true}.
     */
    @Override
    public void cancel() {
        this.cancel = true;
    }

    /**
     * Checks if the image operation has been cancelled.
     *
     * @return {@code true} if the operation is cancelled, {@code false} otherwise.
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * Retrieves the integer value of a specified tag from the associated attributes.
     *
     * @param tag The tag to retrieve.
     * @return The integer value of the tag, or -1 if attributes are null or the tag is not found.
     */
    private int getIntTag(int tag) {
        Attributes dcm = attributes;
        if (dcm == null) {
            return -1;
        }
        return dcm.getInt(tag, -1);
    }

    /**
     * Retrieves the current status of the image operation. If the operation is cancelled, {@link Status#Cancel} is
     * returned. If attributes are null, {@link Status#Pending} is returned.
     *
     * @return The status code of the image operation.
     */
    public int getStatus() {
        if (isCancel()) {
            return Status.Cancel;
        }
        Attributes dcm = attributes;
        if (dcm == null) {
            return Status.Pending;
        }
        return dcm.getInt(Tag.Status, Status.Pending);
    }

    /**
     * Retrieves the error comment from the associated attributes.
     *
     * @return The error comment string, or {@code null} if attributes are null or the tag is not found.
     */
    public String getErrorComment() {
        Attributes dcm = attributes;
        if (dcm == null) {
            return null;
        }
        return dcm.getString(Tag.ErrorComment);
    }

    /**
     * Retrieves the number of remaining sub-operations from the associated attributes.
     *
     * @return The number of remaining sub-operations, or -1 if attributes are null or the tag is not found.
     */
    public int getNumberOfRemainingSuboperations() {
        return getIntTag(Tag.NumberOfRemainingSuboperations);
    }

    /**
     * Retrieves the number of completed sub-operations from the associated attributes.
     *
     * @return The number of completed sub-operations, or -1 if attributes are null or the tag is not found.
     */
    public int getNumberOfCompletedSuboperations() {
        return getIntTag(Tag.NumberOfCompletedSuboperations);
    }

    /**
     * Retrieves the number of failed sub-operations from the associated attributes.
     *
     * @return The number of failed sub-operations, or -1 if attributes are null or the tag is not found.
     */
    public int getNumberOfFailedSuboperations() {
        return getIntTag(Tag.NumberOfFailedSuboperations);
    }

    /**
     * Retrieves the number of warning sub-operations from the associated attributes.
     *
     * @return The number of warning sub-operations, or -1 if attributes are null or the tag is not found.
     */
    public int getNumberOfWarningSuboperations() {
        return getIntTag(Tag.NumberOfWarningSuboperations);
    }

}
