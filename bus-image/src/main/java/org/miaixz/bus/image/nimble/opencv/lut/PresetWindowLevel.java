/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.ImageAdapter;
import org.miaixz.bus.image.nimble.PresentationLutObject;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PresetWindowLevel type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PresetWindowLevel {

    /**
     * The preset list by modality value.
     */
    private static final Map<String, List<PresetWindowLevel>> presetListByModality = getPresetListByModality();

    /**
     * The name value.
     */
    private final String name;

    /**
     * The window value.
     */
    private final double window;

    /**
     * The level value.
     */
    private final double level;

    /**
     * The shape value.
     */
    private final LutShape shape;

    /**
     * The key code value.
     */
    private int keyCode = 0;

    /**
     * Creates a new instance.
     *
     * @param name   the name.
     * @param window the window.
     * @param level  the level.
     * @param shape  the shape.
     */
    public PresetWindowLevel(String name, Double window, Double level, LutShape shape) {
        this.name = Objects.requireNonNull(name);
        this.window = Objects.requireNonNull(window);
        this.level = Objects.requireNonNull(level);
        this.shape = Objects.requireNonNull(shape);
    }

    /**
     * Gets the preset collection.
     *
     * @param adapter the adapter.
     * @param type    the type.
     * @param wl      the wl.
     * @return the preset collection.
     */
    public static List<PresetWindowLevel> getPresetCollection(ImageAdapter adapter, String type, WlPresentation wl) {
        if (adapter == null || wl == null) {
            throw new IllegalArgumentException("Null parameter");
        }

        String dicomKeyWord = Symbol.SPACE + type;

        ArrayList<PresetWindowLevel> presetList = new ArrayList<>();
        ImageDescriptor desc = adapter.getImageDescriptor();
        VoiLutModule vLut = desc.getVoiLutForFrame(adapter.getFrameIndex());
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

    /**
     * Gets the default lut shape.
     *
     * @param vLut         the v lut.
     * @param dicomKeyWord the dicom key word.
     * @return the default lut shape.
     */
    private static LutShape getDefaultLutShape(VoiLutModule vLut, String dicomKeyWord) {
        Optional<String> lutFunctionDescriptor = vLut.getVoiLutFunction();

        // Implicitly defined as default function in DICOM standard
        LutShape defaultLutShape = LutShape.LINEAR;
        if (lutFunctionDescriptor.isPresent()) {
            if ("SIGMOID".equalsIgnoreCase(lutFunctionDescriptor.get())) {
                defaultLutShape = new LutShape(LutShape.Function.SIGMOID, LutShape.Function.SIGMOID + dicomKeyWord);
            } else if ("LINEAR".equalsIgnoreCase(lutFunctionDescriptor.get())) {
                defaultLutShape = new LutShape(LutShape.Function.LINEAR, LutShape.Function.LINEAR + dicomKeyWord);
            }
        }
        return defaultLutShape;
    }

    /**
     * Builds the preset from lut data.
     *
     * @param adapter      the adapter.
     * @param wl           the wl.
     * @param vLut         the v lut.
     * @param dicomKeyWord the dicom key word.
     * @param presetList   the preset list.
     */
    private static void buildPresetFromLutData(
            ImageAdapter adapter,
            WlPresentation wl,
            VoiLutModule vLut,
            String dicomKeyWord,
            ArrayList<PresetWindowLevel> presetList) {
        List<LookupTableCV> voiLUTsData = getVoiLutData(vLut, wl);
        List<String> voiLUTsExplanation = getVoiLUTExplanation(vLut, wl);

        if (!voiLUTsData.isEmpty()) {
            String defaultExplanation = "VOI LUT";

            for (int i = 0; i < voiLUTsData.size(); i++) {
                String explanation = getPresetExplanation(voiLUTsExplanation, i, defaultExplanation + Symbol.SPACE + i);
                PresetWindowLevel preset = buildPresetFromLutData(
                        adapter,
                        voiLUTsData.get(i),
                        wl,
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

    /**
     * Gets the preset explanation.
     *
     * @param wlExplanationList  the wl explanation list.
     * @param index              the index.
     * @param defaultExplanation the default explanation.
     * @return the preset explanation.
     */
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

    /**
     * Builds the preset.
     *
     * @param levelList         the level list.
     * @param windowList        the window list.
     * @param wlExplanationList the wl explanation list.
     * @param dicomKeyWord      the dicom key word.
     * @param defaultLutShape   the default lut shape.
     * @param presetList        the preset list.
     */
    private static void buildPreset(
            List<Double> levelList,
            List<Double> windowList,
            List<String> wlExplanationList,
            String dicomKeyWord,
            LutShape defaultLutShape,
            ArrayList<PresetWindowLevel> presetList) {
        if (!levelList.isEmpty() && !windowList.isEmpty()) {
            String defaultExplanation = "Default";

            int k = 1;
            for (int i = 0; i < levelList.size(); i++) {
                String explanation = defaultExplanation + Symbol.SPACE + k;
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

    /**
     * Gets the window center.
     *
     * @param vLut the v lut.
     * @param wl   the wl.
     * @return the window center.
     */
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

    /**
     * Gets the window width.
     *
     * @param vLut the v lut.
     * @param wl   the wl.
     * @return the window width.
     */
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

    /**
     * Gets the voi lut data.
     *
     * @param vLut the v lut.
     * @param wl   the wl.
     * @return the voi lut data.
     */
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

    /**
     * Gets the voi lut explanation.
     *
     * @param vLut the v lut.
     * @param wl   the wl.
     * @return the voi lut explanation.
     */
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

    /**
     * Builds the preset from lut data.
     *
     * @param adapter     the adapter.
     * @param voiLUTsData the voi lu ts data.
     * @param wl          the wl.
     * @param explanation the explanation.
     * @return the operation result.
     */
    public static PresetWindowLevel buildPresetFromLutData(
            ImageAdapter adapter,
            LookupTableCV voiLUTsData,
            WlPresentation wl,
            String explanation) {
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

    /**
     * Gets the preset list by modality.
     *
     * @return the preset list by modality.
     */
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
            Logger.error(false, "Image", "Cannot read presets file! ", e);
        } finally {
            IoKit.close(xmler);
            IoKit.close(stream);
        }
        return presets;
    }

    /**
     * Gets the integer tag attribute.
     *
     * @param xmler        the xmler.
     * @param attribute    the attribute.
     * @param defaultValue the default value.
     * @return the integer tag attribute.
     */
    public static Integer getIntegerTagAttribute(XMLStreamReader xmler, String attribute, Integer defaultValue) {
        if (attribute != null) {
            String val = xmler.getAttributeValue(null, attribute);
            try {
                if (val != null) {
                    return Integer.valueOf(val);
                }
            } catch (NumberFormatException e) {
                Logger.error(false, "Image", "Cannot parse integer {} of {}", val, attribute);
            }
        }
        return defaultValue;
    }

    /**
     * Reads the preset list by modality.
     *
     * @param xmler                    the xmler.
     * @param presets                  the presets.
     * @param List<PresetWindowLevel>> the list<preset window level>>.
     * @throws XMLStreamException if the operation cannot be completed.
     */
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
                    Logger.error(false, "Image", "Preset {} cannot be read from xml file", name, e);
                }
            }
        }
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the window.
     *
     * @return the window.
     */
    public double getWindow() {
        return window;
    }

    /**
     * Gets the level.
     *
     * @return the level.
     */
    public double getLevel() {
        return level;
    }

    /**
     * Gets the lut shape.
     *
     * @return the lut shape.
     */
    public LutShape getLutShape() {
        return shape;
    }

    /**
     * Gets the key code.
     *
     * @return the key code.
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Sets the key code.
     *
     * @param keyCode the key code.
     */
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * Gets the min box.
     *
     * @return the min box.
     */
    public double getMinBox() {
        return level - window / 2.0;
    }

    /**
     * Gets the max box.
     *
     * @return the max box.
     */
    public double getMaxBox() {
        return level + window / 2.0;
    }

    /**
     * Determines whether auto level.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isAutoLevel() {
        return keyCode == 0x30;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, window, level, shape);
    }

}
