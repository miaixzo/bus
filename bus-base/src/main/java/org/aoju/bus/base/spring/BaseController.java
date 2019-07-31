package org.aoju.bus.base.spring;

import org.aoju.bus.base.service.BaseService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 基础请求封装
 * </p>
 *
 * @param <Service>
 * @param <T>
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BaseController<Service extends BaseService<T>, T> extends Controller {

    @Autowired
    protected Service service;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "通用:添加数据", httpMethod = "POST")
    @ResponseBody
    public String add(T entity) {
        return write(ResultCode.EM_SUCCESS, service.insertSelective(entity));
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
        return write(ResultCode.EM_SUCCESS, service.updateSelectiveById(entity));
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据主键查询", httpMethod = "GET")
    @ResponseBody
    public String get(T entity) {
        return write(ResultCode.EM_SUCCESS, service.selectById(entity));
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据条件查询", httpMethod = "GET")
    @ResponseBody
    public String list(T entity) {
        return write(ResultCode.EM_SUCCESS, service.selectList(entity));
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    @ApiOperation(value = "通用:数据分页查询", httpMethod = "GET")
    @ResponseBody
    public String page(T entity, @RequestParam(value = "pageSize", defaultValue = "20") String pageSize, @RequestParam(value = "pageNo", defaultValue = "1") String pageNo) {
        return write(ResultCode.EM_SUCCESS, service.page(pageNo, pageSize, entity));
    }

}
