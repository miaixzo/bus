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
package org.miaixz.bus.errors.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Consumer;

import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDescriptor;
import org.miaixz.bus.errors.FailureFacts;
import org.miaixz.bus.errors.spi.ErrorClassifier;

/**
 * Runs registered classifiers in their declared order and isolates extension failures.
 *
 * @author Kimi Liu
 */
public final class ClassifierRegistry {

    /**
     * Immutable classifier chain in descending precedence.
     */
    private final List<ErrorClassifier> classifiers;

    /**
     * Creates an immutable classifier chain.
     *
     * @param classifiers classifiers ordered by precedence
     */
    public ClassifierRegistry(List<ErrorClassifier> classifiers) {
        this.classifiers = List.copyOf(classifiers);
        var identities = Collections.newSetFromMap(new IdentityHashMap<ErrorClassifier, Boolean>());
        for (var classifier : this.classifiers) {
            if (!identities.add(classifier)) {
                throw new IllegalArgumentException("A classifier instance can only be registered once");
            }
        }
    }

    /**
     * Selects the first successful classification, leaving the boundary's fallback available.
     *
     * @param context     execution boundary
     * @param facts       verified failure facts
     * @param failure     normalized failure, possibly {@code null}
     * @param diagnostics recipient of isolated classifier failures
     * @return classified error, or {@code null} if none applies
     */
    public ErrorDescriptor classify(
            ErrorContext context,
            FailureFacts facts,
            Throwable failure,
            Consumer<Throwable> diagnostics) {
        for (ErrorClassifier classifier : this.classifiers) {
            try {
                ErrorDescriptor result = classifier.classify(context, facts, failure);
                if (result != null) {
                    return result;
                }
            } catch (RuntimeException | Error secondary) {
                ThrowableNormalizer.rethrowFatal(secondary);
                diagnostics.accept(secondary);
            }
        }
        return null;
    }

}
