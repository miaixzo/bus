package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 用于标识为系统内置的注解实现方式
 * <p>
 * 这个类的实现并不重要，只是为了尽可能降低 annotation 对于实现的依赖。
 * 注意：如果不是系统内置的注解，请勿使用这个标识，否则无法找到对应实现。
 * 在 hibernate-validator 中使用的是数组，然后默认指定 {}，但是缺陷也很明显，
 * 明明是数组，实现却只能是一个
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BuiltInStrategy implements StrategyProvider {

    @Override
    public Object build(Object object, Context context) {
        return null;
    }

}
