package org.example;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private static Map<String, Object[]> buckets = new HashMap<>();
    private static int RATE_LIMIT_PER_IP = 10;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        if(handler instanceof  HandlerMethod handlerMethod){
            Method method = handlerMethod.getMethod();
            if(method.isAnnotationPresent(RateLimiter.class)){
                RateLimiter.RateLimiterStrategy strategy = method.getAnnotation(RateLimiter.class).strategy();
                if(strategy.equals(RateLimiter.RateLimiterStrategy.TOKEN_BUCKET)){
                    return verifyRateLimitByBucket(request, response);
                }
            }
        }
        return true;
    }

    private boolean verifyRateLimitByBucket(HttpServletRequest request, HttpServletResponse response) {
        String ip = getClientIP(request);
        buckets.putIfAbsent(ip, new Object[]{0, Instant.now()});

        Object[] usedToken = buckets.get(ip);
        Instant baseTime = (Instant) usedToken[1];

        if(Instant.now().isAfter(baseTime.plus(1, ChronoUnit.SECONDS))){
            buckets.put(ip, new Object[]{0, Instant.now()});
            return true;
        }else {
            if(RATE_LIMIT_PER_IP > (int) usedToken[0]){
                int usedTokenCount = (int) usedToken[0];
                usedToken[0] = usedTokenCount+1;
                return true;
            }else{
                response.setStatus(429);
                return false;
            }
        }
    }


    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}


