/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.health;

import org.miaixz.bus.core.basic.spring.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for application health checks and state management.
 * <p>
 * This controller provides endpoints to query the application's health status and to manually control its liveness and
 * readiness states, which is particularly useful in containerized environments like Kubernetes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HealthController extends Controller {

    /**
     * The underlying service that handles health logic.
     */
    public final HealthService service;

    /**
     * Constructs the controller and injects the {@link HealthService}.
     *
     * @param service The health provider service.
     */
    public HealthController(HealthService service) {
        this.service = service;
    }

    /**
     * Retrieves system health status information.
     *
     * @param tid An optional request parameter specifying the type of information to retrieve (e.g., "liveness", "cpu",
     *            "memory").
     * @return The operation result, typically a JSON object with health data.
     */
    @ResponseBody
    @RequestMapping(value = "/healthz", method = { RequestMethod.POST, RequestMethod.GET })
    public Object healthz(@RequestParam(value = "tid", required = false) String tid) {
        return write(service.healthz(tid));
    }

    /**
     * Sets the liveness state to BROKEN, causing Kubernetes to kill and restart the pod.
     *
     * @return The operation result.
     */
    @ResponseBody
    @RequestMapping(value = "/broken", method = { RequestMethod.POST, RequestMethod.GET })
    public Object broken() {
        return write(service.broken());
    }

    /**
     * Sets the liveness state to CORRECT, indicating the pod is running normally.
     *
     * @return The operation result.
     */
    @ResponseBody
    @RequestMapping(value = "/correct", method = { RequestMethod.POST, RequestMethod.GET })
    public Object correct() {
        return write(service.correct());
    }

    /**
     * Sets the readiness state to ACCEPTING_TRAFFIC, allowing Kubernetes to route requests to this pod.
     *
     * @return The operation result.
     */
    @ResponseBody
    @RequestMapping(value = "/accept", method = { RequestMethod.POST, RequestMethod.GET })
    public Object accept() {
        return write(service.accept());
    }

    /**
     * Sets the readiness state to REFUSING_TRAFFIC, preventing Kubernetes from routing external requests to this pod.
     *
     * @return The operation result.
     */
    @ResponseBody
    @RequestMapping(value = "/refuse", method = { RequestMethod.POST, RequestMethod.GET })
    public Object refuse() {
        return write(service.refuse());
    }

}
