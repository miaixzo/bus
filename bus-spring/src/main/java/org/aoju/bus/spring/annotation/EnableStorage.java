package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.storage.StorageImportSelector;
import org.aoju.bus.storage.Provider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({StorageImportSelector.class})
public @interface EnableStorage {

    Provider provider();

}
