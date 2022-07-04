package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.protocol.MessageSender;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/4 10:38
 */
/**
 *
 *
 * @author jinjingbo
 * @version 2022/7/4 10:53
 * @param
 * • 触发事件 -
 * 评论后，发布通知 - 点赞后，发布通知 - 关注后，发布通知 •
 *
 * 处理事件 -
 * 封装事件对象 - 开发事件的生产者 - 开发事件的消费者
 * @return
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})//监听主题，有三个主题
    public void handleCommentMessage(ConsumerRecord record) {//封装后的
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//转化json为event
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        // 系统发送通知，
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());//比如点赞呀，评论了，发给帖子的作者
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {//看event的map中有没有其他的数据
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
