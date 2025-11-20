package com.itheima.a47;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Configuration
public class A47_2 {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(A47_2.class);
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1. 数组类型");
//        testArray(beanFactory);
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2. List 类型");
//        testList(beanFactory);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 3. applicationContext");
        testApplicationContext(beanFactory);
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 4. 泛型");
//        testGeneric(beanFactory);
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> 5. @Qualifier");
//        testQualifier(beanFactory);
        /*
            学到了什么
                1. 如何获取数组元素类型
                2. Spring 如何获取泛型中的类型
                3. 特殊对象的处理, 如 ApplicationContext, 并注意 Map 取值时的类型匹配问题 (另见  TestMap)
                4. 谁来进行泛型匹配 (另见 TestGeneric)
                5. 谁来处理 @Qualifier
                6. 刚开始都只是按名字处理, 等候选者确定了, 才会创建实例
         */
    }

    private static void testQualifier(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd5 = new DependencyDescriptor(Target.class.getDeclaredField("service"), true);
        Class<?> type = dd5.getDependencyType();
        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(name);
            //                                                             @Qualifier("service2")
            if (resolver.isAutowireCandidate(new BeanDefinitionHolder(bd,name), dd5)) {
                System.out.println(name);
                System.out.println(dd5.resolveCandidate(name, type, beanFactory));
            }
        }
    }
    private static void testGeneric(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd4 = new DependencyDescriptor(Target.class.getDeclaredField("dao"), true);
        Class<?> type = dd4.getDependencyType();
        ContextAnnotationAutowireCandidateResolver resolver = new ContextAnnotationAutowireCandidateResolver();
        resolver.setBeanFactory(beanFactory);
        for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type)) {
            BeanDefinition bd = beanFactory.getMergedBeanDefinition(name);
            // 对比 BeanDefinition 与 DependencyDescriptor 的泛型是否匹配
            if (resolver.isAutowireCandidate(new BeanDefinitionHolder(bd,name), dd4)) {
                System.out.println(name);
                System.out.println(dd4.resolveCandidate(name, type, beanFactory));
            }
        }
    }
    private static void testApplicationContext(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException, IllegalAccessException {
        DependencyDescriptor dd3 = new DependencyDescriptor(Target.class.getDeclaredField("applicationContext"), true);

        Field resolvableDependencies = DefaultListableBeanFactory.class.getDeclaredField("resolvableDependencies");
        resolvableDependencies.setAccessible(true);
        Map<Class<?>, Object> dependencies = (Map<Class<?>, Object>) resolvableDependencies.get(beanFactory);
        dependencies.forEach((k, v) -> {
            System.out.println("key:" + k + " value: " + v);
        });
        for (Map.Entry<Class<?>, Object> entry : dependencies.entrySet()) {
            // 左边类型                      右边类型
            if (entry.getKey().isAssignableFrom(dd3.getDependencyType())) {
                System.out.println(entry.getValue());
                break;
            }
        }
    }
    private static void testList(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {
        DependencyDescriptor dd2 = new DependencyDescriptor(Target.class.getDeclaredField("serviceList"), true);
        if (dd2.getDependencyType() == List.class) {
            Class<?> resolve = dd2.getResolvableType().getGeneric().resolve();
            System.out.println(resolve);
            List<Object> list = new ArrayList<>();
            String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, resolve);
            for (String name : names) {
                Object bean = dd2.resolveCandidate(name, resolve, beanFactory);
                list.add(bean);
            }
            System.out.println(list);
        }
    }
    /**
     * 测试数组类型依赖注入的方法
     * 演示如何手动解析数组类型的依赖并完成注入
     *
     * @param beanFactory Spring Bean工厂，用于获取和管理Bean
     * @throws NoSuchFieldException 如果指定的字段不存在
     */
    private static void testArray(DefaultListableBeanFactory beanFactory) throws NoSuchFieldException {

        // 1. 创建依赖描述符：指向Target类的serviceArray字段，true表示该依赖是必须的
        // 这个描述符包含了依赖的元数据信息（字段、类型、是否必须等）
        DependencyDescriptor dd1 = new DependencyDescriptor(Target.class.getDeclaredField("serviceArray"), true);

        // 2. 检查依赖类型是否为数组
        if (dd1.getDependencyType().isArray()) {

            // 3. 获取数组的组件类型（即数组元素的类型）
            // 例如：Service[] 的组件类型是 Service.class
            // Class 是一个泛型类：class Class<T>
            //             ? 是通配符，表示"未知类型"

            //                             具体含义对比：
            //            Class<?>           // 表示任意类型的Class对象（类型不确定）
            //            Class<String>      // 表示String类型的Class对象（类型确定）
            //                    Class              // 原始类型，不推荐使用（失去类型安全）

            Class<?> componentType = dd1.getDependencyType().getComponentType();
            System.out.println("数组组件类型: " + componentType);

            // 4. 从BeanFactory中获取所有匹配组件类型的Bean名称
            // BeanFactoryUtils.beanNamesForTypeIncludingAncestors会搜索当前工厂及其祖先工厂
            String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, componentType);

            // 5. 创建集合来存储解析到的Bean实例
            List<Object> beans = new ArrayList<>();

            // 6. 遍历所有匹配的Bean名称
            for (String name : names) {
                System.out.println("找到匹配的Bean名称: " + name);

                // 7. 解析候选Bean：根据名称和类型从BeanFactory中获取Bean实例
                // resolveCandidate方法会处理Bean的创建、依赖注入等生命周期
                Object bean = dd1.resolveCandidate(name, componentType, beanFactory);

                // 8. 将解析到的Bean添加到集合中
                beans.add(bean);
            }

            // 9. 使用类型转换器将List集合转换为目标数组类型
            // convertIfNecessary会自动处理类型转换，将List<Service>转换为Service[]
            Object array = beanFactory.getTypeConverter().convertIfNecessary(beans, dd1.getDependencyType());

            // 10. 输出最终的数组结果
            System.out.println("最终生成的数组: " + array);
            System.out.println("数组类型: " + array.getClass());
            System.out.println("数组长度: " + Array.getLength(array));
        }
    }
    static class Target {
        @Autowired private Service[] serviceArray;
        @Autowired private List<Service> serviceList;
        @Autowired private ConfigurableApplicationContext applicationContext;
        @Autowired private Dao<Teacher> dao;
        @Autowired @Qualifier("service2") private Service service;
    }
    interface Dao<T> {

    }
    @Component("dao1") static class Dao1 implements Dao<Student> {
    }
    @Component("dao2") static class Dao2 implements Dao<Teacher> {
    }

    static class Student {

    }

    static class Teacher {

    }

    interface Service {

    }

    @Component("service1")
    static class Service1 implements Service {

    }

    @Component("service2")
    static class Service2 implements Service {

    }

    @Component("service3")
    static class Service3 implements Service {

    }
}
