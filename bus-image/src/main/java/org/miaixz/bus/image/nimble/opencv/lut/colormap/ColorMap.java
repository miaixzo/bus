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
 * @param id                 stable identifier, independent of the display name; a slug of the name by default
 * @param name               human-readable display name
 * @param type               semantic map type
 * @param modalities         DICOM modality codes the map is meant for; empty means all
 * @param defaultForModality whether the map is picked when a series of one of its modalities opens
 * @param domain             value axis and default range
 * @param bits               index resolution of compiled tables, 8 to 16
 * @param space              color space used for interpolation
 * @param interpolation      interpolation strategy between stops
 * @param stops              color, opacity, and material control points
 * @param outside            colors used outside the stop range
 * @param lighting           volume rendering settings, or null for 2D-only maps
 * @param metadata           free-form provenance (DICOM palette UID, label, creator); empty by default
 * @param category           menu section when modality is not the natural split, or null
 * @param tags               keywords for search; empty by default
 * @param hidden             kept out of menus, e.g. a palette that only a presentation state references
 * @author Kimi Liu
 */
public record ColorMap(String id, String name, ColorMapType type, Set<String> modalities, boolean defaultForModality,
        ColorMapDomain domain, int bits, InterpolationSpace space, Interpolation interpolation, List<ColorStop> stops,
        OutsideColors outside, Lighting lighting, Map<String, String> metadata, String category, Set<String> tags,
        boolean hidden) {

    /**
     * Minimum supported lookup-table index resolution.
     */
    public static final int MIN_BITS = 8;

    /**
     * Maximum supported lookup-table index resolution.
     */
    public static final int MAX_BITS = 16;

    /**
     * Metadata key of the DICOM Color Palette SOP Instance UID the map came from or was given.
     */
    public static final String META_DICOM_UID = "dicom.sopInstanceUID"; // NON-NLS
    /**
     * Metadata key for a DICOM palette content label.
     */
    public static final String META_DICOM_LABEL = "dicom.contentLabel"; // NON-NLS
    /**
     * Metadata key for a DICOM palette content description.
     */
    public static final String META_DICOM_DESCRIPTION = "dicom.contentDescription"; // NON-NLS
    /**
     * Metadata key for a DICOM palette content creator.
     */
    public static final String META_DICOM_CREATOR = "dicom.contentCreator"; // NON-NLS
    /**
     * Stable ordering comparator for stop positions.
     */
    private static final Comparator<ColorStop> BY_POSITION = Comparator.comparingDouble(ColorStop::position);

    /**
     * Validates required properties, derives defaults, sorts stops, and makes collections immutable.
     *
     * @throws NullPointerException     if a required property is null
     * @throws IllegalArgumentException if the identifier, bit depth, or stop list is invalid
     */
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
     *
     * @param name the non-null display name
     * @return the normalized identifier, or an empty string if the name has no letters or digits
     * @throws NullPointerException if {@code name} is null
     */
    public static String slug(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        String dashed = StringKit.toLowerCase(name).replaceAll("[^\\p{L}\\p{N}]+", "-");
        // Separator runs are already collapsed, so at most one dash remains at each end
        int start = dashed.startsWith("-") ? 1 : 0;
        int end = dashed.endsWith("-") ? dashed.length() - 1 : dashed.length();
        return start >= end ? "" : dashed.substring(start, end);
    }

    /**
     * Returns an immutable, stably position-sorted copy of the supplied stops.
     *
     * @param stops stops to copy
     * @return sorted immutable stop list
     */
    private static List<ColorStop> sortedCopy(List<ColorStop> stops) {
        var copy = new ArrayList<>(stops);
        copy.sort(BY_POSITION); // stable: keeps the authoring order of stops sharing a position
        return List.copyOf(copy);
    }

    /**
     * Starts a map builder with the given display name.
     *
     * @param name display name
     * @return new builder
     */
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

    /**
     * Reassembles split color-only and alpha-only stops that share a position and editing group.
     *
     * @param colorParts color-bearing stops
     * @param alphaParts opacity-bearing stops
     * @return merged stop list
     */
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

    /**
     * Creates a builder initialized from this map.
     *
     * @return initialized builder
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns the number of entries implied by the configured bit depth.
     *
     * @return lookup-table entry count
     */
    public int entries() {
        return 1 << bits;
    }

    /**
     * Tests whether the map applies to a modality code.
     *
     * @param modality modality code
     * @return {@code true} when the map is unrestricted or explicitly supports the code
     */
    public boolean appliesTo(String modality) {
        return modalities.isEmpty() || (modality != null && modalities.contains(modality));
    }

    /**
     * Tests whether any stop defines opacity.
     *
     * @return {@code true} when an opacity curve is present
     */
    public boolean hasAlpha() {
        return stops.stream().anyMatch(ColorStop::hasAlpha);
    }

    /**
     * Tests whether any stop defines volume-rendering material coefficients.
     *
     * @return {@code true} when a material curve is present
     */
    public boolean hasMaterial() {
        return stops.stream().anyMatch(ColorStop::hasMaterial);
    }

    /**
     * Returns the position of the first sorted stop.
     *
     * @return first stop position
     */
    public double firstPosition() {
        return stops.get(0).position();
    }

    /**
     * Returns the position of the last sorted stop.
     *
     * @return last stop position
     */
    public double lastPosition() {
        return stops.get(stops.size() - 1).position();
    }

    /**
     * Creates a reusable sampler for this map.
     *
     * @return color-map sampler
     */
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

    /**
     * Returns the same map under a different stable identifier.
     *
     * @param newId replacement identifier
     * @return updated map
     */
    public ColorMap withId(String newId) {
        return toBuilder().id(newId).build();
    }

    /**
     * Builds immutable color map instances with sensible defaults.
     *
     * @author Kimi Liu
     */
    public static final class Builder {

        /**
         * Sentinel indicating that bit depth should be derived from the domain.
         */
        private static final int UNSET_BITS = -1;

        /**
         * Mutable stop collection accumulated by the builder.
         */
        private final List<ColorStop> stops = new ArrayList<>();

        /**
         * Mutable metadata collection accumulated by the builder.
         */
        private final Map<String, String> metadata = new LinkedHashMap<>();

        /**
         * Stable map identifier.
         */
        private String id;

        /**
         * Human-readable map name.
         */
        private String name;

        /**
         * Semantic map type.
         */
        private ColorMapType type = ColorMapType.SEQUENTIAL;

        /**
         * Supported modality codes.
         */
        private Set<String> modalities = Set.of();

        /**
         * Whether the map is the default for its modalities.
         */
        private boolean defaultForModality;

        /**
         * Value domain of the map.
         */
        private ColorMapDomain domain = ColorMapDomain.RELATIVE;

        /**
         * Compiled lookup-table bit depth, or {@link #UNSET_BITS} for automatic selection.
         */
        private int bits = UNSET_BITS;

        /**
         * Color space used to blend stops.
         */
        private InterpolationSpace space = InterpolationSpace.RGB;

        /**
         * Strategy used to interpolate adjacent stops.
         */
        private Interpolation interpolation = Interpolation.LINEAR;

        /**
         * Colors used outside the stop range.
         */
        private OutsideColors outside = OutsideColors.CLAMP;

        /**
         * Optional volume-rendering lighting settings.
         */
        private Lighting lighting;

        /**
         * Optional menu category.
         */
        private String category;

        /**
         * Search tags associated with the map.
         */
        private Set<String> tags = Set.of();

        /**
         * Whether the map is hidden from normal menus.
         */
        private boolean hidden;

        /**
         * Creates a builder for a new named map.
         *
         * @param name display name
         */
        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "Name cannot be null");
        }

        /**
         * Creates a builder populated from an existing map.
         *
         * @param map source map
         */
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

        /**
         * Sets the optional menu category.
         *
         * @param value category name
         * @return this builder
         */
        public Builder category(String value) {
            this.category = value;
            return this;
        }

        /**
         * Replaces the search tags from a set.
         *
         * @param values tag values
         * @return this builder
         */
        public Builder tags(Set<String> values) {
            this.tags = values;
            return this;
        }

        /**
         * Replaces the search tags from individual values.
         *
         * @param values tag values
         * @return this builder
         */
        public Builder tags(String... values) {
            this.tags = Set.of(values);
            return this;
        }

        /**
         * Sets whether the map is hidden from normal menus.
         *
         * @param value hidden flag
         * @return this builder
         */
        public Builder hidden(boolean value) {
            this.hidden = value;
            return this;
        }

        /**
         * Sets the display name.
         *
         * @param value display name
         * @return this builder
         */
        public Builder name(String value) {
            this.name = Objects.requireNonNull(value, "Name cannot be null");
            return this;
        }

        /**
         * Sets the semantic map type.
         *
         * @param value map type
         * @return this builder
         */
        public Builder type(ColorMapType value) {
            this.type = value;
            return this;
        }

        /**
         * Sets supported modality codes from individual values.
         *
         * @param codes modality codes
         * @return this builder
         */
        public Builder modalities(String... codes) {
            this.modalities = Set.of(codes);
            return this;
        }

        /**
         * Sets supported modality codes from a set.
         *
         * @param codes modality codes
         * @return this builder
         */
        public Builder modalities(Set<String> codes) {
            this.modalities = codes;
            return this;
        }

        /**
         * Sets whether the map is the default for its modalities.
         *
         * @param value default flag
         * @return this builder
         */
        public Builder defaultForModality(boolean value) {
            this.defaultForModality = value;
            return this;
        }

        /**
         * Sets the value domain.
         *
         * @param value map domain
         * @return this builder
         */
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

        /**
         * Sets the color interpolation space.
         *
         * @param value interpolation space
         * @return this builder
         */
        public Builder space(InterpolationSpace value) {
            this.space = value;
            return this;
        }

        /**
         * Sets the interpolation strategy.
         *
         * @param value interpolation strategy
         * @return this builder
         */
        public Builder interpolation(Interpolation value) {
            this.interpolation = value;
            return this;
        }

        /**
         * Adds a complete control point.
         *
         * @param value stop to add
         * @return this builder
         */
        public Builder stop(ColorStop value) {
            this.stops.add(Objects.requireNonNull(value, "Stop cannot be null"));
            return this;
        }

        /**
         * Adds an opaque color stop.
         *
         * @param position domain position
         * @param color    stop color
         * @return this builder
         */
        public Builder stop(double position, Color color) {
            return stop(ColorStop.of(position, color));
        }

        /**
         * Adds a stop defining both color and opacity.
         *
         * @param position domain position
         * @param color    stop color
         * @param alpha    opacity value
         * @return this builder
         */
        public Builder stop(double position, Color color, float alpha) {
            return stop(ColorStop.of(position, color, alpha));
        }

        /**
         * Adds an opacity-only stop.
         *
         * @param position domain position
         * @param alpha    opacity value
         * @return this builder
         */
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

        /**
         * Sets colors used outside the stop range.
         *
         * @param value outside-color policy
         * @return this builder
         */
        public Builder outside(OutsideColors value) {
            this.outside = value;
            return this;
        }

        /**
         * Sets optional volume-rendering lighting.
         *
         * @param value lighting settings, or null
         * @return this builder
         */
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

        /**
         * Validates the accumulated values and creates an immutable color map.
         *
         * @return configured color map
         */
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
