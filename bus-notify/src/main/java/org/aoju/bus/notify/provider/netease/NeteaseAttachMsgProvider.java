package org.aoju.bus.notify.provider.netease;

import org.aoju.bus.notify.magic.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 云信通知
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public class NeteaseAttachMsgProvider extends AbstractNeteaseProvider {

    private static final String API = "https://api.netease.im/nimserver/msg/sendAttachMsg.action";

    public NeteaseAttachMsgProvider(NeteaseProperties properties) {
        super(properties);
    }

    @Override
    public Message send(NeteaseMsgTemplate template, Map<String, String> context) {
        Map<String, Object> param = new HashMap<>();
        param.put("from", template.getSender());
        param.put("msgtype", "0");
        param.put("to", template.getReceive());
        param.put("attach", template.getContent());
        return post(API, param);
    }
}
