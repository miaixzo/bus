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
package org.aoju.bus.image.centre;

import org.aoju.bus.image.*;

/**
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public abstract class AbstractCentre implements Centre {

    /**
     * 服务器信息
     */
    protected Node node;
    /**
     * 参数信息
     */
    protected Args args;
    /**
     * 设备信息
     */
    protected Device device;
    /**
     * 业务处理
     */
    protected Rollers rollers;

    public AbstractCentre() {
        this(null, null);
    }

    public AbstractCentre(Node node, Args args) {
        this(node, args, null);
    }

    public AbstractCentre(Node node, Args args, Device device) {
        this(node, args, device, null);
    }

    public AbstractCentre(Node node, Args args, Device device, Rollers rollers) {
        this.node = node;
        this.args = args;
        this.device = device;
        this.rollers = rollers;
    }

    /**
     * 创建此生成器指定的管理器
     *
     * @return 由该生成器指定的管理器
     */
    protected abstract AbstractCentre build();

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Args getArgs() {
        return args;
    }

    public void setArgs(Args args) {
        this.args = args;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Rollers getRollers() {
        return rollers;
    }

    public void setRollers(Rollers rollers) {
        this.rollers = rollers;
    }
}
