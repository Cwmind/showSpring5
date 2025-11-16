package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.*;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class A18 {

    // 定义切面类，包含各种通知类型
    static class Aspect {
        // 前置通知1：在目标方法执行前执行
        @Before("execution(* foo())")
        public void before1() {
            System.out.println("before1");
        }

        // 前置通知2：在目标方法执行前执行
        @Before("execution(* foo())")
        public void before2() {
            System.out.println("before2");
        }

        // 普通方法，没有通知注解，不会被识别为切面通知
        public void after() {
            System.out.println("after");
        }

        // 返回后通知：在目标方法正常返回后执行
        @AfterReturning("execution(* foo())")
        public void afterReturning() {
            System.out.println("afterReturning");
        }

        // 异常通知：在目标方法抛出异常后执行
        @AfterThrowing("execution(* foo())")
        public void afterThrowing(Exception e) {
            System.out.println("afterThrowing " + e.getMessage());
        }

        // 环绕通知：在目标方法执行前后都可以执行逻辑
        @Around("execution(* foo())")
        public Object around(ProceedingJoinPoint pjp) throws Throwable {
            try {
                System.out.println("around...before");
                // 执行目标方法
                return pjp.proceed();
            } finally {
                System.out.println("around...after");
            }
        }
    }

    // 目标类，包含要被增强的方法
    static class Target {
        public void foo() {
            System.out.println("target foo");
        }
    }

    // 抑制所有警告
    @SuppressWarnings("all")
    public static void main(String[] args) throws Throwable {

        // 创建切面实例工厂，使用单例模式创建Aspect实例
        AspectInstanceFactory factory = new SingletonAspectInstanceFactory(new Aspect());
        // 1. 高级切面转低级切面类：将注解形式的切面转换为Spring AOP底层的Advisor列表
        List<Advisor> list = new ArrayList<>();

        // 遍历Aspect类中的所有方法，识别带有切面注解的方法
        for (Method method : Aspect.class.getDeclaredMethods()) {
            // 处理@Before注解的方法
            if (method.isAnnotationPresent(Before.class)) {
                // 解析切点表达式
                String expression = method.getAnnotation(Before.class).value();
                // 创建AspectJ表达式切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(expression);
                // 创建前置通知 advice
                AspectJMethodBeforeAdvice advice = new AspectJMethodBeforeAdvice(method, pointcut, factory);
                // 创建切面 Advisor，将切点和通知绑定
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
                list.add(advisor);
            }
            // 处理@AfterReturning注解的方法
            else if (method.isAnnotationPresent(AfterReturning.class)) {
                // 解析切点表达式
                String expression = method.getAnnotation(AfterReturning.class).value();
                // 创建AspectJ表达式切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(expression);
                // 创建返回后通知 advice
                AspectJAfterReturningAdvice advice = new AspectJAfterReturningAdvice(method, pointcut, factory);
                // 创建切面 Advisor
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
                list.add(advisor);
            }
            // 处理@Around注解的方法
            else if (method.isAnnotationPresent(Around.class)) {
                // 解析切点表达式
                String expression = method.getAnnotation(Around.class).value();
                // 创建AspectJ表达式切点
                AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
                pointcut.setExpression(expression);
                // 创建环绕通知 advice
                AspectJAroundAdvice advice = new AspectJAroundAdvice(method, pointcut, factory);
                // 创建切面 Advisor
                Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
                list.add(advisor);
            }
        }

        // 打印所有创建的Advisor
        for (Advisor advisor : list) {
            System.out.println(advisor);
        }

        /*
            @Before 前置通知会被转换为下面原始的 AspectJMethodBeforeAdvice 形式, 该对象包含了如下信息
                a. 通知代码从哪儿来
                b. 切点是什么
                c. 通知对象如何创建, 本例共用同一个 Aspect 对象
            类似的通知还有
                1. AspectJAroundAdvice (环绕通知)
                2. AspectJAfterReturningAdvice
                3. AspectJAfterThrowingAdvice (环绕通知)
                4. AspectJAfterAdvice (环绕通知)
         */

        // 2. 通知统一转换为环绕通知 MethodInterceptor
        /*

            其实无论 ProxyFactory 基于哪种方式创建代理, 最后干活(调用 advice)的是一个 MethodInvocation 对象
                a. 因为 advisor 有多个, 且一个套一个调用, 因此需要一个调用链对象, 即 MethodInvocation
                b. MethodInvocation 要知道 advice 有哪些, 还要知道目标, 调用次序如下

                将 MethodInvocation 放入当前线程
                    |-> before1 ----------------------------------- 从当前线程获取 MethodInvocation
                    |                                             |
                    |   |-> before2 --------------------          | 从当前线程获取 MethodInvocation
                    |   |                              |          |
                    |   |   |-> target ------ 目标   advice2    advice1
                    |   |                              |          |
                    |   |-> after2 ---------------------          |
                    |                                             |
                    |-> after1 ------------------------------------
                c. 从上图看出, 环绕通知才适合作为 advice, 因此其他 before、afterReturning 都会被转换成环绕通知
                d. 统一转换为环绕通知, 体现的是设计模式中的适配器模式
                    - 对外是为了方便使用要区分 before、afterReturning
                    - 对内统一都是环绕通知, 统一用 MethodInterceptor 表示

            此步获取所有执行时需要的 advice (静态)
                a. 即统一转换为 MethodInterceptor 环绕通知, 这体现在方法名中的 Interceptors 上
                b. 适配如下
                  - MethodBeforeAdviceAdapter 将 @Before AspectJMethodBeforeAdvice 适配为 MethodBeforeAdviceInterceptor
                  - AfterReturningAdviceAdapter 将 @AfterReturning AspectJAfterReturningAdvice 适配为 AfterReturningAdviceInterceptor
         */

        // 创建目标对象实例
        Target target = new Target();
        // 创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory();
        // 设置目标对象
        proxyFactory.setTarget(target);
        // 添加调用暴露拦截器，用于将MethodInvocation放入当前线程上下文
        proxyFactory.addAdvice(ExposeInvocationInterceptor.INSTANCE);
        // 添加所有切面
        proxyFactory.addAdvisors(list);

        // 打印分隔线
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 获取目标方法的拦截器链（已转换为MethodInterceptor形式）
        List<Object> methodInterceptorList = proxyFactory.getInterceptorsAndDynamicInterceptionAdvice(Target.class.getMethod("foo"), Target.class);
        // 打印所有拦截器
        for (Object o : methodInterceptorList) {
            System.out.println(o);
        }

        // 打印分隔线
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 3. 创建并执行调用链 (环绕通知s + 目标)
        // 创建反射方法调用对象，封装了调用链的执行逻辑
        MethodInvocation methodInvocation = new ReflectiveMethodInvocation(
                null,           // 代理对象，这里为null
                target,         // 目标对象
                Target.class.getMethod("foo"), // 目标方法
                new Object[0],  // 方法参数
                Target.class,   // 目标类
                methodInterceptorList // 方法拦截器列表
        );
        // 执行整个调用链
        methodInvocation.proceed();

        /*
            学到了什么
                a. 无参数绑定的通知如何被调用
                b. MethodInvocation 编程技巧: 拦截器、过滤器等等实现都与此类似
                c. 适配器模式在 Spring 中的体现
         */

    }
}
