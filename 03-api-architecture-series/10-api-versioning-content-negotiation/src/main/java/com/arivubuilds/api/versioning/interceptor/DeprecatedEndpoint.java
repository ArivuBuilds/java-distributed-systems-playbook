package com.arivubuilds.api.versioning.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecatedEndpoint {
    String sunsetDate() default "Wed, 31 Mar 2027 23:59:59 GMT";
    String successorUri();
}