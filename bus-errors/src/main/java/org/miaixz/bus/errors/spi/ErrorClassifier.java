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
package org.miaixz.bus.errors.spi;

import org.miaixz.bus.errors.ErrorContext;
import org.miaixz.bus.errors.ErrorDescriptor;
import org.miaixz.bus.errors.FailureFacts;

/**
 * Classifies a failure using structured data without performing I/O.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface ErrorClassifier {

    /**
     * Returns a classification or {@code null} to continue with the next classifier.
     *
     * @param context current execution boundary
     * @param facts   facts verified by the caller
     * @param failure normalized primary failure, possibly {@code null}
     * @return classification, or {@code null} when the classifier does not apply
     */
    ErrorDescriptor classify(ErrorContext context, FailureFacts facts, Throwable failure);

}
