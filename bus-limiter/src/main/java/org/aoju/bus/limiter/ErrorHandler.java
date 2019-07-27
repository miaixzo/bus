package org.aoju.bus.limiter;


import org.aoju.bus.limiter.execute.LimiterExecutionContext;

/**
 * 当limiter由于其他原因不能正常工作(如Redis宕机)
 * 该接口将会被调用，如果你不希望这些异常影响接口提供服务
 * return true，这样将会跳过该limiter，实际上，更好的
 * limiter 降级策略应该由limiter本身实现，这里只是一个简单的替代方案
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface ErrorHandler {

    /**
     * @param throwable
     * @return
     */
    boolean resolve(Throwable throwable, LimiterExecutionContext executionContext);
}
