package org.aoju.bus.pay;

public class Status {

    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //支付中
    public static final byte STATE_SUCCESS = 2; //支付成功
    public static final byte STATE_FAIL = 3; //支付失败
    public static final byte STATE_CANCEL = 4; //已撤销
    public static final byte STATE_REFUND = 5; //已退款
    public static final byte STATE_CLOSED = 6; //订单关闭

    public static final byte REFUND_STATE_NONE = 0; //未发生实际退款
    public static final byte REFUND_STATE_SUB = 1; //部分退款
    public static final byte REFUND_STATE_ALL = 2; //全额退款


    public static final byte DIVISION_MODE_FORBID = 0; //该笔订单不允许分账
    public static final byte DIVISION_MODE_AUTO = 1; //支付成功按配置自动完成分账
    public static final byte DIVISION_MODE_MANUAL = 2; //商户手动分账(解冻商户金额)

    public static final byte DIVISION_STATE_UNHAPPEN = 0; //未发生分账
    public static final byte DIVISION_STATE_WAIT_TASK = 1; //等待分账任务处理
    public static final byte DIVISION_STATE_ING = 2; //分账处理中
    public static final byte DIVISION_STATE_FINISH = 3; //分账任务已结束(不体现状态)

}
