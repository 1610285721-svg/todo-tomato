package org.example.todotomato.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类 —— 对应数据库 users 表
 * <p>
 * 实体类：纯粹存数据的类，一个属性对应表里一个字段。
 * MyBatis 会自动把数据库字段映射到这个类的属性上（开了驼峰转换后
 * 数据库的 user_id 会自动映射成 userId）。
 */
@Data               // Lombok 注解：编译时自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok 注解：自动生成无参构造方法（MyBatis 查数据映射到对象时需要无参构造）
@AllArgsConstructor // Lombok 注解：自动生成全参构造方法（方便 new 对象时一次性赋值）
public class User {

    private Long id;            // 用户 ID，数据库自增主键
    private String openid;      // 微信 openid，每个用户在每个小程序里唯一
    private String phone;       // 手机号，可以为 null（用户未绑定时）
    private String nickname;    // 微信昵称
    private String avatar;      // 微信头像 URL
    private LocalDateTime createdAt;  // 创建时间

    // 不需要手写 getter/setter/构造方法，@Data 和 @NoArgsConstructor 已经帮你生成了
}
