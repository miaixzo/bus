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
package org.miaixz.bus.core.lang.annotation.resolve.synthesize;

/**
 * Annotation selector that selects one annotation from two candidates. Used in {@link SynthesizedAggregateAnnotation}
 * to filter the most appropriate annotation from a set of same-type annotations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface SynthesizedAnnotationSelector {

    /**
     * Selector that returns the annotation closer to the root; prefers the old annotation on equal distance.
     */
    SynthesizedAnnotationSelector NEAREST_AND_OLDEST_PRIORITY = new NearestAndOldestPrioritySelector();

    /**
     * Selector that returns the annotation closer to the root; prefers the new annotation on equal distance.
     */
    SynthesizedAnnotationSelector NEAREST_AND_NEWEST_PRIORITY = new NearestAndNewestPrioritySelector();

    /**
     * Selector that returns the annotation farther from the root; prefers the old annotation on equal distance.
     */
    SynthesizedAnnotationSelector FARTHEST_AND_OLDEST_PRIORITY = new FarthestAndOldestPrioritySelector();

    /**
     * Selector that returns the annotation farther from the root; prefers the new annotation on equal distance.
     */
    SynthesizedAnnotationSelector FARTHEST_AND_NEWEST_PRIORITY = new FarthestAndNewestPrioritySelector();

    /**
     * Compares two synthesized annotations and returns the more appropriate one.
     *
     * @param <T>           the synthesized annotation type
     * @param oldAnnotation the existing annotation; must not be {@code null}
     * @param newAnnotation the new annotation; must not be {@code null}
     * @return the selected annotation
     */
    <T extends SynthesizedAnnotation> T choose(T oldAnnotation, T newAnnotation);

    /**
     * Selector that returns the annotation closer to the root; prefers the old annotation on equal distance.
     */
    class NearestAndOldestPrioritySelector implements SynthesizedAnnotationSelector {

        /**
         * Returns {@code newAnnotation} if it is closer to the root than {@code oldAnnotation}; otherwise returns
         * {@code oldAnnotation}.
         *
         * @param <T>           the synthesized annotation type
         * @param oldAnnotation the existing annotation
         * @param newAnnotation the new annotation
         * @return the selected annotation
         */
        @Override
        public <T extends SynthesizedAnnotation> T choose(final T oldAnnotation, final T newAnnotation) {
            return Hierarchical.Selector.NEAREST_AND_OLDEST_PRIORITY.choose(oldAnnotation, newAnnotation);
        }
    }

    /**
     * Selector that returns the annotation closer to the root; prefers the new annotation on equal distance.
     */
    class NearestAndNewestPrioritySelector implements SynthesizedAnnotationSelector {

        /**
         * Returns {@code newAnnotation} if it is at least as close to the root as {@code oldAnnotation}; otherwise
         * returns {@code oldAnnotation}.
         *
         * @param <T>           the synthesized annotation type
         * @param oldAnnotation the existing annotation
         * @param newAnnotation the new annotation
         * @return the selected annotation
         */
        @Override
        public <T extends SynthesizedAnnotation> T choose(final T oldAnnotation, final T newAnnotation) {
            return Hierarchical.Selector.NEAREST_AND_NEWEST_PRIORITY.choose(oldAnnotation, newAnnotation);
        }
    }

    /**
     * Selector that returns the annotation farther from the root; prefers the old annotation on equal distance.
     */
    class FarthestAndOldestPrioritySelector implements SynthesizedAnnotationSelector {

        /**
         * Returns {@code newAnnotation} if it is farther from the root than {@code oldAnnotation}; otherwise returns
         * {@code oldAnnotation}.
         *
         * @param <T>           the synthesized annotation type
         * @param oldAnnotation the existing annotation
         * @param newAnnotation the new annotation
         * @return the selected annotation
         */
        @Override
        public <T extends SynthesizedAnnotation> T choose(final T oldAnnotation, final T newAnnotation) {
            return Hierarchical.Selector.FARTHEST_AND_OLDEST_PRIORITY.choose(oldAnnotation, newAnnotation);
        }
    }

    /**
     * Selector that returns the annotation farther from the root; prefers the new annotation on equal distance.
     */
    class FarthestAndNewestPrioritySelector implements SynthesizedAnnotationSelector {

        /**
         * Returns {@code newAnnotation} if it is at least as far from the root as {@code oldAnnotation}; otherwise
         * returns {@code oldAnnotation}.
         *
         * @param <T>           the synthesized annotation type
         * @param oldAnnotation the existing annotation
         * @param newAnnotation the new annotation
         * @return the selected annotation
         */
        @Override
        public <T extends SynthesizedAnnotation> T choose(final T oldAnnotation, final T newAnnotation) {
            return Hierarchical.Selector.FARTHEST_AND_NEWEST_PRIORITY.choose(oldAnnotation, newAnnotation);
        }
    }

}
