package com.yby.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YbyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(YbyApiApplication.class, args);
	}

}
