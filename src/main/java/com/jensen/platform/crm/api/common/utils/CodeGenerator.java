/*
 * All rights Reserved, Designed By www.jensen.com
 * @Title:
 * @Package
 * @Description: todo
 * @author: jensen
 * @date:
 * @version V1.0
 * @Copyright:
 */
package com.jensen.platform.crm.api.common.utils;

import com.google.common.base.CaseFormat;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName:
 * @Description:(代码生成器，根据数据表名称生成对应的Model、Mapper、Service、Controller简化开发。)
 * @author: jensen
 * @date:
 */
public class CodeGenerator {

    // JDBC配置，请修改为你项目的实际配置
    private static final String JDBC_URL = "jdbc:mysql://www.luquan.vip:3306/crm";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "Aa123456";
    private static final String JDBC_DIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    // 模板位置
    private static final String TEMPLATE_FILE_PATH = "src/main/resources/templates";
    private static final String JAVA_PATH = "src/main/java"; // java文件路径
    private static final String RESOURCES_PATH = "src/main/resources";// 资源文件路径

    // 项目基础包名称，根据自己公司的项目修改
    public static final String BASE_PACKAGE = "com.jensen.platform.crm.api";

    // Model所在包
    public static String MODEL_PACKAGE = BASE_PACKAGE + ".entity";

    // Mapper所在包
    public static String MAPPER_PACKAGE = BASE_PACKAGE + ".mapper";

    // Service所在包
    public static String SERVICE_PACKAGE = BASE_PACKAGE + ".service";

    // ServiceImpl所在包
    public static String SERVICE_IMPL_PACKAGE = SERVICE_PACKAGE + ".impl";

    // Controller所在包
    public static String CONTROLLER_PACKAGE = BASE_PACKAGE + ".controller";

    // Mapper xml所在包
    public static String SQL_MAP_PACKAGE = "mapper";

    // Mapper插件基础接口的完全限定名
    public static final String MAPPER_INTERFACE_REFERENCE = BASE_PACKAGE + ".common.bean.Mapper";

    // Entity插件基础接口的完全限定名
    public static final String ENTITY_INTERFACE_REFERENCE = BASE_PACKAGE + ".common.bean.Entity";

    // @author
    private static final String AUTHOR = "jensen";
    // @date
    private static final String DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());

    /**
     * genCode("输入表名");
     * @param args
     */
    public static void main(String[] args) {
        genCode("auth", "auth_user");
    }

    /**
     * 通过数据表名称生成代码，下划线转大驼峰的形式。 如输入表名称 "t_user_detail" 将生成
     * TUserDetail、TUserDetailMapper、TUserDetailService ...
     * @param modelName 模块名称，为空默认不按模块管理
     * @param tableNames 数据表名称...
     */
    public static void genCode(String modelName, String... tableNames) {
        if(StringUtils.isNotBlank(modelName)) {
            MODEL_PACKAGE = MODEL_PACKAGE + "." + modelName;
            MAPPER_PACKAGE = MAPPER_PACKAGE + "." + modelName;
            SERVICE_PACKAGE = SERVICE_PACKAGE + "." + modelName;
            SERVICE_IMPL_PACKAGE = SERVICE_PACKAGE + "." + modelName + ".impl";
            CONTROLLER_PACKAGE = CONTROLLER_PACKAGE + "." + modelName;
            SQL_MAP_PACKAGE = SQL_MAP_PACKAGE + "." + modelName;
        }

        for (String tableName : tableNames) {
            genCode(tableName);
        }
    }

    /**
     * 通过数据表名称生成代码 如输入表名称 "user_info"
     * 将生成 UserInfo、UserInfoMapper、UserInfoService ...
     *
     * @param tableName 数据表名称
     */
    public static void genCode(String tableName) {
        genModelAndMapper(tableName);
        genService(tableName);
        genController(tableName);
    }

    public static void genModelAndMapper(String tableName) {
        Context context = getContext();

        JDBCConnectionConfiguration jdbcConnectionConfiguration = getJDBCConnectionConfiguration();
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        PluginConfiguration pluginConfiguration = getPluginConfiguration();
        context.addPluginConfiguration(pluginConfiguration);

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = getJavaModelGeneratorConfiguration();
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = getSqlMapGeneratorConfiguration();
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = getJavaClientGeneratorConfiguration();
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(tableName);
        tableConfiguration.setDomainObjectName(null);
        tableConfiguration.addProperty("useActualColumnNames","true");
        context.addTableConfiguration(tableConfiguration);

        List<String> warnings;
        MyBatisGenerator generator;
        try {
            Configuration config = new Configuration();
            config.addContext(context);
            config.validate();
            DefaultShellCallback callback = new DefaultShellCallback(true);
            warnings = new ArrayList<>();
            generator = new MyBatisGenerator(config, callback, warnings);
            generator.generate(null);
        } catch (Exception e) {
            throw new RuntimeException("生成Model和Mapper失败", e);
        }

        if (generator.getGeneratedJavaFiles().isEmpty() || generator.getGeneratedXmlFiles().isEmpty()) {
            throw new RuntimeException("生成Model和Mapper失败：" + warnings);
        }
        String modelName = tableNameConvertUpperCamel(tableName);
        System.out.println(modelName + ".java 生成成功");
        System.out.println(modelName + "Mapper.java 生成成功");
        System.out.println(modelName + "Mapper.xml 生成成功");
    }

    public static void genService(String tableName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();
            //模板所需要的参数
            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);
            data.put("basePackageService", SERVICE_PACKAGE);
            data.put("basePackageServiceImpl", SERVICE_IMPL_PACKAGE);
            data.put("basePackageModel", MODEL_PACKAGE);
            data.put("basePackageDao", MAPPER_PACKAGE);

            File file = new File(JAVA_PATH + packageConvertPath(SERVICE_PACKAGE) + modelNameUpperCamel + "Service.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("service.ftl").process(data, new FileWriter(file));
            System.out.println(modelNameUpperCamel + "Service.java 生成成功");

            File file1 = new File(JAVA_PATH + packageConvertPath(SERVICE_IMPL_PACKAGE) + modelNameUpperCamel + "ServiceImpl.java");
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            cfg.getTemplate("service-impl.ftl").process(data, new FileWriter(file1));
            System.out.println(modelNameUpperCamel + "ServiceImpl.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Service失败", e);
        }
    }

    public static void genController(String tableName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();
            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = tableNameConvertUpperCamel(tableName);
            data.put("baseRequestMapping", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", BASE_PACKAGE);
            data.put("basePackageController", CONTROLLER_PACKAGE);
            data.put("basePackageService", SERVICE_PACKAGE);
            data.put("basePackageModel", MODEL_PACKAGE);

            File file = new File(JAVA_PATH + packageConvertPath(CONTROLLER_PACKAGE) + modelNameUpperCamel + "Controller.java");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("controller.ftl").process(data, new FileWriter(file));

            System.out.println(modelNameUpperCamel + "Controller.java 生成成功");
        } catch (Exception e) {
            throw new RuntimeException("生成Controller失败", e);
        }

    }

    private static Context getContext() {
        Context context = new Context(ModelType.FLAT);
        context.setId("Potato");
        context.setTargetRuntime("MyBatis3Simple");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "`");
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "`");
        return context;
    }

    private static JDBCConnectionConfiguration getJDBCConnectionConfiguration() {
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(JDBC_URL);
        jdbcConnectionConfiguration.setUserId(JDBC_USERNAME);
        jdbcConnectionConfiguration.setPassword(JDBC_PASSWORD);
        jdbcConnectionConfiguration.setDriverClass(JDBC_DIVER_CLASS_NAME);
        return jdbcConnectionConfiguration;
    }

    private static PluginConfiguration getPluginConfiguration() {
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        pluginConfiguration.addProperty("mappers", MAPPER_INTERFACE_REFERENCE);
        return pluginConfiguration;
    }

    private static JavaModelGeneratorConfiguration getJavaModelGeneratorConfiguration() {
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(JAVA_PATH);
        javaModelGeneratorConfiguration.setTargetPackage(MODEL_PACKAGE);
        javaModelGeneratorConfiguration.addProperty("rootClass", ENTITY_INTERFACE_REFERENCE);
        javaModelGeneratorConfiguration.addProperty("enableSubPackages", "true");
        javaModelGeneratorConfiguration.addProperty("trimStrings", "true");
        return javaModelGeneratorConfiguration;
    }

    private static SqlMapGeneratorConfiguration getSqlMapGeneratorConfiguration() {
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetProject(RESOURCES_PATH);
        sqlMapGeneratorConfiguration.setTargetPackage(SQL_MAP_PACKAGE);
        return sqlMapGeneratorConfiguration;
    }

    private static JavaClientGeneratorConfiguration getJavaClientGeneratorConfiguration() {
        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetProject(JAVA_PATH);
        javaClientGeneratorConfiguration.setTargetPackage(MAPPER_PACKAGE);
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        return javaClientGeneratorConfiguration;
    }

    private static freemarker.template.Configuration getConfiguration() throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_FILE_PATH));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return cfg;
    }

    private static String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());
    }

    private static String packageConvertPath(String packageName) {
        return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
    }
}
