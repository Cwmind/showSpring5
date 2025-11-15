package com.itheima.a05;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 自定义组件扫描后处理器，实现BeanDefinitionRegistryPostProcessor接口
// 该接口允许在Spring容器启动过程中干预Bean定义的注册过程
public class ComponentScanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    /**
     * 在BeanFactory标准初始化之后调用，此时所有的Bean定义已经加载完成
     * 可以在这里对BeanFactory进行额外的配置或修改
     */
    @Override // 这个方法在context.refresh()过程中被调用
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 这里没有实现任何逻辑，表示我们不需要在BeanFactory初始化后进行额外处理
    }

    /**
     * 在Bean定义注册阶段调用，此时容器刚刚启动，可以在这里动态注册额外的Bean定义
     * 这是Spring容器扩展的关键入口点之一
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        try {
            // 1. 查找配置类上的@ComponentScan注解
            // 这里假设有一个Config类，通过反射查找该类上的ComponentScan注解
            ComponentScan componentScan = AnnotationUtils.findAnnotation(Config.class, ComponentScan.class);

            // 2. 如果找到了@ComponentScan注解
            if (componentScan != null) {
                // 3. 遍历注解中指定的所有基础包路径
                for (String p : componentScan.basePackages()) {
                    System.out.println("扫描包路径: " + p);

                    // 4. 将包路径转换为资源路径格式
                    // 例如：com.itheima.a05.component -> classpath*:com/itheima/a05/component/**/*.class
                    String path = "classpath*:" + p.replace(".", "/") + "/**/*.class";
                    System.out.println("资源路径模式: " + path);

                    // 5. 创建元数据读取器工厂（用于高效读取类元数据）
                    CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

                    // 6. 使用路径匹配资源解析器获取所有匹配的.class文件资源
                    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);

                    // 7. 创建Bean名称生成器（遵循Spring的命名规则）
                    AnnotationBeanNameGenerator generator = new AnnotationBeanNameGenerator();

                    // 8. 遍历每个找到的类资源
                    for (Resource resource : resources) {
                        // 打印资源信息（调试用）
                        // System.out.println(resource);

                        // 9. 读取类的元数据信息
                        MetadataReader reader = factory.getMetadataReader(resource);

                        // 打印类名（调试用）
                        // System.out.println("类名:" + reader.getClassMetadata().getClassName());

                        // 10. 获取类的注解元数据
                        AnnotationMetadata annotationMetadata = reader.getAnnotationMetadata();

                        // 打印注解信息（调试用）
                        // System.out.println("是否加了 @Component:" + annotationMetadata.hasAnnotation(Component.class.getName()));
                        // System.out.println("是否加了 @Component 派生:" + annotationMetadata.hasMetaAnnotation(Component.class.getName()));

                        // 11. 检查类是否直接标注了@Component注解或其派生注解（如@Controller, @Service, @Repository等）
                        if (annotationMetadata.hasAnnotation(Component.class.getName())
                                || annotationMetadata.hasMetaAnnotation(Component.class.getName())) {

                            // 12. 为符合条件的类创建Bean定义
                            AbstractBeanDefinition bd = BeanDefinitionBuilder
                                    .genericBeanDefinition(reader.getClassMetadata().getClassName()) // 设置Bean的类
                                    .getBeanDefinition(); // 获取最终的Bean定义对象

                            // 13. 使用Spring的命名规则生成Bean名称
                            String name = generator.generateBeanName(bd, beanFactory);

                            // 14. 将Bean定义注册到Spring容器中
                            beanFactory.registerBeanDefinition(name, bd);

                            // 此时，这个类就被Spring容器管理，后续可以被依赖注入等
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 15. 处理可能的IO异常（如读取类文件失败）
            e.printStackTrace();
        }
    }
}