/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
