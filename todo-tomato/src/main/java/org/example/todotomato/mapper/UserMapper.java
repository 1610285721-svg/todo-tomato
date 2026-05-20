package org.example.todotomato.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.todotomato.entity.User;

/**
 * 用户 Mapper 接口
 * <p>
 * @Mapper：MyBatis 的注解，告诉 Spring "这个接口是 MyBatis 管理的，帮我在容器里创建它的代理对象"。
 * 加了 @Mapper 后，Service 层就可以用 @Autowired 注入它，直接调用它的方法。
 * <p>
 * 接口里的方法名对应 mapper XML 文件里的 SQL id，参数通过 @Param 传过去。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据微信 openid 查用户
     * @param openid 微信 openid
     * @return 查到返回 User 对象，查不到返回 null
     */
    User findByOpenid(@Param("openid") String openid);

    /**
     * 根据用户 ID 查用户
     * @param id 用户 ID
     * @return 查到返回 User 对象，查不到返回 null
     */
    User findById(@Param("id") Long id);

    /**
     * 新增用户
     * @param user 用户对象（不需要设 id，数据库自增）
     */
    void insert(User user);

    /**
     * 更新手机号
     * @param userId 用户 ID
     * @param phone  新手机号
     */
    void updatePhone(@Param("userId") Long userId, @Param("phone") String phone);
}
