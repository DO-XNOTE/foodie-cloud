package com.imooc.config;

//import com.imooc.controller.center.intercepter.UserTokenIntercepter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @描述:
 * @Author
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 发起调用支付中心的请求， 我们使用spring的 RestTemplate 这中rest风格的请求 */
    /** 需要在这里配置成 Bean 放到Spring 容器中才可以使用， 配置当时如下 */
//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder){
//        return builder.build();
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")  /** 映射所有的资源 */
    .addResourceLocations("classpath:/META-INF/resources") /** 为swagger2映射一下 */
    .addResourceLocations("file:D:\\Java\\ideaIU\\workspaces2\\imooc_prictice\\foodie-dev\\images"); // 映射静态资源
    }

//    @Bean
//    public UserTokenIntercepter userTokenIntercepter() {
//        return new UserTokenIntercepter();
//    }

    /**
     * 注册拦截器
     * @param registry
     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 注册那些 拦截器，就直接把拦截器加进来就行，
         * 注册完成之后，那么需要拦截那些请求的路径呢？
         * 这里我们先测试  拦截请求路径    /hello
         * 然后还要把拦截注添加到 MVC中
         */
//        registry.addInterceptor(userTokenIntercepter())
//                .addPathPatterns("/hello");
        /**
         * 然后还要把拦截注添加到 MVC中
         */
//        WebMvcConfigurer.super.addInterceptors(registry);
//    }
}
