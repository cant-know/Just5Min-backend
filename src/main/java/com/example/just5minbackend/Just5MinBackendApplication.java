package com.example.just5minbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.example.just5minbackend.mapper")
public class Just5MinBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Just5MinBackendApplication.class, args);
    }

}
