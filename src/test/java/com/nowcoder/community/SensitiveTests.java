package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/28 23:13
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里他妈的可以赌博,可以嫖娼,可以吸毒,可以开票,嗷嗷啊哈哈哈!";
        //所以叠词没问题的，因为标记了终点
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "打架这他妈的里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,啊啊哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
