/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.shade.screw.engine;

import lombok.Data;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 生成构造工厂
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class EngineFactory implements Serializable {

    private EngineConfig engineConfig;

    public EngineFactory(EngineConfig configuration) {
        Assert.notNull(configuration, "EngineConfig can not be empty!");
        this.engineConfig = configuration;
    }

    private EngineFactory() {
    }

    /**
     * 获取配置的数据库类型实例
     *
     * @return {@link TemplateEngine} 数据库查询对象
     */
    public TemplateEngine newInstance() {
        try {
            //获取实现类
            Class<? extends TemplateEngine> query = this.engineConfig.getProduceType()
                    .getImplClass();
            //获取有参构造
            Constructor<? extends TemplateEngine> constructor = query
                    .getConstructor(EngineConfig.class);
            //实例化
            return constructor.newInstance(engineConfig);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

}
