/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.boot.startup;

/**
 * Names for Spring Boot lifecycle callbacks and Spring startup steps.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StartupStages {

    /**
     * Startup stage name for jvm starting stage.
     */
    public static final String JVM_STARTING_STAGE = "JvmStartingStage";
    /**
     * Startup stage name for environment prepare stage.
     */
    public static final String ENVIRONMENT_PREPARE_STAGE = "EnvironmentPrepareStage";
    /**
     * Startup stage name for application context prepare stage.
     */
    public static final String APPLICATION_CONTEXT_PREPARE_STAGE = "ApplicationContextPrepareStage";
    /**
     * Startup stage name for application context load stage.
     */
    public static final String APPLICATION_CONTEXT_LOAD_STAGE = "ApplicationContextLoadStage";
    /**
     * Startup stage name for application context refresh stage.
     */
    public static final String APPLICATION_CONTEXT_REFRESH_STAGE = "ApplicationContextRefreshStage";
    /**
     * Startup stage name for application started stage.
     */
    public static final String APPLICATION_STARTED_STAGE = "ApplicationStartedStage";
    /**
     * Startup stage name for application ready stage.
     */
    public static final String APPLICATION_READY_STAGE = "ApplicationReadyStage";
    /**
     * Startup stage name for application failed stage.
     */
    public static final String APPLICATION_FAILED_STAGE = "ApplicationFailedStage";
    /**
     * Startup stage name for spring beans instantiate.
     */
    public static final String SPRING_BEANS_INSTANTIATE = "spring.beans.instantiate";
    /**
     * Startup stage name for spring beans smart instantiate.
     */
    public static final String SPRING_BEANS_SMART_INSTANTIATE = "spring.beans.smart-initialize";
    /**
     * Startup stage name for spring context beandef registry post processor.
     */
    public static final String SPRING_CONTEXT_BEANDEF_REGISTRY_POST_PROCESSOR = "spring.context.beandef-registry.post-process";
    /**
     * Startup stage name for spring context bean factory post processor.
     */
    public static final String SPRING_CONTEXT_BEAN_FACTORY_POST_PROCESSOR = "spring.context.bean-factory.post-process";
    /**
     * Startup stage name for spring bean post processor.
     */
    public static final String SPRING_BEAN_POST_PROCESSOR = "spring.context.beans.post-process";
    /**
     * Startup stage name for spring config classes enhance.
     */
    public static final String SPRING_CONFIG_CLASSES_ENHANCE = "spring.context.config-classes.enhance";

    /**
     * Prevents instantiation of this constants holder.
     */
    private StartupStages() {
        // No initialization required.
    }

}
