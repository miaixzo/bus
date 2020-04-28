/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.params;

import lombok.Data;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.galaxy.data.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@Data
public class DicomState {

    private final Progress progress;
    private final List<Attributes> dicomRSP;
    private volatile int status;
    private String message;

    public DicomState() {
        this(Status.Pending, null, null);
    }

    public DicomState(Progress progress) {
        this(Status.Pending, null, progress);
    }

    public DicomState(int status, String message, Progress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.dicomRSP = new ArrayList<>();
    }

    public static DicomState buildMessage(DicomState dcmState, String timeMessage, Exception e) {
        DicomState state = dcmState;
        if (state == null) {
            state = new DicomState(Status.UnableToProcess, null, null);
        }

        Progress p = state.getProgress();
        int s = state.getStatus();

        StringBuilder msg = new StringBuilder();

        boolean hasFailed = false;
        if (p != null) {
            int failed = p.getNumberOfFailedSuboperations();
            int warning = p.getNumberOfWarningSuboperations();
            int remaining = p.getNumberOfRemainingSuboperations();
            if (failed > 0) {
                hasFailed = true;
                msg.append(String.format("%d/%d operations has failed.", failed,
                        failed + p.getNumberOfCompletedSuboperations()));
            } else if (remaining > 0) {
                msg.append(String.format("%d operations remains. ", remaining));
            } else if (warning > 0) {
                msg.append(String.format("%d operations has a warning status. ", warning));
            }
        }
        if (e != null) {
            hasFailed = true;
            if (msg.length() > 0) {
                msg.append(" ");
            }
            msg.append(e.getLocalizedMessage());
        }

        if (p != null && p.getAttributes() != null) {
            String error = p.getErrorComment();
            if (StringUtils.hasText(error)) {
                hasFailed = true;
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM error : ");
                msg.append(error);
            }

            if (!Status.isPending(s) && s != -1 && s != Status.Success && s != Status.Cancel) {
                if (msg.length() > 0) {
                    msg.append("\n");
                }
                msg.append("DICOM status : ");
                msg.append(s);
            }
        }

        if (!hasFailed) {
            if (timeMessage != null) {
                msg.append(timeMessage);
            }
        } else {
            if (Status.isPending(s) || s == -1) {
                state.setStatus(Status.UnableToProcess);
            }
        }
        state.setMessage(msg.toString());
        return state;
    }

    public int getStatus() {
        if (progress != null && progress.getAttributes() != null) {
            return progress.getStatus();
        }
        return status;
    }

    public void addDicomRSP(Attributes dicomRSP) {
        if (dicomRSP != null) {
            this.dicomRSP.add(dicomRSP);
        }
    }

}
