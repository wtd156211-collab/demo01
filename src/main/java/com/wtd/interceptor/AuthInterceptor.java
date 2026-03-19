package com.wtd.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        boolean isCreateUser = "POST".equals(method) && "/api/users".equals(uri);
        boolean isGetUser = "GET".equals(method) && uri.startsWith("/api/users");

        if(isCreateUser || isGetUser){
            return true;
        }

        // 尝试从 HTTP 请求头中截获名为 "Authorization" 的隐藏令牌信息
        String token = request.getHeader("Authorization");

        // 如果没有携带 Token，直接拦截，不放行到 Controller
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=UTF-8");
            // 构造 401 报错的 JSON 字符串返回给前端
            String errorJson = "{\"code\": 401, \"msg\": \"登录凭证已缺失，请重新登录\"}";
            response.getWriter().write(errorJson);
            return false; // 返回 false 表示拦截打回
        }

        return true; // 令牌存在，返回 true 予以放行
    }
}
