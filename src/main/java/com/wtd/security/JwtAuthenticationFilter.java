package com.wtd.security;

import com.wtd.entity.User;
import com.wtd.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 读取请求头中的 Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Authorization，或者不是 Bearer 开头，直接放行给后续过滤器
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 截取真正的 JWT 字符串
        String jwt = authHeader.substring(7);

        String username;
        try {
            // 4. 从 JWT 中解析用户名
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // token 解析失败，直接继续后续过滤器
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果解析到了用户名，并且当前还没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getUsername, username)
            );

            if (user != null && jwtUtil.validateToken(jwt, username)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
