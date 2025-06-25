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
package org.miaixz.bus.base.spring;

import org.miaixz.bus.base.service.BaseService;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.magic.annotation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基础请求封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaseController<T, Service extends BaseService<T>> extends Controller {

    @Autowired
    protected Service service;

    /**
     * 通用:添加数据
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Object add(T entity) {
        T t = (T) service.insertSelective(entity);
        if (ObjectKit.isNotEmpty(t)) {
            return write(t);
        }
        return write(ErrorCode._100513);
    }

    /**
     * 通用:逻辑删除
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public Object remove(T entity) {
        long total = service.remove(entity);
        if (total >= 0) {
            return write(MapKit.of("total", total));
        }
        return write(ErrorCode._100513);
    }

    /**
     * 通用:物理删除
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Object delete(T entity) {
        long total = service.delete(entity);
        if (total >= 0) {
            return write(MapKit.of("total", total));
        }
        return write(ErrorCode._100513);
    }

    /**
     * 通用:主键更新
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Object update(T entity) {
        T t = (T) service.updateSelective(entity);
        if (ObjectKit.isNotEmpty(t)) {
            return write(t);
        }
        return write(ErrorCode._100513);
    }

    /**
     * 通用:数据主键查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public Object get(T entity) {
        return write(service.selectOne(entity));
    }

    /**
     * 通用:数据条件查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Object list(T entity) {
        return write(service.selectList(entity));
    }

    /**
     * 通用:数据分页查询
     *
     * @param entity 对象参数
     * @return 操作结果
     */
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Object page(@Valid({ "pageSize", "pageNo" }) T entity) {
        return write(service.page(entity));
    }

}
