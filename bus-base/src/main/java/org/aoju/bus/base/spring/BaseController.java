/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.base.spring;

import io.swagger.annotations.ApiOperation;
import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 基础请求封装
 *
 * @author Kimi Liu
 * @version 3.1.0
 * @since JDK 1.8
 */
public class BaseController<Service extends BaseService<T>, T> extends Controller {

    @Autowired
    protected Service service;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "通用:添加数据", httpMethod = "POST")
    @ResponseBody
    public String add(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.insertSelective(entity));
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @ApiOperation(value = "通用:主键删除", httpMethod = "POST")
    @ResponseBody
    public String remove(T entity) {
        service.deleteById(entity);
        return write("0,删除成功", null);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation(value = "通用:主键更新", httpMethod = "POST")
    @ResponseBody
    public String update(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.updateSelectiveById(entity));
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据主键查询", httpMethod = "GET")
    @ResponseBody
    public String get(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.selectById(entity));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据条件查询", httpMethod = "GET")
    @ResponseBody
    public String list(T entity) {
        return write(ErrorCode.EM_SUCCESS, service.selectList(entity));
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据分页查询", httpMethod = "GET")
    @ResponseBody
    public String page(T entity, @RequestParam(value = "pageSize", defaultValue = "20") String pageSize, @RequestParam(value = "pageNo", defaultValue = "1") String pageNo) {
        return write(ErrorCode.EM_SUCCESS, service.page(pageNo, pageSize, entity));
    }

}
