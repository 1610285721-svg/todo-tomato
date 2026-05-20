package org.example.todotomato.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类 —— 注册拦截器、RestTemplate、跨域等
 * <p>
 * @Configuration：Spring 注解，告诉 Spring "这个类不是普通的 Bean，它用来做配置"。
 * Spring 启动时会读取这个类里的配置。
 * <p>
 * WebMvcConfigurer：Spring MVC 的配置接口，这里我们实现 addInterceptors 方法来注册拦截器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 注册 RestTemplate
     * <p>
     * @Bean：告诉 Spring "这个方法的返回值对象交给你管理，以后别人可以 @Autowired 注入它"。
     * RestTemplate 是 Spring 提供的 HTTP 客户端，相当于 Java 版的 axios，
     * 用来发送 HTTP 请求（比如调微信接口）。
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 注册拦截器
     * Spring 启动时自动调用这个方法，把我们写的拦截器装上去
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)   // 注册我们的登录拦截器
                .addPathPatterns("/api/**")          // 拦截所有 /api/ 开头的请求
                .excludePathPatterns(                // 但是排除以下路径（不需要登录也能访问）
                        "/api/auth/**"               // 登录接口不拦截（还没登录呢，怎么带 token）
                );
    }
}
