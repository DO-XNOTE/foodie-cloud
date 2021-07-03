package com.imooc.order.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @描述:
 * @Author
 */
@Configuration
public class Rest {

    @Bean("restTemplate")
    public RestTemplate restTemplate() {
       return new RestTemplate();
    }
}
