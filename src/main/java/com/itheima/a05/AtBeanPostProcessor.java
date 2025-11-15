package com.itheima.a05;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.Set;

/**
 * 自定义Bean后处理器，用于解析@Bean注解并注册BeanDefinition
 * 实现了BeanDefinitionRegistryPostProcessor接口，可以在Bean定义注册阶段进行干预
 */
public class AtBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * 后处理BeanFactory，可以在这里对已经注册的BeanDefinition进行修改或添加一些特殊的Bean
     * 这个方法在所有的BeanDefinition都已经加载完成，但还没有实例化任何Bean的时候调用
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 这里可以添加对BeanFactory的后续处理逻辑
        // 比如修改已经注册的BeanDefinition，或者添加BeanPostProcessor等
    }

    /**
     * 后处理BeanDefinition注册器，这是主要的扩展点
     * 在BeanDefinition被加载到注册器后，但在Bean实例化之前调用
     * 可以在这里动态注册额外的BeanDefinition
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        try {
            // 创建元数据读取器工厂，用于读取类的元数据信息
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

            // 直接读字节码，不走类加载，效率高
            // 通过ClassPathResource读取指定类的字节码文件
            MetadataReader reader = factory.getMetadataReader(new ClassPathResource("com/itheima/a05/Config.class"));

            // 获取该类中所有被@Bean注解标记的方法的元数据
            // getAnnotatedMethods方法返回所有被指定注解标注的方法的MethodMetadata
            Set<MethodMetadata> methods = reader.getAnnotationMetadata().getAnnotatedMethods(Bean.class.getName());

            // 遍历所有被@Bean注解标记的方法
            for (MethodMetadata method : methods) {
                System.out.println(method);

                // 获取@Bean注解中的initMethod属性值
                // getAnnotationAttributes方法返回注解的所有属性名值对
                String initMethod = method.getAnnotationAttributes(Bean.class.getName()).get("initMethod").toString();

                // 使用BeanDefinitionBuilder来构建BeanDefinition
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

                // 有工厂对象，才能调用工厂里的方法
                // 设置工厂方法：指定在名为"config"的bean上调用当前方法作为工厂方法
                // method.getMethodName()获取方法名，"config"是工厂bean的名称
                // 我们通过设置BeanDefinition的工厂方法（setFactoryMethodOnBean）来告诉Spring：
                // 这个Bean应该通过调用另一个Bean（即Config实例）的指定方法来创建。这就是工厂方法模式的应用。
                builder.setFactoryMethodOnBean(method.getMethodName(), "config");

                // 指定构造方法自动装配模式为AUTOWIRE_CONSTRUCTOR
                // 表示使用构造函数进行自动装配
                builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

                // 如果initMethod有值（长度大于0），设置初始化方法
                if (initMethod.length() > 0) {
                    builder.setInitMethodName(initMethod);
                }

                // 获取构建好的AbstractBeanDefinition对象
                AbstractBeanDefinition bd = builder.getBeanDefinition();

                // 将BeanDefinition注册到BeanDefinitionRegistry中
                // 使用方法名作为bean的名称
                beanFactory.registerBeanDefinition(method.getMethodName(), bd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}