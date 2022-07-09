package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/9 17:44
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {
    /**
     *
     *
     * @author jinjingbo
     * @version 2022/7/9 17:46
     * @param null
     * @return
     *
     * Spring Boot Testing
     * - 依赖：spring-boot-starter-test
     * - 包括：Junit、Spring Test、AssertJ、...
     * • Test Case
     * - 要求：保证测试方法的独立性。
     * - 步骤：初始化数据、执行测试代码、验证测试结果、清理测试数据。- 常用注解：@BeforeClass、@AfterClass、@Before、@After。
     */
//项目上线前对每个模块的方法，独立的test
    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    @BeforeClass//class之前执行
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }

    @Before//每个测试方法之前执行
    public void before() {
        System.out.println("before");

        // 初始化测试数据，，每次测试之前，都新建一个post进行操作
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    @After
    public void after() {
        System.out.println("after");

        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertNotNull(post);
        Assert.assertEquals(data.getTitle(), post.getTitle());
        Assert.assertEquals(data.getContent(), post.getContent());
    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }

}
