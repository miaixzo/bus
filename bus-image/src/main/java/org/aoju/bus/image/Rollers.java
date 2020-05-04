package org.aoju.bus.image;

import org.aoju.bus.image.galaxy.data.Attributes;

import java.io.File;

/**
 * 图像处理额外接触点
 * 即: 后续业务处理流程
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface Rollers {

    /**
     * @param attributes 完整影像信息
     * @param file       影像原始文件
     * @return 根据业务需要返回不同类型的值
     */
    default Object supports(Attributes attributes, File file) {
        return attributes;
    }

}
