package com.daruda.darudaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;

@EnableFeignClients
@EnableWebMvc
@SpringBootApplication(exclude = {S3AutoConfiguration.class})
public class DarudaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarudaApplication.class, args);
	}

}
