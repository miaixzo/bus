package org.aoju.bus.image.centre;

import org.aoju.bus.image.*;

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
