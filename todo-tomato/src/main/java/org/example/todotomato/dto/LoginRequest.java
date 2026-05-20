package org.example.todotomato.dto;

import lombok.Data;

/**
 * 登录请求 DTO（Data Transfer Object = 数据传输对象）
 * <p>
 * DTO 和 Entity 的区别：
 * - Entity：对应数据库表，和数据库字段一一匹配
 * - DTO：只用于接收前端传来的数据或返回数据给前端，不直接对应某张表
 * <p>
 * 比如这个类只有一个 code 字段，它不需要 id、nickname 等，所以单独建一个 DTO。
 */
@Data  // 自动生成 getter、setter（接收 JSON 参数时 Spring 需要 setter 来赋值）
public class LoginRequest {

    /**
     * 微信登录临时凭证
     * 前端 wx.login() 拿到后传给后端，后端拿它去微信服务器换 openid
     */
    private String code;
}
