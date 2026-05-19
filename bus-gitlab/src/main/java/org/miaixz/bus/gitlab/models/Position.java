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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The position class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Position implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852270036770L;

    /**
     * The position type enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static enum PositionType {

        /**
         * The text position type.
         */
        TEXT,
        /**
         * The image position type.
         */
        IMAGE,
        /**
         * The file position type.
         */
        FILE;

        private static JacksonJsonEnumHelper<PositionType> enumHelper = new JacksonJsonEnumHelper<>(PositionType.class,
                false, false);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static PositionType forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    private String baseSha;
    private String startSha;
    private String headSha;
    private String oldPath;
    private String newPath;
    private PositionType positionType;

    private Integer oldLine;
    private Integer newLine;

    private Integer width;
    private Integer height;
    private Double x;
    private Double y;

    /**
     * Returns the base sha.
     *
     * @return the result
     */

    public String getBaseSha() {
        return baseSha;
    }

    /**
     * Sets the base sha.
     *
     * @param baseSha the base sha value
     */

    public void setBaseSha(String baseSha) {
        this.baseSha = baseSha;
    }

    /**
     * Sets the base sha and returns this instance.
     *
     * @param baseSha the base sha value
     * @return the result
     */

    public Position withBaseSha(String baseSha) {
        this.baseSha = baseSha;
        return (this);
    }

    /**
     * Returns the start sha.
     *
     * @return the result
     */

    public String getStartSha() {
        return startSha;
    }

    /**
     * Sets the start sha.
     *
     * @param startSha the start sha value
     */

    public void setStartSha(String startSha) {
        this.startSha = startSha;
    }

    /**
     * Sets the start sha and returns this instance.
     *
     * @param startSha the start sha value
     * @return the result
     */

    public Position withStartSha(String startSha) {
        this.startSha = startSha;
        return (this);
    }

    /**
     * Returns the head sha.
     *
     * @return the result
     */

    public String getHeadSha() {
        return headSha;
    }

    /**
     * Sets the head sha.
     *
     * @param headSha the head sha value
     */

    public void setHeadSha(String headSha) {
        this.headSha = headSha;
    }

    /**
     * Sets the head sha and returns this instance.
     *
     * @param headSha the head sha value
     * @return the result
     */

    public Position withHeadSha(String headSha) {
        this.headSha = headSha;
        return (this);
    }

    /**
     * Returns the old path.
     *
     * @return the result
     */

    public String getOldPath() {
        return oldPath;
    }

    /**
     * Sets the old path.
     *
     * @param oldPath the old path value
     */

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * Sets the old path and returns this instance.
     *
     * @param oldPath the old path value
     * @return the result
     */

    public Position withOldPath(String oldPath) {
        this.oldPath = oldPath;
        return (this);
    }

    /**
     * Returns the new path.
     *
     * @return the result
     */

    public String getNewPath() {
        return newPath;
    }

    /**
     * Sets the new path.
     *
     * @param newPath the new path value
     */

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    /**
     * Sets the new path and returns this instance.
     *
     * @param newPath the new path value
     * @return the result
     */

    public Position withNewPath(String newPath) {
        this.newPath = newPath;
        return (this);
    }

    /**
     * Returns the position type.
     *
     * @return the result
     */

    public PositionType getPositionType() {
        return positionType;
    }

    /**
     * Sets the position type.
     *
     * @param positionType the position type value
     */

    public void setPositionType(PositionType positionType) {
        this.positionType = positionType;
    }

    /**
     * Sets the position type and returns this instance.
     *
     * @param positionType the position type value
     * @return the result
     */

    public Position withPositionType(PositionType positionType) {
        this.positionType = positionType;
        return (this);
    }

    /**
     * Returns the old line.
     *
     * @return the result
     */

    public Integer getOldLine() {
        return oldLine;
    }

    /**
     * Sets the old line.
     *
     * @param oldLine the old line value
     */

    public void setOldLine(Integer oldLine) {
        this.oldLine = oldLine;
    }

    /**
     * Sets the old line and returns this instance.
     *
     * @param oldLine the old line value
     * @return the result
     */

    public Position withOldLine(Integer oldLine) {
        this.oldLine = oldLine;
        return (this);
    }

    /**
     * Returns the new line.
     *
     * @return the result
     */

    public Integer getNewLine() {
        return newLine;
    }

    /**
     * Sets the new line.
     *
     * @param newLine the new line value
     */

    public void setNewLine(Integer newLine) {
        this.newLine = newLine;
    }

    /**
     * Sets the new line and returns this instance.
     *
     * @param newLine the new line value
     * @return the result
     */

    public Position withNewLine(Integer newLine) {
        this.newLine = newLine;
        return (this);
    }

    /**
     * Returns the width.
     *
     * @return the result
     */

    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width the width value
     */

    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Sets the width and returns this instance.
     *
     * @param width the width value
     * @return the result
     */

    public Position withWidth(Integer width) {
        this.width = width;
        return (this);
    }

    /**
     * Returns the height.
     *
     * @return the result
     */

    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height the height value
     */

    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * Sets the height and returns this instance.
     *
     * @param height the height value
     * @return the result
     */

    public Position withHeight(Integer height) {
        this.height = height;
        return (this);
    }

    /**
     * Returns the x.
     *
     * @return the result
     */

    public Double getX() {
        return x;
    }

    /**
     * Sets the x.
     *
     * @param x the x value
     */

    public void setX(Double x) {
        this.x = x;
    }

    /**
     * Sets the x and returns this instance.
     *
     * @param x the x value
     * @return the result
     */

    public Position withX(Double x) {
        this.x = x;
        return (this);
    }

    /**
     * Returns the y.
     *
     * @return the result
     */

    public Double getY() {
        return y;
    }

    /**
     * Sets the y.
     *
     * @param y the y value
     */

    public void setY(Double y) {
        this.y = y;
    }

    /**
     * Sets the y and returns this instance.
     *
     * @param y the y value
     * @return the result
     */

    public Position withY(Double y) {
        this.y = y;
        return (this);
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
