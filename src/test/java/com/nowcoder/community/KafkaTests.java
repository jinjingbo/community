package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/2 21:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {

    /**
     *
     *
     * @author jinjingbo
     * @version 2022/7/2 21:25
     * @param
     * 1.使用kafka之前要命令行先打开zookeeper ,kafka:
     *  bin\windows\zookeeper-server-start.bat config\zookeeper.properties
     *  bin\windows\kafka-server-start.bat config\server.properties
     *
     * 2.生产者自己设置信息，，，，，，是主动生产的
     *   消费者是只需要设计好监听的端口，Topic    是被动监听的
     *
     *
     * @return
     */
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("test", "你好");
        kafkaProducer.sendMessage("test", "在吗");
        kafkaProducer.sendMessage("test", "吃饭了码");
        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;///

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }

}

@Component
class KafkaConsumer {

    @KafkaListener(topics = {"test"})//声明Topic
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());//返回的是ConsumerRecord类型，封装过的
    }


}