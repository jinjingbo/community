package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/7/5 16:13
 */
//ES类似于数据库，也是先存，再根据特殊分词器进行差分存储，然后再里面搜索关键词
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
    //这个接口中已经实现了基本的查询等功能

}