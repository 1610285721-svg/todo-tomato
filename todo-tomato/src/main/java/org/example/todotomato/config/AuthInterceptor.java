package org.example.todotomato.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器 —— 在 Controller 方法执行之前拦截请求，检查用户是否已登录
 * <p>
 * 工作原理：
 * 每个请求到达后端时，先经过这个拦截器的 preHandle 方法，
 * 验证通过 → 放行给 Controller，验证失败 → 直接返回 401。
 * <p>
 * HandlerInterceptor：Spring MVC 的拦截器接口，有两个常用方法：
 * - preHandle：Controller 执行之前执行（我们在这里校验 token）
 * - afterCompletion：Controller 执行之后执行（可以用来记录日志，本项目不需要）
 * <p>
 * @Component：交给 Spring 管理
 * @Autowired：自动注入 JwtUtils（Spring 会自动找到 JwtUtils 的实例传进来）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 请求到达 Controller 之前执行
     *
     * @param request  客户端发来的请求
     * @param response 将要返回给客户端的响应
     * @param handler  将要执行的 Controller 方法
     * @return true = 放行（交给 Controller），false = 拦截（请求到此为止）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 第一步：从请求头中拿出 Authorization
        String authHeader = request.getHeader("Authorization");

        // 第二步：检查有没有 Authorization 头，且格式必须是 "Bearer xxx"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有 token，直接返回 401
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
            return false;  // false = 拦截，不继续往后走了
        }

        // 第三步：提取 token 字符串（去掉 "Bearer " 前缀，取后面的部分）
        String token = authHeader.substring(7);

        // 第四步：校验 token 是否有效
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
            return false;
        }

        // 第五步：从 token 中取出 userId，存到 request 的属性里
        // 这样 Controller 就能通过 request.getAttribute("userId") 拿到当前用户是谁了
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;  // true = 放行，继续执行 Controller
    }
}
