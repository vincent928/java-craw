package com.moon;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class})
@MapperScan(value = "com.moon")
@ComponentScan(value = "com.moon")
public class CrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawApplication.class, args);
    }

}

