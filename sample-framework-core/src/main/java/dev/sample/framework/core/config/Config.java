package dev.sample.framework.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * 設定ファイル用CDI修飾子.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface Config {

  /**
   * インジェクションしたい設定ファイルのキー.
   *
   * @return キー
   */
  @Nonbinding
  String value() default "";

  /**
   * インジェクションしたいJava Beanの型.
   *
   * @return Java Beanの型
   */
  @Nonbinding
  Class<? extends Configurable> type() default Configurable.class;

}
