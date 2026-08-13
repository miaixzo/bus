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

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.core.basic.entity.Message;

/**
 * Client-side contract implemented by every third-party authentication vendor.
 *
 * <p>
 * Each invocation carries the root immutable {@link Context}. Vendor operations return the Bus client {@link Message}
 * algebra; protocol server operations use their protocol-specific response types instead. The root provider contract
 * supplies the stable type from {@code descriptor().id()}.
 * </p>
 *
 * @author Kimi Liu
 */
public interface VendorProvider extends org.miaixz.bus.auth.Provider {

    /**
     * Builds the vendor authorization request URL.
     *
     * @param context immutable operation context
     * @param state   CSRF correlation value to send to the vendor
     * @return authorization URL result, or the standard unsupported-vendor result
     */
    default Message<String> build(Context context, String state) {
        return Message.failure(VendorErrors._110000);
    }

    /**
     * Completes the vendor login flow from an inbound authorization callback.
     *
     * @param context  immutable operation context
     * @param callback immutable root callback snapshot
     * @return authenticated vendor identity result, or the standard unsupported-vendor result
     */
    default Message<VendorIdentity> authorize(Context context, Callback.Inbound callback) {
        return Message.failure(VendorErrors._110000);
    }

    /**
     * Exchanges an inbound authorization callback for vendor tokens.
     *
     * @param context  immutable operation context
     * @param callback immutable root callback snapshot
     * @return vendor token result, or the standard unsupported-vendor result
     */
    default Message<VendorTokenSet> token(Context context, Callback.Inbound callback) {
        return Message.failure(VendorErrors._110000);
    }

    /**
     * Resolves a vendor identity from an issued token set.
     *
     * @param context immutable operation context
     * @param token   vendor token set; implementations must not log its sensitive fields
     * @return vendor identity result, or the standard unsupported-vendor result
     */
    default Message<VendorIdentity> userInfo(Context context, VendorTokenSet token) {
        return Message.failure(VendorErrors._110000);
    }

    /**
     * Refreshes an issued vendor token set.
     *
     * @param context immutable operation context
     * @param token   vendor token set; implementations must not log its sensitive fields
     * @return refreshed vendor token result, or the standard unsupported-vendor result
     */
    default Message<VendorTokenSet> refresh(Context context, VendorTokenSet token) {
        return Message.failure(VendorErrors._110000);
    }

    /**
     * Revokes an issued vendor token set.
     *
     * @param context immutable operation context
     * @param token   vendor token set; implementations must not log its sensitive fields
     * @return empty revocation result, or the standard unsupported-vendor result
     */
    default Message<Void> revoke(Context context, VendorTokenSet token) {
        return Message.failure(VendorErrors._110000);
    }

}
