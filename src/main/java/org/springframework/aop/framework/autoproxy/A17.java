package org.springframework.aop.framework.autoproxy;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.List;

public class A17 {
    public static void main(String[] args) {
        // 创建通用的 Spring 应用上下文，不基于 XML 配置
        GenericApplicationContext context = new GenericApplicationContext();

        // 手动注册 Bean 定义：注册切面类 Aspect1，指定 Bean 名称为 "aspect1"
        context.registerBean("aspect1", Aspect1.class);
        // 手动注册配置类 Config，指定 Bean 名称为 "config"
        context.registerBean("config", Config.class);
        // 注册配置类后处理器，用于处理 @Configuration 注解的类
        context.registerBean(ConfigurationClassPostProcessor.class);
        // 注册注解感知的 AspectJ 自动代理创建器，这是 AOP 的核心组件
        context.registerBean(AnnotationAwareAspectJAutoProxyCreator.class);
        // BeanPostProcessor 的生命周期说明：
        // 创建 -> (*) 依赖注入 -> 初始化 (*)
        // 星号表示 BeanPostProcessor 的干预点

        // 刷新应用上下文，完成 Bean 工厂的初始化
        context.refresh();

        // 注释掉的代码：可以打印所有 Bean 定义名称用于调试
//        for (String name : context.getBeanDefinitionNames()) {
//            System.out.println(name);
        //        }

        /*
            第一个重要方法 findEligibleAdvisors 找到有【资格】的 Advisors
                a. 有【资格】的 Advisor 一部分是低级的, 可以由自己编写, 如下例中的 advisor3
                b. 有【资格】的 Advisor 另一部分是高级的, 由本章的主角解析 @Aspect 后获得
         */

        // 从上下文中获取自动代理创建器实例
        AnnotationAwareAspectJAutoProxyCreator creator = context.getBean(AnnotationAwareAspectJAutoProxyCreator.class);
        // 调用 findEligibleAdvisors 方法查找适用于 Target2 类的切面顾问
        List<Advisor> advisors = creator.findEligibleAdvisors(Target2.class, "target2");
        // 注释掉的代码：可以打印所有找到的切面顾问
        /*for (Advisor advisor : advisors) {
            System.out.println(advisor);
        }*/

        /*
            第二个重要方法 wrapIfNecessary
                a. 它内部调用 findEligibleAdvisors, 只要返回集合不空, 则表示需要创建代理
         */

        // 对 Target1 实例进行包装，如果需要代理则创建代理对象
        Object o1 = creator.wrapIfNecessary(new Target1(), "target1", "target1");
        // 打印包装后的对象类型，查看是否生成了代理类
        System.out.println(o1.getClass());
        // 对 Target2 实例进行包装
        Object o2 = creator.wrapIfNecessary(new Target2(), "target2", "target2");
        // 打印包装后的对象类型
        System.out.println(o2.getClass());

        // 通过代理对象调用方法，验证切面逻辑是否生效
        ((Target1) o1).foo();

        /*
            学到了什么
                a. 自动代理后处理器 AnnotationAwareAspectJAutoProxyCreator 会帮我们创建代理
                b. 通常代理创建的活在原始对象初始化后执行, 但碰到循环依赖会提前至依赖注入之前执行
                c. 高级的 @Aspect 切面会转换为低级的 Advisor 切面, 理解原理, 大道至简
         */
    }

    // 目标类1，包含 foo 方法
    static class Target1 {
        public void foo() {
            System.out.println("target1 foo");
        }
    }

    // 目标类2，包含 bar 方法
    static class Target2 {
        public void bar() {
            System.out.println("target2 bar");
        }
    }

    // 使用 @Aspect 注解声明这是一个高级切面类
    @Aspect // 高级切面类
    @Order(1) // 定义切面执行顺序，数值越小优先级越高
    static class Aspect1 {
        // 前置通知：在 foo 方法执行前执行
        @Before("execution(* foo())")
        public void before1() {
            System.out.println("aspect1 before1...");
        }

        // 另一个前置通知：同样在 foo 方法执行前执行
        @Before("execution(* foo())")
        public void before2() {
            System.out.println("aspect1 before2...");
        }
    }

    // 配置类，使用 @Configuration 注解声明
    @Configuration
    static class Config {
        // 注释掉的低级切面配置示例
        /*@Bean // 低级切面
        public Advisor advisor3(MethodInterceptor advice3) {
            // 创建 AspectJ 表达式切入点
            AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
            // 设置切入点表达式，匹配所有 foo 方法执行
            pointcut.setExpression("execution(* foo())");
            // 创建默认的切入点顾问，组合切入点和通知
            DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice3);
            return advisor;
        }
        @Bean
        public MethodInterceptor advice3() {
            // 使用方法拦截器实现环绕通知
            return invocation -> {
                System.out.println("advice3 before...");
                // 执行目标方法
                Object result = invocation.proceed();
                System.out.println("advice3 after...");
                return result;
            };
        }*/
    }
}
