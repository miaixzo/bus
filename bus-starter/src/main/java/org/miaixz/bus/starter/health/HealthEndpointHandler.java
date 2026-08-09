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
package org.miaixz.bus.starter.health;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.UncheckedException;
import org.miaixz.bus.logger.Logger;

/**
 * Handles dynamically registered health queries and application availability state-change endpoints.
 *
 * @author Kimi Liu
 */
public class HealthEndpointHandler extends Controller {

    /**
     * Service used to query health data and change application availability states.
     */
    private final HealthService service;

    /**
     * Creates a handler backed by the health and availability service.
     *
     * @param service service used to query health data and change availability states
     */
    public HealthEndpointHandler(HealthService service) {
        this.service = service;
    }

    /**
     * Returns current application availability and any explicitly requested system details using the standard message
     * contract.
     *
     * @param tid optional comma-separated detail identifiers
     * @return success message when the application is available, error code {@code 400} for invalid details, or error
     *         code {@code 503} when the application or requested system details are unavailable
     */
    @ResponseBody
    public Message<?> healthz(@RequestParam(value = "tid", required = false) String tid) {
        Logger.debug(true, "Starter", "Health status requested: details={}", tid);
        try {
            Map<String, Object> body = this.service.healthz(tid);
            if ("UP".equals(body.get("status"))) {
                return write(body);
            }
            return Message.<Map<String, Object>>builder().errcode(ErrorCode._503.getKey())
                    .errmsg(ErrorCode._503.getValue()).data(body).build();
        } catch (UncheckedException e) {
            if (e instanceof InternalException) {
                Logger.error(false, "Starter", "System health details unavailable", e);
            }
            return write(e.getErrcode(), e.getErrmsg());
        }
    }

    /**
     * Marks application liveness as broken.
     *
     * @return success message containing the new liveness state and change timestamp
     */
    @ResponseBody
    public Message<?> broken() {
        Logger.debug(true, "Starter", "Liveness state change requested: state=BROKEN");
        return write(this.service.broken());
    }

    /**
     * Marks application liveness as correct.
     *
     * @return success message containing the new liveness state and change timestamp
     */
    @ResponseBody
    public Message<?> correct() {
        Logger.debug(true, "Starter", "Liveness state change requested: state=CORRECT");
        return write(this.service.correct());
    }

    /**
     * Marks application readiness as accepting traffic.
     *
     * @return success message containing the new readiness state and change timestamp
     */
    @ResponseBody
    public Message<?> accept() {
        Logger.debug(true, "Starter", "Readiness state change requested: state=ACCEPTING_TRAFFIC");
        return write(this.service.accept());
    }

    /**
     * Marks application readiness as refusing traffic.
     *
     * @return success message containing the new readiness state and change timestamp
     */
    @ResponseBody
    public Message<?> refuse() {
        Logger.debug(true, "Starter", "Readiness state change requested: state=REFUSING_TRAFFIC");
        return write(this.service.refuse());
    }

}
