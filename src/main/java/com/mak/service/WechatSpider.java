package com.mak.service;

import com.mak.domain.Content;
import com.mak.domain.ContentRepository;
import com.mak.exception.WechatException;
import com.mak.util.DateUtil;
import com.mak.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/22 0022.
 */
@Service
@Transactional
public class WechatSpider {
    private final Logger LOGGER = LoggerFactory.getLogger(WechatSpider.class);
    @Autowired
    private ContentRepository repository;
    @Value("${wechat_ids}")
    private String wechatIds;

    public void wechatSpider() {
        String[] wechatIdArray = wechatIds.split(",");
        for (String wechatId : wechatIdArray) {
            LOGGER.info("开始抓取" + wechatId);
//            spider(wechatId);
            long begin = System.currentTimeMillis();
            mySpider(wechatId);
            long end = System.currentTimeMillis();
            LOGGER.info("抓取" + wechatId + "结束，用时："+(end-begin));
        }
    }

    private void mySpider(String wechatId) {
        boolean result = check(wechatId);
        if (!result) return;
        String listUrl = WechatUtil.getListUrl(wechatId); //获取页面url
        Map<String, Content> resultMap;
        try {
            resultMap = WechatUtil.getTopicUrlsMap(listUrl);
        } catch (WechatException e) {
            LOGGER.error(wechatId + "出现验证码");
            e.printStackTrace();
            return;
        }
        for (Map.Entry<String, Content> set : resultMap.entrySet()) {
            String url = set.getKey();
            Content content = set.getValue();
            content = WechatUtil.getTopicByUrl(url, content);
            boolean hadResult = hadSpider(wechatId,content.getContentMD5());
            if (!hadResult) continue;
            content.setCreatetime(new Date());
            content.setWechatId(wechatId);
            repository.save(content);
        }
    }

    //爬取数据
    private void spider(String wechatId) {
        boolean result = check(wechatId);
        if (!result) return;
        String listUrl = WechatUtil.getListUrl(wechatId); //获取页面url
        List<String> list = WechatUtil.getTopicUrls(listUrl); //获取当前公号列表
        for (String url : list) {
            Content content = WechatUtil.getTopicByUrl(url); //获取内容
            content.setCreatetime(new Date());
            content.setWechatId(wechatId);
            repository.save(content);
        }
    }

    //判断是否已经抓取到公众号当天发布的文章
    private boolean check(String wechatId) {
        String cur = DateUtil.format(new Date(), "yyyy-MM-dd");
        try {
            List<Content> contents = repository.findByWechatIdAndPt(wechatId, cur);
            if (contents == null || contents.size() == 0) return true;
            LOGGER.info(wechatId + "今日文章已抓取，无需再次抓取");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取数据库信息失败");
            return false;
        }
    }

    //根据url检查文章是否已经抓取
    private boolean hadSpider(String wechatId,String md5){
        List<Content> list = repository.findByWechatIdAndContentMD5(wechatId,md5);
        if (list != null && list.size() >0) return false;
        return true;
    }
}
