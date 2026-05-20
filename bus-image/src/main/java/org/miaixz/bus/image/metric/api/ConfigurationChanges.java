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
package org.miaixz.bus.image.metric.api;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the ConfigurationChanges type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ConfigurationChanges {

    /**
     * The objects value.
     */
    private final List<ModifiedObject> objects = new ArrayList<>();

    /**
     * The verbose value.
     */
    private final boolean verbose;

    /**
     * Creates a new instance.
     *
     * @param verbose the verbose.
     */
    public ConfigurationChanges(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Executes the nullify if not verbose operation.
     *
     * @param <T>    the object type
     * @param diffs  the diffs.
     * @param object the object.
     * @return the operation result.
     */
    public static <T> T nullifyIfNotVerbose(ConfigurationChanges diffs, T object) {
        return diffs != null && diffs.isVerbose() ? object : null;
    }

    /**
     * Adds the modified object if verbose.
     *
     * @param diffs      the diffs.
     * @param dn         the dn.
     * @param changeType the change type.
     * @return the operation result.
     */
    public static ModifiedObject addModifiedObjectIfVerbose(
            ConfigurationChanges diffs,
            String dn,
            ChangeType changeType) {
        if (diffs == null || !diffs.isVerbose())
            return null;

        ModifiedObject object = new ModifiedObject(dn, changeType);
        diffs.add(object);
        return object;
    }

    /**
     * Adds the modified object.
     *
     * @param diffs      the diffs.
     * @param dn         the dn.
     * @param changeType the change type.
     * @return the operation result.
     */
    public static ModifiedObject addModifiedObject(ConfigurationChanges diffs, String dn, ChangeType changeType) {
        if (diffs == null)
            return null;

        ModifiedObject object = new ModifiedObject(dn, changeType);
        diffs.add(object);
        return object;
    }

    /**
     * Removes the last if empty.
     *
     * @param diffs  the diffs.
     * @param object the object.
     */
    public static void removeLastIfEmpty(ConfigurationChanges diffs, ModifiedObject object) {
        if (object != null && object.isEmpty())
            diffs.removeLast();
    }

    /**
     * Removes the last.
     */
    private void removeLast() {
        objects.remove(objects.size() - 1);
    }

    /**
     * Executes the modified objects operation.
     *
     * @return the operation result.
     */
    public List<ModifiedObject> modifiedObjects() {
        return objects;
    }

    /**
     * Executes the add operation.
     *
     * @param object the object.
     */
    public void add(ModifiedObject object) {
        objects.add(object);
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        return objects.isEmpty();
    }

    /**
     * Determines whether verbose.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        if (isEmpty())
            return "[]";

        StringBuilder sb = new StringBuilder(objects.size() * Normal._64);
        for (ModifiedObject object : objects) {
            sb.append(object.changeType).append(Symbol.C_SPACE).append(object.dn).append(Symbol.C_LF);
            if (null != object.attributes) {
                for (ModifiedAttribute attr : object.attributes) {
                    sb.append(Symbol.SPACE).append(attr.name).append(": ").append(attr.removedValues).append("=>")
                            .append(attr.addedValues).append(Symbol.C_LF);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Defines the ChangeType values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ChangeType {
        /**
         * Constant for the c value.
         */
        C,
        /**
         * Constant for the u value.
         */
        U,
        /**
         * Constant for the d value.
         */
        D

    }

    /**
     * Represents the ModifiedAttribute type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ModifiedAttribute {

        /**
         * The name value.
         */
        private final String name;

        /**
         * The added values value.
         */
        private final List<Object> addedValues = new ArrayList<>(1);

        /**
         * The removed values value.
         */
        private final List<Object> removedValues = new ArrayList<>(1);

        /**
         * Creates a new instance.
         *
         * @param name the name.
         */
        public ModifiedAttribute(String name) {
            this.name = name;
        }

        /**
         * Creates a new instance.
         *
         * @param name the name.
         * @param prev the prev.
         * @param val  the val.
         */
        public ModifiedAttribute(String name, Object prev, Object val) {
            this.name = name;
            removeValue(prev);
            addValue(val);
        }

        /**
         * Executes the name operation.
         *
         * @return the operation result.
         */
        public String name() {
            return name;
        }

        /**
         * Adds the ed values.
         *
         * @return the operation result.
         */
        public List<Object> addedValues() {
            return addedValues;
        }

        /**
         * Removes the d values.
         *
         * @return the operation result.
         */
        public List<Object> removedValues() {
            return removedValues;
        }

        /**
         * Adds the value.
         *
         * @param value the value.
         */
        public void addValue(Object value) {
            if (value != null && !removedValues.remove(value))
                addedValues.add(value);
        }

        /**
         * Removes the value.
         *
         * @param value the value.
         */
        public void removeValue(Object value) {
            if (value != null && !addedValues.remove(value))
                removedValues.add(value);
        }

    }

    /**
     * Represents the ModifiedObject type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static class ModifiedObject {

        /**
         * The dn value.
         */
        private final String dn;

        /**
         * The change type value.
         */
        private final ChangeType changeType;

        /**
         * The attributes value.
         */
        private final List<ModifiedAttribute> attributes = new ArrayList<>();

        /**
         * Creates a new instance.
         *
         * @param dn         the dn.
         * @param changeType the change type.
         */
        public ModifiedObject(String dn, ChangeType changeType) {
            this.dn = dn;
            this.changeType = changeType;
        }

        /**
         * Executes the dn operation.
         *
         * @return the operation result.
         */
        public String dn() {
            return dn;
        }

        /**
         * Executes the change type operation.
         *
         * @return the operation result.
         */
        public ChangeType changeType() {
            return changeType;
        }

        /**
         * Determines whether empty.
         *
         * @return true if the condition is met; otherwise false.
         */
        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        /**
         * Executes the modified attributes operation.
         *
         * @return the operation result.
         */
        public List<ModifiedAttribute> modifiedAttributes() {
            return attributes;
        }

        /**
         * Executes the add operation.
         *
         * @param attribute the attribute.
         */
        public void add(ModifiedAttribute attribute) {
            this.attributes.add(attribute);
        }

    }

}
