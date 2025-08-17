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
package org.miaixz.bus.cache.reader;

import org.miaixz.bus.cache.Builder;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.cache.support.PreventObjects;
import org.miaixz.bus.core.lang.annotation.Singleton;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * 单缓存读取器
 * <p>
 * 用于处理单键缓存操作，支持缓存命中、未命中和防击穿场景。 提供缓存命中率统计功能，并能够根据配置决定是否写入缓存。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Singleton
public class SingleCacheReader extends AbstractReader {

    /**
     * 执行缓存读取操作
     *
     * @param annoHolder   注解持有者，包含缓存相关的注解信息
     * @param methodHolder 方法持有者，包含方法相关的信息
     * @param baseInvoker  代理调用链，用于执行原始方法
     * @param needWrite    是否需要写入缓存
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Override
    public Object read(AnnoHolder annoHolder, MethodHolder methodHolder, ProxyChain baseInvoker, boolean needWrite)
            throws Throwable {
        String key = Builder.generateSingleKey(annoHolder, baseInvoker.getArguments());
        Object readResult = this.manage.readSingle(annoHolder.getCache(), key);
        doRecord(readResult, key, annoHolder);
        // 命中
        if (null != readResult) {
            // 是防击穿对象
            if (PreventObjects.isPrevent(readResult)) {
                return null;
            }
            return readResult;
        }
        Object invokeResult = doLogInvoke(baseInvoker::proceed);
        if (null != invokeResult && null == methodHolder.getInnerReturnType()) {
            methodHolder.setInnerReturnType(invokeResult.getClass());
        }
        if (!needWrite) {
            return invokeResult;
        }
        if (null != invokeResult) {
            this.manage.writeSingle(annoHolder.getCache(), key, invokeResult, annoHolder.getExpire());
            return invokeResult;
        }
        if (this.context.isPreventOn()) {
            this.manage.writeSingle(annoHolder.getCache(), key, PreventObjects.getPreventObject(),
                    annoHolder.getExpire());
        }
        return null;
    }

    /**
     * 记录缓存命中率
     *
     * @param result     缓存读取结果
     * @param key        缓存键
     * @param annoHolder 注解持有者
     */
    private void doRecord(Object result, String key, AnnoHolder annoHolder) {
        Logger.info("single cache hit rate: {}/1, key: {}", null == result ? 0 : 1, key);
        if (null != this.metrics) {
            String pattern = Builder.generatePattern(annoHolder);
            if (null != result) {
                this.metrics.hitIncr(pattern, 1);
            }
            this.metrics.reqIncr(pattern, 1);
        }
    }

}