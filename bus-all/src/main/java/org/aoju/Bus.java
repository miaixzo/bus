package org.aoju;

/**
 * 用于识别当前版本号和版权声明! <br/>
 * Bus is Licensed under the MIT License, Bus 3.0 (the "License")
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Bus {

    /**
     * 标示类
     */
    Bus() {
    }

    /**
     * 获取 Bus 的版本号，版本号的命名规范
     *
     * <pre>
     * [大版本].[小版本].[发布流水号]
     * </pre>
     * <p>
     * 这里有点说明
     * <ul>
     * <li>大版本 - 表示API的版本，如果没有重大变化，基本上同样的大版本号，使用方式是一致的
     * <li>质量号 - alpha内部测试, beta 公测品质,RELEASE 生产品质
     * <li>小版本 - 每次发布增加1
     * </ul>
     *
     * @return 项目的版本号
     */

    public static String version() {
        return major() + "." + minor() + "." + stage() + "." + level();
    }

    /**
     * 主要版本号
     */
    public static String major() {
        return "3";
    }

    /**
     * 次要版本号
     */
    public static String minor() {
        return "0";
    }

    /**
     * 阶段版本号
     */
    public static String stage() {
        return "0";
    }

    /**
     * 版本质量
     */
    public static String level() {
        return "RELEASE";
    }

}
