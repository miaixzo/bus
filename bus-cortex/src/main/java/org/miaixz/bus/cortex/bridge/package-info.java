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
/**
 * Gateway sync bridge that converts registry change events into HTTP calls and forwards them asynchronously to the
 * Vortex gateway.
 * <p>
 * {@code SyncEvent} is the event envelope: it carries a REGISTER or DEREGISTER action, the target namespace, the asset
 * payload and a monotonic sequence number used to preserve ordering. {@code ApiAssetsConverter} maps an
 * {@code ApiDefinition} combined with a live {@code Instance} into the gateway-ready {@code Assets} format required by
 * the sync endpoint, using simple setter calls with no reflection. {@code VortexBridge} is the background worker that
 * drains a bounded {@code LinkedBlockingQueue} of up to 10 000 queued events, HTTP-POSTs each one to the Vortex
 * internal sync endpoint, and retries up to a configurable maximum before discarding a failed event.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.bridge;
