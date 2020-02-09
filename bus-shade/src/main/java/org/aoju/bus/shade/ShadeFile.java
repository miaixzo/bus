/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.shade;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.shade.entity.PropertyInfo;
import org.aoju.bus.shade.entity.TableEntity;

import java.util.List;

/**
 * 获取文件路径调用创建文件
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class ShadeFile {

    //路径信息
    public static final String ENTITY = "Entity";
    public static final String MAPPER = "Mapper";
    public static final String MAPPER_XML = "MapperXml";
    public static final String SERVICE = "Service";
    public static final String SERVICE_IMPL = "ServiceImpl";
    public static final String CONTROLLER = "Controller";
    public static final String SUFFIX = ".ftl";

    //①创建实体类
    public static Object createEntity(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getEntityUrl(), bi.getEntityName(), ENTITY);
        return Freemarker.createFile(bi, ENTITY + SUFFIX, fileUrl);
    }

    //②创建DAO
    public static Object createMapper(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getMapperUrl(), bi.getEntityName(), MAPPER);
        return Freemarker.createFile(bi, MAPPER + SUFFIX, fileUrl);
    }

    //③创建mapper配置文件
    public static Object createMapperXml(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getMapperXmlUrl(), bi.getEntityName(), MAPPER_XML);
        List<PropertyInfo> list = bi.getCis();
        String agile = Normal.EMPTY;
        for (PropertyInfo propertyInfo : list) {
            agile = agile + propertyInfo.getColumn() + ",\n\t\t";
        }
        agile = agile.substring(0, agile.length() - 4);
        bi.setAgile(agile);
        return Freemarker.createFile(bi, MAPPER_XML + SUFFIX, fileUrl);
    }

    //④创建SERVICE
    public static Object createService(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getServiceUrl(), bi.getEntityName(), SERVICE);
        return Freemarker.createFile(bi, SERVICE + SUFFIX, fileUrl);
    }

    //⑤创建SERVICE_IMPL
    public static Object createServiceImpl(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getServiceImplUrl(), bi.getEntityName(), SERVICE_IMPL);
        return Freemarker.createFile(bi, SERVICE_IMPL + SUFFIX, fileUrl);
    }

    //⑥创建CONTROLLER
    public static Object createController(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getControllerUrl(), bi.getEntityName(), CONTROLLER);
        return Freemarker.createFile(bi, CONTROLLER + SUFFIX, fileUrl);
    }

    //生成文件路径和名字
    public static String getFileUrl(String url, String packageUrl, String entityName, String type) {
        if (ENTITY.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + ".java";
        } else if (MAPPER.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + "Mapper.java";
        } else if (MAPPER_XML.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + "Mapper.xml";
        } else if (SERVICE.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + "Service.java";
        } else if (SERVICE_IMPL.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + "ServiceImpl.java";
        } else if (CONTROLLER.equals(type)) {
            return url + pageToUrl(packageUrl) + entityName + "Controller.java";
        }
        return null;
    }

    public static String pageToUrl(String url) {
        return url.replace(Symbol.DOT, Symbol.SLASH) + Symbol.SLASH;
    }

}
