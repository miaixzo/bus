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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageProgress implements CancelListener {

    private final List<ProgressListener> listenerList;
    private Attributes attributes;
    private volatile boolean cancel;
    private File processedFile;
    private volatile boolean lastFailed = false;

    public ImageProgress() {
        this.cancel = false;
        this.listenerList = new ArrayList<>();
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        synchronized (this) {
            int failed = getNumberOfFailedSuboperations();
            failed = Math.max(failed, 0);
            this.attributes = attributes;
            lastFailed = failed < getNumberOfFailedSuboperations();
        }

        fireProgress();
    }

    public boolean isLastFailed() {
        return lastFailed;
    }

    public synchronized File getProcessedFile() {
        return processedFile;
    }

    public synchronized void setProcessedFile(File processedFile) {
        this.processedFile = processedFile;
    }

    public void addProgressListener(ProgressListener listener) {
        if (listener != null && !listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public void removeProgressListener(ProgressListener listener) {
        if (listener != null) {
            listenerList.remove(listener);
        }
    }

    private void fireProgress() {
        for (ProgressListener progressListener : listenerList) {
            progressListener.handle(this);
        }
    }

    @Override
    public void cancel() {
        this.cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    private int getIntTag(int tag) {
        Attributes dcm = attributes;
        if (dcm == null) {
            return -1;
        }
        return dcm.getInt(tag, -1);
    }

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

    public String getErrorComment() {
        Attributes dcm = attributes;
        if (dcm == null) {
            return null;
        }
        return dcm.getString(Tag.ErrorComment);
    }

    public int getNumberOfRemainingSuboperations() {
        return getIntTag(Tag.NumberOfRemainingSuboperations);
    }

    public int getNumberOfCompletedSuboperations() {
        return getIntTag(Tag.NumberOfCompletedSuboperations);
    }

    public int getNumberOfFailedSuboperations() {
        return getIntTag(Tag.NumberOfFailedSuboperations);
    }

    public int getNumberOfWarningSuboperations() {
        return getIntTag(Tag.NumberOfWarningSuboperations);
    }

}
