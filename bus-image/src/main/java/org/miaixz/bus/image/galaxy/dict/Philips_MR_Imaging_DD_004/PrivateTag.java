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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_004;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips MR Imaging DD 004";

    /** (2005,xx00) VR=SS VM=1 Spectrum Extra Number */
    public static final int SpectrumExtraNumber = 0x20050000;

    /** (2005,xx01) VR=SS VM=1 Spectrum Kx Coordinate */
    public static final int SpectrumKxCoordinate = 0x20050001;

    /** (2005,xx02) VR=SS VM=1 Spectrum Ky Coordinate */
    public static final int SpectrumKyCoordinate = 0x20050002;

    /** (2005,xx03) VR=SS VM=1 Spectrum Location Number */
    public static final int SpectrumLocationNumber = 0x20050003;

    /** (2005,xx04) VR=SS VM=1 Spectrum Mix Number */
    public static final int SpectrumMixNumber = 0x20050004;

    /** (2005,xx05) VR=SS VM=1 Spectrum X Coordinate */
    public static final int SpectrumXCoordinate = 0x20050005;

    /** (2005,xx06) VR=SS VM=1 Spectrum Y Coordinate */
    public static final int SpectrumYCoordinate = 0x20050006;

    /** (2005,xx07) VR=FL VM=1 Spectrum DC Level */
    public static final int SpectrumDCLevel = 0x20050007;

    /** (2005,xx08) VR=FL VM=1 Spectrum Noise Level */
    public static final int SpectrumNoiseLevel = 0x20050008;

    /** (2005,xx09) VR=FL VM=1 Spectrum Begin Time */
    public static final int SpectrumBeginTime = 0x20050009;

    /** (2005,xx10) VR=FL VM=1 Spectrum Echo Time */
    public static final int SpectrumEchoTime = 0x20050010;

    /** (2005,xx12) VR=FL VM=1 Spectrum Inversion Time */
    public static final int SpectrumInversionTime = 0x20050012;

    /** (2005,xx13) VR=SS VM=1 Spectrum Number */
    public static final int SpectrumNumber = 0x20050013;

    /** (2005,xx14) VR=SS VM=1 Spectrum Number of Averages */
    public static final int SpectrumNumberOfAverages = 0x20050014;

    /** (2005,xx15) VR=SS VM=1 Spectrum Number of Samples */
    public static final int SpectrumNumberOfSamples = 0x20050015;

    /** (2005,xx16) VR=SS VM=1 Spectrum Scan Sequence Number */
    public static final int SpectrumScanSequenceNumber = 0x20050016;

    /** (2005,xx17) VR=SS VM=1 Spectrum Number of Peaks */
    public static final int SpectrumNumberOfPeaks = 0x20050017;

    /** (2005,xx18) VR=SQ VM=1 Spectrum Peak */
    public static final int SpectrumPeak = 0x20050018;

    /** (2005,xx19) VR=FL VM=1-n Spectrum Peak Intensity */
    public static final int SpectrumPeakIntensity = 0x20050019;

    /** (2005,xx20) VR=LO VM=1-n Spectrum Peak Label */
    public static final int SpectrumPeakLabel = 0x20050020;

    /** (2005,xx21) VR=FL VM=1-n Spectrum Peak Phase */
    public static final int SpectrumPeakPhase = 0x20050021;

    /** (2005,xx22) VR=FL VM=1-n Spectrum Peak Position */
    public static final int SpectrumPeakPosition = 0x20050022;

    /** (2005,xx23) VR=CS VM=1-n Spectrum Peak Type */
    public static final int SpectrumPeakType = 0x20050023;

    /** (2005,xx24) VR=FL VM=1-n Spectrum Peak Width */
    public static final int SpectrumPeakWidth = 0x20050024;

    /** (2005,xx25) VR=CS VM=1 Spectro SI B0 Correction */
    public static final int SpectroSIB0Correction = 0x20050025;

    /** (2005,xx26) VR=FL VM=1 Spectro B0 Echo Top Position */
    public static final int SpectroB0EchoTopPosition = 0x20050026;

    /** (2005,xx27) VR=CS VM=1 Spectro Complex Component */
    public static final int SpectroComplexComponent = 0x20050027;

    /** (2005,xx28) VR=CS VM=1 Spectro Data Origin */
    public static final int SpectroDataOrigin = 0x20050028;

    /** (2005,xx29) VR=FL VM=1 Spectro Echo Top Position */
    public static final int SpectroEchoTopPosition = 0x20050029;

    /** (2005,xx30) VR=CS VM=1-n InPlane Transforms */
    public static final int InPlaneTransforms = 0x20050030;

    /** (2005,xx31) VR=SS VM=1 Number of Spectra Acquired */
    public static final int NumberOfSpectraAcquired = 0x20050031;

    /** (2005,xx33) VR=FL VM=1 Phase Encoding Echo Top Positions */
    public static final int PhaseEncodingEchoTopPositions = 0x20050033;

    /**
     * (2005,xx34) VR=CS VM=1 Physical Quantity for Chemical Shift
     */
    public static final int PhysicalQuantityForChemicalShift = 0x20050034;

    /** (2005,xx35) VR=CS VM=1 Physical Quantity Spatial */
    public static final int PhysicalQuantitySpatial = 0x20050035;

    /** (2005,xx36) VR=FL VM=1 Reference Frequency */
    public static final int ReferenceFrequency = 0x20050036;

    /** (2005,xx37) VR=FL VM=1 Sample Offset */
    public static final int SampleOffset = 0x20050037;

    /** (2005,xx38) VR=FL VM=1 Sample Pitch */
    public static final int SamplePitch = 0x20050038;

    /** (2005,xx39) VR=SS VM=2 Search Interval for Peaks */
    public static final int SearchIntervalForPeaks = 0x20050039;

    /** (2005,xx40) VR=CS VM=1 Signal Domain for Chemical Shift */
    public static final int SignalDomainForChemicalShift = 0x20050040;

    /** (2005,xx41) VR=CS VM=1 Signal Domain Spatial */
    public static final int SignalDomainSpatial = 0x20050041;

    /** (2005,xx42) VR=CS VM=1 Signal Type */
    public static final int SignalType = 0x20050042;

    /** (2005,xx43) VR=CS VM=1 Spectro Additional Rotations */
    public static final int SpectroAdditionalRotations = 0x20050043;

    /** (2005,xx44) VR=SS VM=1-n Spectro Display Ranges */
    public static final int SpectroDisplayRanges = 0x20050044;

    /** (2005,xx45) VR=CS VM=1 Spectro Echo Acquisition */
    public static final int SpectroEchoAcquisition = 0x20050045;

    /** (2005,xx46) VR=CS VM=1 Spectro Frequency Unit */
    public static final int SpectroFrequencyUnit = 0x20050046;

    /** (2005,xx47) VR=FL VM=1 Spectro Gamma */
    public static final int SpectroGamma = 0x20050047;

    /** (2005,xx48) VR=CS VM=1 Spectro Hidden Line Removal */
    public static final int SpectroHiddenLineRemoval = 0x20050048;

    /** (2005,xx49) VR=FL VM=1 Spectro Horizontal Shift */
    public static final int SpectroHorizontalShift = 0x20050049;

    /** (2005,xx50) VR=FL VM=2 Spectro Horizontal Window */
    public static final int SpectroHorizontalWindow = 0x20050050;

    /** (2005,xx51) VR=SS VM=1 Spectro Number of Display Ranges */
    public static final int SpectroNumberOfDisplayRanges = 0x20050051;

    /** (2005,xx52) VR=SS VM=1 Spectro Number of Echo Pulses */
    public static final int SpectroNumberOfEchoPulses = 0x20050052;

    /** (2005,xx53) VR=LO VM=1-n Spectro Processing History */
    public static final int SpectroProcessingHistory = 0x20050053;

    /** (2005,xx54) VR=CS VM=1 Spectro Scan Type */
    public static final int SpectroScanType = 0x20050054;

    /** (2005,xx55) VR=FL VM=1-n Spectro SI CS Intervals */
    public static final int SpectroSICSIntervals = 0x20050055;

    /** (2005,xx56) VR=CS VM=1 Spectro SI Mode */
    public static final int SpectroSIMode = 0x20050056;

    /** (2005,xx57) VR=SS VM=1 Spectro Spectral BW */
    public static final int SpectroSpectralBW = 0x20050057;

    /** (2005,xx58) VR=LO VM=1 Spectro Title Line */
    public static final int SpectroTitleLine = 0x20050058;

    /** (2005,xx59) VR=FL VM=1 Spectro Turbo Echo Spacing */
    public static final int SpectroTurboEchoSpacing = 0x20050059;

    /** (2005,xx60) VR=FL VM=1 Spectro Vertical Shift */
    public static final int SpectroVerticalShift = 0x20050060;

    /** (2005,xx61) VR=FL VM=2 Spectro Vertical Window */
    public static final int SpectroVerticalWindow = 0x20050061;

    /** (2005,xx62) VR=FL VM=1 Spectro Offset */
    public static final int SpectroOffset = 0x20050062;

    /** (2005,xx63) VR=FL VM=1 Spectrum Pitch */
    public static final int SpectrumPitch = 0x20050063;

    /** (2005,xx64) VR=CS VM=1 Volume Selection */
    public static final int VolumeSelection = 0x20050064;

    /** (2005,xx70) VR=SS VM=1 Number Mixes Spectro */
    public static final int NumberMixesSpectro = 0x20050070;

    /** (2005,xx71) VR=SQ VM=1 Series SP Mix */
    public static final int SeriesSPMix = 0x20050071;

    /** (2005,xx72) VR=SS VM=1-2 SP Mix T Resolution */
    public static final int SPMixTResolution = 0x20050072;

    /** (2005,xx73) VR=SS VM=1-2 SP Mix KX Resolution */
    public static final int SPMixKXResolution = 0x20050073;

    /** (2005,xx74) VR=SS VM=1-2 SP Mix KY Resolution */
    public static final int SPMixKYResolution = 0x20050074;

    /** (2005,xx75) VR=SS VM=1-2 SP Mix F Resolution */
    public static final int SPMixFResolution = 0x20050075;

    /** (2005,xx76) VR=SS VM=1-2 SP Mix X Resolution */
    public static final int SPMixXResolution = 0x20050076;

    /** (2005,xx77) VR=SS VM=1-2 SP Mix Y Resolution */
    public static final int SPMixYResolution = 0x20050077;

    /** (2005,xx78) VR=SS VM=1-2 SP Mix Number of Spectra Intended */
    public static final int SPMixNumberOfSpectraIntended = 0x20050078;

    /** (2005,xx79) VR=SS VM=1-2 SP Mix Number of Averages */
    public static final int SPMixNumberOfAverages = 0x20050079;

    /** (2005,xx80) VR=SL VM=1 Number of MF Image Objects */
    public static final int NumberOfMFImageObjects = 0x20050080;

    /** (2005,xx81) VR=IS VM=1 ScanoGram Survey Number of Images */
    public static final int ScanoGramSurveyNumberOfImages = 0x20050081;

    /** (2005,xx82) VR=UL VM=1 Number of Procedure Codes */
    public static final int NumberOfProcedureCodes = 0x20050082;

    /** (2005,xx83) VR=CS VM=1-n Sort Attributes */
    public static final int SortAttributes = 0x20050083;

    /** (2005,xx84) VR=SS VM=1 Number of Sort Attributes */
    public static final int NumberOfSortAttributes = 0x20050084;

    /** (2005,xx85) VR=CS VM=1 Image Display Direction */
    public static final int ImageDisplayDirection = 0x20050085;

    /** (2005,xx86) VR=CS VM=1 Inset Scanogram */
    public static final int InsetScanogram = 0x20050086;

    /** (2005,xx87) VR=SS VM=1 Display Layout Number of Columns */
    public static final int DisplayLayoutNumberOfColumns = 0x20050087;

    /** (2005,xx88) VR=SS VM=1 Display Layout Number of Rows */
    public static final int DisplayLayoutNumberOfRows = 0x20050088;

    /** (2005,xx89) VR=SQ VM=1 Viewing Protocol */
    public static final int ViewingProtocol = 0x20050089;

    /** (2005,xx90) VR=CS VM=1 Stack Coil Function */
    public static final int StackCoilFunction = 0x20050090;

    /** (2005,xx91) VR=PN VM=1 Patient Name Job In Params */
    public static final int PatientNameJobInParams = 0x20050091;

    /** (2005,xx92) VR=IS VM=1 Geolink ID */
    public static final int GeolinkID = 0x20050092;

    /** (2005,xx93) VR=IS VM=1 Station Number */
    public static final int StationNumber = 0x20050093;

    /** (2005,xx94) VR=CS VM=1-n Processing History */
    public static final int ProcessingHistory = 0x20050094;

    /** (2005,xx95) VR=ST VM=1 View Procedure String */
    public static final int ViewProcedureString = 0x20050095;

    /** (2005,xx96) VR=CS VM=1 Flow Images Present */
    public static final int FlowImagesPresent = 0x20050096;

    /** (2005,xx97) VR=LO VM=1 Anatomic Region Code Value */
    public static final int AnatomicRegionCodeValue = 0x20050097;

    /** (2005,xx98) VR=CS VM=1 Mobiview Enabled */
    public static final int MobiviewEnabled = 0x20050098;

    /** (2005,xx99) VR=CS VM=1 IViewBold Enabled */
    public static final int IViewBoldEnabled = 0x20050099;

}
