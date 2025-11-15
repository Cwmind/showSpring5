package com.itheima.a05;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;

/**
 * MapperBean定义后处理器，实现BeanDefinitionRegistryPostProcessor接口
 * 用于在Bean定义注册阶段对Mapper接口进行动态注册
 */
public class MapperPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * 后处理Bean定义注册表的主要方法
     * 在Bean定义加载之后、Bean实例化之前执行，用于动态注册Bean定义
     *
     * @param beanFactory Bean定义注册中心，用于注册和管理Bean定义
     * @throws BeansException 如果处理过程中发生错误
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        try {
            // 创建路径匹配资源模式解析器，用于扫描类路径下的资源
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 使用通配符模式扫描指定包路径下的所有class文件
            // 这里的模式表示扫描com.itheima.a05.mapper包及其子包下的所有类
            Resource[] resources = resolver.getResources("classpath:com/itheima/a05/mapper/**/*.class");

            // 创建注解Bean名称生成器，用于为Bean生成唯一的名称
            AnnotationBeanNameGenerator generator = new AnnotationBeanNameGenerator();

            // 创建缓存的元数据读取器工厂，提高元数据读取效率
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

            // 遍历扫描到的所有资源（类文件）
            for (Resource resource : resources) {
                // 通过元数据读取器读取类的元数据信息
                MetadataReader reader = factory.getMetadataReader(resource);

                // 获取类的元数据，包含类名、接口信息、注解信息等
                ClassMetadata classMetadata = reader.getClassMetadata();

                // 判断当前类是否为接口（只处理Mapper接口）
                if (classMetadata.getClassName().endsWith("Mapper")) {
                    // 使用BeanDefinitionBuilder构建MapperFactoryBean的定义
                    // MapperFactoryBean是MyBatis-Spring整合的核心，负责创建Mapper接口的代理实例
                    AbstractBeanDefinition bd = BeanDefinitionBuilder
                            //
                            .genericBeanDefinition(MapperFactoryBean.class)
                            // 设置构造参数值为Mapper接口的全限定名
                            .addConstructorArgValue(classMetadata.getClassName())
                            // 设置自动装配模式为按类型自动装配
                            .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                            // 获取最终的Bean定义对象
                            .getBeanDefinition();

                    // 为接口本身创建一个临时的Bean定义，仅用于生成Bean名称
                    AbstractBeanDefinition bd2 = BeanDefinitionBuilder.genericBeanDefinition(classMetadata.getClassName()).getBeanDefinition();

                    // 使用注解Bean名称生成器为Bean生成名称
                    // 生成规则：如果类有注解则使用注解值，否则使用类名首字母小写
                    String name = generator.generateBeanName(bd2, beanFactory);

                    // 将MapperFactoryBean的Bean定义注册到Spring容器中
                    beanFactory.registerBeanDefinition(name, bd);
                }
            }
        } catch (IOException e) {
            // 处理IO异常，打印堆栈跟踪
            e.printStackTrace();
        }
    }

    /**
     * 后处理Bean工厂的方法（在此实现中为空实现）
     * 在Bean定义注册完成后执行，用于对BeanFactory进行额外的配置或修改
     *
     * @param beanFactory 可配置的ListableBeanFactory，可以获取和修改Bean的定义信息
     * @throws BeansException 如果处理过程中发生错误
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 此方法在当前实现中为空，可以在此处添加对BeanFactory的后续处理逻辑
        // 例如：修改已有的Bean定义，注册Bean后处理器等
    }
}
