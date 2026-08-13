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
package org.miaixz.bus.auth.vendor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.Principal;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.fabric.Options;

/**
 * User information after successful authorization. The completeness of the data obtained varies depending on the
 * authorization platform.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class VendorIdentity {

    /**
     * The unique ID of the user in the third-party system. When integrating this component, the user can be uniquely
     * identified by combining uuid + source.
     */
    private String uuid;
    /**
     * User's username.
     */
    private String username;
    /**
     * User's nickname.
     */
    private String nickname;
    /**
     * User's avatar URL.
     */
    private String avatar;
    /**
     * User's blog or personal website URL.
     */
    private String blog;
    /**
     * User's company.
     */
    private String company;
    /**
     * User's location.
     */
    private String location;
    /**
     * User's email address.
     */
    private String email;
    /**
     * User's remarks (personal introduction from various platforms).
     */
    private String remark;
    /**
     * User's gender.
     */
    private Gender gender;
    /**
     * User's source platform.
     */
    private String source;
    /**
     * User's authorization token information.
     */
    private VendorTokenSet token;
    /**
     * Raw user information returned by the third-party platform.
     */
    private String rawJson;
    /**
     * WeChat Official Account - available for web authorization login. WeChat adds a snapshot page logic for web
     * authorization login, where the uid, oid, avatar, and nickname obtained are virtual information.
     */
    private boolean snapshotUser;

    /**
     * Constructs a new {@code VendorIdentity} instance.
     */
    public VendorIdentity() {
        // No initialization required.
    }

    /**
     * Adds a non-null scalar claim to a conversion-local map.
     *
     * @param values conversion-local mutable claim map
     * @param name   claim name
     * @param value  optional scalar claim value
     */
    private static void put(final Map<String, Object> values, final String name, final Object value) {
        if (value != null) {
            values.put(name, value);
        }
    }

    /**
     * Converts this client DTO to an immutable protocol-neutral claim snapshot.
     *
     * <p>
     * The vendor token and raw response are intentionally excluded because they may contain credentials or
     * provider-specific wire data. Every invocation creates a new snapshot without mutable state shared with this DTO.
     * </p>
     *
     * @return immutable claims containing only the frozen identity projection
     */
    public Claims toClaims() {
        final Map<String, Object> values = new LinkedHashMap<>();
        put(values, "username", username);
        put(values, "nickname", nickname);
        put(values, "avatar", avatar);
        put(values, "blog", blog);
        put(values, "company", company);
        put(values, "location", location);
        put(values, "email", email);
        put(values, "remark", remark);
        put(values, "gender", gender);
        put(values, "source", source);
        values.put("snapshotUser", snapshotUser);
        put(values, "iss", source);
        put(values, "sub", uuid);
        return Claims.from(values);
    }

    /**
     * Converts this DTO to an immutable authentication subject.
     *
     * @return subject identified by {@link #uuid} with empty Fabric options
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if {@link #uuid} is null or blank
     */
    public Subject toSubject() {
        return new Subject(uuid, toClaims(), Options.empty());
    }

    /**
     * Converts this DTO to an immutable invocation principal.
     *
     * @return subject principal with no client identifier and no granted scopes
     * @throws org.miaixz.bus.core.lang.exception.ValidateException if {@link #uuid} is null or blank
     */
    public Principal toPrincipal() {
        return new Principal(uuid, null, Set.of(), toClaims());
    }

}
