/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper;

import java.io.Serializable;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.miaixz.bus.mapper.binding.BasicMapper;
import org.miaixz.bus.mapper.provider.EntityProvider;

/**
 * 自定义 Mapper 接口示例，基于主键自增重写了 insert 方法，主要用于展示用法。
 * <p>
 * 在使用 Oracle 或其他数据库时，可通过 @SelectKey 注解自定义主键生成逻辑。
 * </p>
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Mapper<T, I extends Serializable> extends BasicMapper<T, I>, Marker {

    /**
     * 保存实体，假设主键自增且名称为 id。
     * <p>
     * 此方法为示例，可在自定义接口中以相同方式覆盖父接口配置。
     * </p>
     *
     * @param entity 实体对象
     * @param <S>    实体类的子类型
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @InsertProvider(type = EntityProvider.class, method = "insert")
    <S extends T> int insert(S entity);

    /**
     * 保存实体中非空字段，假设主键自增且名称为 id。
     * <p>
     * 此方法为示例，可在自定义接口中以相同方式覆盖父接口配置。
     * </p>
     *
     * @param entity 实体对象
     * @param <S>    实体类的子类型
     * @return 1 表示成功，0 表示失败
     */
    @Override
    @Lang(Caching.class)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @InsertProvider(type = EntityProvider.class, method = "insertSelective")
    <S extends T> int insertSelective(S entity);

}