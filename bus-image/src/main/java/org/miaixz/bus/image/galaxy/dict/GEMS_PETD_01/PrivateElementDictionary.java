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
package org.miaixz.bus.image.galaxy.dict.GEMS_PETD_01;

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
        
            case PrivateTag.ImageSetReference:
                return VR.DS;
            case PrivateTag.PatientDateTime:
            case PrivateTag.ScanDateTime:
            case PrivateTag.ScanReady:
            case PrivateTag.MeasuredDateTime:
            case PrivateTag.AdministeredDateTime:
            case PrivateTag.PostInjectedDateTime:
            case PrivateTag.Source1MeasDT:
            case PrivateTag.Source2MeasDT:
            case PrivateTag.landmarkdatetime:
            case PrivateTag.acqstart:
            case PrivateTag.ImageSetDateTime:
            case PrivateTag.caldatetime:
            case PrivateTag.scandatetime:
            case PrivateTag.measdatetime:
            case PrivateTag.GraphDateTime:
                return VR.DT;
            case PrivateTag.GantryTiltAngle:
            case PrivateTag.TracerActivity:
            case PrivateTag.PreInjVolume:
            case PrivateTag.PostInjectedActivity:
            case PrivateTag.HalfLife:
            case PrivateTag.PositronFraction:
            case PrivateTag.Source1Activity:
            case PrivateTag.Source1HalfLife:
            case PrivateTag.Source2Activity:
            case PrivateTag.Source2HalfLife:
            case PrivateTag.SourceLocation:
            case PrivateTag.tableheight:
            case PrivateTag.tablezposition:
            case PrivateTag.startlocation:
            case PrivateTag.Rotate:
            case PrivateTag.Zoom:
            case PrivateTag.WindowLevelMin:
            case PrivateTag.WindowLevelMax:
            case PrivateTag.AttenuationCoefficient:
            case PrivateTag.BPFilterCutoff:
            case PrivateTag.BPFilterCenterI:
            case PrivateTag.BPFilterCenterP:
            case PrivateTag.CACEdgeThreshold:
            case PrivateTag.CACSkullOffset:
            case PrivateTag.RadialCutoff3D:
            case PrivateTag.AxialCutoff3D:
            case PrivateTag.AxialStart:
            case PrivateTag.AxialSpacing:
            case PrivateTag.totalcounts:
            case PrivateTag.bpcenterx:
            case PrivateTag.bpcentery:
            case PrivateTag.profilespacing:
            case PrivateTag.IRReconFOV:
            case PrivateTag.IRPreFiltParam:
            case PrivateTag.IRLoopFiltParam:
            case PrivateTag.ResponseFiltParam:
            case PrivateTag.PostFilterParam:
            case PrivateTag.IRRegularizeParam:
            case PrivateTag.ACBPFiltCutoff:
            case PrivateTag.ACImgSmoothParm:
            case PrivateTag.ScatterParm:
            case PrivateTag.segqcparm:
            case PrivateTag.vqcxaxistrans:
            case PrivateTag.vqcxaxistilt:
            case PrivateTag.vqcyaxistrans:
            case PrivateTag.vqcyaxisswivel:
            case PrivateTag.vqczaxistrans:
            case PrivateTag.vqczaxisroll:
            case PrivateTag.loopfilterparm:
            case PrivateTag.imageoneloc:
            case PrivateTag.imageindexloc:
            case PrivateTag.irzfilterratio:
            case PrivateTag.phasepercentage:
            case PrivateTag.leftshift:
            case PrivateTag.posteriorshift:
            case PrivateTag.superiorshift:
            case PrivateTag.acqbindurpercent:
            case PrivateTag._0009_xxEB_:
            case PrivateTag.activityfactorhr:
            case PrivateTag.activityfactorhs:
            case PrivateTag.activityfactor3d:
            case PrivateTag.measactivity:
            case PrivateTag.axialcutoff3d:
            case PrivateTag.XMajorTics:
            case PrivateTag.XAxisMin:
            case PrivateTag.XAxisMax:
            case PrivateTag.YMajorTics:
            case PrivateTag.YAxisMin:
            case PrivateTag.YAxisMax:
                return VR.FL;
            case PrivateTag.totalprompts:
            case PrivateTag.totaldelays:
                return VR.FD;
            case PrivateTag.ImplementationVersionName:
            case PrivateTag.PatientID:
            case PrivateTag.HospitalName:
            case PrivateTag.ScannerDescription:
            case PrivateTag.Manufacturer:
            case PrivateTag.LandmarkName:
            case PrivateTag.TracerName:
            case PrivateTag.BatchDescription:
            case PrivateTag.ContrastAgent:
            case PrivateTag.patientid:
            case PrivateTag.ctacconvscale:
            case PrivateTag.caldescription:
            case PrivateTag.norm2dcalid:
            case PrivateTag.calhardware:
            case PrivateTag.Title:
            case PrivateTag.TitleFontName:
            case PrivateTag.Footer:
            case PrivateTag.ForegroundColor:
            case PrivateTag.BackgroundColor:
            case PrivateTag.LabelFontName:
            case PrivateTag.AxesColor:
            case PrivateTag.XAxisLabel:
            case PrivateTag.YAxisLabel:
            case PrivateTag.LegendFontName:
            case PrivateTag.CurveLabel:
            case PrivateTag.Color:
            case PrivateTag.PointColor:
                return VR.LO;
            case PrivateTag.HowDerived:
                return VR.LT;
            case PrivateTag.otheratts:
            case PrivateTag._0011_xx18_:
            case PrivateTag.coefficients:
            case PrivateTag.rawdatablob:
                return VR.OB;
            case PrivateTag.PatientCompatibleVersion:
            case PrivateTag.PatientSoftwareVersion:
            case PrivateTag.ExamCompatibleVersion:
            case PrivateTag.ExamSoftwareVersion:
            case PrivateTag.ScanCompatibleVersion:
            case PrivateTag.ScanSoftwareVersion:
            case PrivateTag.LandmarkAbbrev:
            case PrivateTag.RadioNuclideName:
            case PrivateTag.Source1RadioNuclide:
            case PrivateTag.Source2RadioNuclide:
            case PrivateTag.compatibleversion:
            case PrivateTag.softwareversion:
            case PrivateTag.ImageSetCompatibleVersion:
            case PrivateTag.ImageSetSoftwareVersion:
            case PrivateTag.hospidentifier:
            case PrivateTag.CurveCompatibleVersion:
            case PrivateTag.CurveSoftwareVersion:
            case PrivateTag.GraphCompatibleVersion:
            case PrivateTag.GraphSoftwareVersion:
            case PrivateTag.TitleFontSize:
            case PrivateTag.FooterFontSize:
            case PrivateTag.LabelFontSize:
            case PrivateTag.LegendFontSize:
            case PrivateTag.CurvePresentationCompatibleVersion:
            case PrivateTag.CurvePresentationSoftwareVersion:
                return VR.SH;
            case PrivateTag.PatientType:
            case PrivateTag.PatientPosition:
            case PrivateTag.ScanPerspective:
            case PrivateTag.ScanType:
            case PrivateTag.ScanMode:
            case PrivateTag.StartCondition:
            case PrivateTag.StartConditionData:
            case PrivateTag.SelStopCondition:
            case PrivateTag.SelStopConditionData:
            case PrivateTag.CollectDeadtime:
            case PrivateTag.CollectSingles:
            case PrivateTag.CollectCountRate:
            case PrivateTag.CountRatePeriod:
            case PrivateTag.DelayedEvents:
            case PrivateTag.DelayedBias:
            case PrivateTag.WordSize:
            case PrivateTag.AxialAcceptance:
            case PrivateTag.AxialAngle3D:
            case PrivateTag.ThetaCompression:
            case PrivateTag.AxialCompression:
            case PrivateTag.Collimation:
            case PrivateTag.ScanFOV:
            case PrivateTag.AxialFOV:
            case PrivateTag.EventSeparation:
            case PrivateTag.MaskWidth:
            case PrivateTag.BinningMode:
            case PrivateTag.TrigRejMethod:
            case PrivateTag.NumberForReject:
            case PrivateTag.LowerRejectLimit:
            case PrivateTag.UpperRejectLimit:
            case PrivateTag.TriggersAcquired:
            case PrivateTag.TriggersRejected:
            case PrivateTag.Source1Holder:
            case PrivateTag.Source2Holder:
            case PrivateTag.SourceSpeed:
            case PrivateTag.EmissionPresent:
            case PrivateTag.LowerAxialAcc:
            case PrivateTag.UpperAxialAcc:
            case PrivateTag.LowerCoincLimit:
            case PrivateTag.UpperCoincLimit:
            case PrivateTag.CoincDelayOffset:
            case PrivateTag.CoincOutputMode:
            case PrivateTag.UpperEnergyLimit:
            case PrivateTag.LowerEnergyLimit:
            case PrivateTag.Derived:
            case PrivateTag.framesize:
            case PrivateTag.fileexists:
            case PrivateTag.patiententry:
            case PrivateTag.slicecount:
            case PrivateTag.acqdelay:
            case PrivateTag.acqduration:
            case PrivateTag.acqbindur:
            case PrivateTag.acqbinstart:
            case PrivateTag.actualstopcond:
            case PrivateTag.framevalid:
            case PrivateTag.validityinfo:
            case PrivateTag.archived:
            case PrivateTag.compression:
            case PrivateTag.uncompressedsize:
            case PrivateTag.accumbindur:
            case PrivateTag.ImageSetSource:
            case PrivateTag.ImageSetContents:
            case PrivateTag.ImageSetType:
            case PrivateTag.MultiPatient:
            case PrivateTag.NumberOfNormals:
            case PrivateTag.WindowLevelType:
            case PrivateTag.Flip:
            case PrivateTag.PanX:
            case PrivateTag.PanY:
            case PrivateTag.ReconMethod:
            case PrivateTag.Attenuation:
            case PrivateTag.BPFilter:
            case PrivateTag.BPFilterOrder:
            case PrivateTag.AttenSmooth:
            case PrivateTag.AttenSmoothParam:
            case PrivateTag.AngleSmoothParam:
            case PrivateTag.AxialFilter3D:
            case PrivateTag.AxialAnglesUsed:
            case PrivateTag.slicenumber:
            case PrivateTag.otherattssize:
            case PrivateTag.ACBPFiltOrder:
            case PrivateTag.ScatterMethod:
            case PrivateTag.overlap:
            case PrivateTag.contrastroute:
            case PrivateTag.framenumber:
            case PrivateTag.listfileexists:
            case PrivateTag.irzfilterflag:
            case PrivateTag.unlistedscan:
            case PrivateTag.reststress:
            case PrivateTag.acqbinnum:
            case PrivateTag._0009_xxEA_:
            case PrivateTag._0009_xxEC_:
            case PrivateTag.caltype:
            case PrivateTag.corrfilesize:
            case PrivateTag.axialfilter3d:
            case PrivateTag.defaultflag:
            case PrivateTag.wccalrecmethod:
            case PrivateTag.activityfactor2d:
            case PrivateTag.isotope:
            case PrivateTag.StatisticsType:
            case PrivateTag.HowDerivedSize:
            case PrivateTag.Deadtime:
            case PrivateTag.GraphBorder:
            case PrivateTag.GraphWidth:
            case PrivateTag.GraphHeight:
            case PrivateTag.Grid:
            case PrivateTag.XAxisUnits:
            case PrivateTag.YAxisUnits:
            case PrivateTag.LegendLocationX:
            case PrivateTag.LegendLocationY:
            case PrivateTag.LegendWidth:
            case PrivateTag.LegendHeight:
            case PrivateTag.LegendBorder:
            case PrivateTag.LineType:
            case PrivateTag.LineWidth:
            case PrivateTag.PointSymbol:
            case PrivateTag.PointSymbolDim:
                return VR.SL;
            case PrivateTag._0011_xx01_:
            case PrivateTag._0013_xx01_:
            case PrivateTag.GraphSequence:
            case PrivateTag.CurvePresentationSequence:
                return VR.SQ;
            case PrivateTag.RadialFilter3D:
            case PrivateTag.IRNumIterations:
            case PrivateTag.IRNumSubsets:
            case PrivateTag.IRCorrModel:
            case PrivateTag.IRLoopFilter:
            case PrivateTag.PostFilter:
            case PrivateTag.IRRegularize:
            case PrivateTag.ACBPFilter:
            case PrivateTag.ACImgSmooth:
            case PrivateTag.ScatterNumIter:
                return VR.SS;
            case PrivateTag.ScanDescription:
            case PrivateTag.whereisframe:
            case PrivateTag.whereislistframe:
            case PrivateTag._0009_xxE4_:
            case PrivateTag.whereiscorr:
            case PrivateTag.GraphDescription:
                return VR.ST;
            case PrivateTag.ExamID:
            case PrivateTag.ScanID:
            case PrivateTag.FORIdentifier:
            case PrivateTag.NormalCalID:
            case PrivateTag.Normal2DCalID:
            case PrivateTag.BlankCalID:
            case PrivateTag.WCCalID:
            case PrivateTag.frameid:
            case PrivateTag.scanid:
            case PrivateTag.examid:
            case PrivateTag.ColorMapID:
            case PrivateTag.WellCounterCalID:
            case PrivateTag.TransScanID:
            case PrivateTag.NormCalID:
            case PrivateTag.BlnkCalID:
            case PrivateTag.EmissSubID:
            case PrivateTag.transframeid:
            case PrivateTag.tpluseframeid:
            case PrivateTag.ovlpfrmid:
            case PrivateTag.ovlptransfrmid:
            case PrivateTag.ovlptpulsefrmid:
            case PrivateTag.imagesetid:
            case PrivateTag.correctioncalid:
            case PrivateTag.wccalid:
            case PrivateTag.CurveID:
            case PrivateTag.GraphID:
            case PrivateTag.CurvePresentationID:
                return VR.UI;
            case PrivateTag.rawdatasize:
                return VR.UL;
            case PrivateTag.numofrrinterval:
            case PrivateTag.numoftimeslots:
            case PrivateTag.numofslices:
            case PrivateTag.numoftimeslices:
            case PrivateTag.rawdatatype:
                return VR.US;
        }
        return VR.UN;
    }
}
