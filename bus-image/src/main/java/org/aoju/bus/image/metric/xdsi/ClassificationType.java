/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.xdsi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassificationType")
public class ClassificationType extends RegistryObjectType {
    @XmlAttribute(name = "classificationScheme")
    protected String classificationScheme;
    @XmlAttribute(name = "classifiedObject", required = true)
    protected String classifiedObject;
    @XmlAttribute(name = "classificationNode")
    protected String classificationNode;
    @XmlAttribute(name = "nodeRepresentation")
    protected String nodeRepresentation;

    public String getClassificationScheme() {
        return this.classificationScheme;
    }

    public void setClassificationScheme(String value) {
        this.classificationScheme = value;
    }

    public String getClassifiedObject() {
        return this.classifiedObject;
    }

    public void setClassifiedObject(String value) {
        this.classifiedObject = value;
    }

    public String getClassificationNode() {
        return this.classificationNode;
    }

    public void setClassificationNode(String value) {
        this.classificationNode = value;
    }

    public String getNodeRepresentation() {
        return this.nodeRepresentation;
    }

    public void setNodeRepresentation(String value) {
        this.nodeRepresentation = value;
    }

}
