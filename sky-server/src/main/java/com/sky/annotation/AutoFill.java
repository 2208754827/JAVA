package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行公共字段自动填充
 */
@Target(ElementType.METHOD)          // 规定这个注解只能加在方法上
@Retention(RetentionPolicy.RUNTIME)  // 规定运行时JVM能读取到这个注解（切面要靠它判断）
public @interface AutoFill {

    // 数据库操作类型：UPDATE 或 INSERT，使用时必须传一个值
    OperationType value();

}
