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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersonType", propOrder = {"address", "personName", "telephoneNumber", "emailAddress"})
@XmlSeeAlso({UserType.class})
public class PersonType extends RegistryObjectType {

    @XmlElement(name = "Address")
    protected List<PostalAddressType> address;
    @XmlElement(name = "PersonName")
    protected PersonNameType personName;
    @XmlElement(name = "TelephoneNumber")
    protected List<TelephoneNumberType> telephoneNumber;
    @XmlElement(name = "EmailAddress")
    protected List<EmailAddressType> emailAddress;

    public List<PostalAddressType> getAddress() {
        if (this.address == null) {
            this.address = new ArrayList();
        }
        return this.address;
    }

    public PersonNameType getPersonName() {
        return this.personName;
    }

    public void setPersonName(PersonNameType value) {
        this.personName = value;
    }

    public List<TelephoneNumberType> getTelephoneNumber() {
        if (this.telephoneNumber == null) {
            this.telephoneNumber = new ArrayList();
        }
        return this.telephoneNumber;
    }

    public List<EmailAddressType> getEmailAddress() {
        if (this.emailAddress == null) {
            this.emailAddress = new ArrayList();
        }
        return this.emailAddress;
    }

}
