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
package org.miaixz.bus.image.galaxy.media;

import java.util.Locale;
import java.util.Objects;

/**
 * Structured message for manifest consumers.
 *
 * @param title   the title.
 * @param message the message.
 * @param level   the level.
 * @author Kimi Liu
 * @since Java 21+
 */
public record ViewerMessage(String title, String message, Level level) {

    /**
     * The tag document msg value.
     */
    public static final String TAG_DOCUMENT_MSG = "Message";

    /**
     * The msg attribute title value.
     */
    public static final String MSG_ATTRIBUTE_TITLE = "title";

    /**
     * The msg attribute desc value.
     */
    public static final String MSG_ATTRIBUTE_DESC = "description";

    /**
     * The msg attribute level value.
     */
    public static final String MSG_ATTRIBUTE_LEVEL = "severity";

    /**
     * Creates a new instance.
     *
     * @param title   the title.
     * @param message the message.
     * @param level   the level.
     */
    public ViewerMessage {
        Objects.requireNonNull(title, "Message title cannot be null");
        Objects.requireNonNull(message, "Message content cannot be null");
        Objects.requireNonNull(level, "Message level cannot be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Message title cannot be blank");
        }
    }

    /**
     * Executes the info operation.
     *
     * @param title   the title.
     * @param message the message.
     * @return the operation result.
     */
    public static ViewerMessage info(String title, String message) {
        return new ViewerMessage(title, message, Level.INFO);
    }

    /**
     * Executes the warn operation.
     *
     * @param title   the title.
     * @param message the message.
     * @return the operation result.
     */
    public static ViewerMessage warn(String title, String message) {
        return new ViewerMessage(title, message, Level.WARN);
    }

    /**
     * Executes the error operation.
     *
     * @param title   the title.
     * @param message the message.
     * @return the operation result.
     */
    public static ViewerMessage error(String title, String message) {
        return new ViewerMessage(title, message, Level.ERROR);
    }

    /**
     * Determines whether problem.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isProblem() {
        return level.isProblem();
    }

    /**
     * Gets the display text.
     *
     * @return the display text.
     */
    public String getDisplayText() {
        return "[" + level.getDisplayName() + "] " + title + ": " + message;
    }

    /**
     * Defines the Level values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Level {

        /**
         * Constant for the info value.
         */
        INFO("Information", "info"),
        /**
         * Constant for the warn value.
         */
        WARN("Warning", "warning"),
        /**
         * Constant for the error value.
         */
        ERROR("Error", "error");

        /**
         * The display name value.
         */
        private final String displayName;

        /**
         * The xml value value.
         */
        private final String xmlValue;

        /**
         * Creates a new instance.
         *
         * @param displayName the display name.
         * @param xmlValue    the xml value.
         */
        Level(String displayName, String xmlValue) {
            this.displayName = displayName;
            this.xmlValue = xmlValue;
        }

        /**
         * Gets the display name.
         *
         * @return the display name.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the xml value.
         *
         * @return the xml value.
         */
        public String getXmlValue() {
            return xmlValue;
        }

        /**
         * Executes the from xml value operation.
         *
         * @param xmlValue the xml value.
         * @return the operation result.
         */
        public static Level fromXmlValue(String xmlValue) {
            if (xmlValue == null) {
                return INFO;
            }
            return switch (xmlValue.toLowerCase(Locale.ROOT)) {
                case "warning", "warn" -> WARN;
                case "error", "err" -> ERROR;
                default -> INFO;
            };
        }

        /**
         * Determines whether problem.
         *
         * @return true if the condition is met; otherwise false.
         */
        public boolean isProblem() {
            return this != INFO;
        }

    }

}
