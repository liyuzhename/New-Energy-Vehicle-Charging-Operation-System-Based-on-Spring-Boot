package com.charging;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.charging.mapper")
@EnableScheduling
public class ChargingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingApplication.class, args);
    }
}
