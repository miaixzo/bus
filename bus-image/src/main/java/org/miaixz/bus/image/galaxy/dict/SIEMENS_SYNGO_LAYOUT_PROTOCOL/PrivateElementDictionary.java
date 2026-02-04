/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_LAYOUT_PROTOCOL;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.TemplateDataRoleID:
            case PrivateTag.DataSharingFlag:
            case PrivateTag.ReferenceTemplateDataRoleID:
            case PrivateTag.ModelTemplateDataRoleID:
            case PrivateTag.ReferencedTemplateDataRole:
            case PrivateTag.CustomPropertyType:
            case PrivateTag.PresentationCreatorType:
            case PrivateTag.CineNavigationType:
            case PrivateTag.SortingOrder:
            case PrivateTag.syngoTemplateType:
            case PrivateTag.SorterType:
            case PrivateTag.TimepointValue:
            case PrivateTag.SharingGroupSequence:
            case PrivateTag.TemplateSelectorOperator:
            case PrivateTag.SharingType:
            case PrivateTag.ProtocolType:
            case PrivateTag.DefaultTemplate:
            case PrivateTag.IsPreferred:
            case PrivateTag.TimepointVariable:
            case PrivateTag.DisplayProtocolLevel:
            case PrivateTag.SegmentType:
            case PrivateTag.FillOrder:
            case PrivateTag.SegmentSmallScrollType:
            case PrivateTag.SegmentLargeScrollType:
            case PrivateTag.SharingEnabled:
            case PrivateTag.DataProtocolLevel:
            case PrivateTag.SelectorUsageFlag:
            case PrivateTag.SelectByAttributePresence:
            case PrivateTag.SelectByCategory:
            case PrivateTag.SelectByOperator:
            case PrivateTag.SelectorOperator:
            case PrivateTag.ReformattingRequired:
            case PrivateTag.TimePoint:
            case PrivateTag.InternalFlag:
            case PrivateTag.UnassignedFlag:
            case PrivateTag.InitialDisplayScrollPosition:
                return VR.CS;

            case PrivateTag.SelectorDAValue:
                return VR.DA;

            case PrivateTag.SelectorDTValue:
            case PrivateTag.DisplayProtocolCreationDatetime:
            case PrivateTag.DataProtocolCreationDatetime:
                return VR.DT;

            case PrivateTag.Transparency:
                return VR.FD;

            case PrivateTag.SynchronizationType:
            case PrivateTag.CustomFilterType:
            case PrivateTag.CustomSorterType:
            case PrivateTag.CustomPropertyName:
            case PrivateTag.CustomPropertyValue:
            case PrivateTag.SemanticNamingStrategy:
            case PrivateTag.ParameterString:
            case PrivateTag.DisplayProtocolDescription:
            case PrivateTag.DisplayProtocolCreator:
            case PrivateTag.LayoutDescription:
            case PrivateTag.SegmentDescription:
            case PrivateTag.DataProtocolDescription:
            case PrivateTag.DataProtocolCreator:
            case PrivateTag.CustomSelectorType:
            case PrivateTag.FirstTimePointToken:
            case PrivateTag.LastTimePointToken:
            case PrivateTag.IntermediateTimePointToken:
            case PrivateTag.DataProcessorType:
            case PrivateTag.ViewType:
            case PrivateTag.CustomBaggingType:
            case PrivateTag.DataRoleType:
            case PrivateTag.VRTPreset:
                return VR.LO;

            case PrivateTag.DataDisplayProtocolVersion:
            case PrivateTag.DisplayProtocolName:
            case PrivateTag.DataProtocolName:
            case PrivateTag.DataRoleName:
                return VR.SH;

            case PrivateTag.BaggingOperationsSequence:
            case PrivateTag.CustomPropertySequence:
            case PrivateTag.LayoutPropertySequence:
            case PrivateTag.SynchronizationSequence:
            case PrivateTag.ViewportDefinitionsSequence:
            case PrivateTag.TemplateSelectorSequence:
            case PrivateTag.TimepointInitialValueSequence:
            case PrivateTag.LayoutSequence:
            case PrivateTag.SegmentSequence:
            case PrivateTag.DataRoleViewSequence:
            case PrivateTag.DataProtocolDefinitionSequence:
            case PrivateTag.DataRoleSequence:
            case PrivateTag.SelectorOperationsSequence:
            case PrivateTag.RegistrationDataSequence:
            case PrivateTag.ModelDataSequence:
            case PrivateTag.FusionDisplaySequence:
            case PrivateTag.DataProcessorSequence:
            case PrivateTag.TemplateDataRoleSequence:
            case PrivateTag.ViewSequence:
                return VR.SQ;

            case PrivateTag.SelectorTMValue:
                return VR.TM;

            case PrivateTag.SelectorUIValue:
            case PrivateTag.ReferencedDataProtocol:
                return VR.UI;

            case PrivateTag.HangingProtocolExcellenceRank:
            case PrivateTag.DisplayProtocolExcellenceRank:
            case PrivateTag.LayoutNumber:
            case PrivateTag.SegmentNumber:
            case PrivateTag.TileHorizontalDimension:
            case PrivateTag.TileVerticalDimension:
            case PrivateTag.SegmentSmallScrollAmount:
            case PrivateTag.SegmentLargeScrollAmount:
            case PrivateTag.SegmentOverlapPriority:
            case PrivateTag.DataRoleViewNumber:
            case PrivateTag.ReferencedDataRole:
            case PrivateTag.ReferencedDataRoleViews:
            case PrivateTag.DataProtocolExcellenceRank:
            case PrivateTag.DataRoleNumber:
            case PrivateTag.ReferenceDataRoleNumber:
            case PrivateTag.ModelDataRoleNumber:
            case PrivateTag.ReferencedDisplaySegmentNumber:
                return VR.US;
        }
        return VR.UN;
    }

}
