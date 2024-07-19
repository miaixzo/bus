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
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO LAYOUT PROTOCOL";

    /** (0073,xx02) VR=US VM=1 Hanging Protocol Excellence Rank */
    public static final int HangingProtocolExcellenceRank = 0x00730002;

    /** (0073,xx04) VR=CS VM=1 Template Data Role ID */
    public static final int TemplateDataRoleID = 0x00730004;

    /** (0073,xx06) VR=CS VM=1 Data Sharing Flag */
    public static final int DataSharingFlag = 0x00730006;

    /** (0073,xx08) VR=SQ VM=1 Bagging Operations Sequence */
    public static final int BaggingOperationsSequence = 0x00730008;

    /** (0073,xx10) VR=LO VM=1 Synchronization Type */
    public static final int SynchronizationType = 0x00730010;

    /** (0073,xx12) VR=LO VM=1 Custom Filter Type */
    public static final int CustomFilterType = 0x00730012;

    /** (0073,xx14) VR=LO VM=1 Custom Sorter Type */
    public static final int CustomSorterType = 0x00730014;

    /** (0073,xx16) VR=CS VM=1 Reference Template Data Role ID */
    public static final int ReferenceTemplateDataRoleID = 0x00730016;

    /** (0073,xx18) VR=CS VM=1 Model Template Data Role ID */
    public static final int ModelTemplateDataRoleID = 0x00730018;

    /** (0073,xx20) VR=DT VM=1-n Selector DT Value */
    public static final int SelectorDTValue = 0x00730020;

    /** (0073,xx22) VR=DA VM=1-n Selector DA Value */
    public static final int SelectorDAValue = 0x00730022;

    /** (0073,xx24) VR=TM VM=1-n Selector TM Value */
    public static final int SelectorTMValue = 0x00730024;

    /** (0073,xx26) VR=UI VM=1-n Selector UI Value */
    public static final int SelectorUIValue = 0x00730026;

    /** (0073,xx28) VR=CS VM=1 Referenced Template Data Role */
    public static final int ReferencedTemplateDataRole = 0x00730028;

    /** (0073,xx30) VR=SQ VM=1 Custom Property Sequence */
    public static final int CustomPropertySequence = 0x00730030;

    /** (0073,xx32) VR=CS VM=1 Custom Property Type */
    public static final int CustomPropertyType = 0x00730032;

    /** (0073,xx34) VR=LO VM=1 Custom Property Name */
    public static final int CustomPropertyName = 0x00730034;

    /** (0073,xx36) VR=LO VM=1 Custom Property Value */
    public static final int CustomPropertyValue = 0x00730036;

    /** (0073,xx38) VR=SQ VM=1 Layout Property Sequence */
    public static final int LayoutPropertySequence = 0x00730038;

    /** (0073,xx40) VR=SQ VM=1 Synchronization Sequence */
    public static final int SynchronizationSequence = 0x00730040;

    /** (0073,xx42) VR=CS VM=1 Presentation Creator Type */
    public static final int PresentationCreatorType = 0x00730042;

    /** (0073,xx44) VR=CS VM=1 Cine Navigation Type */
    public static final int CineNavigationType = 0x00730044;

    /** (0073,xx48) VR=LO VM=1 Semantic Naming Strategy */
    public static final int SemanticNamingStrategy = 0x00730048;

    /** (0073,xx50) VR=LO VM=1 Parameter String */
    public static final int ParameterString = 0x00730050;

    /** (0073,xx52) VR=CS VM=1 Sorting Order */
    public static final int SortingOrder = 0x00730052;

    /** (0073,xx54) VR=CS VM=1 syngo Template Type */
    public static final int syngoTemplateType = 0x00730054;

    /** (0073,xx56) VR=CS VM=1 Sorter Type */
    public static final int SorterType = 0x00730056;

    /** (0073,xx58) VR=SH VM=1 Data Display Protocol Version */
    public static final int DataDisplayProtocolVersion = 0x00730058;

    /** (0073,xx5A) VR=CS VM=1 Timepoint Value */
    public static final int TimepointValue = 0x0073005A;

    /** (0073,xx5B) VR=CS VM=1 Sharing Group Sequence */
    public static final int SharingGroupSequence = 0x0073005B;

    /** (0073,xx5C) VR=CS VM=1 Template Selector Operator */
    public static final int TemplateSelectorOperator = 0x0073005C;

    /** (0073,xx5D) VR=CS VM=1 Sharing Type */
    public static final int SharingType = 0x0073005D;

    /** (0073,xx60) VR=SQ VM=1 Viewport Definitions Sequenc */
    public static final int ViewportDefinitionsSequence = 0x00730060;

    /** (0073,xx62) VR=CS VM=1 Protocol Type */
    public static final int ProtocolType = 0x00730062;

    /** (0073,xx64) VR=SQ VM=1 Template Selector Sequence */
    public static final int TemplateSelectorSequence = 0x00730064;

    /** (0073,xx66) VR=CS VM=1 Default Template */
    public static final int DefaultTemplate = 0x00730066;

    /** (0073,xx68) VR=CS VM=1 Is Preferred */
    public static final int IsPreferred = 0x00730068;

    /** (0073,xx6A) VR=SQ VM=1 Timepoint Initial Value Sequence */
    public static final int TimepointInitialValueSequence = 0x0073006A;

    /** (0073,xx6C) VR=CS VM=1 Timepoint Variable */
    public static final int TimepointVariable = 0x0073006C;

    /** (0073,xx70) VR=SH VM=1 Display Protocol Name */
    public static final int DisplayProtocolName = 0x00730070;

    /** (0073,xx72) VR=LO VM=1 Display Protocol Description */
    public static final int DisplayProtocolDescription = 0x00730072;

    /** (0073,xx74) VR=CS VM=1 Display Protocol Level */
    public static final int DisplayProtocolLevel = 0x00730074;

    /** (0073,xx76) VR=LO VM=1 Display Protocol Creator */
    public static final int DisplayProtocolCreator = 0x00730076;

    /** (0073,xx78) VR=DT VM=1 Display Protocol Creation Datetime */
    public static final int DisplayProtocolCreationDatetime = 0x00730078;

    /** (0073,xx7A) VR=UI VM=1 Referenced Data Protocol */
    public static final int ReferencedDataProtocol = 0x0073007A;

    /** (0073,xx7C) VR=US VM=1 Display Protocol Excellence Rank */
    public static final int DisplayProtocolExcellenceRank = 0x0073007C;

    /** (0073,xx7E) VR=SQ VM=1 Layout Sequence */
    public static final int LayoutSequence = 0x0073007E;

    /** (0073,xx80) VR=US VM=1 Layout Number */
    public static final int LayoutNumber = 0x00730080;

    /** (0073,xx82) VR=LO VM=1 Layout Description */
    public static final int LayoutDescription = 0x00730082;

    /** (0073,xx84) VR=SQ VM=1 Segment Sequence */
    public static final int SegmentSequence = 0x00730084;

    /** (0073,xx86) VR=US VM=1 Segment Number */
    public static final int SegmentNumber = 0x00730086;

    /** (0073,xx88) VR=LO VM=1 Segment Description */
    public static final int SegmentDescription = 0x00730088;

    /** (0073,xx8A) VR=CS VM=1 Segment Type */
    public static final int SegmentType = 0x0073008A;

    /** (0073,xx8C) VR=US VM=1 Tile Horizontal Dimension */
    public static final int TileHorizontalDimension = 0x0073008C;

    /** (0073,xx8E) VR=US VM=1 Tile Vertical Dimension */
    public static final int TileVerticalDimension = 0x0073008E;

    /** (0073,xx90) VR=CS VM=1 Fill Order */
    public static final int FillOrder = 0x00730090;

    /** (0073,xx92) VR=CS VM=1 Segment Small Scroll Type */
    public static final int SegmentSmallScrollType = 0x00730092;

    /** (0073,xx94) VR=US VM=1 Segment Small Scroll Amount */
    public static final int SegmentSmallScrollAmount = 0x00730094;

    /** (0073,xx96) VR=CS VM=1 Segment Large Scroll Type */
    public static final int SegmentLargeScrollType = 0x00730096;

    /** (0073,xx98) VR=US VM=1 Segment Large Scroll Amount */
    public static final int SegmentLargeScrollAmount = 0x00730098;

    /** (0073,xx9A) VR=US VM=1 Segment Overlap Priority */
    public static final int SegmentOverlapPriority = 0x0073009A;

    /** (0073,xx9C) VR=SQ VM=1 Data Role View Sequence */
    public static final int DataRoleViewSequence = 0x0073009C;

    /** (0073,xx9E) VR=US VM=1 Data Role View Number */
    public static final int DataRoleViewNumber = 0x0073009E;

    /** (0073,xxA2) VR=US VM=1 Referenced Data Role */
    public static final int ReferencedDataRole = 0x007300A2;

    /** (0073,xxA4) VR=CS VM=1 Sharing Enabled */
    public static final int SharingEnabled = 0x007300A4;

    /** (0073,xxA8) VR=US VM=2-n Referenced Data Role Views */
    public static final int ReferencedDataRoleViews = 0x007300A8;

    /** (0073,xxB0) VR=SH VM=1 Data Protocol Nam */
    public static final int DataProtocolName = 0x007300B0;

    /** (0073,xxB2) VR=LO VM=1 Data Protocol Description */
    public static final int DataProtocolDescription = 0x007300B2;

    /** (0073,xxB4) VR=CS VM=1 Data Protocol Level */
    public static final int DataProtocolLevel = 0x007300B4;

    /** (0073,xxB6) VR=LO VM=1 Data Protocol Creator */
    public static final int DataProtocolCreator = 0x007300B6;

    /** (0073,xxB8) VR=DT VM=1 Data Protocol Creation Datetime */
    public static final int DataProtocolCreationDatetime = 0x007300B8;

    /** (0073,xxBA) VR=US VM=1 Data Protocol Excellence Rank */
    public static final int DataProtocolExcellenceRank = 0x007300BA;

    /** (0073,xxBC) VR=SQ VM=1 Data Protocol Definition Sequence */
    public static final int DataProtocolDefinitionSequence = 0x007300BC;

    /** (0073,xxBE) VR=SQ VM=1 Data Role Sequence */
    public static final int DataRoleSequence = 0x007300BE;

    /** (0073,xxC0) VR=US VM=1 Data Role Number */
    public static final int DataRoleNumber = 0x007300C0;

    /** (0073,xxC2) VR=SH VM=1 Data Role Name */
    public static final int DataRoleName = 0x007300C2;

    /** (0073,xxC6) VR=SQ VM=1 Selector Operations Sequence */
    public static final int SelectorOperationsSequence = 0x007300C6;

    /** (0073,xxC8) VR=CS VM=1 Selector Usage Flag */
    public static final int SelectorUsageFlag = 0x007300C8;

    /** (0073,xxCA) VR=CS VM=1 Select by Attribute Presence */
    public static final int SelectByAttributePresence = 0x007300CA;

    /** (0073,xxCC) VR=CS VM=1 Select by Category */
    public static final int SelectByCategory = 0x007300CC;

    /** (0073,xxCE) VR=CS VM=1 Select by Operator */
    public static final int SelectByOperator = 0x007300CE;

    /** (0073,xxD0) VR=LO VM=1 Custom Selector Type */
    public static final int CustomSelectorType = 0x007300D0;

    /** (0073,xxD2) VR=CS VM=1 Selector Operator */
    public static final int SelectorOperator = 0x007300D2;

    /** (0073,xxD4) VR=CS VM=1 Reformatting Require */
    public static final int ReformattingRequired = 0x007300D4;

    /** (0073,xxD6) VR=SQ VM=1 Registration Data Sequence */
    public static final int RegistrationDataSequence = 0x007300D6;

    /** (0073,xxD8) VR=US VM=1 Reference Data Role Number */
    public static final int ReferenceDataRoleNumber = 0x007300D8;

    /** (0073,xxDA) VR=SQ VM=1 Model Data Sequence */
    public static final int ModelDataSequence = 0x007300DA;

    /** (0073,xxDC) VR=US VM=1 Model Data Role Number */
    public static final int ModelDataRoleNumber = 0x007300DC;

    /** (0073,xxDE) VR=SQ VM=1 Fusion Display Sequence */
    public static final int FusionDisplaySequence = 0x007300DE;

    /** (0073,xxE0) VR=FD VM=1 Transparency */
    public static final int Transparency = 0x007300E0;

    /** (0073,xxE2) VR=CS VM=1 Time Point */
    public static final int TimePoint = 0x007300E2;

    /** (0073,xxE4) VR=LO VM=1 First Time Point Token */
    public static final int FirstTimePointToken = 0x007300E4;

    /** (0073,xxE6) VR=LO VM=1 Last Time Point Token */
    public static final int LastTimePointToken = 0x007300E6;

    /** (0073,xxE8) VR=LO VM=1 Intermediate Time Point Token */
    public static final int IntermediateTimePointToken = 0x007300E8;

    /** (0073,xxEA) VR=SQ VM=1 Data Processor Sequence */
    public static final int DataProcessorSequence = 0x007300EA;

    /** (0073,xxEC) VR=LO VM=1 Data Processor Type */
    public static final int DataProcessorType = 0x007300EC;

    /** (0073,xxEE) VR=SQ VM=1 Template Data Role Sequence */
    public static final int TemplateDataRoleSequence = 0x007300EE;

    /** (0073,xxF0) VR=SQ VM=1 View Sequence */
    public static final int ViewSequence = 0x007300F0;

    /** (0073,xxF4) VR=LO VM=1 View Type */
    public static final int ViewType = 0x007300F4;

    /** (0073,xxF6) VR=LO VM=1 Custom Bagging Type */
    public static final int CustomBaggingType = 0x007300F6;

    /** (0073,xxF8) VR=US VM=1 Referenced Display Segment Number */
    public static final int ReferencedDisplaySegmentNumber = 0x007300F8;

    /** (0073,xxFA) VR=LO VM=1 Data Role Type */
    public static final int DataRoleType = 0x007300FA;

    /** (0073,xx46) VR=CS VM=1 Internal Flag */
    public static final int InternalFlag = 0x00730046;

    /** (0073,xxFC) VR=CS VM=1 Unassigned Flag */
    public static final int UnassignedFlag = 0x007300FC;

    /** (0073,xxFE) VR=CS VM=1 Initial Display Scroll Position */
    public static final int InitialDisplayScrollPosition = 0x007300FE;

    /** (0073,xxFF) VR=LO VM=1 VRT Preset */
    public static final int VRTPreset = 0x007300FF;

}
