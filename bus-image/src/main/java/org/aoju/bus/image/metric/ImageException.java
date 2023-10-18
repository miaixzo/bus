/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric;

import org.aoju.bus.core.exception.RelevantException;
import org.aoju.bus.image.Status;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.Property;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.data.ValidationResult;

/**
 * 自定义异常: 影像异常
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageException extends RelevantException {

    private final Attributes rsp;
    private Attributes data;

    public ImageException(int status) {
        rsp = new Attributes();
        rsp.setInt(Tag.Status, VR.US, status);
    }

    public ImageException(int status, String message) {
        super(message);
        rsp = new Attributes();
        rsp.setInt(Tag.Status, VR.US, status);
        setErrorComment(getMessage());
    }

    public ImageException(int status, Throwable cause) {
        super(cause);
        rsp = new Attributes();
        rsp.setInt(Tag.Status, VR.US, status);
        setErrorComment(getMessage());
    }

    public static ImageException valueOf(ValidationResult result,
                                         Attributes attrs) {
        if (result.hasNotAllowedAttributes())
            return new ImageException(Status.NoSuchAttribute)
                    .setAttributeIdentifierList(result.tagsOfNotAllowedAttributes());
        if (result.hasMissingAttributes())
            return new ImageException(Status.MissingAttribute)
                    .setAttributeIdentifierList(result.tagsOfMissingAttributes());
        if (result.hasMissingAttributeValues())
            return new ImageException(Status.MissingAttributeValue)
                    .setDataset(new Attributes(attrs, result.tagsOfMissingAttributeValues()));
        if (result.hasInvalidAttributeValues())
            return new ImageException(Status.InvalidAttributeValue)
                    .setDataset(new Attributes(attrs, result.tagsOfInvalidAttributeValues()));
        return null;
    }

    public ImageException setErrorComment(String val) {
        if (null != val)
            rsp.setString(Tag.ErrorComment, VR.LO, Property.truncate(val, 64));
        return this;
    }

    public ImageException setErrorID(int val) {
        rsp.setInt(Tag.ErrorID, VR.US, val);
        return this;
    }

    public ImageException setEventTypeID(int val) {
        rsp.setInt(Tag.EventTypeID, VR.US, val);
        return this;
    }

    public ImageException setActionTypeID(int val) {
        rsp.setInt(Tag.ActionTypeID, VR.US, val);
        return this;
    }

    public ImageException setOffendingElements(int... tags) {
        rsp.setInt(Tag.OffendingElement, VR.AT, tags);
        return this;
    }

    public ImageException setAttributeIdentifierList(int... tags) {
        rsp.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
        return this;
    }

    public ImageException setUID(int tag, String value) {
        rsp.setString(tag, VR.UI, value);
        return this;
    }

    public final Attributes getDataset() {
        return data;
    }

    public final ImageException setDataset(Attributes data) {
        this.data = data;
        return this;
    }

    public Attributes mkRSP(int cmdField, int msgId) {
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rsp;
    }

}
