/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.goalie.secure;

import org.aoju.bus.core.lang.exception.InvalidParamsException;
import org.aoju.bus.goalie.ApiContext;
import org.aoju.bus.goalie.manual.ApiHandlerAdapter;
import org.aoju.bus.goalie.manual.ApiParam;
import org.aoju.bus.goalie.manual.Visitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负责监控的拦截器
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8++
 */
public class MonitorHandler extends ApiHandlerAdapter implements Visitor {

    private static final String START_TIME = MonitorHandler.class.getSimpleName() + "_START_TIME";

    private volatile ExecutorService executorService = null;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object serviceObj, Object argu) {
        this.in(request, serviceObj, argu);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object serviceObj,
                                Object argu, Object result, Exception e) {
        this.out(request, serviceObj, argu, result, e);
    }

    @Override
    public void in(HttpServletRequest request, Object serviceObj, Object argu) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void out(final HttpServletRequest request, Object serviceObj, final Object argu, final Object result, final Exception e) {
        if (e instanceof InvalidParamsException) {
            return;
        }
        if (executorService == null) {
            synchronized (MonitorHandler.class) {
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(ApiContext.getConfig().getMonitorExecutorSize());
                }
            }
        }
        final long endTime = System.currentTimeMillis();
        final Long startTime = (Long) request.getAttribute(START_TIME);
        final MonitorStore store = this.getMonitorStore();
        final ApiParam param = ApiContext.getApiParam();
        if (param != null) {
            final ApiParam input = param.clone();
            executorService.execute(() -> store.stat(input, startTime, endTime, argu, result, e));
        }
    }

    public MonitorStore getMonitorStore() {
        return ApiContext.getConfig().getMonitorStore();
    }

}
