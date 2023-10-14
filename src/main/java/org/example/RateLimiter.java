package org.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface RateLimiter {
    RateLimiterStrategy strategy();

    enum RateLimiterStrategy {
        TOKEN_BUCKET,
    }
}
