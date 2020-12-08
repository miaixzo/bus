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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryObjectType", propOrder = {"name", "description", "versionInfo", "classification", "externalIdentifier"})
@XmlSeeAlso({AdhocQueryType.class, AssociationType.class, AuditableEventType.class, ClassificationType.class, ClassificationNodeType.class, ClassificationSchemeType.class, ExternalIdentifierType.class, ExternalLinkType.class, ExtrinsicObjectType.class, OrganizationType.class, RegistryPackageType.class, ServiceType.class, ServiceBindingType.class, SpecificationLinkType.class, PersonType.class, RegistryType.class, FederationType.class, NotificationType.class, SubscriptionType.class})
public class RegistryObjectType extends IdentifiableType {

    @XmlElement(name = "Name")
    protected InternationalStringType name;
    @XmlElement(name = "Description")
    protected InternationalStringType description;
    @XmlElement(name = "VersionInfo")
    protected VersionInfoType versionInfo;
    @XmlElement(name = "Classification")
    protected List<ClassificationType> classification;
    @XmlElement(name = "ExternalIdentifier")
    protected List<ExternalIdentifierType> externalIdentifier;
    @XmlAttribute(name = "lid")
    @XmlSchemaType(name = "anyURI")
    protected String lid;
    @XmlAttribute(name = "objectType")
    protected String objectType;
    @XmlAttribute(name = "status")
    protected String status;

    public InternationalStringType getName() {
        return this.name;
    }

    public void setName(InternationalStringType value) {
        this.name = value;
    }

    public InternationalStringType getDescription() {
        return this.description;
    }

    public void setDescription(InternationalStringType value) {
        this.description = value;
    }

    public VersionInfoType getVersionInfo() {

        return this.versionInfo;
    }


    public void setVersionInfo(VersionInfoType value) {
        this.versionInfo = value;
    }

    public List<ClassificationType> getClassification() {
        if (this.classification == null) {
            this.classification = new ArrayList();
        }
        return this.classification;
    }

    public List<ExternalIdentifierType> getExternalIdentifier() {
        if (this.externalIdentifier == null) {
            this.externalIdentifier = new ArrayList();
        }
        return this.externalIdentifier;
    }

    public String getLid() {
        return this.lid;
    }

    public void setLid(String value) {
        this.lid = value;
    }

    public String getObjectType() {
        return this.objectType;
    }

    public void setObjectType(String value) {
        this.objectType = value;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

}

