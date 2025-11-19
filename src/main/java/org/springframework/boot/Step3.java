package org.springframework.boot;

import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

public class Step3 {
    public static void main(String[] args) throws IOException {
        //环境变量的抽象成对象，作用，就是个map
        ApplicationEnvironment env = new ApplicationEnvironment(); // 系统环境变量, properties, yaml
        env.getPropertySources().addLast(new ResourcePropertySource(new ClassPathResource("step3.properties")));
        env.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        for (PropertySource<?> ps : env.getPropertySources()) {
            System.out.println(ps);
        }
        System.out.println(env.getProperty("JAVA_HOME"));

        System.out.println(env.getProperty("server.port"));
        System.out.println("Java运行时版本: " + System.getProperty("java.version"));
        System.out.println("Java Home(运行时): " + System.getProperty("java.home"));
    }
}
