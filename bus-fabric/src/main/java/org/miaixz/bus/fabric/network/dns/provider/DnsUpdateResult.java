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
package org.miaixz.bus.fabric.network.dns.provider;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;

/**
 * Result returned by an external Dynamic Update sink.
 *
 * @author Kimi Liu
 */
public class DnsUpdateResult {

    /**
     * DNS RCODE for a successful update.
     */
    public static final int NOERROR = 0;

    /**
     * DNS RCODE for a server-side update failure.
     */
    public static final int SERVFAIL = 2;

    /**
     * DNS RCODE for a refused update.
     */
    public static final int REFUSED = 5;

    /**
     * DNS response code returned to the client.
     */
    private final int responseCode;

    /**
     * Replacement snapshot returned by the external project, or {@code null}.
     */
    private final DnsSnapshot snapshot;

    /**
     * Creates an update result.
     *
     * @param responseCode DNS response code returned to the client
     * @param snapshot     replacement snapshot returned by the external project, or {@code null}
     */
    public DnsUpdateResult(final int responseCode, final DnsSnapshot snapshot) {
        if (responseCode < 0 || responseCode > 15) {
            throw new ValidateException("DNS update response code must be a 4-bit value");
        }
        this.responseCode = responseCode;
        this.snapshot = snapshot;
    }

    /**
     * Creates an accepted update result.
     *
     * @param snapshot replacement snapshot returned by the external project
     * @return accepted update result
     */
    public static DnsUpdateResult accepted(final DnsSnapshot snapshot) {
        return new DnsUpdateResult(NOERROR, snapshot);
    }

    /**
     * Creates a refused update result.
     *
     * @return refused update result
     */
    public static DnsUpdateResult refused() {
        return new DnsUpdateResult(REFUSED, null);
    }

    /**
     * Creates a failed update result.
     *
     * @return failed update result
     */
    public static DnsUpdateResult failed() {
        return new DnsUpdateResult(SERVFAIL, null);
    }

    /**
     * Returns the DNS response code.
     *
     * @return DNS RCODE
     */
    public int responseCode() {
        return responseCode;
    }

    /**
     * Returns the replacement snapshot.
     *
     * @return replacement snapshot, or {@code null}
     */
    public DnsSnapshot snapshot() {
        return snapshot;
    }

}
