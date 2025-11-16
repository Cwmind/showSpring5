package org.springframework.aop.framework.autoproxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;

public class A17_1 {

    public static void main(String[] args) {
        // 创建通用的Spring应用上下文
        GenericApplicationContext context = new GenericApplicationContext();
        // 注册配置类后处理器，用于处理@Configuration注解的配置类
        context.registerBean(ConfigurationClassPostProcessor.class);
        // 注册配置类
        context.registerBean(Config.class);
        // 刷新上下文，完成Bean工厂的初始化
        context.refresh();
        // 关闭上下文
        context.close();

        // Bean生命周期说明：
        // 创建 -> (*) 依赖注入 -> 初始化 (*)
        // 星号表示BeanPostProcessor的干预点

        /*
            学到了什么
                a. 代理的创建时机
                    1. 初始化之后 (无循环依赖时)
                    2. 实例创建后, 依赖注入前 (有循环依赖时), 并暂存于二级缓存
                b. 依赖注入与初始化不应该被增强, 仍应被施加于原始对象
         */
    }

    // 配置类，使用@Configuration注解声明
    @Configuration
    static class Config {
        // 注册自动代理创建器Bean，用于解析@Aspect注解并创建代理
        @Bean // 解析 @Aspect、产生代理
        public AnnotationAwareAspectJAutoProxyCreator annotationAwareAspectJAutoProxyCreator() {
            return new AnnotationAwareAspectJAutoProxyCreator();
        }

        // 注册自动装配注解后处理器Bean，用于处理@Autowired注解
        @Bean // 解析 @Autowired
        public AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor() {
            return new AutowiredAnnotationBeanPostProcessor();
        }

        // 注册通用注解后处理器Bean，用于处理@PostConstruct等注解
        @Bean // 解析 @PostConstruct
        public CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor() {
            return new CommonAnnotationBeanPostProcessor();
        }

        // 注册切面顾问Bean，组合切入点和通知
        @Bean
        public Advisor advisor(MethodInterceptor advice) {
            // 创建AspectJ表达式切入点
            AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
            // 设置切入点表达式，匹配所有foo方法执行
            pointcut.setExpression("execution(* foo())");
            // 返回默认的切入点顾问
            return new DefaultPointcutAdvisor(pointcut, advice);
        }

        // 注册方法拦截器Bean，作为通知逻辑
        @Bean
        public MethodInterceptor advice() {
            // 实现方法拦截器接口，定义环绕通知逻辑
            return (MethodInvocation invocation) -> {
                System.out.println("before...");
                // 继续执行目标方法
                return invocation.proceed();
            };
        }

        // 注册Bean1
        @Bean
        public Bean1 bean1() {
            return new Bean1();
        }

        // 注册Bean2
        @Bean
        public Bean2 bean2() {
            return new Bean2();
        }
    }

    // Bean1类定义

    static class Bean1 {
        // 目标方法，会被切面增强
        public void foo() {

        }

        // 构造方法
        public Bean1() {
            System.out.println("Bean1()");
        }

        // 设置Bean2的setter方法，使用@Autowired进行依赖注入
        @Autowired public void setBean2(Bean2 bean2) {
            System.out.println("Bean1 setBean2(bean2) class is: " + bean2.getClass());
        }

        // 初始化方法，使用@PostConstruct注解
        @PostConstruct public void init() {
            System.out.println("Bean1 init()");
        }
    }

    // Bean2类定义
    static class Bean2 {
        // 构造方法
        public Bean2() {
            System.out.println("Bean2()");
        }

        // 设置Bean1的setter方法，使用@Autowired进行依赖注入
        @Autowired public void setBean1(Bean1 bean1) {
            System.out.println("Bean2 setBean1(bean1) class is: " + bean1.getClass());
        }

        // 初始化方法，使用@PostConstruct注解
        @PostConstruct public void init() {
            System.out.println("Bean2 init()");
        }
    }
}