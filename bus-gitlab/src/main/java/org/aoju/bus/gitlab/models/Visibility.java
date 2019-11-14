

package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.gitlab.utils.JacksonJsonEnumHelper;

public enum Visibility {

    PUBLIC, PRIVATE, INTERNAL;

    private static JacksonJsonEnumHelper<Visibility> enumHelper = new JacksonJsonEnumHelper<>(Visibility.class);

    @JsonCreator
    public static Visibility forValue(String value) {
        return enumHelper.forValue(value);
    }

    @JsonValue
    public String toValue() {
        return (enumHelper.toString(this));
    }

    @Override
    public String toString() {
        return (enumHelper.toString(this));
    }
}
