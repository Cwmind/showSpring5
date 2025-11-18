package org.springframework.boot;

import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.event.EventPublishingRunListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessorApplicationListener;
import org.springframework.boot.env.RandomValuePropertySourceEnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;

import javax.swing.*;
import java.util.List;

/*
    可以添加参数 --spring.application.json={\"server\":{\"port\":9090}} 测试 SpringApplicationJsonEnvironmentPostProcessor
 */
/**
 * Spring Boot 环境准备阶段教学演示类
 * 本类演示了Spring Boot启动过程中环境配置的加载和增强机制
 */
public class Step5 {

    public static void main(String[] args) {
        // 1. 创建Spring应用实例 - 这是Spring Boot应用的入口类
        SpringApplication app = new SpringApplication();

        // 2. 添加环境后处理器监听器 - 这个监听器负责处理EnvironmentPostProcessor
        // EnvironmentPostProcessor用于在环境准备完成后对Environment进行后期处理
        app.addListeners(new EnvironmentPostProcessorApplicationListener());

        /*
         * 3. 注释掉的代码：演示如何通过SpringFactoriesLoader加载EnvironmentPostProcessor实现
         * 这部分代码展示了Spring Boot的SPI机制，可以从META-INF/spring.factories文件中加载配置
         * 取消注释可以查看当前类路径下所有注册的EnvironmentPostProcessor实现类
         */
        //List<String> names = SpringFactoriesLoader.loadFactoryNames(EnvironmentPostProcessor.class, Step5.class.getClassLoader());
        //for (String name : names) {
        //    System.out.println(name);
        //}

        // 4. 创建事件发布运行监听器 - 这个监听器负责在Spring Boot启动过程中发布各种事件
        // 它封装了应用实例和命令行参数，用于事件传播
        EventPublishingRunListener publisher = new EventPublishingRunListener(app, args);

        // 5. 创建应用环境实例 - 这是Spring的环境抽象，用于管理配置属性
        // ApplicationEnvironment继承自StandardEnvironment，增加了对Profile的支持
        ApplicationEnvironment env = new ApplicationEnvironment();

        // 6. 打印环境增强前的属性源信息
        // 属性源(PropertySource)是Spring中配置属性的载体，如系统属性、环境变量、配置文件等
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强前");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }

        // 7. 发布"环境准备完成"事件 - 这是Spring Boot启动过程中的关键事件
        // 这个事件会触发所有注册的EnvironmentPostProcessor对环境进行增强
        // DefaultBootstrapContext是启动上下文，用于在启动阶段共享组件
        publisher.environmentPrepared(new DefaultBootstrapContext(), env);

        // 8. 打印环境增强后的属性源信息
        // 对比增强前后，可以看到EnvironmentPostProcessor添加了新的属性源
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }

        // 教学要点说明：
        // - 演示了Spring Boot的事件驱动架构
        // - 展示了EnvironmentPostProcessor的工作机制
        // - 说明了属性源的加载顺序和覆盖规则
        // - 体现了Spring Boot的自动配置原理
    }


    private static void test1() {
        SpringApplication app = new SpringApplication();
        ApplicationEnvironment env = new ApplicationEnvironment();

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强前");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        ConfigDataEnvironmentPostProcessor postProcessor1 = new ConfigDataEnvironmentPostProcessor(new DeferredLogs(), new DefaultBootstrapContext());
        postProcessor1.postProcessEnvironment(env, app);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        RandomValuePropertySourceEnvironmentPostProcessor postProcessor2 = new RandomValuePropertySourceEnvironmentPostProcessor(new DeferredLog());
        postProcessor2.postProcessEnvironment(env, app);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> 增强后");
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        System.out.println(env.getProperty("server.port"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.int"));
        System.out.println(env.getProperty("random.uuid"));
        System.out.println(env.getProperty("random.uuid"));
        System.out.println(env.getProperty("random.uuid"));
    }
}
