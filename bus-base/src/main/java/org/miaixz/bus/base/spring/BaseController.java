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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.miaixz.bus.base.service.BaseService;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.spring.Controller;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.validate.magic.annotation.Valid;

/**
 * Base request encapsulation for common CRUD operations. This controller provides a set of RESTful endpoints for
 * adding, removing, deleting, updating, and querying data.
 *
 * @param <T>       the entity type
 * @param <Service> the service type, which must extend {@link BaseService}
 * @author Kimi Liu
 * @since Java 21+
 */
public class BaseController<T, Service extends BaseService<T>> extends Controller {

    /**
     * Constructs a new BaseController instance.
     */
    public BaseController() {
        // No initialization required.
    }

    /**
     * The service instance for performing business logic operations.
     */
    @Autowired
    protected Service service;

    /**
     * Adds a new entity to the database.
     *
     * @param entity the entity to be added
     * @return the operation result, containing the number of affected rows if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Message<Integer> add(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD add request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        Integer result = service.insertSelective(entity);
        if (ObjectKit.isNotEmpty(result)) {
            Logger.info(
                    false,
                    "Base",
                    "CRUD add completed: entityType={}, success=true",
                    entity == null ? null : entity.getClass().getSimpleName());
            return write(result);
        }
        Logger.warn(
                false,
                "Base",
                "CRUD add failed: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
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
    public Message<Map<String, Integer>> remove(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD remove request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        Integer total = service.remove(entity);
        if (total >= 0) {
            Logger.info(
                    false,
                    "Base",
                    "CRUD remove completed: entityType={}, affectedRows={}",
                    entity == null ? null : entity.getClass().getSimpleName(),
                    total);
            Map<String, Integer> result = MapKit.of("total", total);
            return write(result);
        }
        Logger.warn(
                false,
                "Base",
                "CRUD remove failed: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
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
    public Message<Map<String, Integer>> delete(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD delete request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        Integer total = service.delete(entity);
        if (total >= 0) {
            Logger.info(
                    false,
                    "Base",
                    "CRUD delete completed: entityType={}, affectedRows={}",
                    entity == null ? null : entity.getClass().getSimpleName(),
                    total);
            Map<String, Integer> result = MapKit.of("total", total);
            return write(result);
        }
        Logger.warn(
                false,
                "Base",
                "CRUD delete failed: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        return write(ErrorCode._100807);
    }

    /**
     * Updates an existing entity based on its primary key.
     *
     * @param entity the entity with updated information
     * @return the operation result, containing the number of affected rows if successful, or an error code otherwise
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Message<Integer> update(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD update request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        Integer result = service.updateSelective(entity);
        if (ObjectKit.isNotEmpty(result)) {
            Logger.info(
                    false,
                    "Base",
                    "CRUD update completed: entityType={}, success=true",
                    entity == null ? null : entity.getClass().getSimpleName());
            return write(result);
        }
        Logger.warn(
                false,
                "Base",
                "CRUD update failed: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
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
    public Message<T> get(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD get request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        T result = service.selectOne(entity);
        Logger.info(
                false,
                "Base",
                "CRUD get completed: entityType={}, found={}, resultType={}",
                entity == null ? null : entity.getClass().getSimpleName(),
                ObjectKit.isNotEmpty(result),
                result == null ? null : result.getClass().getSimpleName());
        return write(result);
    }

    /**
     * Retrieves a list of entities based on specified conditions.
     *
     * @param entity an entity containing the query conditions
     * @return the operation result, containing a list of entities matching the conditions
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Message<List<T>> list(T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD list request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        List<T> list = service.selectList(entity);
        Logger.info(
                false,
                "Base",
                "CRUD list completed: entityType={}, rowCount={}",
                entity == null ? null : entity.getClass().getSimpleName(),
                list == null ? 0 : list.size());
        return write(list);
    }

    /**
     * Retrieves a paginated list of entities based on specified conditions.
     *
     * @param entity an entity containing the query conditions, including page size and page number
     * @return the operation result, containing a paginated list of entities
     */
    @ResponseBody
    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Message<Result<T>> page(@Valid({ "pageSize", "pageNo" }) T entity) {
        Logger.info(
                true,
                "Base",
                "CRUD page request received: entityType={}",
                entity == null ? null : entity.getClass().getSimpleName());
        Result<T> result = service.page(entity);
        Logger.info(
                false,
                "Base",
                "CRUD page completed: entityType={}, total={}, rowCount={}",
                entity == null ? null : entity.getClass().getSimpleName(),
                result == null ? 0 : result.getTotal(),
                result == null || result.getRows() == null ? 0 : result.getRows().size());
        return write(result);
    }

}
