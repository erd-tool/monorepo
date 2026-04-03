package com.erdcloud.erd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.erdcloud.erd", "com.erdcloud.common"})
@EnableFeignClients(basePackages = "com.erdcloud.erd.client")
public class ErdApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErdApplication.class, args);
    }
}
