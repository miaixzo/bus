/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.image.metric.internal.xdsi;

import javax.xml.bind.annotation.*;

/**
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotifyActionType")
public class NotifyActionType extends ActionType {

    @XmlAttribute(name = "notificationOption")
    protected String notificationOption;
    @XmlAttribute(name = "endPoint", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String endPoint;

    public String getNotificationOption() {
        if (this.notificationOption == null) {
            return "urn:oasis:names:tc:ebxml-regrep:NotificationOptionType:ObjectRefs";
        }
        return this.notificationOption;
    }

    public void setNotificationOption(String value) {
        this.notificationOption = value;
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(String value) {
        this.endPoint = value;
    }

}
