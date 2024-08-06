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
package org.miaixz.bus.image.metric.service;

import java.io.IOException;

import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Status;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.data.ValidationResult;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageServiceException extends IOException {

    private static final long serialVersionUID = -1L;

    private final Attributes rsp;
    private Attributes data;

    public ImageServiceException(int status) {
        rsp = new Attributes();
        setStatus(status);
    }

    public ImageServiceException(int status, String message) {
        this(status, message, true);
    }

    public ImageServiceException(int status, String message, boolean errorComment) {
        super(message);
        rsp = new Attributes();
        setStatus(status);
        if (errorComment) {
            setErrorComment(getMessage());
        }
    }

    public ImageServiceException(int status, Throwable cause) {
        this(status, cause, true);
    }

    public ImageServiceException(int status, Throwable cause, boolean errorComment) {
        super(cause);
        rsp = new Attributes();
        setStatus(status);
        if (errorComment) {
            setErrorComment(getMessage());
        }
    }

    public static Throwable initialCauseOf(Throwable e) {
        if (e == null)
            return null;

        Throwable cause;
        while ((cause = e.getCause()) != null)
            e = cause;
        return e;
    }

    public static ImageServiceException valueOf(ValidationResult result, Attributes attrs) {
        if (result.hasNotAllowedAttributes())
            return new ImageServiceException(Status.NoSuchAttribute, result.getErrorComment(), false)
                    .setAttributeIdentifierList(result.tagsOfNotAllowedAttributes());
        if (result.hasMissingAttributes())
            return new ImageServiceException(Status.MissingAttribute, result.getErrorComment(), false)
                    .setAttributeIdentifierList(result.tagsOfMissingAttributes());
        if (result.hasMissingAttributeValues())
            return new ImageServiceException(Status.MissingAttributeValue, result.getErrorComment(), false)
                    .setDataset(new Attributes(attrs, result.tagsOfMissingAttributeValues()));
        if (result.hasInvalidAttributeValues())
            return new ImageServiceException(Status.InvalidAttributeValue, result.getErrorComment(), false)
                    .setDataset(new Attributes(attrs, result.tagsOfInvalidAttributeValues()));
        return null;
    }

    public int getStatus() {
        return rsp.getInt(Tag.Status, 0);
    }

    private void setStatus(int status) {
        rsp.setInt(Tag.Status, VR.US, status);
    }

    public ImageServiceException setUID(int tag, String value) {
        rsp.setString(tag, VR.UI, value);
        return this;
    }

    public ImageServiceException setErrorComment(String val) {
        if (val != null)
            rsp.setString(Tag.ErrorComment, VR.LO, Builder.truncate(val, 64));
        return this;
    }

    public ImageServiceException setErrorID(int val) {
        rsp.setInt(Tag.ErrorID, VR.US, val);
        return this;
    }

    public ImageServiceException setEventTypeID(int val) {
        rsp.setInt(Tag.EventTypeID, VR.US, val);
        return this;
    }

    public ImageServiceException setActionTypeID(int val) {
        rsp.setInt(Tag.ActionTypeID, VR.US, val);
        return this;
    }

    public ImageServiceException setNumberOfCompletedFailedWarningSuboperations(int completed, int failed,
            int warning) {
        rsp.setInt(Tag.NumberOfCompletedSuboperations, VR.US, completed);
        rsp.setInt(Tag.NumberOfFailedSuboperations, VR.US, failed);
        rsp.setInt(Tag.NumberOfWarningSuboperations, VR.US, warning);
        return this;
    }

    public ImageServiceException setOffendingElements(int... tags) {
        rsp.setInt(Tag.OffendingElement, VR.AT, tags);
        return this;
    }

    public ImageServiceException setAttributeIdentifierList(int... tags) {
        rsp.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
        return this;
    }

    public Attributes mkRSP(int cmdField, int msgId) {
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rsp;
    }

    public final Attributes getDataset() {
        return data;
    }

    public final ImageServiceException setDataset(Attributes data) {
        this.data = data;
        return this;
    }

}
