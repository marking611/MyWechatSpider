package com.mak.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by makai on 2017/8/24.
 */
@Aspect
@Component
public class IpProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpProxy.class);

    @Value("${ip_proxy}")
    private String ipProxy;

    //获取可用的ip
    private Map<String,String> usable(){
        Map<String,String> map = new HashMap<>();
        while (map.isEmpty()){
            String[] array = ipProxy.split(";");
            int i = (int) (Math.random()*array.length);
            String host = array[i].split(":")[0];
            String port = array[i].split(":")[1];
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(host,Integer.valueOf(port)));
            } catch (Exception e) {
                LOGGER.error(host+":"+port+"不可用");
                continue;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.error("关闭连接失败");
                }
            }
            map.put("host",host);
            map.put("port",port);
        }
        return map;
    }

    @Before(value = "execution(* com.mak.service.WechatSpider.*(..))")
    public void before() {
        Map<String,String> map = usable();
        System.setProperty("http.proxyHost",map.get("host"));
        System.setProperty("http.proxyPort",map.get("port"));
    }
}
