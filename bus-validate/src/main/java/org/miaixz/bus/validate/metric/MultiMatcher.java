/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.Registry;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Multi;

import java.util.ArrayList;
import java.util.List;

/**
 * 多规则匹配校验
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultiMatcher implements Matcher<Object, Multi> {

    @Override
    public boolean on(Object object, Multi multi, Context context) {
        List<Matcher> validators = new ArrayList<>();
        for (String validatorName : multi.value()) {
            if (!Registry.getInstance().contains(validatorName)) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + validatorName);
            }
            validators.add((Matcher) Registry.getInstance().require(validatorName));
        }
        for (Class<? extends Matcher> clazz : multi.classes()) {
            if (!Registry.getInstance().contains(clazz.getSimpleName())) {
                throw new NoSuchException("尝试使用一个不存在的校验器：" + clazz.getName());
            }
            validators.add((Matcher) Registry.getInstance().require(clazz.getSimpleName()));
        }
        for (Matcher validator : validators) {
            if (!validator.on(object, null, context)) {
                return false;
            }
        }
        return true;
    }

}
