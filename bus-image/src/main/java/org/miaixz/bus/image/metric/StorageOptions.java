/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.metric;

import org.miaixz.bus.image.metric.pdu.ExtendedNegotiation;

import java.io.Serializable;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StorageOptions implements Serializable {

    private static final long serialVersionUID = -1L;

    private LevelOfSupport levelOfSupport;

    private DigitalSignatureSupport digitalSignatureSupport;

    private ElementCoercion elementCoercion;

    public StorageOptions() {
        this(LevelOfSupport.UNSPECIFIED,
                DigitalSignatureSupport.UNSPECIFIED,
                ElementCoercion.UNSPECIFIED);
    }

    public StorageOptions(LevelOfSupport levelOfSupport,
                          DigitalSignatureSupport levelOfDigitalSignatureSupport,
                          ElementCoercion getElementCoercion) {
        this.levelOfSupport = levelOfSupport;
        this.digitalSignatureSupport = levelOfDigitalSignatureSupport;
        this.elementCoercion = getElementCoercion;
    }

    public static StorageOptions valueOf(ExtendedNegotiation extNeg) {
        return new StorageOptions(
                LevelOfSupport.valueOf(extNeg.getField(0, (byte) 3)),
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

    public final void setDigitalSignatureSupport(
            DigitalSignatureSupport digitalSignatureSupport) {
        this.digitalSignatureSupport = digitalSignatureSupport;
    }

    public final ElementCoercion getElementCoercion() {
        return elementCoercion;
    }

    public final void setElementCoercion(ElementCoercion elementCoercion) {
        this.elementCoercion = elementCoercion;
    }

    public byte[] toExtendedNegotiationInformation() {
        return new byte[]{
                (byte) levelOfSupport.ordinal(), 0,
                (byte) digitalSignatureSupport.ordinal(), 0,
                (byte) elementCoercion.ordinal(), 0};
    }

    @Override
    public int hashCode() {
        return levelOfSupport.hashCode()
                + digitalSignatureSupport.hashCode()
                + elementCoercion.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof StorageOptions other))
            return false;

        return levelOfSupport == other.levelOfSupport
                && digitalSignatureSupport == other.digitalSignatureSupport
                && elementCoercion == other.elementCoercion;
    }

    @Override
    public String toString() {
        return "StorageOptions[levelOfSupport=" + levelOfSupport.ordinal()
                + ", digitalSignatureSupport=" + digitalSignatureSupport.ordinal()
                + ", elementCoercion=" + elementCoercion.ordinal() + "]";
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
