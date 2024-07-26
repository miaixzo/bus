/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_LAYOUT_PROTOCOL;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.HangingProtocolExcellenceRank:
            return "HangingProtocolExcellenceRank";
        case PrivateTag.TemplateDataRoleID:
            return "TemplateDataRoleID";
        case PrivateTag.DataSharingFlag:
            return "DataSharingFlag";
        case PrivateTag.BaggingOperationsSequence:
            return "BaggingOperationsSequence";
        case PrivateTag.SynchronizationType:
            return "SynchronizationType";
        case PrivateTag.CustomFilterType:
            return "CustomFilterType";
        case PrivateTag.CustomSorterType:
            return "CustomSorterType";
        case PrivateTag.ReferenceTemplateDataRoleID:
            return "ReferenceTemplateDataRoleID";
        case PrivateTag.ModelTemplateDataRoleID:
            return "ModelTemplateDataRoleID";
        case PrivateTag.SelectorDTValue:
            return "SelectorDTValue";
        case PrivateTag.SelectorDAValue:
            return "SelectorDAValue";
        case PrivateTag.SelectorTMValue:
            return "SelectorTMValue";
        case PrivateTag.SelectorUIValue:
            return "SelectorUIValue";
        case PrivateTag.ReferencedTemplateDataRole:
            return "ReferencedTemplateDataRole";
        case PrivateTag.CustomPropertySequence:
            return "CustomPropertySequence";
        case PrivateTag.CustomPropertyType:
            return "CustomPropertyType";
        case PrivateTag.CustomPropertyName:
            return "CustomPropertyName";
        case PrivateTag.CustomPropertyValue:
            return "CustomPropertyValue";
        case PrivateTag.LayoutPropertySequence:
            return "LayoutPropertySequence";
        case PrivateTag.SynchronizationSequence:
            return "SynchronizationSequence";
        case PrivateTag.PresentationCreatorType:
            return "PresentationCreatorType";
        case PrivateTag.CineNavigationType:
            return "CineNavigationType";
        case PrivateTag.SemanticNamingStrategy:
            return "SemanticNamingStrategy";
        case PrivateTag.ParameterString:
            return "ParameterString";
        case PrivateTag.SortingOrder:
            return "SortingOrder";
        case PrivateTag.syngoTemplateType:
            return "syngoTemplateType";
        case PrivateTag.SorterType:
            return "SorterType";
        case PrivateTag.DataDisplayProtocolVersion:
            return "DataDisplayProtocolVersion";
        case PrivateTag.TimepointValue:
            return "TimepointValue";
        case PrivateTag.SharingGroupSequence:
            return "SharingGroupSequence";
        case PrivateTag.TemplateSelectorOperator:
            return "TemplateSelectorOperator";
        case PrivateTag.SharingType:
            return "SharingType";
        case PrivateTag.ViewportDefinitionsSequence:
            return "ViewportDefinitionsSequence";
        case PrivateTag.ProtocolType:
            return "ProtocolType";
        case PrivateTag.TemplateSelectorSequence:
            return "TemplateSelectorSequence";
        case PrivateTag.DefaultTemplate:
            return "DefaultTemplate";
        case PrivateTag.IsPreferred:
            return "IsPreferred";
        case PrivateTag.TimepointInitialValueSequence:
            return "TimepointInitialValueSequence";
        case PrivateTag.TimepointVariable:
            return "TimepointVariable";
        case PrivateTag.DisplayProtocolName:
            return "DisplayProtocolName";
        case PrivateTag.DisplayProtocolDescription:
            return "DisplayProtocolDescription";
        case PrivateTag.DisplayProtocolLevel:
            return "DisplayProtocolLevel";
        case PrivateTag.DisplayProtocolCreator:
            return "DisplayProtocolCreator";
        case PrivateTag.DisplayProtocolCreationDatetime:
            return "DisplayProtocolCreationDatetime";
        case PrivateTag.ReferencedDataProtocol:
            return "ReferencedDataProtocol";
        case PrivateTag.DisplayProtocolExcellenceRank:
            return "DisplayProtocolExcellenceRank";
        case PrivateTag.LayoutSequence:
            return "LayoutSequence";
        case PrivateTag.LayoutNumber:
            return "LayoutNumber";
        case PrivateTag.LayoutDescription:
            return "LayoutDescription";
        case PrivateTag.SegmentSequence:
            return "SegmentSequence";
        case PrivateTag.SegmentNumber:
            return "SegmentNumber";
        case PrivateTag.SegmentDescription:
            return "SegmentDescription";
        case PrivateTag.SegmentType:
            return "SegmentType";
        case PrivateTag.TileHorizontalDimension:
            return "TileHorizontalDimension";
        case PrivateTag.TileVerticalDimension:
            return "TileVerticalDimension";
        case PrivateTag.FillOrder:
            return "FillOrder";
        case PrivateTag.SegmentSmallScrollType:
            return "SegmentSmallScrollType";
        case PrivateTag.SegmentSmallScrollAmount:
            return "SegmentSmallScrollAmount";
        case PrivateTag.SegmentLargeScrollType:
            return "SegmentLargeScrollType";
        case PrivateTag.SegmentLargeScrollAmount:
            return "SegmentLargeScrollAmount";
        case PrivateTag.SegmentOverlapPriority:
            return "SegmentOverlapPriority";
        case PrivateTag.DataRoleViewSequence:
            return "DataRoleViewSequence";
        case PrivateTag.DataRoleViewNumber:
            return "DataRoleViewNumber";
        case PrivateTag.ReferencedDataRole:
            return "ReferencedDataRole";
        case PrivateTag.SharingEnabled:
            return "SharingEnabled";
        case PrivateTag.ReferencedDataRoleViews:
            return "ReferencedDataRoleViews";
        case PrivateTag.DataProtocolName:
            return "DataProtocolName";
        case PrivateTag.DataProtocolDescription:
            return "DataProtocolDescription";
        case PrivateTag.DataProtocolLevel:
            return "DataProtocolLevel";
        case PrivateTag.DataProtocolCreator:
            return "DataProtocolCreator";
        case PrivateTag.DataProtocolCreationDatetime:
            return "DataProtocolCreationDatetime";
        case PrivateTag.DataProtocolExcellenceRank:
            return "DataProtocolExcellenceRank";
        case PrivateTag.DataProtocolDefinitionSequence:
            return "DataProtocolDefinitionSequence";
        case PrivateTag.DataRoleSequence:
            return "DataRoleSequence";
        case PrivateTag.DataRoleNumber:
            return "DataRoleNumber";
        case PrivateTag.DataRoleName:
            return "DataRoleName";
        case PrivateTag.SelectorOperationsSequence:
            return "SelectorOperationsSequence";
        case PrivateTag.SelectorUsageFlag:
            return "SelectorUsageFlag";
        case PrivateTag.SelectByAttributePresence:
            return "SelectByAttributePresence";
        case PrivateTag.SelectByCategory:
            return "SelectByCategory";
        case PrivateTag.SelectByOperator:
            return "SelectByOperator";
        case PrivateTag.CustomSelectorType:
            return "CustomSelectorType";
        case PrivateTag.SelectorOperator:
            return "SelectorOperator";
        case PrivateTag.ReformattingRequired:
            return "ReformattingRequired";
        case PrivateTag.RegistrationDataSequence:
            return "RegistrationDataSequence";
        case PrivateTag.ReferenceDataRoleNumber:
            return "ReferenceDataRoleNumber";
        case PrivateTag.ModelDataSequence:
            return "ModelDataSequence";
        case PrivateTag.ModelDataRoleNumber:
            return "ModelDataRoleNumber";
        case PrivateTag.FusionDisplaySequence:
            return "FusionDisplaySequence";
        case PrivateTag.Transparency:
            return "Transparency";
        case PrivateTag.TimePoint:
            return "TimePoint";
        case PrivateTag.FirstTimePointToken:
            return "FirstTimePointToken";
        case PrivateTag.LastTimePointToken:
            return "LastTimePointToken";
        case PrivateTag.IntermediateTimePointToken:
            return "IntermediateTimePointToken";
        case PrivateTag.DataProcessorSequence:
            return "DataProcessorSequence";
        case PrivateTag.DataProcessorType:
            return "DataProcessorType";
        case PrivateTag.TemplateDataRoleSequence:
            return "TemplateDataRoleSequence";
        case PrivateTag.ViewSequence:
            return "ViewSequence";
        case PrivateTag.ViewType:
            return "ViewType";
        case PrivateTag.CustomBaggingType:
            return "CustomBaggingType";
        case PrivateTag.ReferencedDisplaySegmentNumber:
            return "ReferencedDisplaySegmentNumber";
        case PrivateTag.DataRoleType:
            return "DataRoleType";
        case PrivateTag.InternalFlag:
            return "InternalFlag";
        case PrivateTag.UnassignedFlag:
            return "UnassignedFlag";
        case PrivateTag.InitialDisplayScrollPosition:
            return "InitialDisplayScrollPosition";
        case PrivateTag.VRTPreset:
            return "VRTPreset";
        }
        return "";
    }

}
