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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric.internal.xdsi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganizationQueryType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {"addressFilter", "telephoneNumberFilter", "emailAddressFilter", "parentQuery", "childOrganizationQuery", "primaryContactQuery"})
public class OrganizationQueryType extends RegistryObjectQueryType {

    @XmlElement(name = "AddressFilter")
    protected List<FilterType> addressFilter;
    @XmlElement(name = "TelephoneNumberFilter")
    protected List<FilterType> telephoneNumberFilter;
    @XmlElement(name = "EmailAddressFilter")
    protected List<FilterType> emailAddressFilter;
    @XmlElement(name = "ParentQuery")
    protected OrganizationQueryType parentQuery;
    @XmlElement(name = "ChildOrganizationQuery")
    protected List<OrganizationQueryType> childOrganizationQuery;
    @XmlElement(name = "PrimaryContactQuery")
    protected PersonQueryType primaryContactQuery;

    public List<FilterType> getAddressFilter() {
        if (this.addressFilter == null) {
            this.addressFilter = new ArrayList();
        }
        return this.addressFilter;
    }


    public List<FilterType> getTelephoneNumberFilter() {
        if (this.telephoneNumberFilter == null) {
            this.telephoneNumberFilter = new ArrayList();
        }
        return this.telephoneNumberFilter;
    }

    public List<FilterType> getEmailAddressFilter() {
        if (this.emailAddressFilter == null) {
            this.emailAddressFilter = new ArrayList();
        }
        return this.emailAddressFilter;
    }

    public OrganizationQueryType getParentQuery() {
        return this.parentQuery;
    }

    public void setParentQuery(OrganizationQueryType value) {
        this.parentQuery = value;
    }

    public List<OrganizationQueryType> getChildOrganizationQuery() {
        if (this.childOrganizationQuery == null) {
            this.childOrganizationQuery = new ArrayList();
        }
        return this.childOrganizationQuery;
    }

    public PersonQueryType getPrimaryContactQuery() {
        return this.primaryContactQuery;
    }

    public void setPrimaryContactQuery(PersonQueryType value) {
        this.primaryContactQuery = value;
    }

}
