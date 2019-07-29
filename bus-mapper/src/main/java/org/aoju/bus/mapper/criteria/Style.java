package org.aoju.bus.mapper.criteria;

/**
 * 字段转换方式
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum Style {
    normal,                     //原值
    camelhump,                  //驼峰转下划线
    uppercase,                  //转换为大写
    lowercase,                  //转换为小写
    camelhumpAndUppercase,      //驼峰转下划线大写形式
    camelhumpAndLowercase,      //驼峰转下划线小写形式
}
