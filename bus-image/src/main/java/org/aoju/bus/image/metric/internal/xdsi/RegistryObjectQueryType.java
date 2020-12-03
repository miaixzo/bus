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
 * @version 6.1.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryObjectQueryType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {"slotBranch", "nameBranch", "descriptionBranch", "versionInfoFilter", "classificationQuery", "externalIdentifierQuery", "objectTypeQuery", "statusQuery", "sourceAssociationQuery", "targetAssociationQuery"})
@XmlSeeAlso({AssociationQueryType.class, AuditableEventQueryType.class, ClassificationQueryType.class, ClassificationNodeQueryType.class, ClassificationSchemeQueryType.class, ExternalIdentifierQueryType.class, ExternalLinkQueryType.class, ExtrinsicObjectQueryType.class, OrganizationQueryType.class, RegistryPackageQueryType.class, ServiceQueryType.class, ServiceBindingQueryType.class, SpecificationLinkQueryType.class, PersonQueryType.class, RegistryQueryType.class, FederationQueryType.class, AdhocQueryQueryType.class, NotificationQueryType.class, SubscriptionQueryType.class})
public class RegistryObjectQueryType extends FilterQueryType {

    @XmlElement(name = "SlotBranch")
    protected List<SlotBranchType> slotBranch;
    @XmlElement(name = "NameBranch")
    protected InternationalStringBranchType nameBranch;
    @XmlElement(name = "DescriptionBranch")
    protected InternationalStringBranchType descriptionBranch;
    @XmlElement(name = "VersionInfoFilter")
    protected FilterType versionInfoFilter;
    @XmlElement(name = "ClassificationQuery")
    protected List<ClassificationQueryType> classificationQuery;
    @XmlElement(name = "ExternalIdentifierQuery")
    protected List<ExternalIdentifierQueryType> externalIdentifierQuery;
    @XmlElement(name = "ObjectTypeQuery")
    protected ClassificationNodeQueryType objectTypeQuery;
    @XmlElement(name = "StatusQuery")
    protected ClassificationNodeQueryType statusQuery;
    @XmlElement(name = "SourceAssociationQuery")
    protected List<AssociationQueryType> sourceAssociationQuery;
    @XmlElement(name = "TargetAssociationQuery")
    protected List<AssociationQueryType> targetAssociationQuery;

    public List<SlotBranchType> getSlotBranch() {
        if (this.slotBranch == null) {
            this.slotBranch = new ArrayList();
        }
        return this.slotBranch;
    }

    public InternationalStringBranchType getNameBranch() {
        return this.nameBranch;
    }

    public void setNameBranch(InternationalStringBranchType value) {
        this.nameBranch = value;
    }

    public InternationalStringBranchType getDescriptionBranch() {
        return this.descriptionBranch;
    }

    public void setDescriptionBranch(InternationalStringBranchType value) {
        this.descriptionBranch = value;
    }

    public FilterType getVersionInfoFilter() {
        return this.versionInfoFilter;
    }

    public void setVersionInfoFilter(FilterType value) {
        this.versionInfoFilter = value;
    }

    public List<ClassificationQueryType> getClassificationQuery() {
        if (this.classificationQuery == null) {
            this.classificationQuery = new ArrayList();
        }
        return this.classificationQuery;
    }

    public List<ExternalIdentifierQueryType> getExternalIdentifierQuery() {
        if (this.externalIdentifierQuery == null) {
            this.externalIdentifierQuery = new ArrayList();
        }
        return this.externalIdentifierQuery;
    }

    public ClassificationNodeQueryType getObjectTypeQuery() {
        return this.objectTypeQuery;
    }

    public void setObjectTypeQuery(ClassificationNodeQueryType value) {
        this.objectTypeQuery = value;
    }

    public ClassificationNodeQueryType getStatusQuery() {
        return this.statusQuery;
    }

    public void setStatusQuery(ClassificationNodeQueryType value) {
        this.statusQuery = value;
    }

    public List<AssociationQueryType> getSourceAssociationQuery() {
        if (this.sourceAssociationQuery == null) {
            this.sourceAssociationQuery = new ArrayList();
        }
        return this.sourceAssociationQuery;
    }

    public List<AssociationQueryType> getTargetAssociationQuery() {
        if (this.targetAssociationQuery == null) {
            this.targetAssociationQuery = new ArrayList();
        }
        return this.targetAssociationQuery;
    }

}

