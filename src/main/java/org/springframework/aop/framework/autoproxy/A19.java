package org.springframework.aop.framework.autoproxy;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Field;
import java.util.List;

public class A19 {

    // 定义切面类，使用@Aspect注解标记
    @Aspect
    static class MyAspect {
        // 静态前置通知：不带参数绑定，执行时不需要切点对象
        @Before("execution(* foo(..))")
        public void before1() {
            System.out.println("before1");
        }

        // 动态前置通知：带参数绑定，执行时需要切点对象进行参数解析
        @Before("execution(* foo(..)) && args(x)")
        public void before2(int x) {
            System.out.printf("before2(%d)%n", x);
        }
    }

    // 目标类，包含要被增强的方法
    static class Target {
        public void foo(int x) {
            System.out.printf("target foo(%d)%n", x);
        }
    }

    // Spring配置类
    @Configuration
    static class MyConfig {
        // 注册AspectJ自动代理创建器Bean
        @Bean
        AnnotationAwareAspectJAutoProxyCreator proxyCreator() {
            return new AnnotationAwareAspectJAutoProxyCreator();
        }

        // 注册切面Bean
        @Bean
        public MyAspect myAspect() {
            return new MyAspect();
        }
    }

    // 主方法，演示带参数绑定的通知调用
    public static void main(String[] args) throws Throwable {
        // 创建通用应用上下文
        GenericApplicationContext context = new GenericApplicationContext();
        // 注册配置类后处理器，用于处理@Configuration注解
        context.registerBean(ConfigurationClassPostProcessor.class);
        // 注册配置类
        context.registerBean(MyConfig.class);
        // 刷新应用上下文，初始化所有Bean
        context.refresh();

        // 从容器中获取AspectJ自动代理创建器
        AnnotationAwareAspectJAutoProxyCreator creator = context.getBean(AnnotationAwareAspectJAutoProxyCreator.class);
        // 查找目标类适用的所有Advisor（切面）
        List<Advisor> list = creator.findEligibleAdvisors(Target.class, "target");

        // 创建目标对象实例
        Target target = new Target();
        // 创建代理工厂
        ProxyFactory factory = new ProxyFactory();
        // 设置目标对象
        factory.setTarget(target);
        // 添加所有切面
        factory.addAdvisors(list);
        // 获取代理对象
        Object proxy = factory.getProxy();

        // 获取目标方法的拦截器链（包含动态方法匹配器）
        List<Object> interceptorList = factory.getInterceptorsAndDynamicInterceptionAdvice(
                Target.class.getMethod("foo", int.class), // 目标方法
                Target.class // 目标类
        );

        // 遍历并显示每个拦截器的详细信息
        for (Object o : interceptorList) {
            showDetail(o);
        }
//        普通环绕通知：org.springframework.aop.interceptor.ExposeInvocationInterceptor@4f5991f6
//        普通环绕通知：org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor@38234a38
//        环绕通知和切点：org.springframework.aop.framework.InterceptorAndDynamicMethodMatcher@63fbfaeb
//        切点为：AspectJExpressionPointcut: (int x) execution(* foo(..)) && args(x)
//        通知为：org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor@e57b96d

//        也就是说,有的环绕通知还带着切点信息，也就是有参数值的环绕通知

        // 打印分隔线
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 创建反射方法调用对象，封装整个调用链的执行
        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
                proxy,           // 代理对象
                target,          // 目标对象
                Target.class.getMethod("foo", int.class), // 目标方法（带int参数）
                new Object[]{100}, // 方法参数值
                Target.class,    // 目标类
                interceptorList  // 拦截器列表
        ) {
            // 匿名类，可以添加自定义逻辑
            //这个语法创建了一个 ReflectiveMethodInvocation的匿名子类，但内部没有任何重写或新增的方法。
        };

        // 执行整个调用链
        invocation.proceed();

        /*
            学到了什么
                a. 有参数绑定的通知调用时还需要切点，对参数进行匹配及绑定
                b. 复杂程度高, 性能比无参数绑定的通知调用低
         */
    }

    // 显示拦截器详细信息的工具方法
    public static void showDetail(Object o) {
        try {
            // 通过反射获取InterceptorAndDynamicMethodMatcher类
            Class<?> clazz = Class.forName("org.springframework.aop.framework.InterceptorAndDynamicMethodMatcher");
            // 检查对象是否是InterceptorAndDynamicMethodMatcher类型
            if (clazz.isInstance(o)) {
                // 获取methodMatcher字段（动态方法匹配器）
                Field methodMatcher = clazz.getDeclaredField("methodMatcher");
                methodMatcher.setAccessible(true); // 设置可访问
                // 获取interceptor字段（拦截器）
                Field methodInterceptor = clazz.getDeclaredField("interceptor");
                methodInterceptor.setAccessible(true); // 设置可访问

                System.out.println("环绕通知和切点：" + o);
                System.out.println("\t切点为：" + methodMatcher.get(o));     // 打印切点信息
                System.out.println("\t通知为：" + methodInterceptor.get(o)); // 打印通知信息
            } else {
                // 如果是普通环绕通知，直接打印
                System.out.println("普通环绕通知：" + o);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
        }
    }
}
