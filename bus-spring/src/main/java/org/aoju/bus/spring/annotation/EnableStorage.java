package org.aoju.bus.spring.annotation;

import org.aoju.bus.spring.storage.StorageImportSelector;
import org.aoju.bus.storage.Provider;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author funcas
 * @version 1.0
 * @date 2019年04月12日
 */
@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({StorageImportSelector.class})
public @interface EnableStorage {

    Provider provider();

}
