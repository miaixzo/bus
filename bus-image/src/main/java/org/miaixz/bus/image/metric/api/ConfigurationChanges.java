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
 * @author Kimi Liu
 * @since Java 21+
 */
public class ConfigurationChanges {

    private final List<ModifiedObject> objects = new ArrayList<>();
    private final boolean verbose;

    public ConfigurationChanges(boolean verbose) {
        this.verbose = verbose;
    }

    public static <T> T nullifyIfNotVerbose(ConfigurationChanges diffs, T object) {
        return diffs != null && diffs.isVerbose() ? object : null;
    }

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

    public static ModifiedObject addModifiedObject(ConfigurationChanges diffs, String dn, ChangeType changeType) {
        if (diffs == null)
            return null;

        ModifiedObject object = new ModifiedObject(dn, changeType);
        diffs.add(object);
        return object;
    }

    public static void removeLastIfEmpty(ConfigurationChanges diffs, ModifiedObject object) {
        if (object != null && object.isEmpty())
            diffs.removeLast();
    }

    private void removeLast() {
        objects.remove(objects.size() - 1);
    }

    public List<ModifiedObject> modifiedObjects() {
        return objects;
    }

    public void add(ModifiedObject object) {
        objects.add(object);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public boolean isVerbose() {
        return verbose;
    }

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

    public enum ChangeType {
        C, U, D
    }

    public static class ModifiedAttribute {

        private final String name;
        private final List<Object> addedValues = new ArrayList<>(1);
        private final List<Object> removedValues = new ArrayList<>(1);

        public ModifiedAttribute(String name) {
            this.name = name;
        }

        public ModifiedAttribute(String name, Object prev, Object val) {
            this.name = name;
            removeValue(prev);
            addValue(val);
        }

        public String name() {
            return name;
        }

        public List<Object> addedValues() {
            return addedValues;
        }

        public List<Object> removedValues() {
            return removedValues;
        }

        public void addValue(Object value) {
            if (value != null && !removedValues.remove(value))
                addedValues.add(value);
        }

        public void removeValue(Object value) {
            if (value != null && !addedValues.remove(value))
                removedValues.add(value);
        }

    }

    public static class ModifiedObject {

        private final String dn;
        private final ChangeType changeType;
        private final List<ModifiedAttribute> attributes = new ArrayList<>();

        public ModifiedObject(String dn, ChangeType changeType) {
            this.dn = dn;
            this.changeType = changeType;
        }

        public String dn() {
            return dn;
        }

        public ChangeType changeType() {
            return changeType;
        }

        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        public List<ModifiedAttribute> modifiedAttributes() {
            return attributes;
        }

        public void add(ModifiedAttribute attribute) {
            this.attributes.add(attribute);
        }
    }

}
