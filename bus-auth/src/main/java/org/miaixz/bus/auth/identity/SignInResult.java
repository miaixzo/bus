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
package org.miaixz.bus.auth.identity;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Evidence;
import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.core.lang.Assert;

/**
 * Carries the framework result of a fully completed Source sign-in.
 * <p>
 * The result contains only the authenticated Principal, active Session, and verified evidence. Protocol tokens,
 * authorization codes, Vendor payloads, callback data, and private adapter values are excluded.
 * </p>
 *
 * @param principal authenticated provider-neutral Principal
 * @param session   framework Session created for the Principal
 * @param evidence  immutable verified authentication evidence
 * @author Kimi Liu
 */
public record SignInResult(Principal principal, Session session, List<Evidence> evidence) {

    /**
     * Creates an immutable completed sign-in result.
     *
     * @param principal authenticated Principal
     * @param session   created framework Session
     * @param evidence  verified evidence supporting the authentication
     * @throws IllegalArgumentException if a component or evidence entry is {@code null}
     */
    public SignInResult {
        Assert.notNull(principal, "Sign-in Principal must not be null");
        Assert.notNull(session, "Sign-in Session must not be null");
        Assert.notNull(evidence, "Sign-in evidence list must not be null");
        final List<Evidence> copy = new ArrayList<>(evidence.size());
        for (Evidence item : evidence) {
            copy.add(Assert.notNull(item, "Sign-in evidence entry must not be null"));
        }
        evidence = List.copyOf(copy);
    }

}
