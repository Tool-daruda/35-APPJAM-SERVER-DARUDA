package com.daruda.darudaserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Client;

@Configuration
public class FeignConfig {
	@Bean
	public Client feignClient() {
		return new Client.Default(null, null);
	}
}
