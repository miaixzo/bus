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

import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;

/**
 * Listener notified when DNS runtime snapshots are accepted, rejected, or rolled back.
 *
 * @author Kimi Liu
 */
public interface DnsSnapshotListener {

    /**
     * Handles a snapshot that was compiled and installed as the active runtime index.
     *
     * @param snapshot accepted snapshot
     */
    void onAccepted(DnsSnapshot snapshot);

    /**
     * Handles a snapshot that failed validation or compilation.
     *
     * @param snapshot rejected snapshot, or {@code null} when the caller supplied no snapshot
     * @param cause    rejection cause
     */
    void onRejected(DnsSnapshot snapshot, Throwable cause);

    /**
     * Handles preservation of the previously active snapshot after a replacement failure.
     *
     * @param activeSnapshot snapshot that remains active
     * @param cause          failure that prevented replacement
     */
    void onRolledBack(DnsSnapshot activeSnapshot, Throwable cause);

}
