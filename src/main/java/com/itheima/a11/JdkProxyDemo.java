package com.itheima.a11;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkProxyDemo {

    interface Foo {
        void foo();
    }
    //目标对象可以是final的，因为代理是他的兄弟，不是儿子
    static final class Target implements Foo {
        public void foo() {
            System.out.println("target foo");
        }
    }

    // jdk 只能针对接口代理
    // cglib
    public static void main(String[] param) throws IOException {
        // 目标对象
        Target target = new Target();

        ClassLoader loader = JdkProxyDemo.class.getClassLoader(); // 用来加载在运行期间动态生成的字节码
//        // 代理和原始对象都实现了Foo接口，他们是兄弟关系，他们之间没法强制转换
//        Foo proxy = (Foo) Proxy.newProxyInstance(loader, new Class[]{Foo.class}, (p, method, args) -> {
//            System.out.println("before...");
//            // 目标.方法(参数)
//            // 方法.invoke(目标, 参数);
//            Object result = method.invoke(target, args);
//            System.out.println("after....");
//            return result; // 让代理也返回目标方法执行的结果
//        });


        // 代理和原始对象都实现了Foo接口，他们是兄弟关系，他们之间没法强制转换
        // 第一个参数用来加载在运行期间动态生成的字节码，第二个参数，代理什么接口，
        Foo proxy = (Foo) Proxy.newProxyInstance(loader, new Class[]{Foo.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object p, Method method, Object[] args) throws Throwable {
                System.out.println("before...");
                // 目标.方法(参数)
                // 方法.invoke(目标, 参数);
                Object result = method.invoke(target, args);
                System.out.println("after....");
                return result; // 让代理也返回目标方法执行的结果
            }
        });

        System.out.println(proxy.getClass());

        proxy.foo();

        System.in.read();
    }
}