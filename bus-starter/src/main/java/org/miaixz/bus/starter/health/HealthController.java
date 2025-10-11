/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
