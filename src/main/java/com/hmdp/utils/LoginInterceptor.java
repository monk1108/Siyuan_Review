package com.hmdp.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. check if the request should be intercepted (whether there's user in ThreadLocal)
        if (UserHolder.getUser() == null) {
            // no user in threadlocal
            response.setStatus(401);
            return false;
        }
        // user exists, let go
        return true;
    }
}
