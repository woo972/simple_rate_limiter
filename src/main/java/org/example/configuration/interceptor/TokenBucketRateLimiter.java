package org.example.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.configuration.util.HttpRequestHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenBucketRateLimiter {
    private static Map<String, Object[]> TOKEN_BUCKET = new HashMap<>();
    private static int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    @Value("${rate_limiter.token_bucket.request_threshold}")
    private int RATE_LIMIT_THRESHOLD_PER_IP;
    @Value("${rate_limiter.token_bucket.reset_time_second}")
    private int RESET_TOKEN_BUCKET_TIME;


    public boolean apply(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = HttpRequestHelper.getClientIP(request);
        initBucket(clientIp);

        int usedTokenCount = (int) TOKEN_BUCKET.get(clientIp)[0];
        Instant baseTime = (Instant) TOKEN_BUCKET.get(clientIp)[1];

        if(isTimeToResetBucket(baseTime)){
            refillToken(clientIp);
            return true;
        }

        if(RATE_LIMIT_THRESHOLD_PER_IP > usedTokenCount){
            consumeToken(clientIp);
            return true;
        }else{
            response.setStatus(HTTP_STATUS_TOO_MANY_REQUESTS);
            return false;
        }
    }


    private void initBucket(String clientIp) {
        TOKEN_BUCKET.putIfAbsent(clientIp, new Object[]{0, Instant.now()});
    }

    private void refillToken(String clientIp) {
        TOKEN_BUCKET.put(clientIp, new Object[]{0, Instant.now()});
    }
    private boolean isTimeToResetBucket(Instant baseTime) {
        return Instant.now().isAfter(baseTime.plus(RESET_TOKEN_BUCKET_TIME, ChronoUnit.SECONDS));
    }

    private void consumeToken(String clientIp) {
        TOKEN_BUCKET.get(clientIp)[0] = (int) TOKEN_BUCKET.get(clientIp)[0] + 1;
    }
}
