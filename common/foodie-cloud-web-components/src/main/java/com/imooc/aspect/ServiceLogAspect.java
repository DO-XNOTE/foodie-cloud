package com.imooc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @描述:
 * @Author
 * @Since 2021/3/30 10:40
 */
@Aspect
@Component
public class ServiceLogAspect {
    public static final Logger log =
            LoggerFactory.getLogger(ServiceLogAspect.class);
    /**
     * Aop通知
     * 1：前置通知：在方法调用前通知
     * 2: 后置通知：方法正常调用之后通知
     * 3：环绕通知：在方法调用过程之后，都分别执行通知
     * 4: 异常通知：如果方法在调用过程中发生异常，则通知
     * 5：最终通知：在方法调用之后执行
     */
    /**
     * 切面表达式：
     * execution：代表要执行的方法体
     *  第一处： * 代表方法返回的类型 *代表所有的类型
     *  第二处： 包名代表aop监控的类所在的包
     *  第三处： ..代表该包以及子包下面所有类的方法
     *  第四处：* 代表类名, *代表所有类
     *  第五处：*(..) *代表类中的方法名，（..）表示方法中的所有的参数
     * @param joinPoint
     * @return
     * @throws Throwable
     *
     *                             .*. 微服务改造后多一个文件目录这样改
     */
    @Around("execution(* com.imooc..*.service.impl..*.*(..))")
//    @Around("execution(* com.imooc.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint)throws Throwable{
        log.info("==== 开始执行{}.{} ======",
                joinPoint.getTarget().getClass(), /** 某个service对应的某个方法 */
                joinPoint.getSignature().getName()); /** getSignature：方法的名称 */
        // 记录开始的使时间
        long start = System.currentTimeMillis();
        // 执行目标service方法
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        long takeTime = end - start;
        if (takeTime > 3000) {
            log.error("====执行结束，耗时{}毫秒 =====", takeTime);
        }else if (takeTime > 2000) {
            log.warn("====执行结束，耗时{}毫秒 =====", takeTime);
        } else {
            log.info("====执行结束，耗时{}毫秒 =====", takeTime);
        }
        return result;
    }
}
