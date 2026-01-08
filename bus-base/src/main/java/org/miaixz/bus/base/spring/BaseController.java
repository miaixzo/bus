/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Base request encapsulation for common CRUD operations. This controller provides a set of RESTful endpoints for
 * adding, removing, deleting, updating, and querying data.
 *
 * @param <T>       the entity type
 * @param <Service> the service type, which must extend {@link BaseService}
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaseController<T, Service extends BaseService<T>> extends Controller {

    /**
     * The service instance for performing business logic operations.
     */
    @Autowired
    protected Service service;

    /**
     * Adds a new entity to the database.
     *
     * @param entity the entity to be added
     * @return the operation result, containing the added entity if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Object add(T entity) {
        T t = (T) service.insertSelective(entity);
        if (ObjectKit.isNotEmpty(t)) {
            return write(t);
        }
        return write(ErrorCode._100807);
    }

    /**
     * Performs a logical removal of an entity. This typically involves updating a status field rather than physically
     * deleting the record.
     *
     * @param entity the entity to be logically removed
     * @return the operation result, containing the number of affected rows if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public Object remove(T entity) {
        long total = service.remove(entity);
        if (total >= 0) {
            return write(MapKit.of("total", total));
        }
        return write(ErrorCode._100807);
    }

    /**
     * Performs a physical deletion of an entity from the database.
     *
     * @param entity the entity to be physically deleted
     * @return the operation result, containing the number of affected rows if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Object delete(T entity) {
        long total = service.delete(entity);
        if (total >= 0) {
            return write(MapKit.of("total", total));
        }
        return write(ErrorCode._100807);
    }

    /**
     * Updates an existing entity based on its primary key.
     *
     * @param entity the entity with updated information
     * @return the operation result, containing the updated entity if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Object update(T entity) {
        T t = (T) service.updateSelective(entity);
        if (ObjectKit.isNotEmpty(t)) {
            return write(t);
        }
        return write(ErrorCode._100807);
    }

    /**
     * Retrieves an entity by its primary key.
     *
     * @param entity an entity containing the primary key for lookup
     * @return the operation result, containing the found entity or null if not found
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public Object get(T entity) {
        return write(service.selectOne(entity));
    }

    /**
     * Retrieves a list of entities based on specified conditions.
     *
     * @param entity an entity containing the query conditions
     * @return the operation result, containing a list of entities matching the conditions
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Object list(T entity) {
        return write(service.selectList(entity));
    }

    /**
     * Retrieves a paginated list of entities based on specified conditions.
     *
     * @param entity an entity containing the query conditions, including page size and page number
     * @return the operation result, containing a paginated list of entities
     */
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Object page(@Valid({ "pageSize", "pageNo" }) T entity) {
        return write(service.page(entity));
    }

}
