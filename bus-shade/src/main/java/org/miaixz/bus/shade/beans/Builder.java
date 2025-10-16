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

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Builds source code files based on FreeMarker templates and table entity information. This class provides methods to
 * generate various components of a typical web application, such as entities, mappers, services, and controllers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * Constant for identifying the Entity template.
     */
    public static final String ENTITY = "Entity";

    /**
     * Constant for identifying the Mapper interface template.
     */
    public static final String MAPPER = "Mapper";

    /**
     * Constant for identifying the Mapper XML template.
     */
    public static final String MAPPER_XML = "MapperXml";

    /**
     * Constant for identifying the Service interface template.
     */
    public static final String SERVICE = "Service";

    /**
     * Constant for identifying the Service implementation template.
     */
    public static final String SERVICE_IMPL = "ServiceImpl";

    /**
     * Constant for identifying the Controller template.
     */
    public static final String CONTROLLER = "Controller";

    /**
     * The file extension for FreeMarker templates.
     */
    public static final String SUFFIX = ".ftl";

    /**
     * Creates an entity class file.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createEntity(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getEntityUrl(), tableEntity.getEntityName(), ENTITY);
        return createFile(tableEntity, ENTITY + SUFFIX, fileUrl);
    }

    /**
     * Creates a mapper interface file.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createMapper(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getMapperUrl(), tableEntity.getEntityName(), MAPPER);
        return createFile(tableEntity, MAPPER + SUFFIX, fileUrl);
    }

    /**
     * Creates a mapper XML configuration file. This method also prepares a comma-separated list of column names for the
     * template.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createMapperXml(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getMapperXmlUrl(), tableEntity.getEntityName(), MAPPER_XML);
        List<PropertyInfo> list = tableEntity.getCis();
        StringBuilder agile = new StringBuilder();
        for (PropertyInfo propertyInfo : list) {
            agile.append(propertyInfo.getColumn()).append(",\n\t\t");
        }
        if (agile.length() > 4) {
            agile.setLength(agile.length() - 4);
        }
        tableEntity.setAgile(agile.toString());
        return createFile(tableEntity, MAPPER_XML + SUFFIX, fileUrl);
    }

    /**
     * Creates a service interface file.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createService(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getServiceUrl(), tableEntity.getEntityName(), SERVICE);
        return createFile(tableEntity, SERVICE + SUFFIX, fileUrl);
    }

    /**
     * Creates a service implementation class file.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createServiceImpl(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getServiceImplUrl(), tableEntity.getEntityName(), SERVICE_IMPL);
        return createFile(tableEntity, SERVICE_IMPL + SUFFIX, fileUrl);
    }

    /**
     * Creates a controller class file.
     *
     * @param url         The base URL for file generation.
     * @param tableEntity The entity representing the table structure.
     * @return A status message indicating success or failure.
     */
    public static Object createController(String url, TableEntity tableEntity) {
        String fileUrl = getFileUrl(url, tableEntity.getControllerUrl(), tableEntity.getEntityName(), CONTROLLER);
        return createFile(tableEntity, CONTROLLER + SUFFIX, fileUrl);
    }

    /**
     * Constructs the full file path for the generated file.
     *
     * @param url        The base output directory.
     * @param packageUrl The package name for the file.
     * @param entityName The name of the entity, used as the base for the file name.
     * @param type       The type of file to generate (e.g., ENTITY, MAPPER).
     * @return The fully qualified path for the new file, or {@code null} if the type is unknown.
     */
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

    /**
     * Converts a package name string (e.g., "org.miaixz.bus") into a URL path string (e.g., "org/miaixz/bus/").
     *
     * @param url The package name.
     * @return The corresponding path string.
     */
    private static String pageToUrl(String url) {
        return url.replace(Symbol.DOT, Symbol.SLASH) + Symbol.SLASH;
    }

    /**
     * Creates a file using a FreeMarker template.
     *
     * @param tableEntity  The data model for the template.
     * @param templateName The name of the FreeMarker template file.
     * @param filePath     The path where the new file will be created.
     * @return A status message indicating the result of the operation.
     */
    private static Object createFile(TableEntity tableEntity, String templateName, String filePath) {
        FileWriter out = null;
        try {
            // Configure FreeMarker
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setClassForTemplateLoading(Builder.class, Symbol.C_SLASH + Normal.META_INF + "/shade/beans");
            configuration.setDefaultEncoding(Charset.DEFAULT_UTF_8);

            // Get the template
            Template template = configuration.getTemplate(templateName);

            // Create file and directories if they don't exist
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                return "The file already exists: " + filePath;
            }

            // Process the template and write the output file
            out = new FileWriter(file);
            template.process(tableEntity, out);
            return "Created a file: " + filePath;
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
        return "Failed to create file: " + filePath;
    }

}
