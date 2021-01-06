package org.aoju.bus.shade.beans;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
    public static Object createEntity(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getEntityUrl(), bi.getEntityName(), ENTITY);
        return createFile(bi, ENTITY + SUFFIX, fileUrl);
    }

    // ②创建DAO
    public static Object createMapper(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getMapperUrl(), bi.getEntityName(), MAPPER);
        return createFile(bi, MAPPER + SUFFIX, fileUrl);
    }

    // ③创建mapper配置文件
    public static Object createMapperXml(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getMapperXmlUrl(), bi.getEntityName(), MAPPER_XML);
        List<PropertyInfo> list = bi.getCis();
        String agile = Normal.EMPTY;
        for (PropertyInfo propertyInfo : list) {
            agile = agile + propertyInfo.getColumn() + ",\n\t\t";
        }
        agile = agile.substring(0, agile.length() - 4);
        bi.setAgile(agile);
        return createFile(bi, MAPPER_XML + SUFFIX, fileUrl);
    }

    // ④创建SERVICE
    public static Object createService(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getServiceUrl(), bi.getEntityName(), SERVICE);
        return createFile(bi, SERVICE + SUFFIX, fileUrl);
    }

    // ⑤创建SERVICE_IMPL
    public static Object createServiceImpl(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getServiceImplUrl(), bi.getEntityName(), SERVICE_IMPL);
        return createFile(bi, SERVICE_IMPL + SUFFIX, fileUrl);
    }

    // ⑥创建CONTROLLER
    public static Object createController(String url, TableEntity bi) {
        String fileUrl = getFileUrl(url, bi.getControllerUrl(), bi.getEntityName(), CONTROLLER);
        return createFile(bi, CONTROLLER + SUFFIX, fileUrl);
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

    private static Object createFile(TableEntity dataModel, String templateName, String filePath) {
        FileWriter out = null;
        try {
            // 通过FreeMarker的Confuguration读取相应的模板文件
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            // 设置模板路径
            configuration.setClassForTemplateLoading(org.aoju.bus.shade.screw.Builder.class, Symbol.C_SLASH + Normal.META_DATA_INF + "/template");
            // 设置默认字体
            configuration.setDefaultEncoding("utf-8");
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

            //设置输出流
            out = new FileWriter(file);
            //模板输出静态文件
            template.process(dataModel, out);
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
