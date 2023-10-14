package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimiterTargetController {

    @GetMapping("/unlimited")
    public String unlimited(){
        return "Unlimited";
    }

    @RateLimiter(strategy = RateLimiter.RateLimiterStrategy.TOKEN_BUCKET)
    @GetMapping("limited")
    public String limited(){
        return "Limited";
    }
}
