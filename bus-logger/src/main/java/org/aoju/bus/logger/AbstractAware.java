package org.aoju.bus.logger;

/**
 * 抽象定位感知日志实现<br>
 * 此抽象类实现了LocationAwareLog接口，从而支持完全限定类名(Fully Qualified Class Name)，用于纠正定位错误行号
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractAware extends AbstractLog implements LocationAware {

    private static final long serialVersionUID = -5529674971846264145L;

}
