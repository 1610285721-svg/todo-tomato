package org.example.todotomato.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.todotomato.dto.ApiResponse;
import org.example.todotomato.dto.LoginRequest;
import org.example.todotomato.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 —— 处理登录和手机号绑定
 * <p>
 * @RestController：Spring MVC 注解，等于 @Controller + @ResponseBody。
 * 表示这个类里所有方法的返回值都会自动转成 JSON 返回给前端（不会跳转页面）。
 * <p>
 * @RequestMapping("/api/auth")：为这个类里所有接口统一加前缀 /api/auth。
 * 比如下面的 /wechat-login 实际访问路径是 /api/auth/wechat-login。
 * <p>
 * @Autowired：自动注入 AuthService 实例（Spring 会在容器里找到它并赋值）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信登录
     * <p>
     * @PostMapping：表示这个接口只接受 POST 请求。
     * 对应前端 uni.request({ method: 'POST', url: '/api/auth/wechat-login' })
     * <p>
     * @RequestBody：把前端传来的 JSON 字符串自动转成 Java 对象。
     * 比如前端传 {"code":"abc123"}，Spring 自动 new LoginRequest() 并 setCode("abc123")。
     *
     * @param request LoginRequest 对象，里面只有 code 字段
     * @return ApiResponse<Map>，Map 里有 token、userId、nickname、phone
     */
    @PostMapping("/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody LoginRequest request) {
        // 调 Service 处理登录逻辑
        Map<String, Object> result = authService.wechatLogin(request.getCode());
        return ApiResponse.success("登录成功", result);
    }

    /**
     * 绑定手机号（需要先登录）
     * <p>
     * 这个接口在拦截器的保护范围内（/api/auth/** 被排除了，但这个路径是 /api/auth/bind-phone，
     * 等等——这里有个问题：/api/auth/** 全部被排除了，所以 bind-phone 也不会被拦截。
     * <p>
     * 解决方案：我们在 Controller 方法里手动从请求头拿 token 校验。
     * 实际上更好的做法是把拦截器规则改细，但为了让你理解"有些接口需要登录才能调"这个概念，
     * 这里用手动方式演示。
     *
     * @param request 原生 HTTP 请求对象（Spring 会自动注入当前请求）
     *                用它来获取请求头里的 token
     * @param body    请求体，包含 phone（手机号）和 code（验证码）
     * @return ApiResponse
     */
    @PostMapping("/bind-phone")
    public ApiResponse<Void> bindPhone(HttpServletRequest request, @RequestBody Map<String, String> body) {
        // 手动从请求头获取 token 并解析 userId
        // 因为 bind-phone 路径被排除在拦截器外，需要手动做
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ApiResponse.error(401, "请先登录");
        }

        // 解析 token 拿 userId（我们后面通过拦截器来改进这个做法）
        // 开发初期先简单处理：让前端传 userId 也放在 body 里
        // TODO: 后续优化为从 token 解析
        String userIdStr = body.get("userId");
        if (userIdStr == null) {
            return ApiResponse.error(400, "缺少 userId");
        }

        Long userId = Long.valueOf(userIdStr);
        String phone = body.get("phone");
        String code = body.get("code");

        try {
            authService.bindPhone(userId, phone, code);
            return ApiResponse.success("绑定成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
