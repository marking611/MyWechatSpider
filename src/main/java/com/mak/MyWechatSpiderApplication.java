package com.mak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //执行定时任务
public class MyWechatSpiderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyWechatSpiderApplication.class, args);
	}
}
