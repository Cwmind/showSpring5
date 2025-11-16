package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

/*
    模拟调用链过程, 是一个简单的递归过程
        1. proceed() 方法调用链中下一个环绕通知
        2. 每个环绕通知内部继续调用 proceed()
        3. 调用到没有更多通知了, 就调用目标方法
 */
public class A18_1 {

    // 目标类，包含要被增强的方法
    static class Target {
        public void foo() {
            System.out.println("Target.foo()");
        }
    }

    // 第一个环绕通知实现类，实现MethodInterceptor接口
    static class Advice1 implements MethodInterceptor {
        // 实现invoke方法，这是环绕通知的核心方法
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            System.out.println("Advice1.before()");
            // 调用下一个拦截器或目标方法，并获取返回值
            Object result = invocation.proceed();
            System.out.println("Advice1.after()");
            // 返回目标方法的执行结果
            return result;
        }
    }

    // 第二个环绕通知实现类
    static class Advice2 implements MethodInterceptor {
        // 实现invoke方法
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            System.out.println("Advice2.before()");
            // 调用下一个拦截器或目标方法
            Object result = invocation.proceed();
            System.out.println("Advice2.after()");
            // 返回执行结果
            return result;
        }
    }

    // 自定义方法调用实现，模拟Spring AOP中的MethodInvocation
    static class MyInvocation implements MethodInvocation {
        private Object target;  // 1. 目标对象
        private Method method;  // 目标方法
        private Object[] args;  // 方法参数
        List<MethodInterceptor> methodInterceptorList; // 2. 拦截器列表
        private int count = 1; // 调用次数计数器，用于记录执行到第几个拦截器

        // 构造函数，初始化方法调用所需的各个组件
        public MyInvocation(Object target, Method method, Object[] args, List<MethodInterceptor> methodInterceptorList) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.methodInterceptorList = methodInterceptorList;
        }

        // 获取目标方法
        @Override
        public Method getMethod() {
            return method;
        }

        // 获取方法参数
        @Override
        public Object[] getArguments() {
            return args;
        }

        // 核心方法：执行拦截器链或目标方法
        @Override
        public Object proceed() throws Throwable {
            // 调用每一个环绕通知, 调用目标
            // 如果计数器已经超过拦截器列表大小，说明所有拦截器都已执行完毕，直接调用目标方法
            if (count > methodInterceptorList.size()) {
                // 调用目标方法，使用反射执行，返回结果并结束递归调用
                return method.invoke(target, args);
            }
            // 逐一调用通知, count + 1
            // 获取当前要执行的拦截器（count-1是因为列表索引从0开始）
            MethodInterceptor methodInterceptor = methodInterceptorList.get(count++ - 1);
            // 执行当前拦截器，并将当前MethodInvocation对象传递给它
            return methodInterceptor.invoke(this);
        }

        // 获取目标对象（被代理的对象）
        @Override
        public Object getThis() {
            return target;
        }

        // 获取方法的可访问对象（这里返回方法本身）
        @Override
        public AccessibleObject getStaticPart() {
            return method;
        }
    }

    // 主方法，演示自定义AOP调用链的执行过程
    public static void main(String[] args) throws Throwable {
        // 创建目标对象实例
        Target target = new Target();
        // 创建拦截器列表，包含两个环绕通知
        List<MethodInterceptor> list = List.of(
                new Advice1(),  // 第一个环绕通知
                new Advice2()   // 第二个环绕通知
        );
        // 创建自定义的方法调用对象，封装目标方法调用和拦截器链
        MyInvocation invocation = new MyInvocation(
                target,                             // 目标对象
                Target.class.getMethod("foo"),      // 目标方法（通过反射获取）
                new Object[0],                      // 方法参数（空数组）
                list                                // 拦截器列表
        );
        // 开始执行整个调用链
        invocation.proceed();
    }
}
