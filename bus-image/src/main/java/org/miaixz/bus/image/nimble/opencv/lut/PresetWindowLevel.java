/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.opencv.lut;

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.ImageAdapter;
import org.miaixz.bus.image.nimble.PresentationLutObject;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PresetWindowLevel {

    private static final Map<String, List<PresetWindowLevel>> presetListByModality = getPresetListByModality();

    private final String name;
    private final double window;
    private final double level;
    private final LutShape shape;
    private int keyCode = 0;

    public PresetWindowLevel(String name, Double window, Double level, LutShape shape) {
        this.name = Objects.requireNonNull(name);
        this.window = Objects.requireNonNull(window);
        this.level = Objects.requireNonNull(level);
        this.shape = Objects.requireNonNull(shape);
    }

    public static List<PresetWindowLevel> getPresetCollection(ImageAdapter adapter, String type, WlPresentation wl) {
        if (adapter == null || wl == null) {
            throw new IllegalArgumentException("Null parameter");
        }

        String dicomKeyWord = " " + type;

        ArrayList<PresetWindowLevel> presetList = new ArrayList<>();
        ImageDescriptor desc = adapter.getImageDescriptor();
        VoiLutModule vLut = desc.getVoiLUT();
        List<Double> levelList = getWindowCenter(vLut, wl);
        List<Double> windowList = getWindowWidth(vLut, wl);

        // optional attributes
        List<String> wlExplanationList = vLut.getWindowCenterWidthExplanation();
        LutShape defaultLutShape = getDefaultLutShape(vLut, dicomKeyWord);

        buildPreset(levelList, windowList, wlExplanationList, dicomKeyWord, defaultLutShape, presetList);

        buildPresetFromLutData(adapter, wl, vLut, dicomKeyWord, presetList);

        PresetWindowLevel autoLevel = new PresetWindowLevel("Auto Level [Image]", adapter.getFullDynamicWidth(wl),
                adapter.getFullDynamicCenter(wl), defaultLutShape);
        autoLevel.setKeyCode(0x30);
        presetList.add(autoLevel);

        // Exclude Secondary Capture CT
        if (adapter.getBitsStored() > 8) {
            List<PresetWindowLevel> modPresets = presetListByModality.get(desc.getModality());
            if (modPresets != null) {
                presetList.addAll(modPresets);
            }
        }

        return presetList;
    }

    private static LutShape getDefaultLutShape(VoiLutModule vLut, String dicomKeyWord) {
        Optional<String> lutFunctionDescriptor = vLut.getVoiLutFunction();

        // Implicitly defined as default function in DICOM standard
        LutShape defaultLutShape = LutShape.LINEAR;
        if (lutFunctionDescriptor.isPresent()) {
            if ("SIGMOID".equalsIgnoreCase(lutFunctionDescriptor.get())) {
                defaultLutShape = new LutShape(LutShape.eFunction.SIGMOID, LutShape.eFunction.SIGMOID + dicomKeyWord);
            } else if ("LINEAR".equalsIgnoreCase(lutFunctionDescriptor.get())) {
                defaultLutShape = new LutShape(LutShape.eFunction.LINEAR, LutShape.eFunction.LINEAR + dicomKeyWord);
            }
        }
        return defaultLutShape;
    }

    private static void buildPresetFromLutData(ImageAdapter adapter, WlPresentation wl, VoiLutModule vLut,
            String dicomKeyWord, ArrayList<PresetWindowLevel> presetList) {
        List<LookupTableCV> voiLUTsData = getVoiLutData(vLut, wl);
        List<String> voiLUTsExplanation = getVoiLUTExplanation(vLut, wl);

        if (!voiLUTsData.isEmpty()) {
            String defaultExplanation = "VOI LUT";

            for (int i = 0; i < voiLUTsData.size(); i++) {
                String explanation = getPresetExplanation(voiLUTsExplanation, i, defaultExplanation + " " + i);
                PresetWindowLevel preset = buildPresetFromLutData(adapter, voiLUTsData.get(i), wl,
                        explanation + dicomKeyWord);
                if (preset == null) {
                    continue;
                }
                // Only set shortcuts for the two first presets
                int presetNumber = presetList.size();
                if (presetNumber == 0) {
                    preset.setKeyCode(0x31);
                } else if (presetNumber == 1) {
                    preset.setKeyCode(0x32);
                }
                presetList.add(preset);
            }
        }
    }

    private static String getPresetExplanation(List<String> wlExplanationList, int index, String defaultExplanation) {
        String explanation = defaultExplanation;
        if (index < wlExplanationList.size()) {
            String wexpl = wlExplanationList.get(index);
            if (StringKit.hasText(wexpl)) {
                explanation = wexpl;
            }
        }
        return explanation;
    }

    private static void buildPreset(List<Double> levelList, List<Double> windowList, List<String> wlExplanationList,
            String dicomKeyWord, LutShape defaultLutShape, ArrayList<PresetWindowLevel> presetList) {
        if (!levelList.isEmpty() && !windowList.isEmpty()) {
            String defaultExplanation = "Default";

            int k = 1;
            for (int i = 0; i < levelList.size(); i++) {
                String explanation = defaultExplanation + " " + k;
                explanation = getPresetExplanation(wlExplanationList, i, explanation);
                PresetWindowLevel preset = new PresetWindowLevel(explanation + dicomKeyWord, windowList.get(i),
                        levelList.get(i), defaultLutShape);
                // Only set shortcuts for the two first presets
                if (k == 1) {
                    preset.setKeyCode(0x31);
                } else if (k == 2) {
                    preset.setKeyCode(0x32);
                }
                if (!presetList.contains(preset)) {
                    presetList.add(preset);
                    k++;
                }
            }
        }
    }

    private static List<Double> getWindowCenter(VoiLutModule vLut, WlPresentation wl) {
        List<Double> luts = new ArrayList<>();
        if (wl.getPresentationState() instanceof PresentationLutObject pr) {
            Optional<VoiLutModule> voiLUT = pr.getVoiLUT();
            voiLUT.ifPresent(voiLutModule -> luts.addAll(voiLutModule.getWindowCenter()));
        }
        if (!vLut.getWindowCenter().isEmpty()) {
            luts.addAll(vLut.getWindowCenter());
        }
        return luts;
    }

    private static List<Double> getWindowWidth(VoiLutModule vLut, WlPresentation wl) {
        List<Double> luts = new ArrayList<>();
        PresentationStateLut pr = wl.getPresentationState();
        if (wl.getPresentationState() instanceof PresentationLutObject) {
            Optional<VoiLutModule> voiLUT = ((PresentationLutObject) pr).getVoiLUT();
            voiLUT.ifPresent(voiLutModule -> luts.addAll(voiLutModule.getWindowWidth()));
        }
        if (!vLut.getWindowWidth().isEmpty()) {
            luts.addAll(vLut.getWindowWidth());
        }
        return luts;
    }

    private static List<LookupTableCV> getVoiLutData(VoiLutModule vLut, WlPresentation wl) {
        List<LookupTableCV> luts = new ArrayList<>();
        if (wl.getPresentationState() instanceof PresentationLutObject pr) {
            Optional<VoiLutModule> vlut = pr.getVoiLUT();
            vlut.ifPresent(voiLutModule -> luts.addAll(voiLutModule.getLut()));
        }
        if (!vLut.getLut().isEmpty()) {
            luts.addAll(vLut.getLut());
        }
        return luts;
    }

    private static List<String> getVoiLUTExplanation(VoiLutModule vLut, WlPresentation wl) {
        List<String> luts = new ArrayList<>();
        if (wl.getPresentationState() instanceof PresentationLutObject pr) {
            Optional<VoiLutModule> vlut = pr.getVoiLUT();
            vlut.ifPresent(voiLutModule -> luts.addAll(voiLutModule.getLutExplanation()));
        }
        if (!vLut.getLutExplanation().isEmpty()) {
            luts.addAll(vLut.getLutExplanation());
        }
        return luts;
    }

    public static PresetWindowLevel buildPresetFromLutData(ImageAdapter adapter, LookupTableCV voiLUTsData,
            WlPresentation wl, String explanation) {
        if (adapter == null || voiLUTsData == null || explanation == null) {
            return null;
        }

        Object inLut;

        if (voiLUTsData.getDataType() == DataBuffer.TYPE_BYTE) {
            inLut = voiLUTsData.getByteData(0);
        } else if (voiLUTsData.getDataType() <= DataBuffer.TYPE_SHORT) {
            inLut = voiLUTsData.getShortData(0);
        } else {
            return null;
        }

        int minValueLookup = voiLUTsData.getOffset();
        int maxValueLookup = voiLUTsData.getOffset() + Array.getLength(inLut) - 1;

        minValueLookup = Math.min(minValueLookup, maxValueLookup);
        maxValueLookup = Math.max(minValueLookup, maxValueLookup);
        int minAllocatedValue = adapter.getMinAllocatedValue(wl);
        if (minValueLookup < minAllocatedValue) {
            minValueLookup = minAllocatedValue;
        }
        int maxAllocatedValue = adapter.getMaxAllocatedValue(wl);
        if (maxValueLookup > maxAllocatedValue) {
            maxValueLookup = maxAllocatedValue;
        }

        double fullDynamicWidth = (double) maxValueLookup - minValueLookup;
        double fullDynamicCenter = minValueLookup + fullDynamicWidth / 2f;

        LutShape newLutShape = new LutShape(voiLUTsData, explanation);
        return new PresetWindowLevel(newLutShape.toString(), fullDynamicWidth, fullDynamicCenter, newLutShape);
    }

    public static Map<String, List<PresetWindowLevel>> getPresetListByModality() {
        Map<String, List<PresetWindowLevel>> presets = new TreeMap<>();

        XMLStreamReader xmler = null;
        InputStream stream = null;
        try {
            File file;
            String path = System.getProperty("dicom.presets.path");
            if (StringKit.hasText(path)) {
                file = new File(path);
            } else {
                file = new File(PresetWindowLevel.class.getResource("presets.xml").getFile());
            }
            if (!file.canRead()) {
                return Collections.emptyMap();
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            // disable external entities for security
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            stream = new FileInputStream(file);
            xmler = factory.createXMLStreamReader(stream);

            int eventType;
            while (xmler.hasNext()) {
                eventType = xmler.next();
                if (eventType == XMLStreamConstants.START_ELEMENT && "presets".equals(xmler.getName().getLocalPart())) {
                    while (xmler.hasNext()) {
                        readPresetListByModality(xmler, presets);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Cannot read presets file! ", e);
        } finally {
            IoKit.close(xmler);
            IoKit.close(stream);
        }
        return presets;
    }

    public static Integer getIntegerTagAttribute(XMLStreamReader xmler, String attribute, Integer defaultValue) {
        if (attribute != null) {
            String val = xmler.getAttributeValue(null, attribute);
            try {
                if (val != null) {
                    return Integer.valueOf(val);
                }
            } catch (NumberFormatException e) {
                Logger.error("Cannot parse integer {} of {}", val, attribute);
            }
        }
        return defaultValue;
    }

    private static void readPresetListByModality(XMLStreamReader xmler, Map<String, List<PresetWindowLevel>> presets)
            throws XMLStreamException {
        int eventType = xmler.next();
        String key;
        if (eventType == XMLStreamConstants.START_ELEMENT) {
            key = xmler.getName().getLocalPart();
            if ("preset".equals(key) && xmler.getAttributeCount() >= 4) {
                String name = xmler.getAttributeValue(null, "name");
                try {
                    String modality = xmler.getAttributeValue(null, "modality");
                    double window = Double.parseDouble(xmler.getAttributeValue(null, "window"));
                    double level = Double.parseDouble(xmler.getAttributeValue(null, "level"));
                    String shape = xmler.getAttributeValue(null, "shape");
                    Integer keyCode = getIntegerTagAttribute(xmler, "key", null);
                    LutShape lutShape = LutShape.getLutShape(shape);
                    PresetWindowLevel preset = new PresetWindowLevel(name, window, level,
                            lutShape == null ? LutShape.LINEAR : lutShape);
                    if (keyCode != null) {
                        preset.setKeyCode(keyCode);
                    }
                    List<PresetWindowLevel> presetList = presets.computeIfAbsent(modality, k -> new ArrayList<>());
                    presetList.add(preset);
                } catch (Exception e) {
                    Logger.error("Preset {} cannot be read from xml file", name, e);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public double getWindow() {
        return window;
    }

    public double getLevel() {
        return level;
    }

    public LutShape getLutShape() {
        return shape;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public double getMinBox() {
        return level - window / 2.0;
    }

    public double getMaxBox() {
        return level + window / 2.0;
    }

    public boolean isAutoLevel() {
        return keyCode == 0x30;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PresetWindowLevel that = (PresetWindowLevel) o;
        return Double.compare(that.window, window) == 0 && Double.compare(that.level, level) == 0
                && name.equals(that.name) && shape.equals(that.shape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, window, level, shape);
    }

}
