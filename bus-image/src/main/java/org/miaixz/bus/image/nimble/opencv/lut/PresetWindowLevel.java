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
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.ImageAdapter;
import org.miaixz.bus.image.nimble.PresentationLutObject;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.lut.LutShape.Function;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the PresetWindowLevel type.
 *
 * @author Kimi Liu
 */
public class PresetWindowLevel {

    /**
     * The preset list by modality value.
     */
    private static final Map<String, List<PresetWindowLevel>> presetListByModality = getPresetListByModality();

    /**
     * Keyboard code assigned to the full-dynamic-range preset.
     */
    private static final int AUTO_LEVEL_KEY = 0x30;

    /**
     * Keyboard code assigned to the first image preset.
     */
    private static final int FIRST_PRESET_KEY = 0x31;

    /**
     * Keyboard code assigned to the second image preset.
     */
    private static final int SECOND_PRESET_KEY = 0x32;

    /**
     * Optional application provider overriding the module XML preset configuration.
     */
    private static final AtomicReference<ModalityPresetProvider> modalityPresetProvider = new AtomicReference<>();

    /**
     * Stable configured preset identifier, or null for image-derived presets.
     */
    private String id;

    /**
     * Whether this preset is preferred when neither the image nor its presentation state supplies a VOI.
     */
    private boolean fallbackDefault;

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
     * Builds a collection of window/level presets from DICOM image data.
     *
     * @param adapter the DICOM image adapter containing the data
     * @param type    the type identifier to append to preset names
     * @param wl      the window/level presentation state
     * @return list of presets extracted from the image data
     * @throws IllegalArgumentException if adapter or wl is null
     */
    public static List<PresetWindowLevel> getPresetCollection(ImageAdapter adapter, String type, WlPresentation wl) {
        if (adapter == null || wl == null) {
            throw new IllegalArgumentException("Image adapter and presentation must not be null");
        }

        var presetBuilder = new PresetCollectionBuilder(adapter, type, wl);
        return presetBuilder.buildPresets();
    }

    /**
     * Creates a preset from LUT data by calculating equivalent window/level values.
     *
     * @param adapter     the image adapter providing frame-specific pixel and LUT metadata
     * @param voiLUTsData the source VOI lookup table
     * @param wl          the presentation-state and pixel-padding settings
     * @param explanation the display explanation of the LUT preset
     * @return the LUT-derived preset, or null when its data type is unsupported
     */
    public static PresetWindowLevel buildPresetFromLutData(
            ImageAdapter adapter,
            LookupTableCV voiLUTsData,
            WlPresentation wl,
            String explanation) {
        if (adapter == null || voiLUTsData == null || explanation == null) {
            return null;
        }

        var lutData = extractLutData(voiLUTsData);
        if (lutData == null)
            return null;

        var valueRange = calculateLutValueRange(voiLUTsData, lutData, adapter, wl);
        var windowLevel = calculateWindowLevelFromRange(valueRange);
        var newLutShape = new LutShape(voiLUTsData, explanation);

        return new PresetWindowLevel(newLutShape.toString(), windowLevel[0], windowLevel[1], newLutShape);
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
            String path = System.getProperty("dicom.presets.path");
            if (StringKit.hasText(path)) {
                File file = new File(path);
                if (!file.canRead()) {
                    return Collections.emptyMap();
                }
                stream = new FileInputStream(file);
            } else {
                stream = PresetWindowLevel.class.getResourceAsStream("presets.xml");
            }
            if (stream == null) {
                return Collections.emptyMap();
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            // disable external entities for security
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
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
     * @param xmler   the reader positioned in the presets document
     * @param presets the map receiving presets grouped by DICOM modality
     * @throws XMLStreamException if the XML cannot be read
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
        return keyCode == AUTO_LEVEL_KEY;
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        var that = (PresetWindowLevel) o;
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

    /**
     * Stable identifier of a configured preset; null for presets built from the image.
     *
     * @return stable configured preset identifier, or null for image-derived presets
     */
    public String getId() {
        return id;
    }

    /**
     * Assigns the stable identifier used to match configured presets across images.
     *
     * @param id the stable configured preset identifier, or null for an image-derived preset
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Whether this configured preset becomes the default, placed first, for an image that carries no window/level and
     * no VOI LUT. Values carried by the image always stay the default.
     *
     * @return whether this preset is preferred when neither the image nor its presentation state supplies a VOI
     */
    public boolean isFallbackDefault() {
        return fallbackDefault;
    }

    /**
     * Marks whether this preset is preferred when image-derived VOI values are absent.
     *
     * @param fallbackDefault whether this preset becomes the fallback when image VOI values are absent
     */
    public void setFallbackDefault(boolean fallbackDefault) {
        this.fallbackDefault = fallbackDefault;
    }

    /**
     * Whether both presets are the same preset on different images: same id when both have one, otherwise same name.
     * Unlike {@link #equals(Object)}, the values may differ.
     *
     * @param other the preset to compare, or null
     * @return Whether both presets are the same preset on different images: same id when both have one, otherwise same
     *         name. Unlike {@link #equals(Object)}, the values may differ
     */
    public boolean isSamePreset(PresetWindowLevel other) {
        if (other == null) {
            return false;
        }
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        return name.equals(other.name);
    }

    /**
     * Sets the configured preset provider. Passing {@code null} restores the built-in or externally configured XML
     * presets.
     *
     * @param provider the custom provider, or {@code null} to restore the default provider
     */
    public static void setModalityPresetProvider(ModalityPresetProvider provider) {
        modalityPresetProvider.set(provider);
    }

    /**
     * Returns optional application provider overriding the module XML preset configuration.
     *
     * @return optional application provider overriding the module XML preset configuration
     */
    public static ModalityPresetProvider getModalityPresetProvider() {
        ModalityPresetProvider provider = modalityPresetProvider.get();
        return provider == null
                ? (adapter, wl) -> adapter.getBitsStored() > 8 && adapter.getImageDescriptor().getModality() != null
                        ? presetListByModality.getOrDefault(adapter.getImageDescriptor().getModality(), List.of())
                        : List.of()
                : provider;
    }

    /**
     * Extracts the first component of a byte or short VOI lookup table.
     *
     * @param voiLUTsData the source VOI lookup table
     * @return the underlying primitive array, or null for unsupported LUT types
     */
    private static Object extractLutData(LookupTableCV voiLUTsData) {
        return switch (voiLUTsData.getDataType()) {
            case DataBuffer.TYPE_BYTE -> voiLUTsData.getByteData(0);
            case DataBuffer.TYPE_SHORT, DataBuffer.TYPE_USHORT -> voiLUTsData.getShortData(0);
            default -> null;
        };
    }

    /**
     * Bounds the LUT input range by the values allocated to the image.
     *
     * @param voiLUTsData the source VOI lookup table
     * @param lutData     the primitive array backing the LUT component
     * @param adapter     the image adapter providing frame-specific pixel and LUT metadata
     * @param wl          the presentation-state and pixel-padding settings
     * @return the inclusive minimum and maximum LUT input values
     */
    private static int[] calculateLutValueRange(
            LookupTableCV voiLUTsData,
            Object lutData,
            ImageAdapter adapter,
            WlPresentation wl) {
        int minValue = voiLUTsData.getOffset();
        int maxValue = voiLUTsData.getOffset() + Array.getLength(lutData) - 1;

        // Ensure proper ordering and clamp to allocated value range
        minValue = Math.max(Math.min(minValue, maxValue), adapter.getMinAllocatedValue(wl));
        maxValue = Math.min(Math.max(minValue, maxValue), adapter.getMaxAllocatedValue(wl));

        return new int[] { minValue, maxValue };
    }

    /**
     * Calculates window width and center for an inclusive LUT input range.
     *
     * @param range the inclusive minimum and maximum LUT inputs
     * @return the window width and center
     */
    private static double[] calculateWindowLevelFromRange(int[] range) {
        double width = (double) range[1] - range[0];
        double center = range[0] + width / 2.0;
        return new double[] { width, center };
    }

    /**
     * Helper class to build preset collections from DICOM data.
     *
     * @author Kimi Liu
     */
    private static class PresetCollectionBuilder {

        /**
         * Image and frame metadata used to derive rendering values.
         */
        private final ImageAdapter adapter;
        /**
         * Source label appended to image and presentation-state preset names.
         */
        private final String dicomKeyWord;
        /**
         * Presentation-state and pixel-padding settings used to calculate dynamic ranges.
         */
        private final WlPresentation wl;
        /**
         * Ordered presets accumulated for the current image and frame.
         */
        private final List<PresetWindowLevel> presetList;
        /**
         * Image VOI module for the selected frame.
         */
        private final VoiLutModule vLut;

        /**
         * Creates a preset accumulator for the selected image, frame and presentation state.
         *
         * @param adapter the image adapter providing frame-specific pixel and LUT metadata
         * @param type    the source label appended to preset names
         * @param wl      the presentation-state and pixel-padding settings
         */
        PresetCollectionBuilder(ImageAdapter adapter, String type, WlPresentation wl) {
            this.adapter = adapter;
            this.dicomKeyWord = " " + type;
            this.wl = wl;
            this.presetList = new ArrayList<>();
            ImageDescriptor desc = adapter.getImageDescriptor();
            this.vLut = desc.getVoiLutForFrame(adapter.getFrameIndex());
        }

        /**
         * Combines image, presentation-state, automatic and configured presets in default-selection order.
         *
         * @return the ordered preset collection
         */
        List<PresetWindowLevel> buildPresets() {
            buildPresetsFromWindowLevel();
            buildPresetsFromLutData();
            boolean carriedByImage = !presetList.isEmpty();
            PresetWindowLevel autoLevel = addAutoLevelPreset();
            if (wl.getPresentationState() instanceof PresentationLutObject && prVoi().isEmpty()) {
                // The VOI LUT of a presentation state replaces the one of the image, and none applying to
                // the frame leaves the values as they are: the full range comes first, the image windows
                // stay available after it.
                presetList.remove(autoLevel);
                presetList.add(0, autoLevel);
                carriedByImage = true;
            }
            addModalityPresets(carriedByImage);
            return presetList;
        }

        // The presentation state windows come first, each source with its own explanations and VOI
        // LUT Function (PS3.3 C.11.2 for the image, C.11.8 for the presentation state).
        /**
         * Adds presentation-state windows before image windows, retaining each source LUT function.
         */
        private void buildPresetsFromWindowLevel() {
            int presetCounter = 1;
            VoiLutModule prVoi = effectiveVoi();
            if (prVoi != vLut) {
                presetCounter = addWindowPresets(prVoi, presetCounter);
            }
            addWindowPresets(vLut, presetCounter);
        }

        /**
         * Selects the applicable presentation-state VOI or the image VOI fallback.
         *
         * @return the effective VOI module
         */
        private VoiLutModule effectiveVoi() {
            return prVoi().orElse(vLut);
        }

        // The presentation state may window each image or frame differently, or leave some alone
        /**
         * Finds the presentation-state VOI item referencing the selected image and frame.
         *
         * @return the applicable presentation-state VOI, if any
         */
        private Optional<VoiLutModule> prVoi() {
            if (wl.getPresentationState() instanceof PresentationLutObject pr) {
                ImageDescriptor desc = adapter.getImageDescriptor();
                return pr.getVoiLUT(desc.getSopInstanceUID(), adapter.getFrameIndex() + 1);
            }
            return Optional.empty();
        }

        /**
         * Adds distinct window/level pairs and assigns shortcuts to the first two image presets.
         *
         * @param voi               the source VOI module
         * @param firstPresetNumber the first one-based preset number available for assignment
         * @return the next available preset number
         */
        private int addWindowPresets(VoiLutModule voi, int firstPresetNumber) {
            var levelList = voi.getWindowCenter();
            var windowList = voi.getWindowWidth();
            var explanationList = voi.getWindowCenterWidthExplanation();
            var lutShape = getLutShape(voi);
            int presetCounter = firstPresetNumber;
            int count = Math.min(levelList.size(), windowList.size());
            for (int i = 0; i < count; i++) {
                var explanation = getPresetExplanation(explanationList, i, "Default " + presetCounter);
                var preset = new PresetWindowLevel(explanation + dicomKeyWord, windowList.get(i), levelList.get(i),
                        lutShape);

                if (!presetList.contains(preset)) {
                    setPresetKeyCode(preset, presetCounter - 1);
                    presetList.add(preset);
                    presetCounter++;
                }
            }
            return presetCounter;
        }

        /**
         * Adds named VOI lookup-table presets from the presentation state and image.
         */
        private void buildPresetsFromLutData() {
            var voiLUTsData = getVoiLutData();
            var voiLUTsExplanation = getVoiLUTExplanation();

            if (voiLUTsData.isEmpty())
                return;

            var defaultExplanation = "VOI LUT";
            for (int i = 0; i < voiLUTsData.size(); i++) {
                var explanation = getPresetExplanation(voiLUTsExplanation, i, defaultExplanation + " " + i);
                var preset = buildPresetFromLutData(adapter, voiLUTsData.get(i), wl, explanation + dicomKeyWord);

                if (preset != null) {
                    setPresetKeyCode(preset, presetList.size());
                    presetList.add(preset);
                }
            }
        }

        /**
         * Adds a full-range preset using LINEAR_EXACT for the default linear function.
         *
         * @return the newly added automatic preset
         */
        private PresetWindowLevel addAutoLevelPreset() {
            // The window covers exactly the range of the image: LINEAR_EXACT (PS3.3 C.11.2.1.3.2) maps
            // [c - w / 2, c + w / 2], where LINEAR would end one unit short and turn a range of a
            // single value into a threshold.
            LutShape shape = getLutShape(effectiveVoi());
            if (shape.getFunctionType() == Function.LINEAR) {
                shape = LutShape.LINEAR_EXACT;
            }
            var autoLevel = new PresetWindowLevel("Auto Level [Image]", adapter.getFullDynamicWidth(wl),
                    adapter.getFullDynamicCenter(wl), shape);
            autoLevel.setKeyCode(AUTO_LEVEL_KEY);
            presetList.add(autoLevel);
            return autoLevel;
        }

        /**
         * Adds configured modality presets and promotes a fallback only when the image has no default VOI.
         *
         * @param carriedByImage whether image or presentation-state values already supply the default VOI
         */
        private void addModalityPresets(boolean carriedByImage) {
            List<PresetWindowLevel> modPresets;
            try {
                modPresets = getModalityPresetProvider().getPresets(adapter, wl);
            } catch (RuntimeException e) {
                Logger.error("Cannot get the configured presets", e);
                return;
            }
            if (modPresets == null) {
                return;
            }
            PresetWindowLevel fallback = carriedByImage ? null
                    : modPresets.stream().filter(PresetWindowLevel::isFallbackDefault).findFirst().orElse(null);
            if (fallback != null) {
                presetList.add(0, fallback);
            }
            modPresets.stream().filter(p -> p != fallback).forEach(presetList::add);
        }

        /**
         * Collects presentation-state lookup tables before image lookup tables.
         *
         * @return the ordered VOI lookup tables
         */
        private List<LookupTableCV> getVoiLutData() {
            var luts = new ArrayList<LookupTableCV>();
            prVoi().ifPresent(voiLutModule -> luts.addAll(voiLutModule.getLut()));
            luts.addAll(vLut.getLut());
            return luts;
        }

        /**
         * Collects explanations in the same order as the presentation-state and image lookup tables.
         *
         * @return the ordered lookup-table explanations
         */
        private List<String> getVoiLUTExplanation() {
            var explanations = new ArrayList<String>();
            prVoi().ifPresent(voiLutModule -> explanations.addAll(voiLutModule.getLutExplanation()));
            explanations.addAll(vLut.getLutExplanation());
            return explanations;
        }

        /**
         * Resolves the VOI function, falling back to LINEAR for an unknown defined term.
         *
         * @param voi the source VOI module
         * @return the resolved VOI lookup shape
         */
        private LutShape getLutShape(VoiLutModule voi) {
            return voi.getVoiLutFunction().map(function -> switch (function.trim().toUpperCase(Locale.ROOT)) {
                case "SIGMOID" -> new LutShape(Function.SIGMOID, Function.SIGMOID + dicomKeyWord);
                case "LINEAR_EXACT" -> new LutShape(Function.LINEAR_EXACT, Function.LINEAR_EXACT + dicomKeyWord);
                case "LINEAR" -> new LutShape(Function.LINEAR, Function.LINEAR + dicomKeyWord);
                default -> {
                    // Deviation: an undefined VOI LUT Function (PS3.3 C.11.2.1.3 only defines
                    // LINEAR, LINEAR_EXACT and SIGMOID) falls back to LINEAR instead of
                    // rejecting the window.
                    Logger.warn("Unknown VOI LUT Function '{}', LINEAR is used", function);
                    yield LutShape.LINEAR;
                }
            }).orElse(LutShape.LINEAR);
        }

        /**
         * Selects a nonblank explanation or the supplied fallback label.
         *
         * @param explanationList    the explanations aligned with the LUT or window entries
         * @param index              the zero-based entry index
         * @param defaultExplanation the fallback label used when no nonblank explanation exists
         * @return the display label of the preset
         */
        private static String getPresetExplanation(List<String> explanationList, int index, String defaultExplanation) {
            if (index < explanationList.size()) {
                var explanation = explanationList.get(index);
                if (StringKit.hasText(explanation)) {
                    return explanation;
                }
            }
            return defaultExplanation;
        }

        /**
         * Assigns shortcut codes only to the first two image presets.
         *
         * @param preset the preset receiving a shortcut
         * @param index  the zero-based entry index
         */
        private static void setPresetKeyCode(PresetWindowLevel preset, int index) {
            if (index == 0) {
                preset.setKeyCode(FIRST_PRESET_KEY);
            } else if (index == 1) {
                preset.setKeyCode(SECOND_PRESET_KEY);
            }
        }

    }

}
