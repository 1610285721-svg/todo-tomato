package org.example.todotomato.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 手动配置类
 * <p>
 * 为什么需要这个类？
 * mybatis-spring-boot-starter 3.0.4 的自动配置是为 Spring Boot 3.x 设计的，
 * 当前项目用的是 Spring Boot 4.0.6，自动配置不生效，导致 SqlSessionFactory
 * 没有被自动创建。所以这里手动创建 SqlSessionFactory 和 SqlSessionTemplate。
 * <p>
 * SqlSessionFactory：MyBatis 的核心对象，负责创建 SqlSession（一次数据库会话）
 * SqlSessionTemplate：线程安全的 SqlSession，Mapper 接口代理需要用它
 */
@Configuration
public class MyBatisConfig {

    /**
     * 创建 SqlSessionFactory
     * <p>
     * @Bean：把方法返回值交给 Spring 管理，之后其他 Bean 可以 @Autowired 注入
     * @Qualifier：指定使用哪个 DataSource（如果只有一个 DataSource，可省略）
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        // 1. 设置数据源
        factoryBean.setDataSource(dataSource);

        // 2. 设置实体类别名包（和 application.yml 里的 type-aliases-package 一致）
        factoryBean.setTypeAliasesPackage("org.example.todotomato.entity");

        // 3. 设置 mapper XML 文件位置
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/*.xml")
        );

        // 4. 设置驼峰转换
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);

        return factoryBean.getObject();
    }

    /**
     * 创建 SqlSessionTemplate
     * 所有 @Mapper 接口的代理对象需要 SqlSessionTemplate 才能操作数据库
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
