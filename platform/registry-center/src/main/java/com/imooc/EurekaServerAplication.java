package com.imooc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @描述:
 * @Author
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerAplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(EurekaServerAplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
