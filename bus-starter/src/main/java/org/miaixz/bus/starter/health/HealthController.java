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
import org.springframework.web.bind.annotation.*;

/**
 * 健康检查
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@RestController
public class HealthController extends Controller {

    public HealthProviderService service;

    /**
     * 构造函数，注入 HealthProviderService。
     *
     * @param service 健康状态服务
     */
    public HealthController(HealthProviderService service) {
        this.service = service;
    }

    /**
     * 获取系统健康状态信息
     *
     * @param tid 参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/healthz", method = { RequestMethod.POST, RequestMethod.GET })
    public Object healthz(@RequestParam(value = "tid", required = false) String tid) {
        return write(service.healthz(tid));
    }

    /**
     * 将存活状态改为 BROKEN，导致 Kubernetes 杀死并重启 pod。
     * 
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/broken", method = { RequestMethod.POST, RequestMethod.GET })
    public Object broken() {
        return write(service.broken());
    }

    /**
     * 将存活状态改为 CORRECT，表示 pod 正常运行。
     * 
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/correct", method = { RequestMethod.POST, RequestMethod.GET })
    public Object correct() {
        return write(service.correct());
    }

    /**
     * 将就绪状态改为 ACCEPTING_TRAFFIC，Kubernetes 将请求转发到此 pod。
     * 
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/accept", method = { RequestMethod.POST, RequestMethod.GET })
    public Object accept() {
        return write(service.accept());
    }

    /**
     * 将就绪状态改为 REFUSING_TRAFFIC，Kubernetes 拒绝外部请求。
     *
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/refuse", method = { RequestMethod.POST, RequestMethod.GET })
    public Object refuse() {
        return write(service.refuse());
    }

}
