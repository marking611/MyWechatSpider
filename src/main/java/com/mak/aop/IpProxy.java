package com.mak.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by makai on 2017/8/24.
 */
@Aspect
public class IpProxy {

    @Value("${ip_proxy}")
    private static String ipProxy;

    @Before(value = "execution(* com.mak.service.WechatSpider.*)")
    public void before() throws Throwable {
        try {
            String[] array = ipProxy.split(";");
            int i = (int) (Math.random()*array.length);
            System.setProperty("http.proxyHost",array[i].split(":")[0]);
            System.setProperty("http.proxyPort",array[i].split(":")[1]);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
