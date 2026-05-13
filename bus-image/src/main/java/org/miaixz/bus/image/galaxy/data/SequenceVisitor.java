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
package org.miaixz.bus.image.galaxy.data;

/**
 * Defines the SequenceVisitor contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface SequenceVisitor extends Visitor {

    /**
     * Executes the start item operation.
     *
     * @param sqTag     the sq tag.
     * @param itemIndex the item index.
     */
    void startItem(int sqTag, int itemIndex);

    /**
     * Executes the end item operation.
     */
    void endItem();

    /**
     * Executes the start sequence operation.
     *
     * @param sqTag the sq tag.
     */
    default void startSequence(int sqTag) {
    }

    /**
     * Executes the end sequence operation.
     */
    default void endSequence() {
    }

}
