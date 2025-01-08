package com.daruda.darudaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //시간 자동 생성을 위해서는 Auditing 기능 활성화
public class DarudaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarudaApplication.class, args);
	}

}
