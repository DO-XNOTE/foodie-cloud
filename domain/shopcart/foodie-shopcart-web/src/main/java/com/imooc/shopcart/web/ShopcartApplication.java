package com.imooc.shopcart.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @描述:
 */
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@MapperScan(basePackages = "com.imooc.shopcart.mapper")
@ComponentScan(basePackages = {"com.imooc","org.n3r.idworker"})
@EnableDiscoveryClient
public class ShopcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopcartApplication.class, args);
    }
}
