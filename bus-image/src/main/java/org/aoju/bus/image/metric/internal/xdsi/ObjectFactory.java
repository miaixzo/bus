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


import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author Kimi Liu
 * @version 6.1.1
 * @since JDK 1.8+
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RegistryRequest_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryRequest");
    private final static QName _RegistryResponse_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", "RegistryResponse");
    private final static QName _ResponseOption_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ResponseOption");
    private final static QName _AdhocQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AdhocQuery");
    private final static QName _RegistryObject_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryObject");
    private final static QName _Identifiable_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Identifiable");
    private final static QName _RegistryObjectList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryObjectList");
    private final static QName _RegistryObjectQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "RegistryObjectQuery");
    private final static QName _AssociationQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AssociationQuery");
    private final static QName _AuditableEventQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AuditableEventQuery");
    private final static QName _ClassificationQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ClassificationQuery");
    private final static QName _ClassificationNodeQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ClassificationNodeQuery");
    private final static QName _ClassificationSchemeQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ClassificationSchemeQuery");
    private final static QName _ExternalIdentifierQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ExternalIdentifierQuery");
    private final static QName _ExternalLinkQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ExternalLinkQuery");
    private final static QName _ExtrinsicObjectQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ExtrinsicObjectQuery");
    private final static QName _OrganizationQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "OrganizationQuery");
    private final static QName _RegistryPackageQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "RegistryPackageQuery");
    private final static QName _ServiceQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ServiceQuery");
    private final static QName _ServiceBindingQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "ServiceBindingQuery");
    private final static QName _SpecificationLinkQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "SpecificationLinkQuery");
    private final static QName _PersonQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "PersonQuery");
    private final static QName _UserQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "UserQuery");
    private final static QName _RegistryQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "RegistryQuery");
    private final static QName _FederationQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "FederationQuery");
    private final static QName _AdhocQueryQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "AdhocQueryQuery");
    private final static QName _NotificationQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "NotificationQuery");
    private final static QName _SubscriptionQuery_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "SubscriptionQuery");
    private final static QName _Filter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "Filter");
    private final static QName _CompoundFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "CompoundFilter");
    private final static QName _BooleanFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "BooleanFilter");
    private final static QName _IntegerFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "IntegerFilter");
    private final static QName _FloatFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "FloatFilter");
    private final static QName _DateTimeFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "DateTimeFilter");
    private final static QName _StringFilter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", "StringFilter");
    private final static QName _HomeCommunityId_QNAME = new QName("urn:ihe:iti:xds-b:2007", "HomeCommunityId");
    private final static QName _RepositoryUniqueId_QNAME = new QName("urn:ihe:iti:xds-b:2007", "RepositoryUniqueId");
    private final static QName _DocumentUniqueId_QNAME = new QName("urn:ihe:iti:xds-b:2007", "DocumentUniqueId");
    private final static QName _MimeType_QNAME = new QName("urn:ihe:iti:xds-b:2007", "mimeType");
    private final static QName _Document_QNAME = new QName("urn:ihe:iti:xds-b:2007", "Document");
    private final static QName _RetrieveDocumentSetRequest_QNAME = new QName("urn:ihe:iti:xds-b:2007", "RetrieveDocumentSetRequest");
    private final static QName _RetrieveDocumentSetResponse_QNAME = new QName("urn:ihe:iti:xds-b:2007", "RetrieveDocumentSetResponse");
    private final static QName _ProvideAndRegisterDocumentSetRequest_QNAME = new QName("urn:ihe:iti:xds-b:2007", "ProvideAndRegisterDocumentSetRequest");
    private final static QName _ObjectRefList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ObjectRefList");
    private final static QName _InternationalString_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "InternationalString");
    private final static QName _Name_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Name");
    private final static QName _Description_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Description");
    private final static QName _LocalizedString_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "LocalizedString");
    private final static QName _Slot_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Slot");
    private final static QName _ValueList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ValueList");
    private final static QName _Value_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Value");
    private final static QName _SlotList_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "SlotList");
    private final static QName _ObjectRef_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ObjectRef");
    private final static QName _Association_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Association");
    private final static QName _AuditableEvent_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "AuditableEvent");
    private final static QName _Classification_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Classification");
    private final static QName _ClassificationNode_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationNode");
    private final static QName _ClassificationScheme_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ClassificationScheme");
    private final static QName _ExternalIdentifier_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalIdentifier");
    private final static QName _ExternalLink_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExternalLink");
    private final static QName _ExtrinsicObject_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ExtrinsicObject");
    private final static QName _Address_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Address");
    private final static QName _Organization_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Organization");
    private final static QName _PersonName_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "PersonName");
    private final static QName _EmailAddress_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "EmailAddress");
    private final static QName _PostalAddress_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "PostalAddress");
    private final static QName _RegistryPackage_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "RegistryPackage");
    private final static QName _Service_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Service");
    private final static QName _ServiceBinding_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "ServiceBinding");
    private final static QName _SpecificationLink_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "SpecificationLink");
    private final static QName _UsageDescription_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "UsageDescription");
    private final static QName _UsageParameter_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "UsageParameter");
    private final static QName _TelephoneNumber_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "TelephoneNumber");
    private final static QName _Person_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Person");
    private final static QName _User_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "User");
    private final static QName _Registry_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Registry");
    private final static QName _Federation_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Federation");
    private final static QName _QueryExpression_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "QueryExpression");
    private final static QName _Notification_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Notification");
    private final static QName _Action_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Action");
    private final static QName _Subscription_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "Subscription");
    private final static QName _NotifyAction_QNAME = new QName("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", "NotifyAction");

    public ObjectFactory() {
    }

    public ProvideAndRegisterDocumentSetRequestType createProvideAndRegisterDocumentSetRequestType() {
        return new ProvideAndRegisterDocumentSetRequestType();
    }

    public RetrieveDocumentSetResponseType createRetrieveDocumentSetResponseType() {
        return new RetrieveDocumentSetResponseType();
    }

    public RetrieveDocumentSetRequestType createRetrieveDocumentSetRequestType() {
        return new RetrieveDocumentSetRequestType();
    }

    public RegistryRequestType createRegistryRequestType() {
        return new RegistryRequestType();
    }

    public RegistryErrorList createRegistryErrorList() {
        return new RegistryErrorList();
    }

    public RegistryError createRegistryError() {
        return new RegistryError();
    }

    public RegistryResponseType createRegistryResponseType() {
        return new RegistryResponseType();
    }

    public ResponseOptionType createResponseOptionType() {
        return new ResponseOptionType();
    }

    public AdhocQueryRequest createAdhocQueryRequest() {
        return new AdhocQueryRequest();
    }

    public SlotListType createSlotListType() {
        return new SlotListType();
    }

    public AdhocQueryType createAdhocQueryType() {
        return new AdhocQueryType();
    }

    public RegistryObjectType createRegistryObjectType() {
        return new RegistryObjectType();
    }

    public IdentifiableType createIdentifiableType() {
        return new IdentifiableType();
    }

    public AdhocQueryResponse createAdhocQueryResponse() {
        return new AdhocQueryResponse();
    }

    public RegistryObjectListType createRegistryObjectListType() {
        return new RegistryObjectListType();
    }

    public RegistryObjectQueryType createRegistryObjectQueryType() {
        return new RegistryObjectQueryType();
    }

    public AssociationQueryType createAssociationQueryType() {
        return new AssociationQueryType();
    }

    public AuditableEventQueryType createAuditableEventQueryType() {
        return new AuditableEventQueryType();
    }

    public ClassificationQueryType createClassificationQueryType() {
        return new ClassificationQueryType();
    }

    public ClassificationNodeQueryType createClassificationNodeQueryType() {
        return new ClassificationNodeQueryType();
    }

    public ClassificationSchemeQueryType createClassificationSchemeQueryType() {
        return new ClassificationSchemeQueryType();
    }

    public ExternalIdentifierQueryType createExternalIdentifierQueryType() {
        return new ExternalIdentifierQueryType();
    }

    public ExternalLinkQueryType createExternalLinkQueryType() {
        return new ExternalLinkQueryType();
    }

    public ExtrinsicObjectQueryType createExtrinsicObjectQueryType() {
        return new ExtrinsicObjectQueryType();
    }

    public OrganizationQueryType createOrganizationQueryType() {
        return new OrganizationQueryType();
    }

    public RegistryPackageQueryType createRegistryPackageQueryType() {
        return new RegistryPackageQueryType();
    }

    public ServiceQueryType createServiceQueryType() {
        return new ServiceQueryType();
    }

    public ServiceBindingQueryType createServiceBindingQueryType() {
        return new ServiceBindingQueryType();
    }

    public SpecificationLinkQueryType createSpecificationLinkQueryType() {
        return new SpecificationLinkQueryType();
    }

    public PersonQueryType createPersonQueryType() {
        return new PersonQueryType();
    }

    public UserQueryType createUserQueryType() {
        return new UserQueryType();
    }

    public RegistryQueryType createRegistryQueryType() {
        return new RegistryQueryType();
    }

    public FederationQueryType createFederationQueryType() {
        return new FederationQueryType();
    }

    public AdhocQueryQueryType createAdhocQueryQueryType() {
        return new AdhocQueryQueryType();
    }

    public NotificationQueryType createNotificationQueryType() {
        return new NotificationQueryType();
    }

    public SubscriptionQueryType createSubscriptionQueryType() {
        return new SubscriptionQueryType();
    }

    public FilterType createFilterType() {
        return new FilterType();
    }

    public CompoundFilterType createCompoundFilterType() {
        return new CompoundFilterType();
    }

    public BooleanFilterType createBooleanFilterType() {
        return new BooleanFilterType();
    }

    public IntegerFilterType createIntegerFilterType() {
        return new IntegerFilterType();
    }

    public FloatFilterType createFloatFilterType() {
        return new FloatFilterType();
    }

    public DateTimeFilterType createDateTimeFilterType() {
        return new DateTimeFilterType();
    }

    public StringFilterType createStringFilterType() {
        return new StringFilterType();
    }

    public InternationalStringBranchType createInternationalStringBranchType() {
        return new InternationalStringBranchType();
    }

    public SlotBranchType createSlotBranchType() {
        return new SlotBranchType();
    }

    public QueryExpressionBranchType createQueryExpressionBranchType() {
        return new QueryExpressionBranchType();
    }

    public SubmitObjectsRequest createSubmitObjectsRequest() {
        return new SubmitObjectsRequest();
    }

    public UpdateObjectsRequest createUpdateObjectsRequest() {
        return new UpdateObjectsRequest();
    }

    public ApproveObjectsRequest createApproveObjectsRequest() {
        return new ApproveObjectsRequest();
    }

    public ObjectRefListType createObjectRefListType() {
        return new ObjectRefListType();
    }

    public DeprecateObjectsRequest createDeprecateObjectsRequest() {
        return new DeprecateObjectsRequest();
    }

    public UndeprecateObjectsRequest createUndeprecateObjectsRequest() {
        return new UndeprecateObjectsRequest();
    }

    public RemoveObjectsRequest createRemoveObjectsRequest() {
        return new RemoveObjectsRequest();
    }

    public RelocateObjectsRequest createRelocateObjectsRequest() {
        return new RelocateObjectsRequest();
    }

    public ObjectRefType createObjectRefType() {
        return new ObjectRefType();
    }

    public AcceptObjectsRequest createAcceptObjectsRequest() {
        return new AcceptObjectsRequest();
    }

    public InternationalStringType createInternationalStringType() {
        return new InternationalStringType();
    }

    public LocalizedStringType createLocalizedStringType() {
        return new LocalizedStringType();
    }

    public SlotType createSlotType1() {
        return new SlotType();
    }

    public ValueListType createValueListType() {
        return new ValueListType();
    }

    public AssociationType createAssociationType() {
        return new AssociationType();
    }

    public AuditableEventType createAuditableEventType() {
        return new AuditableEventType();
    }

    public ClassificationType createClassificationType() {
        return new ClassificationType();
    }

    public ClassificationNodeType createClassificationNodeType() {
        return new ClassificationNodeType();
    }

    public ClassificationSchemeType createClassificationSchemeType() {
        return new ClassificationSchemeType();
    }

    public ExternalIdentifierType createExternalIdentifierType() {
        return new ExternalIdentifierType();
    }

    public ExternalLinkType createExternalLinkType() {
        return new ExternalLinkType();
    }

    public ExtrinsicObjectType createExtrinsicObjectType() {
        return new ExtrinsicObjectType();
    }

    public PostalAddressType createPostalAddressType() {
        return new PostalAddressType();
    }

    public OrganizationType createOrganizationType() {
        return new OrganizationType();
    }

    public PersonNameType createPersonNameType() {
        return new PersonNameType();
    }

    public EmailAddressType createEmailAddressType() {
        return new EmailAddressType();
    }

    public RegistryPackageType createRegistryPackageType() {
        return new RegistryPackageType();
    }

    public ServiceType createServiceType() {
        return new ServiceType();
    }

    public ServiceBindingType createServiceBindingType() {
        return new ServiceBindingType();
    }

    public SpecificationLinkType createSpecificationLinkType() {
        return new SpecificationLinkType();
    }

    public TelephoneNumberType createTelephoneNumberType() {
        return new TelephoneNumberType();
    }

    public PersonType createPersonType() {
        return new PersonType();
    }

    public UserType createUserType() {
        return new UserType();
    }

    public RegistryType createRegistryType() {
        return new RegistryType();
    }

    public FederationType createFederationType() {
        return new FederationType();
    }

    public QueryExpressionType createQueryExpressionType() {
        return new QueryExpressionType();
    }

    public NotificationType createNotificationType() {
        return new NotificationType();
    }

    public SubscriptionType createSubscriptionType() {
        return new SubscriptionType();
    }

    public NotifyActionType createNotifyActionType() {
        return new NotifyActionType();
    }

    public VersionInfoType createVersionInfoType() {
        return new VersionInfoType();
    }

    public TelephoneNumberListType createTelephoneNumberListType() {
        return new TelephoneNumberListType();
    }

    public ProvideAndRegisterDocumentSetRequestType.Document createProvideAndRegisterDocumentSetRequestTypeDocument() {
        return new ProvideAndRegisterDocumentSetRequestType.Document();
    }

    public RetrieveDocumentSetResponseType.DocumentResponse createRetrieveDocumentSetResponseTypeDocumentResponse() {
        return new RetrieveDocumentSetResponseType.DocumentResponse();
    }

    public RetrieveDocumentSetRequestType.DocumentRequest createRetrieveDocumentSetRequestTypeDocumentRequest() {
        return new RetrieveDocumentSetRequestType.DocumentRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryRequestType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", name = "RegistryRequest")
    public JAXBElement<RegistryRequestType> createRegistryRequest(RegistryRequestType value) {
        return new JAXBElement<>(_RegistryRequest_QNAME, RegistryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryResponseType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryResponseType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", name = "RegistryResponse")
    public JAXBElement<RegistryResponseType> createRegistryResponse(RegistryResponseType value) {
        return new JAXBElement<>(_RegistryResponse_QNAME, RegistryResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseOptionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ResponseOptionType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ResponseOption")
    public JAXBElement<ResponseOptionType> createResponseOption(ResponseOptionType value) {
        return new JAXBElement<>(_ResponseOption_QNAME, ResponseOptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdhocQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AdhocQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "AdhocQuery", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "RegistryObject")
    public JAXBElement<AdhocQueryType> createAdhocQuery(AdhocQueryType value) {
        return new JAXBElement<>(_AdhocQuery_QNAME, AdhocQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryObjectType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "RegistryObject", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<RegistryObjectType> createRegistryObject(RegistryObjectType value) {
        return new JAXBElement<>(_RegistryObject_QNAME, RegistryObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifiableType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link IdentifiableType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Identifiable")
    public JAXBElement<IdentifiableType> createIdentifiable(IdentifiableType value) {
        return new JAXBElement<>(_Identifiable_QNAME, IdentifiableType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectListType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryObjectListType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "RegistryObjectList")
    public JAXBElement<RegistryObjectListType> createRegistryObjectList(RegistryObjectListType value) {
        return new JAXBElement<>(_RegistryObjectList_QNAME, RegistryObjectListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryObjectQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryObjectQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "RegistryObjectQuery")
    public JAXBElement<RegistryObjectQueryType> createRegistryObjectQuery(RegistryObjectQueryType value) {
        return new JAXBElement<>(_RegistryObjectQuery_QNAME, RegistryObjectQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssociationQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AssociationQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "AssociationQuery")
    public JAXBElement<AssociationQueryType> createAssociationQuery(AssociationQueryType value) {
        return new JAXBElement<>(_AssociationQuery_QNAME, AssociationQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuditableEventQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AuditableEventQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "AuditableEventQuery")
    public JAXBElement<AuditableEventQueryType> createAuditableEventQuery(AuditableEventQueryType value) {
        return new JAXBElement<>(_AuditableEventQuery_QNAME, AuditableEventQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ClassificationQuery")
    public JAXBElement<ClassificationQueryType> createClassificationQuery(ClassificationQueryType value) {
        return new JAXBElement<>(_ClassificationQuery_QNAME, ClassificationQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationNodeQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationNodeQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ClassificationNodeQuery")
    public JAXBElement<ClassificationNodeQueryType> createClassificationNodeQuery(ClassificationNodeQueryType value) {
        return new JAXBElement<>(_ClassificationNodeQuery_QNAME, ClassificationNodeQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationSchemeQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationSchemeQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ClassificationSchemeQuery")
    public JAXBElement<ClassificationSchemeQueryType> createClassificationSchemeQuery(ClassificationSchemeQueryType value) {
        return new JAXBElement<>(_ClassificationSchemeQuery_QNAME, ClassificationSchemeQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalIdentifierQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExternalIdentifierQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ExternalIdentifierQuery")
    public JAXBElement<ExternalIdentifierQueryType> createExternalIdentifierQuery(ExternalIdentifierQueryType value) {
        return new JAXBElement<>(_ExternalIdentifierQuery_QNAME, ExternalIdentifierQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalLinkQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExternalLinkQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ExternalLinkQuery")
    public JAXBElement<ExternalLinkQueryType> createExternalLinkQuery(ExternalLinkQueryType value) {
        return new JAXBElement<>(_ExternalLinkQuery_QNAME, ExternalLinkQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtrinsicObjectQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExtrinsicObjectQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ExtrinsicObjectQuery")
    public JAXBElement<ExtrinsicObjectQueryType> createExtrinsicObjectQuery(ExtrinsicObjectQueryType value) {
        return new JAXBElement<>(_ExtrinsicObjectQuery_QNAME, ExtrinsicObjectQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrganizationQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link OrganizationQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "OrganizationQuery")
    public JAXBElement<OrganizationQueryType> createOrganizationQuery(OrganizationQueryType value) {
        return new JAXBElement<>(_OrganizationQuery_QNAME, OrganizationQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryPackageQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryPackageQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "RegistryPackageQuery")
    public JAXBElement<RegistryPackageQueryType> createRegistryPackageQuery(RegistryPackageQueryType value) {
        return new JAXBElement<>(_RegistryPackageQuery_QNAME, RegistryPackageQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ServiceQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ServiceQuery")
    public JAXBElement<ServiceQueryType> createServiceQuery(ServiceQueryType value) {
        return new JAXBElement<>(_ServiceQuery_QNAME, ServiceQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceBindingQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ServiceBindingQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "ServiceBindingQuery")
    public JAXBElement<ServiceBindingQueryType> createServiceBindingQuery(ServiceBindingQueryType value) {
        return new JAXBElement<>(_ServiceBindingQuery_QNAME, ServiceBindingQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SpecificationLinkQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SpecificationLinkQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "SpecificationLinkQuery")
    public JAXBElement<SpecificationLinkQueryType> createSpecificationLinkQuery(SpecificationLinkQueryType value) {
        return new JAXBElement<>(_SpecificationLinkQuery_QNAME, SpecificationLinkQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PersonQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link PersonQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "PersonQuery")
    public JAXBElement<PersonQueryType> createPersonQuery(PersonQueryType value) {
        return new JAXBElement<>(_PersonQuery_QNAME, PersonQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link UserQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "UserQuery")
    public JAXBElement<UserQueryType> createUserQuery(UserQueryType value) {
        return new JAXBElement<>(_UserQuery_QNAME, UserQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "RegistryQuery")
    public JAXBElement<RegistryQueryType> createRegistryQuery(RegistryQueryType value) {
        return new JAXBElement<>(_RegistryQuery_QNAME, RegistryQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FederationQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link FederationQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "FederationQuery")
    public JAXBElement<FederationQueryType> createFederationQuery(FederationQueryType value) {
        return new JAXBElement<>(_FederationQuery_QNAME, FederationQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdhocQueryQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AdhocQueryQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "AdhocQueryQuery")
    public JAXBElement<AdhocQueryQueryType> createAdhocQueryQuery(AdhocQueryQueryType value) {
        return new JAXBElement<>(_AdhocQueryQuery_QNAME, AdhocQueryQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link NotificationQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "NotificationQuery")
    public JAXBElement<NotificationQueryType> createNotificationQuery(NotificationQueryType value) {
        return new JAXBElement<>(_NotificationQuery_QNAME, NotificationQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscriptionQueryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SubscriptionQueryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "SubscriptionQuery")
    public JAXBElement<SubscriptionQueryType> createSubscriptionQuery(SubscriptionQueryType value) {
        return new JAXBElement<>(_SubscriptionQuery_QNAME, SubscriptionQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link FilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "Filter")
    public JAXBElement<FilterType> createFilter(FilterType value) {
        return new JAXBElement<>(_Filter_QNAME, FilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompoundFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link CompoundFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "CompoundFilter")
    public JAXBElement<CompoundFilterType> createCompoundFilter(CompoundFilterType value) {
        return new JAXBElement<>(_CompoundFilter_QNAME, CompoundFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link BooleanFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "BooleanFilter")
    public JAXBElement<BooleanFilterType> createBooleanFilter(BooleanFilterType value) {
        return new JAXBElement<>(_BooleanFilter_QNAME, BooleanFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IntegerFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link IntegerFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "IntegerFilter")
    public JAXBElement<IntegerFilterType> createIntegerFilter(IntegerFilterType value) {
        return new JAXBElement<>(_IntegerFilter_QNAME, IntegerFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FloatFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link FloatFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "FloatFilter")
    public JAXBElement<FloatFilterType> createFloatFilter(FloatFilterType value) {
        return new JAXBElement<>(_FloatFilter_QNAME, FloatFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link DateTimeFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "DateTimeFilter")
    public JAXBElement<DateTimeFilterType> createDateTimeFilter(DateTimeFilterType value) {
        return new JAXBElement<>(_DateTimeFilter_QNAME, DateTimeFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringFilterType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link StringFilterType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", name = "StringFilter")
    public JAXBElement<StringFilterType> createStringFilter(StringFilterType value) {
        return new JAXBElement<>(_StringFilter_QNAME, StringFilterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "HomeCommunityId")
    public JAXBElement<String> createHomeCommunityId(String value) {
        return new JAXBElement<>(_HomeCommunityId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "RepositoryUniqueId")
    public JAXBElement<String> createRepositoryUniqueId(String value) {
        return new JAXBElement<>(_RepositoryUniqueId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "DocumentUniqueId")
    public JAXBElement<String> createDocumentUniqueId(String value) {
        return new JAXBElement<>(_DocumentUniqueId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "mimeType")
    public JAXBElement<String> createMimeType(String value) {
        return new JAXBElement<>(_MimeType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataHandler }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link DataHandler }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "Document")
    @XmlMimeType("*/*")
    public JAXBElement<DataHandler> createDocument(DataHandler value) {
        return new JAXBElement<>(_Document_QNAME, DataHandler.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveDocumentSetRequestType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RetrieveDocumentSetRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "RetrieveDocumentSetRequest")
    public JAXBElement<RetrieveDocumentSetRequestType> createRetrieveDocumentSetRequest(RetrieveDocumentSetRequestType value) {
        return new JAXBElement<>(_RetrieveDocumentSetRequest_QNAME, RetrieveDocumentSetRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveDocumentSetResponseType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RetrieveDocumentSetResponseType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "RetrieveDocumentSetResponse")
    public JAXBElement<RetrieveDocumentSetResponseType> createRetrieveDocumentSetResponse(RetrieveDocumentSetResponseType value) {
        return new JAXBElement<>(_RetrieveDocumentSetResponse_QNAME, RetrieveDocumentSetResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvideAndRegisterDocumentSetRequestType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ProvideAndRegisterDocumentSetRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:ihe:iti:xds-b:2007", name = "ProvideAndRegisterDocumentSetRequest")
    public JAXBElement<ProvideAndRegisterDocumentSetRequestType> createProvideAndRegisterDocumentSetRequest(ProvideAndRegisterDocumentSetRequestType value) {
        return new JAXBElement<>(_ProvideAndRegisterDocumentSetRequest_QNAME, ProvideAndRegisterDocumentSetRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectRefListType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ObjectRefListType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ObjectRefList")
    public JAXBElement<ObjectRefListType> createObjectRefList(ObjectRefListType value) {
        return new JAXBElement<>(_ObjectRefList_QNAME, ObjectRefListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "InternationalString")
    public JAXBElement<InternationalStringType> createInternationalString(InternationalStringType value) {
        return new JAXBElement<>(_InternationalString_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Name")
    public JAXBElement<InternationalStringType> createName(InternationalStringType value) {
        return new JAXBElement<>(_Name_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Description")
    public JAXBElement<InternationalStringType> createDescription(InternationalStringType value) {
        return new JAXBElement<>(_Description_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocalizedStringType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link LocalizedStringType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "LocalizedString")
    public JAXBElement<LocalizedStringType> createLocalizedString(LocalizedStringType value) {
        return new JAXBElement<>(_LocalizedString_QNAME, LocalizedStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SlotType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SlotType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Slot")
    public JAXBElement<SlotType> createSlot(SlotType value) {
        return new JAXBElement<>(_Slot_QNAME, SlotType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValueListType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ValueListType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ValueList")
    public JAXBElement<ValueListType> createValueList(ValueListType value) {
        return new JAXBElement<>(_ValueList_QNAME, ValueListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SlotListType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SlotListType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "SlotList")
    public JAXBElement<SlotListType> createSlotList(SlotListType value) {
        return new JAXBElement<>(_SlotList_QNAME, SlotListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectRefType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ObjectRefType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ObjectRef", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ObjectRefType> createObjectRef(ObjectRefType value) {
        return new JAXBElement<>(_ObjectRef_QNAME, ObjectRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssociationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AssociationType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Association", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<AssociationType> createAssociation(AssociationType value) {
        return new JAXBElement<>(_Association_QNAME, AssociationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuditableEventType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link AuditableEventType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "AuditableEvent", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<AuditableEventType> createAuditableEvent(AuditableEventType value) {
        return new JAXBElement<>(_AuditableEvent_QNAME, AuditableEventType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Classification", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ClassificationType> createClassification(ClassificationType value) {
        return new JAXBElement<>(_Classification_QNAME, ClassificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationNodeType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationNodeType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ClassificationNode", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ClassificationNodeType> createClassificationNode(ClassificationNodeType value) {
        return new JAXBElement<>(_ClassificationNode_QNAME, ClassificationNodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClassificationSchemeType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClassificationSchemeType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ClassificationScheme", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ClassificationSchemeType> createClassificationScheme(ClassificationSchemeType value) {
        return new JAXBElement<>(_ClassificationScheme_QNAME, ClassificationSchemeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalIdentifierType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExternalIdentifierType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ExternalIdentifier", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ExternalIdentifierType> createExternalIdentifier(ExternalIdentifierType value) {
        return new JAXBElement<>(_ExternalIdentifier_QNAME, ExternalIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalLinkType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExternalLinkType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ExternalLink", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ExternalLinkType> createExternalLink(ExternalLinkType value) {
        return new JAXBElement<>(_ExternalLink_QNAME, ExternalLinkType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtrinsicObjectType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ExtrinsicObjectType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ExtrinsicObject", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ExtrinsicObjectType> createExtrinsicObject(ExtrinsicObjectType value) {
        return new JAXBElement<ExtrinsicObjectType>(_ExtrinsicObject_QNAME, ExtrinsicObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PostalAddressType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link PostalAddressType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Address")
    public JAXBElement<PostalAddressType> createAddress(PostalAddressType value) {
        return new JAXBElement<>(_Address_QNAME, PostalAddressType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OrganizationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link OrganizationType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Organization", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<OrganizationType> createOrganization(OrganizationType value) {
        return new JAXBElement<>(_Organization_QNAME, OrganizationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PersonNameType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link PersonNameType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "PersonName")
    public JAXBElement<PersonNameType> createPersonName(PersonNameType value) {
        return new JAXBElement<>(_PersonName_QNAME, PersonNameType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EmailAddressType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link EmailAddressType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "EmailAddress")
    public JAXBElement<EmailAddressType> createEmailAddress(EmailAddressType value) {
        return new JAXBElement<>(_EmailAddress_QNAME, EmailAddressType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PostalAddressType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link PostalAddressType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "PostalAddress")
    public JAXBElement<PostalAddressType> createPostalAddress(PostalAddressType value) {
        return new JAXBElement<>(_PostalAddress_QNAME, PostalAddressType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryPackageType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryPackageType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "RegistryPackage", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<RegistryPackageType> createRegistryPackage(RegistryPackageType value) {
        return new JAXBElement<>(_RegistryPackage_QNAME, RegistryPackageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ServiceType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Service", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ServiceType> createService(ServiceType value) {
        return new JAXBElement<>(_Service_QNAME, ServiceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceBindingType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ServiceBindingType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "ServiceBinding", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<ServiceBindingType> createServiceBinding(ServiceBindingType value) {
        return new JAXBElement<>(_ServiceBinding_QNAME, ServiceBindingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SpecificationLinkType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SpecificationLinkType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "SpecificationLink", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<SpecificationLinkType> createSpecificationLink(SpecificationLinkType value) {
        return new JAXBElement<>(_SpecificationLink_QNAME, SpecificationLinkType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link InternationalStringType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "UsageDescription")
    public JAXBElement<InternationalStringType> createUsageDescription(InternationalStringType value) {
        return new JAXBElement<>(_UsageDescription_QNAME, InternationalStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "UsageParameter")
    public JAXBElement<String> createUsageParameter(String value) {
        return new JAXBElement<>(_UsageParameter_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TelephoneNumberType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TelephoneNumberType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "TelephoneNumber")
    public JAXBElement<TelephoneNumberType> createTelephoneNumber(TelephoneNumberType value) {
        return new JAXBElement<>(_TelephoneNumber_QNAME, TelephoneNumberType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PersonType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link PersonType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Person", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<PersonType> createPerson(PersonType value) {
        return new JAXBElement<>(_Person_QNAME, PersonType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link UserType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "User", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<UserType> createUser(UserType value) {
        return new JAXBElement<>(_User_QNAME, UserType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegistryType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link RegistryType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Registry", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<RegistryType> createRegistry(RegistryType value) {
        return new JAXBElement<>(_Registry_QNAME, RegistryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FederationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link FederationType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Federation", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<FederationType> createFederation(FederationType value) {
        return new JAXBElement<>(_Federation_QNAME, FederationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryExpressionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link QueryExpressionType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "QueryExpression")
    public JAXBElement<QueryExpressionType> createQueryExpression(QueryExpressionType value) {
        return new JAXBElement<>(_QueryExpression_QNAME, QueryExpressionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Notification")
    public JAXBElement<NotificationType> createNotification(NotificationType value) {
        return new JAXBElement<>(_Notification_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ActionType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Action")
    public JAXBElement<ActionType> createAction(ActionType value) {
        return new JAXBElement<>(_Action_QNAME, ActionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscriptionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link SubscriptionType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "Subscription", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Identifiable")
    public JAXBElement<SubscriptionType> createSubscription(SubscriptionType value) {
        return new JAXBElement<>(_Subscription_QNAME, SubscriptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotifyActionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link NotifyActionType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", name = "NotifyAction", substitutionHeadNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0", substitutionHeadName = "Action")
    public JAXBElement<NotifyActionType> createNotifyAction(NotifyActionType value) {
        return new JAXBElement<>(_NotifyAction_QNAME, NotifyActionType.class, null, value);
    }

}
