package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 结果：signature = 被拦截方法的签名对象（包含方法名、参数类型等信息）

        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 结果：autoFill = 方法上的 @AutoFill 注解对象（比如 @AutoFill(INSERT)）

        OperationType operationType = autoFill.value();
        // 结果：operationType = INSERT（从注解里取出的值，告诉切面是新增还是修改）

         Object[] args = joinPoint.getArgs();

         if(args==null||args.length==0){
             return;
         }


         Object entity=args[0];

        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        if (operationType == OperationType.INSERT) {
            // 新增操作：给4个公共字段赋值
            try {
                // 通过反射拿到实体类的4个set方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射调用set方法，给实体对象赋值
                setCreateTime.invoke(entity, now);       // 等价于 entity.setCreateTime(now)
                setCreateUser.invoke(entity, currentId);  // 等价于 entity.setCreateUser(currentId)
                setUpdateTime.invoke(entity, now);       // 等价于 entity.setUpdateTime(now)
                setUpdateUser.invoke(entity, currentId);  // 等价于 entity.setUpdateUser(currentId)
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            // 修改操作：只给2个公共字段赋值（创建时间和创建人不能改）
            try {
                // 通过反射拿到实体类的2个set方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射调用set方法，给实体对象赋值
                setUpdateTime.invoke(entity, now);       // 等价于 entity.setUpdateTime(now)
                setUpdateUser.invoke(entity, currentId);  // 等价于 entity.setUpdateUser(currentId)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
