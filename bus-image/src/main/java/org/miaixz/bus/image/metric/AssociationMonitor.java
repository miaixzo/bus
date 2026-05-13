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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.metric.pdu.AAssociateRJ;

/**
 * Defines the AssociationMonitor contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface AssociationMonitor {

    /**
     * Executes the on association established operation.
     *
     * @param as the as.
     */
    void onAssociationEstablished(Association as);

    /**
     * Executes the on association failed operation.
     *
     * @param as the as.
     * @param e  the e.
     */
    void onAssociationFailed(Association as, Throwable e);

    /**
     * Executes the on association rejected operation.
     *
     * @param as   the as.
     * @param aarj the aarj.
     */
    void onAssociationRejected(Association as, AAssociateRJ aarj);

    /**
     * Executes the on association accepted operation.
     *
     * @param as the as.
     */
    void onAssociationAccepted(Association as);

}
