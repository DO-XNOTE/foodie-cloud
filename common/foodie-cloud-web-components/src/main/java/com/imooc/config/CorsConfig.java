package com.imooc.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @描述:   前端住了的信息请求地址是8080，所以我们要进行<<<跨域>>>>>请求处理、
 *         使用CorsFilter这对象处理，其实就是一个过滤器
 * @Author
 * @Since 2021/3/29 16:14
 */
@Configuration
public class CorsConfig {

    public CorsConfig() {}

    /** 其实就是一个过滤器 ，处理跨域请求注册功能*/
    @Bean
    public CorsFilter corsFilter(){
        // 1:添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        /** 可以请求的跨域的地址 */
//        config.addAllowedOrigin("http://localhost:8080");   // * 会导致所有的请求都可以访问到，安全问题
//        config.addAllowedOrigin("http://shop.z.mukewang.com:8080");   // * 会导致所有的请求都可以访问到，安全问题
//        config.addAllowedOrigin("http://center.z.mukewang.com:8080");   // * 会导致所有的请求都可以访问到，安全问题
        config.addAllowedOrigin("192.168.15.100:8080/foodie-shop/");   // * 会导致所有的请求都可以访问到，安全问题
        config.addAllowedOrigin("192.168.15.100:8080/foodie-center/");   // * 会导致所有的请求都可以访问到，安全问题

        // 设置时候是否发送cookie信息
        config.setAllowCredentials(true);

        // 是否可以请求的方式
        config.addAllowedMethod("*");

        // 设置允许的header
        config.addAllowedHeader("*");

        //2：为url添加映射url
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", config);

        //3: 返回重新定义好的corsSource
        return new CorsFilter(corsSource);
    }
}
