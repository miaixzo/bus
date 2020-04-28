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
package org.aoju.bus.image.metric.service;

import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.Property;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.data.ValidationResult;

import java.io.IOException;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class ServiceException extends IOException {

    private final Attributes rsp;
    private Attributes data;

    public ServiceException(int status) {
        rsp = new Attributes();
        setStatus(status);
    }

    public ServiceException(int status, String message) {
        super(message);
        rsp = new Attributes();
        setStatus(status);
        setErrorComment(getMessage());
    }

    public ServiceException(int status, Throwable cause) {
        super(cause);
        rsp = new Attributes();
        setStatus(status);
        setErrorComment(getMessage());
    }

    public static Throwable initialCauseOf(Throwable e) {
        if (e == null)
            return null;

        Throwable cause;
        while ((cause = e.getCause()) != null)
            e = cause;
        return e;
    }

    public static ServiceException valueOf(ValidationResult result,
                                           Attributes attrs) {
        if (result.hasNotAllowedAttributes())
            return new ServiceException(Status.NoSuchAttribute)
                    .setAttributeIdentifierList(result.tagsOfNotAllowedAttributes());
        if (result.hasMissingAttributes())
            return new ServiceException(Status.MissingAttribute)
                    .setAttributeIdentifierList(result.tagsOfMissingAttributes());
        if (result.hasMissingAttributeValues())
            return new ServiceException(Status.MissingAttributeValue)
                    .setDataset(new Attributes(attrs, result.tagsOfMissingAttributeValues()));
        if (result.hasInvalidAttributeValues())
            return new ServiceException(Status.InvalidAttributeValue)
                    .setDataset(new Attributes(attrs, result.tagsOfInvalidAttributeValues()));
        return null;
    }

    public int getStatus() {
        return rsp.getInt(Tag.Status, 0);
    }

    private void setStatus(int status) {
        rsp.setInt(Tag.Status, VR.US, status);
    }

    public ServiceException setUID(int tag, String value) {
        rsp.setString(tag, VR.UI, value);
        return this;
    }

    public ServiceException setErrorComment(String val) {
        if (val != null)
            rsp.setString(Tag.ErrorComment, VR.LO, Property.truncate(val, 64));
        return this;
    }

    public ServiceException setErrorID(int val) {
        rsp.setInt(Tag.ErrorID, VR.US, val);
        return this;
    }

    public ServiceException setEventTypeID(int val) {
        rsp.setInt(Tag.EventTypeID, VR.US, val);
        return this;
    }

    public ServiceException setActionTypeID(int val) {
        rsp.setInt(Tag.ActionTypeID, VR.US, val);
        return this;
    }

    public ServiceException setOffendingElements(int... tags) {
        rsp.setInt(Tag.OffendingElement, VR.AT, tags);
        return this;
    }

    public ServiceException setAttributeIdentifierList(int... tags) {
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

    public final ServiceException setDataset(Attributes data) {
        this.data = data;
        return this;
    }

}
