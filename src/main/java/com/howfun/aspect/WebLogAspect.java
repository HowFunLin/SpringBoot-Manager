package com.howfun.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP记录方法运行时间
 */
@Component
@Aspect
public class WebLogAspect {

    /**
     * 线程ID --> 方法执行时间表
     */
    private Map<Long, Map<String, List<Long>>> threadMap = new ConcurrentHashMap<>(200);

    //匹配com.howfun.controller包及其子包下的所有类的所有方法
    @Pointcut("execution(* com.howfun.controller..*.*(..))")
    public void executeService() {
        // 声明切入点
    }

    /**
     * 前置通知，方法调用前被调用
     */
    @Before("executeService()")
    public void doBeforeAdvice(JoinPoint joinPoint) {
        System.out.println(joinPoint.toShortString() + " 开始");

        /*
          切入点 --> 方法执行时间（堆栈方式存入，用于计算递归调用）
         */
        Map<String, List<Long>> methodTimeMap = threadMap.get(Thread.currentThread().getId());
        List<Long> list;

        if (methodTimeMap == null) {
            methodTimeMap = new HashMap<>();
            list = new LinkedList<>();

            list.add(System.currentTimeMillis());
            methodTimeMap.put(joinPoint.toShortString(), list);
            threadMap.put(Thread.currentThread().getId(), methodTimeMap);
        } else {
            list = methodTimeMap.get(joinPoint.toShortString());

            if (list == null)
                list = new LinkedList<>();

            list.add(System.currentTimeMillis());
            methodTimeMap.put(joinPoint.toShortString(), list);
        }
    }

    /**
     * 后置通知，方法调用结束后执行
     */
    @After("executeService()")
    public void doAfterAdvice(JoinPoint joinPoint) {
        //获取目标方法的参数信息
        Object[] obj = joinPoint.getArgs();
        //通知的签名
        Signature signature = joinPoint.getSignature();

        //代理的是哪一个方法
        System.out.println("代理方法:" + signature.getName());
        //AOP代理类的名字
        System.out.println("AOP代理类的名字:" + signature.getDeclaringTypeName());

        //获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        // 遍历并存储请求参数的信息
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, String> parameterMap = new HashMap<>();

        while (enumeration.hasMoreElements()) {
            String parameter = enumeration.nextElement();
            parameterMap.put(parameter, request.getParameter(parameter));
        }

        String str = JSON.toJSONString(parameterMap);

        if (obj.length > 0)
            System.out.println("请求的参数信息为：" + str);

        System.out.println(joinPoint.toShortString() + " 结束");

        // 计算具体耗时（单位：ms）
        Map<String, List<Long>> methodTimeMap = threadMap.get(Thread.currentThread().getId());
        List<Long> list = methodTimeMap.get(joinPoint.toShortString());

        System.out.println("代理方法:" + signature.getName() + ", 耗时：" +
                (System.currentTimeMillis() - list.get(list.size() - 1)) + "ms");

        list.remove(list.size() - 1);
    }
}
