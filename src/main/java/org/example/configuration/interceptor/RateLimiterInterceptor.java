package org.example.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.lang.reflect.Method;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter tokenBucketRateLimiter;

    public RateLimiterInterceptor(TokenBucketRateLimiter tokenBucketRateLimiter) {
        this.tokenBucketRateLimiter = tokenBucketRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        if(handler instanceof  HandlerMethod handlerMethod){
            Method method = handlerMethod.getMethod();
            if(method.isAnnotationPresent(RateLimiter.class)){
                RateLimiter.RateLimiterStrategy strategy = method.getAnnotation(RateLimiter.class).strategy();
                if(strategy.equals(RateLimiter.RateLimiterStrategy.TOKEN_BUCKET)){
                    return tokenBucketRateLimiter.apply(request, response);
                }
            }
        }
        return true;
    }
}


