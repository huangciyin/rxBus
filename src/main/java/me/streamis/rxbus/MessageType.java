package me.streamis.rxbus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageType {
  String value() default "";
  boolean fail() default false;
}
