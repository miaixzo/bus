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
package org.miaixz.bus.image.nimble.opencv.lut.colormap;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.lut.ByteLut;
import org.miaixz.bus.image.nimble.opencv.lut.ColorLut;

/**
 * Declarative color map: control points on a value axis, compiled on demand into the tables the rendering code consumes
 * (see {@link ColorMapCompiler}). Stops are kept sorted by position; two stops at one position make a hard edge.
 *
 * @param modalities         DICOM modality codes the map is meant for; empty means all
 * @param defaultForModality whether the map is picked when a series of one of its modalities opens
 * @param bits               index resolution of compiled tables, 8 to 16
 * @param lighting           volume rendering settings, or null for 2D-only maps
 * @param metadata           free-form provenance (DICOM palette UID, label, creator); empty by default
 * @param id                 stable identifier, independent of the display name; a slug of the name by default
 * @param category           menu section when modality is not the natural split, or null
 * @param tags               keywords for search; empty by default
 * @param hidden             kept out of menus, e.g. a palette that only a presentation state references
 * @author Kimi Liu
 */
public record ColorMap(String id, String name, ColorMapType type, Set<String> modalities, boolean defaultForModality,
        ColorMapDomain domain, int bits, InterpolationSpace space, Interpolation interpolation, List<ColorStop> stops,
        OutsideColors outside, Lighting lighting, Map<String, String> metadata, String category, Set<String> tags,
        boolean hidden) {

    public static final int MIN_BITS = 8;
    public static final int MAX_BITS = 16;
    /**
     * Metadata key of the DICOM Color Palette SOP Instance UID the map came from or was given.
     */
    public static final String META_DICOM_UID = "dicom.sopInstanceUID"; // NON-NLS
    public static final String META_DICOM_LABEL = "dicom.contentLabel"; // NON-NLS
    public static final String META_DICOM_DESCRIPTION = "dicom.contentDescription"; // NON-NLS
    public static final String META_DICOM_CREATOR = "dicom.contentCreator"; // NON-NLS
    private static final Comparator<ColorStop> BY_POSITION = Comparator.comparingDouble(ColorStop::position);

    public ColorMap {
        Objects.requireNonNull(name, "Name cannot be null");
        id = StringKit.hasText(id) ? id : slug(name);
        if (id.isBlank()) {
            throw new IllegalArgumentException("A color map needs an id or a name to derive it from");
        }
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(domain, "Domain cannot be null");
        Objects.requireNonNull(space, "Interpolation space cannot be null");
        Objects.requireNonNull(interpolation, "Interpolation cannot be null");
        Objects.requireNonNull(outside, "Outside colors cannot be null");
        if (bits < MIN_BITS || bits > MAX_BITS) {
            throw new IllegalArgumentException("Bits must be in [8, 16]: " + bits);
        }
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("A color map needs at least one stop");
        }
        modalities = modalities == null ? Set.of() : Set.copyOf(modalities);
        stops = sortedCopy(stops);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        tags = tags == null ? Set.of() : Set.copyOf(tags);
        category = StringKit.hasText(category) ? category : null;
    }

    /**
     * Lower-case identifier derived from a display name: Unicode letters, digits and dashes only.
     */
    public static String slug(String name) {
        String dashed = Objects.requireNonNull(name, "Name cannot be null").toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", "-");
        // Separator runs are already collapsed, so at most one dash remains at each end
        int start = dashed.startsWith("-") ? 1 : 0;
        int end = dashed.endsWith("-") ? dashed.length() - 1 : dashed.length();
        return start >= end ? "" : dashed.substring(start, end);
    }

    private static List<ColorStop> sortedCopy(List<ColorStop> stops) {
        var copy = new ArrayList<>(stops);
        copy.sort(BY_POSITION); // stable: keeps the authoring order of stops sharing a position
        return List.copyOf(copy);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Sampled map from a {@code [3][n]} table in B, G, R order, the {@link ByteLut} layout.
     */
    public static ColorMap fromBgrTable(String name, byte[][] bgr) {
        Objects.requireNonNull(bgr, "Table cannot be null");
        if (bgr.length < 3 || bgr[0] == null || bgr[0].length < 2) {
            throw new IllegalArgumentException("Table must have 3 bands of at least 2 entries");
        }
        int n = bgr[0].length;
        if (bgr[1] == null || bgr[1].length != n || bgr[2] == null || bgr[2].length != n) {
            throw new IllegalArgumentException("Table bands must have the same length");
        }
        var stops = new ArrayList<ColorStop>(n);
        for (int i = 0; i < n; i++) {
            var color = new Color(Byte.toUnsignedInt(bgr[2][i]), Byte.toUnsignedInt(bgr[1][i]),
                    Byte.toUnsignedInt(bgr[0][i]));
            stops.add(ColorStop.of((double) i / (n - 1), color));
        }
        return builder(name).id("legacy." + slug(name)) // NON-NLS
                .interpolation(Interpolation.SAMPLED).stops(stops).build();
    }

    /**
     * The map a byte LUT was compiled from, else a sampled map of its table; a null table is the identity gray ramp.
     */
    public static ColorMap fromByteLut(ByteLut lut) {
        if (lut.source() != null) {
            return lut.source();
        }
        byte[][] table = lut.lutTable() != null ? lut.lutTable() : ColorLut.GRAY.getByteLut().lutTable();
        return fromBgrTable(lut.name(), table);
    }

    // Re-joins a color-only and an alpha-only stop sharing position and group: a split stop
    // reassembled.
    private static List<ColorStop> mergeAtSamePosition(List<ColorStop> colorParts, List<ColorStop> alphaParts) {
        var merged = new ArrayList<ColorStop>(colorParts.size() + alphaParts.size());
        var unmatched = new ArrayList<>(alphaParts);
        for (ColorStop color : colorParts) {
            ColorStop match = null;
            for (ColorStop alpha : unmatched) {
                if (alpha.position() == color.position() && Objects.equals(alpha.group(), color.group())) {
                    match = alpha;
                    break;
                }
            }
            if (match == null) {
                merged.add(color);
            } else {
                unmatched.remove(match);
                merged.add(color.withAlpha(match.alpha()));
            }
        }
        merged.addAll(unmatched);
        return merged;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public int entries() {
        return 1 << bits;
    }

    public boolean appliesTo(String modality) {
        return modalities.isEmpty() || (modality != null && modalities.contains(modality));
    }

    public boolean hasAlpha() {
        return stops.stream().anyMatch(ColorStop::hasAlpha);
    }

    public boolean hasMaterial() {
        return stops.stream().anyMatch(ColorStop::hasMaterial);
    }

    public double firstPosition() {
        return stops.get(0).position();
    }

    public double lastPosition() {
        return stops.get(stops.size() - 1).position();
    }

    public ColorMapSampler sampler() {
        return new ColorMapSampler(this);
    }

    /**
     * Color at a domain value; build a {@link #sampler()} once when sampling many values.
     */
    public Rgba sample(double value) {
        return sampler().sample(value);
    }

    /**
     * Same map with the color curve mirrored around the domain center. The alpha curve stays where it is: reversing a
     * transfer map must not make air opaque. Reversing twice gives back an equal map.
     */
    public ColorMap reversed() {
        double pivot = domain.min() + domain.max();
        var colorParts = new ArrayList<ColorStop>();
        var alphaParts = new ArrayList<ColorStop>();
        // Walked backwards so stops sharing a position swap order and hard edges stay hard
        for (int i = stops.size() - 1; i >= 0; i--) {
            ColorStop stop = stops.get(i);
            if (stop.hasColor()) {
                colorParts.add(stop.withAlpha(null).withPosition(pivot - stop.position()));
            }
        }
        for (ColorStop stop : stops) {
            if (stop.hasAlpha()) {
                alphaParts.add(stop.withColor(null).withMaterial(null));
            }
        }
        colorParts.sort(BY_POSITION);
        return toBuilder().stops(mergeAtSamePosition(colorParts, alphaParts)).build();
    }

    /**
     * Same map under another display name; the id is unchanged.
     */
    public ColorMap withName(String newName) {
        return toBuilder().name(newName).build();
    }

    public ColorMap withId(String newId) {
        return toBuilder().id(newId).build();
    }

    /**
     * Builds immutable color map instances with sensible defaults.
     *
     * @author Kimi Liu
     */
    public static final class Builder {

        private static final int UNSET_BITS = -1;
        private final List<ColorStop> stops = new ArrayList<>();
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private String id;
        private String name;
        private ColorMapType type = ColorMapType.SEQUENTIAL;
        private Set<String> modalities = Set.of();
        private boolean defaultForModality;
        private ColorMapDomain domain = ColorMapDomain.RELATIVE;
        private int bits = UNSET_BITS;
        private InterpolationSpace space = InterpolationSpace.RGB;
        private Interpolation interpolation = Interpolation.LINEAR;
        private OutsideColors outside = OutsideColors.CLAMP;
        private Lighting lighting;
        private String category;
        private Set<String> tags = Set.of();
        private boolean hidden;

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "Name cannot be null");
        }

        private Builder(ColorMap map) {
            this.id = map.id;
            this.name = map.name;
            this.type = map.type;
            this.modalities = map.modalities;
            this.defaultForModality = map.defaultForModality;
            this.domain = map.domain;
            this.bits = map.bits;
            this.space = map.space;
            this.interpolation = map.interpolation;
            this.stops.addAll(map.stops);
            this.outside = map.outside;
            this.lighting = map.lighting;
            this.metadata.putAll(map.metadata);
            this.category = map.category;
            this.tags = map.tags;
            this.hidden = map.hidden;
        }

        /**
         * Stable identifier; when unset, a slug of the name.
         */
        public Builder id(String value) {
            this.id = value;
            return this;
        }

        public Builder category(String value) {
            this.category = value;
            return this;
        }

        public Builder tags(Set<String> values) {
            this.tags = values;
            return this;
        }

        public Builder tags(String... values) {
            this.tags = Set.of(values);
            return this;
        }

        public Builder hidden(boolean value) {
            this.hidden = value;
            return this;
        }

        public Builder name(String value) {
            this.name = Objects.requireNonNull(value, "Name cannot be null");
            return this;
        }

        public Builder type(ColorMapType value) {
            this.type = value;
            return this;
        }

        public Builder modalities(String... codes) {
            this.modalities = Set.of(codes);
            return this;
        }

        public Builder modalities(Set<String> codes) {
            this.modalities = codes;
            return this;
        }

        public Builder defaultForModality(boolean value) {
            this.defaultForModality = value;
            return this;
        }

        public Builder domain(ColorMapDomain value) {
            this.domain = value;
            return this;
        }

        /**
         * Index resolution of compiled tables; defaults to 8 for relative domains, 16 otherwise.
         */
        public Builder bits(int value) {
            this.bits = value;
            return this;
        }

        public Builder space(InterpolationSpace value) {
            this.space = value;
            return this;
        }

        public Builder interpolation(Interpolation value) {
            this.interpolation = value;
            return this;
        }

        public Builder stop(ColorStop value) {
            this.stops.add(Objects.requireNonNull(value, "Stop cannot be null"));
            return this;
        }

        public Builder stop(double position, Color color) {
            return stop(ColorStop.of(position, color));
        }

        public Builder stop(double position, Color color, float alpha) {
            return stop(ColorStop.of(position, color, alpha));
        }

        public Builder alphaStop(double position, float alpha) {
            return stop(ColorStop.ofAlpha(position, alpha));
        }

        /**
         * Replaces the stops collected so far.
         */
        public Builder stops(List<ColorStop> values) {
            this.stops.clear();
            this.stops.addAll(values);
            return this;
        }

        public Builder outside(OutsideColors value) {
            this.outside = value;
            return this;
        }

        public Builder lighting(Lighting value) {
            this.lighting = value;
            return this;
        }

        /**
         * Sets one metadata entry; a null value removes it.
         */
        public Builder metadata(String key, String value) {
            if (value == null) {
                this.metadata.remove(key);
            } else {
                this.metadata.put(Objects.requireNonNull(key, "Key cannot be null"), value);
            }
            return this;
        }

        /**
         * Replaces all metadata.
         */
        public Builder metadata(Map<String, String> values) {
            this.metadata.clear();
            this.metadata.putAll(values);
            return this;
        }

        public ColorMap build() {
            int resolvedBits = bits;
            if (resolvedBits == UNSET_BITS) {
                resolvedBits = domain != null && domain.isRelative() ? MIN_BITS : MAX_BITS;
            }
            return new ColorMap(id, name, type, modalities, defaultForModality, domain, resolvedBits, space,
                    interpolation, stops, outside, lighting, metadata, category, tags, hidden);
        }
    }
}
