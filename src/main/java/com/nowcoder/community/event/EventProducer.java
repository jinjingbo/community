package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @description：事件生产者
 * @author： jinji
 * @create： 2022/7/4 10:36
 */

@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
