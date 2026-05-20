package org.example.todotomato.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todotomato.config.JwtUtils;
import org.example.todotomato.entity.User;
import org.example.todotomato.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证业务层 —— 处理微信登录、手机号绑定等逻辑
 * <p>
 * @Service：Spring 注解，和 @Component 作用一样（标记为 Spring 管理的 Bean），
 * 但 @Service 放在业务层，表意更清晰。Controller 层用 @Autowired 注入它。
 * <p>
 * 分层职责：
 * Controller → 接收请求，返回响应（对接前端）
 * Service   → 处理业务逻辑（本类）
 * Mapper    → 操作数据库
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;      // 操作 users 表

    @Autowired
    private JwtUtils jwtUtils;          // 生成/校验 JWT

    @Autowired
    private RestTemplate restTemplate;  // Spring 提供的 HTTP 客户端，用于调用微信接口

    /**
     * 微信小程序的 AppID，从配置文件注入
     */
    @Value("${wechat.appid}")
    private String appid;

    /**
     * 微信小程序的 AppSecret，从配置文件注入
     */
    @Value("${wechat.secret}")
    private String secret;

    /**
     * 微信登录
     * <p>
     * 流程：
     * 1. 拿前端传来的临时 code
     * 2. 调微信官方接口，用 code 换 openid
     * 3. 根据 openid 查数据库，有就登录，没有就注册新用户
     * 4. 生成 JWT token
     * 5. 返回 token + 用户信息
     *
     * @param code 前端 wx.login() 拿到的临时凭证
     * @return Map，包含 token、userId、nickname、phone
     */
    public Map<String, Object> wechatLogin(String code) {
        // 第一步：调微信接口，用临时 code 换 openid
        String openid = getOpenidFromWechat(code);

        // 第二步：根据 openid 查用户，查不到就创建新用户
        User user = userMapper.findByOpenid(openid);
        if (user == null) {
            // 首次登录，创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");  // 默认昵称，后续可改
            userMapper.insert(user);      // 插入数据库后，user.id 会自动回填
        }

        // 第三步：生成 JWT token
        String token = jwtUtils.generateToken(user.getId());

        // 第四步：组装返回数据（用 Map 而不是 DTO，因为数据简单，单独建 DTO 反而啰嗦）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());  // 可能为 null

        return result;
    }

    /**
     * 调用微信官方接口，用临时 code 换取 openid
     * <p>
     * 微信接口文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
     * 接口地址：https://api.weixin.qq.com/sns/jscode2session
     * <p>
     * RestTemplate：Spring 的 HTTP 客户端，相当于 Java 版的 axios。
     * getForObject(地址, 返回类型) = 发送 GET 请求并把结果自动转成指定类型。
     *
     * @param code 前端拿到的临时登录 code
     * @return openid（微信用户唯一标识）
     */
    private String getOpenidFromWechat(String code) {
        // 【开发模式】如果没有配置真实的微信 AppID，直接用 code 当作 openid 测试
        // 正式上线前把 application.yml 里的 appid 和 secret 换成真实值即可无缝切换
        if ("your-appid-here".equals(appid)) {
            return "dev_openid_" + code;
        }

        // 【正式模式】调用微信官方接口换取 openid
        String url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=" + appid +
                "&secret=" + secret +
                "&js_code=" + code +
                "&grant_type=authorization_code";

        String response = restTemplate.getForObject(url, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new RuntimeException("微信登录失败: " + json.get("errmsg").asText());
            }

            return json.get("openid").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage());
        }
    }

    /**
     * 绑定手机号
     *
     * @param userId 当前登录用户 ID
     * @param phone  手机号码
     * @param code   验证码（开发环境填 "1234" 即可验证通过）
     */
    public void bindPhone(Long userId, String phone, String code) {
        // 校验验证码（开发环境固定 1234，生产环境接短信服务商）
        if (!"1234".equals(code)) {
            throw new RuntimeException("验证码错误");
        }

        userMapper.updatePhone(userId, phone);
    }
}
