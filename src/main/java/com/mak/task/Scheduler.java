package com.mak.task;


import com.mak.service.WechatSpider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时器
 * Created by Administrator on 2017/7/21 0021.
 */
@Component
public class Scheduler {
    private final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    private WechatSpider wechatSpider;

    @Scheduled(fixedRate = 1000*60*60*2) //2小时执行一次
    public void wechatSpider(){
        logger.info("开始抓取文章");
        long begin = System.currentTimeMillis();
        wechatSpider.wechatSpider();
        long end = System.currentTimeMillis();
        logger.info("抓取文章结束，一共用时："+(end-begin));
    }
}

