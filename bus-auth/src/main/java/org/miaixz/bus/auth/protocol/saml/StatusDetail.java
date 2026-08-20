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
package org.miaixz.bus.auth.protocol.saml;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Preserves the ordered XML wildcard children of SAML protocol {@code StatusDetailType}.
 * <p>
 * Every byte array is one complete namespace-aware element serialized by the secure SAML XML codec. The record owns
 * defensive copies and never accepts parser objects or mutable DOM nodes.
 * </p>
 *
 * @param elements ordered complete XML element serializations
 * @author Kimi Liu
 */
public record StatusDetail(List<byte[]> elements) {

    /**
     * Deep-copies all wildcard elements.
     *
     * @throws IllegalArgumentException if the list or an element is {@code null}
     */
    public StatusDetail {
        Assert.notNull(elements, "SAML StatusDetail element list must not be null");
        final List<byte[]> copy = new ArrayList<>(elements.size());
        for (byte[] element : elements) {
            copy.add(Assert.notNull(element, "SAML StatusDetail element must not be null").clone());
        }
        elements = List.copyOf(copy);
    }

    /**
     * Returns deep copies of the ordered wildcard element serializations.
     *
     * @return immutable list containing newly copied byte arrays
     */
    @Override
    public List<byte[]> elements() {
        final List<byte[]> copy = new ArrayList<>(elements.size());
        for (byte[] element : elements) {
            copy.add(element.clone());
        }
        return List.copyOf(copy);
    }

}
