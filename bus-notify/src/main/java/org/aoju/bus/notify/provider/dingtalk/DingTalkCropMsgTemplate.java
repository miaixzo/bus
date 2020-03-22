package org.aoju.bus.notify.provider.dingtalk;

import lombok.Getter;
import lombok.Setter;
import org.aoju.bus.notify.metric.Template;

/**
 * 钉钉通知模版
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Setter
public class DingTalkCropMsgTemplate extends Template {

    /**
     * 应用agentId
     */
    private String agentId;
    /**
     * 接收者的用户userId列表，最大列表长度：100
     */
    private String userIdList;
    /**
     * 接收者的部门id列表，最大列表长度：20,  接收者是部门id下(包括子部门下)的所有用户
     */
    private String deptIdList;
    /**
     * 是否发送给企业全部用户 true,false
     */
    private boolean toAllUser = false;

    /**
     * json字符串
     */
    private String msg;


}
