/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.metric.internal.xdsi;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganizationType", propOrder = {"address", "telephoneNumber", "emailAddress"})
public class OrganizationType extends RegistryObjectType {

    @XmlElement(name = "Address")
    protected List<PostalAddressType> address;
    @XmlElement(name = "TelephoneNumber")
    protected List<TelephoneNumberType> telephoneNumber;
    @XmlElement(name = "EmailAddress")
    protected List<EmailAddressType> emailAddress;
    @XmlAttribute(name = "parent")
    protected String parent;
    @XmlAttribute(name = "primaryContact")
    protected String primaryContact;

    public List<PostalAddressType> getAddress() {
        if (null == this.address) {
            this.address = new ArrayList();
        }
        return this.address;
    }

    public List<TelephoneNumberType> getTelephoneNumber() {
        if (null == this.telephoneNumber) {
            this.telephoneNumber = new ArrayList();
        }
        return this.telephoneNumber;
    }

    public List<EmailAddressType> getEmailAddress() {
        if (null == this.emailAddress) {
            this.emailAddress = new ArrayList();
        }
        return this.emailAddress;
    }

    public String getParent() {
        return this.parent;
    }

    public void setParent(String value) {
        this.parent = value;
    }


    public String getPrimaryContact() {
        return this.primaryContact;
    }

    public void setPrimaryContact(String value) {
        this.primaryContact = value;
    }

}
