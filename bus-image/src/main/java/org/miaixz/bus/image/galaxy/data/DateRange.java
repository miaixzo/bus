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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents the DateRange type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DateRange implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852262327192L;

    /**
     * The start value.
     */
    private final Date start;

    /**
     * The end value.
     */
    private final Date end;

    /**
     * Creates a new instance.
     *
     * @param start the start.
     * @param end   the end.
     */
    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the start date.
     *
     * @return the start date.
     */
    public final Date getStartDate() {
        return start;
    }

    /**
     * Gets the end date.
     *
     * @return the end date.
     */
    public final Date getEndDate() {
        return end;
    }

    /**
     * Determines whether start date exeeds end date.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isStartDateExeedsEndDate() {
        return start != null && end != null && start.after(end);
    }

    /**
     * Executes the contains operation.
     *
     * @param when the when.
     * @return true if the condition is met; otherwise false.
     */
    public boolean contains(Date when) {
        return !(start != null && start.after(when) || end != null && end.before(when));
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;

        if (!(object instanceof DateRange other))
            return false;

        return (Objects.equals(start, other.start)) && (Objects.equals(end, other.end));
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        int code = 0;
        if (start != null)
            code = start.hashCode();
        if (end != null)
            code ^= start.hashCode();
        return code;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

}
