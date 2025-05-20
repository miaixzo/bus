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
package org.miaixz.bus.shade.beans;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Builder {

    // 路径信息
    public static final String ENTITY = "Entity";
    public static final String MAPPER = "Mapper";
    public static final String MAPPER_XML = "MapperXml";
    public static final String SERVICE = "Service";
    public static final String SERVICE_IMPL = "ServiceImpl";
    public static final String CONTROLLER = "Controller";
    public static final String SUFFIX = ".ftl";

    // ①创建实体类
    public static Object createEntity(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getEntityUrl(), tableEntity.getEntityName(), ENTITY);
        return createFile(tableEntity, ENTITY + SUFFIX, fileUrl);
    }

    // ②创建DAO
    public static Object createMapper(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getMapperUrl(), tableEntity.getEntityName(), MAPPER);
        return createFile(tableEntity, MAPPER + SUFFIX, fileUrl);
    }

    // ③创建mapper配置文件
    public static Object createMapperXml(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getMapperXmlUrl(), tableEntity.getEntityName(), MAPPER_XML);
        List<PropertyInfo> list = tableEntity.getCis();
        String agile = Normal.EMPTY;
        for (PropertyInfo propertyInfo : list) {
            agile = agile + propertyInfo.getColumn() + ",\n\t\t";
        }
        agile = agile.substring(0, agile.length() - 4);
        tableEntity.setAgile(agile);
        return createFile(tableEntity, MAPPER_XML + SUFFIX, fileUrl);
    }

    // ④创建SERVICE
    public static Object createService(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getServiceUrl(), tableEntity.getEntityName(), SERVICE);
        return createFile(tableEntity, SERVICE + SUFFIX, fileUrl);
    }

    // ⑤创建SERVICE_IMPL
    public static Object createServiceImpl(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getServiceImplUrl(), tableEntity.getEntityName(), SERVICE_IMPL);
        return createFile(tableEntity, SERVICE_IMPL + SUFFIX, fileUrl);
    }

    // ⑥创建CONTROLLER
    public static Object createController(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getControllerUrl(), tableEntity.getEntityName(), CONTROLLER);
        return createFile(tableEntity, CONTROLLER + SUFFIX, fileUrl);
    }

    // 生成文件路径和名字
    private static String getFileUrl(String url, String packageUrl, String entityName, String type) {
        if (ENTITY.equals(type)) {
            return url + packageUrl.replace(Symbol.DOT, Symbol.SLASH) + Symbol.SLASH + entityName + ".java";
        } else if (MAPPER.equals(type)) {
            return url + packageUrl.replace(Symbol.DOT, Symbol.SLASH) + Symbol.SLASH + entityName + "Mapper.java";
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

    private static String pageToUrl(String url) {
        return url.replace(Symbol.DOT, Symbol.SLASH) + Symbol.SLASH;
    }

    private static Object createFile(TableEntity tableEntity, String templateName, String filePath) {
        FileWriter out = null;
        try {
            // 通过FreeMarker的Confuguration读取相应的模板文件
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            // 设置模板路径
            configuration.setClassForTemplateLoading(org.miaixz.bus.shade.beans.Builder.class,
                    Symbol.C_SLASH + Normal.META_INF + "/shade/beans");
            // 设置默认字体
            configuration.setDefaultEncoding(Charset.DEFAULT_UTF_8);
            // 获取模板
            Template template = configuration.getTemplate(templateName);
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                return "The file already exists:" + filePath;
            }

            // 设置输出流
            out = new FileWriter(file);
            // 模板输出静态文件
            template.process(tableEntity, out);
            return "create a file :" + filePath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "failed to create file :" + filePath;
    }

}
