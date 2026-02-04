/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.annotation;

/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * Annotation used to control the <b>data visibility scope</b> for specific entities or methods.
 * <p>
 * This annotation serves as a marker for Row-Level Security (RLS). It instructs the underlying framework (e.g., MyBatis
 * Interceptor, AOP Aspect) to automatically inject SQL predicates to filter data based on the current user's context
 * (e.g., Tenant ID, Department ID, or User ID).
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD }) // 建议增加 METHOD 支持，以便灵活控制
@Retention(RetentionPolicy.RUNTIME)
public @interface Visible {

    /**
     * Defines the scope of data visibility.
     * <p>
     * Common values might include:
     * <ul>
     * <li>{@code TENANT}: Visible only within the current tenant.</li>
     * <li>{@code DEPT}: Visible only within the current department (and sub-departments).</li>
     * <li>{@code SELF}: Visible only to the creator/owner.</li>
     * </ul>
     *
     * @return the visibility scope(s) required.
     */
    String[] value() default {};

    /**
     * Specifies the table alias used in the SQL statement.
     * <p>
     * This is crucial for SQL parsing when joins are involved. For example, if the SQL is
     * {@code SELECT * FROM sys_user u}, the alias is "u". The injector will append {@code AND u.dept_id = ...}.
     *
     * @return the table alias, defaults to empty (auto-detect or no alias).
     */
    String alias() default Normal.EMPTY;

    /**
     * Whether to ignore specific scopes if they are globally enabled.
     * <p>
     * Useful for "Super Admin" interfaces where visibility restrictions should be bypassed.
     *
     * @return true if visibility checks should be skipped.
     */
    boolean ignore() default false;

}
