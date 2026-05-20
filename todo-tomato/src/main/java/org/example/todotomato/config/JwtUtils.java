package org.example.todotomato.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 —— 负责 Token 的生成和校验
 * <p>
 * JWT（JSON Web Token）是什么？
 * 通俗理解：登录后后端给你一张"电子身份证"，之后每次请求带着它就行。
 * 后端不用存 session，直接解密 token 就知道你是谁。小程序不能像网页那样用
 * Cookie+Session（小程序没有跨页面共享 Cookie 的概念），所以 JWT 是最佳选择。
 * <p>
 * JWT 的结构：xxxxx.yyyyy.zzzzz（三段，用 . 分隔）
 * - 第一段 Header：算法类型
 * - 第二段 Payload：存的数据（比如 userId）
 * - 第三段 Signature：签名，防止数据被篡改
 * <p>
 * @Component：Spring 注解，告诉 Spring "把这个类的对象交给你管理"。
 * 然后其他类（比如 Service）可以用 @Autowired 注入它，直接调用它的方法。
 */
@Component
public class JwtUtils {

    /**
     * 签名密钥
     * @Value：把 application.yml 里的配置值注入到这个字段
     * "${jwt.secret}" 对应 yml 里的 jwt.secret: todo-tomato-jwt-secret-key-2026-nicotine
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（秒）
     * "${jwt.expiration}" 对应 yml 里的 jwt.expiration: 604800（7天）
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 把字符串密钥转成 Java 加密库能用的密钥对象
     * 每次调用生成同一个 SecretKey 对象
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户 ID，存到 token 的 payload 里
     * @return 生成的 JWT 字符串（三段 base64，用 . 分隔）
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration * 1000);  // 过期时间 = 当前时间 + 7天

        return Jwts.builder()
                .subject(String.valueOf(userId))        // 主体信息，存 userId
                .issuedAt(now)                          // 签发时间
                .expiration(expireDate)                 // 过期时间
                .signWith(getKey())                     // 签名（防篡改）
                .compact();                             // 生成最终 token 字符串
    }

    /**
     * 从一个 token 里解析出所有数据（payload）
     *
     * @param token JWT 字符串
     * @return Claims 对象，可以 .getSubject() 拿到 userId，.getExpiration() 拿到过期时间
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())       // 用同一个密钥验证签名
                .build()
                .parseSignedClaims(token)   // 解析 token
                .getPayload();              // 拿到 payload 部分
    }

    /**
     * 校验 token 是否有效
     * - 签名是否正确（有没有被篡改）
     * - 是否过期
     *
     * @param token JWT 字符串
     * @return true = 有效，false = 无效或过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);  // 能解析成功 = 签名正确 + 未过期
            return true;
        } catch (Exception e) {
            // 签名错误、token 格式不对、已过期等情况都会抛异常，统一返回 false
            return false;
        }
    }

    /**
     * 从 token 中取出 userId
     * 调用前应该先用 validateToken() 确认 token 有效
     *
     * @param token JWT 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());  // subject 就是我们存进去的 userId
    }
}
