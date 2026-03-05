/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.metric.service;

import java.io.IOException;
import java.io.Serial;

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

    @Serial
    private static final long serialVersionUID = 2852276570267L;

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

    public ImageServiceException setNumberOfCompletedFailedWarningSuboperations(
            int completed,
            int failed,
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
