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
package org.miaixz.bus.image.metric;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;

/**
 * Represents the StorageOptions type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StorageOptions implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852261697969L;

    /**
     * The level of support value.
     */
    private LevelOfSupport levelOfSupport;

    /**
     * The digital signature support value.
     */
    private DigitalSignatureSupport digitalSignatureSupport;

    /**
     * The element coercion value.
     */
    private ElementCoercion elementCoercion;

    /**
     * Creates a new instance.
     */
    public StorageOptions() {
        this(LevelOfSupport.UNSPECIFIED, DigitalSignatureSupport.UNSPECIFIED, ElementCoercion.UNSPECIFIED);
    }

    /**
     * Creates a new instance.
     *
     * @param levelOfSupport                 the level of support.
     * @param levelOfDigitalSignatureSupport the level of digital signature support.
     * @param getElementCoercion             the get element coercion.
     */
    public StorageOptions(LevelOfSupport levelOfSupport, DigitalSignatureSupport levelOfDigitalSignatureSupport,
            ElementCoercion getElementCoercion) {
        this.levelOfSupport = levelOfSupport;
        this.digitalSignatureSupport = levelOfDigitalSignatureSupport;
        this.elementCoercion = getElementCoercion;
    }

    /**
     * Executes the value of operation.
     *
     * @param extNeg the ext neg.
     * @return the operation result.
     */
    public static StorageOptions valueOf(ExtendedNegotiation extNeg) {
        return new StorageOptions(LevelOfSupport.valueOf(extNeg.getField(0, (byte) 3)),
                DigitalSignatureSupport.valueOf(extNeg.getField(2, (byte) 0)),
                ElementCoercion.valueOf(extNeg.getField(4, (byte) 2)));
    }

    /**
     * Gets the level of support.
     *
     * @return the level of support.
     */
    public final LevelOfSupport getLevelOfSupport() {
        return levelOfSupport;
    }

    /**
     * Sets the level of support.
     *
     * @param levelOfSupport the level of support.
     */
    public final void setLevelOfSupport(LevelOfSupport levelOfSupport) {
        this.levelOfSupport = levelOfSupport;
    }

    /**
     * Gets the digital signature support.
     *
     * @return the digital signature support.
     */
    public final DigitalSignatureSupport getDigitalSignatureSupport() {
        return digitalSignatureSupport;
    }

    /**
     * Sets the digital signature support.
     *
     * @param digitalSignatureSupport the digital signature support.
     */
    public final void setDigitalSignatureSupport(DigitalSignatureSupport digitalSignatureSupport) {
        this.digitalSignatureSupport = digitalSignatureSupport;
    }

    /**
     * Gets the element coercion.
     *
     * @return the element coercion.
     */
    public final ElementCoercion getElementCoercion() {
        return elementCoercion;
    }

    /**
     * Sets the element coercion.
     *
     * @param elementCoercion the element coercion.
     */
    public final void setElementCoercion(ElementCoercion elementCoercion) {
        this.elementCoercion = elementCoercion;
    }

    /**
     * Converts this value to extended negotiation information.
     *
     * @return the operation result.
     */
    public byte[] toExtendedNegotiationInformation() {
        return new byte[] { (byte) levelOfSupport.ordinal(), 0, (byte) digitalSignatureSupport.ordinal(), 0,
                (byte) elementCoercion.ordinal(), 0 };
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        return levelOfSupport.hashCode() + digitalSignatureSupport.hashCode() + elementCoercion.hashCode();
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param o the o.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof StorageOptions other))
            return false;

        return levelOfSupport == other.levelOfSupport && digitalSignatureSupport == other.digitalSignatureSupport
                && elementCoercion == other.elementCoercion;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "StorageOptions[levelOfSupport=" + levelOfSupport.ordinal() + ", digitalSignatureSupport="
                + digitalSignatureSupport.ordinal() + ", elementCoercion=" + elementCoercion.ordinal() + "]";
    }

    /**
     * Defines the LevelOfSupport values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum LevelOfSupport {

        /**
         * Constant for the level 0 value.
         */
        LEVEL_0,
        /**
         * Constant for the level 1 value.
         */
        LEVEL_1,
        /**
         * Constant for the level 2 value.
         */
        LEVEL_2,
        /**
         * Constant for the unspecified value.
         */
        UNSPECIFIED;

        /**
         * Executes the value of operation.
         *
         * @param level the level.
         * @return the operation result.
         */
        public static LevelOfSupport valueOf(int level) {
            switch (level) {
                case 0:
                    return LEVEL_0;

                case 1:
                    return LEVEL_1;

                case 2:
                    return LEVEL_2;
            }
            return UNSPECIFIED;
        }

    }

    /**
     * Defines the DigitalSignatureSupport values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum DigitalSignatureSupport {

        /**
         * Constant for the unspecified value.
         */
        UNSPECIFIED,
        /**
         * Constant for the level 1 value.
         */
        LEVEL_1,
        /**
         * Constant for the level 2 value.
         */
        LEVEL_2,
        /**
         * Constant for the level 3 value.
         */
        LEVEL_3;

        /**
         * Executes the value of operation.
         *
         * @param level the level.
         * @return the operation result.
         */
        public static DigitalSignatureSupport valueOf(int level) {
            switch (level) {
                case 1:
                    return LEVEL_1;

                case 2:
                    return LEVEL_2;

                case 3:
                    return LEVEL_3;
            }
            return UNSPECIFIED;
        }

    }

    /**
     * Defines the ElementCoercion values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ElementCoercion {

        /**
         * Constant for the no value.
         */
        NO,
        /**
         * Constant for the yes value.
         */
        YES,
        /**
         * Constant for the unspecified value.
         */
        UNSPECIFIED;

        /**
         * Executes the value of operation.
         *
         * @param i the i.
         * @return the operation result.
         */
        public static ElementCoercion valueOf(int i) {
            switch (i) {
                case 0:
                    return NO;

                case 1:
                    return YES;
            }
            return UNSPECIFIED;
        }

    }

}
