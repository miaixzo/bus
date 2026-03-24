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
 * @author Kimi Liu
 * @since Java 21+
 */
public class StorageOptions implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852261697969L;

    private LevelOfSupport levelOfSupport;

    private DigitalSignatureSupport digitalSignatureSupport;

    private ElementCoercion elementCoercion;

    public StorageOptions() {
        this(LevelOfSupport.UNSPECIFIED, DigitalSignatureSupport.UNSPECIFIED, ElementCoercion.UNSPECIFIED);
    }

    public StorageOptions(LevelOfSupport levelOfSupport, DigitalSignatureSupport levelOfDigitalSignatureSupport,
            ElementCoercion getElementCoercion) {
        this.levelOfSupport = levelOfSupport;
        this.digitalSignatureSupport = levelOfDigitalSignatureSupport;
        this.elementCoercion = getElementCoercion;
    }

    public static StorageOptions valueOf(ExtendedNegotiation extNeg) {
        return new StorageOptions(LevelOfSupport.valueOf(extNeg.getField(0, (byte) 3)),
                DigitalSignatureSupport.valueOf(extNeg.getField(2, (byte) 0)),
                ElementCoercion.valueOf(extNeg.getField(4, (byte) 2)));
    }

    public final LevelOfSupport getLevelOfSupport() {
        return levelOfSupport;
    }

    public final void setLevelOfSupport(LevelOfSupport levelOfSupport) {
        this.levelOfSupport = levelOfSupport;
    }

    public final DigitalSignatureSupport getDigitalSignatureSupport() {
        return digitalSignatureSupport;
    }

    public final void setDigitalSignatureSupport(DigitalSignatureSupport digitalSignatureSupport) {
        this.digitalSignatureSupport = digitalSignatureSupport;
    }

    public final ElementCoercion getElementCoercion() {
        return elementCoercion;
    }

    public final void setElementCoercion(ElementCoercion elementCoercion) {
        this.elementCoercion = elementCoercion;
    }

    public byte[] toExtendedNegotiationInformation() {
        return new byte[] { (byte) levelOfSupport.ordinal(), 0, (byte) digitalSignatureSupport.ordinal(), 0,
                (byte) elementCoercion.ordinal(), 0 };
    }

    @Override
    public int hashCode() {
        return levelOfSupport.hashCode() + digitalSignatureSupport.hashCode() + elementCoercion.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof StorageOptions other))
            return false;

        return levelOfSupport == other.levelOfSupport && digitalSignatureSupport == other.digitalSignatureSupport
                && elementCoercion == other.elementCoercion;
    }

    @Override
    public String toString() {
        return "StorageOptions[levelOfSupport=" + levelOfSupport.ordinal() + ", digitalSignatureSupport="
                + digitalSignatureSupport.ordinal() + ", elementCoercion=" + elementCoercion.ordinal() + "]";
    }

    public enum LevelOfSupport {

        LEVEL_0, LEVEL_1, LEVEL_2, UNSPECIFIED;

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

    public enum DigitalSignatureSupport {

        UNSPECIFIED, LEVEL_1, LEVEL_2, LEVEL_3;

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

    public enum ElementCoercion {

        NO, YES, UNSPECIFIED;

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
