package org.example.todotomato;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * <p>
 * @SpringBootApplication：核心注解，三合一——
 * - @Configuration：允许定义 @Bean
 * - @EnableAutoConfiguration：自动装配（根据 pom.xml 的依赖自动配置）
 * - @ComponentScan：扫描当前包及子包下的 @Component、@Service、@Controller 等
 * <p>
 * @MapperScan：MyBatis 专属，告诉 MyBatis 去哪个包下找 Mapper 接口。
 * 不加这个的话 @Mapper 注解可能不会被 MyBatis 识别到。
 */
@SpringBootApplication
@MapperScan("org.example.todotomato.mapper")
public class TodoTomatoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoTomatoApplication.class, args);
    }

}
