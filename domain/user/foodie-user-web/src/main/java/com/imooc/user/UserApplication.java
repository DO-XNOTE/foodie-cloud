package com.imooc.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @描述:
 * @Author
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
/** 扫描 mybatis 通用 mapper所在的包 */
@MapperScan(basePackages = "com.imooc.user.mapper")
/** 扫描所有包以及相关组件包 */
@ComponentScan(basePackages = {"com.imooc","org.n3r.idworker"})
/** 服务发现的注解 */
@EnableDiscoveryClient
// TODO geign
public class UserApplication {

    public static void main(String[] args) {
         SpringApplication.run(UserApplication.class, args);
    }
}
