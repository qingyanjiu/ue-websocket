package com.zeta.ai;

import com.zeta.ai.druid.EnableDruidSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.logging.LogManager;

@SpringBootApplication
@ComponentScan("com.zeta.ai")
@EnableScheduling
@EnableOpenApi
@EnableDruidSupport
@EnableCaching
@Slf4j
public class WebsocketBootstrap extends LogManager {

	private static String[] args;
	private static ConfigurableApplicationContext context;
	public static int GB_SYNC_LIMIT = 10;

	public static void main(String[] args) {
		try {
			WebsocketBootstrap.args = args;
			WebsocketBootstrap.context = SpringApplication.run(WebsocketBootstrap.class, args);
		} catch (Exception e) {
			log.error("项目启动时出现问题: \n{}\n退出启动... ", e.getMessage());
			if (context != null && context.isActive()) {
				SpringApplication.exit(context);
			}
		}
	}
	// 项目重启
	public static void restart() {
		context.close();
		WebsocketBootstrap.context = SpringApplication.run(WebsocketBootstrap.class, args);
	}

}
