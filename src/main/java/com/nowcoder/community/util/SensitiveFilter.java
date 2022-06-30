package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2022/6/28 19:36
 */
/*实现前缀树，过滤敏感词
重点理解的是算法匹配敏感词部分，指针
* */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点，前缀树头节点是空的
    private TrieNode rootNode = new TrieNode();

    //通过text文件读取敏感词，然后构造前缀树
    @PostConstruct//只编译一次
    public void init() {
        try (
                //通过text文件读取敏感词，
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;//读每一行
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // ！！！！！将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;//头
//理解为每个除root节点，包含了词节点还有下面的前缀树
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);//看这个单词
            TrieNode subNode = tempNode.getSubNode(c);//看有没有已经连接欸蓝这个节点了

            if (subNode == null) {
                // 还没连接，自己先初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //已经包含了，记得更新，看下一个
            // 指向子节点,进入下一轮循环，看下一个单词
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);//此时这个节点的指标是true,表示终点了
            }
        }
    }

    /////////////////这个是核心算法了！！！！！三个指针运动的过程，画一个图理解
    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1，指向树
        TrieNode tempNode = rootNode;
        // 指针2，指向text字符串的遍历开始
        int begin = 0;
        // 指针3，指向text字符串的遍历开始
        int position = 0;
        //注意，始终明确，使用指针2，3来圈出敏感词所在的区间的
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 是特殊符号，就跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                // 。。。。因为如果这个特殊符号出现在敏感词之间，我们也是舍弃用*代替的，不再考虑敏感词中间的值
                if (tempNode == rootNode) {//没有在敏感词中间出现
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }
            //c不是特殊符号
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            //举例子，
            if (tempNode == null) {//这个路径没在前缀树中出现过，所以只能添加这个路径的头，因为其他部分不知道（举例子：敏感词：abc,k,此时找ak）
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                //模拟过程可知
                position = ++begin;
                // 重新指向根节点，从树的头开始遍历一整条路径
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                //模拟过程可知
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
            // 提前判断postion是不是到达结尾，要跳出while,如果是，则说明begin-position这个区间不是敏感词，但是里面不一定没有敏感词
            // 举例子（敏感词abcd,bc,,,,此时的词有bcd...）
            if (position==text.length() && begin!=position){
                //右区间到终点了，但是最后一段的部分要继续缩进，只有begin=position=text.length才停止
                //改变区间，继续while循环里判断内部有没有敏感词，，递归
                // 说明还剩下一段需要判断，则把position==++begin
                // 并且当前的区间的开头字符是合法的
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;  // 前缀表从头开始了
            }
        }

        //// 将最后一批字符计入结果
        //sb.append(text.substring(begin));
        //
        //return sb.toString();
        return sb.toString();
    }

    // 判断是否为特殊符号，是就返回true
    private boolean isSymbol(Character c) {
        //CharUtils.isAsciiAlphanumeric(c),,合法符号返回true,非法是false
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }




    // 实现前缀树
    private class TrieNode {//这个表示一个新的自建的数据结构

        // 关键词结束标识，是否到叶子了
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)，为了存放此时的结点以及这个节点为root下面的前缀树
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }//是否结尾了

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }//自己设置是否是终点了

        // 添加子节点，链接，方便构造前缀树
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点，看这个节点下面的后缀（包括自己这个节点），就是路径
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }



}
