package org.aoju.bus.starter.annotation;

import org.aoju.bus.starter.preview.PreviewConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用在线预览
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({PreviewConfiguration.class})
public @interface EnablePreview {

}
