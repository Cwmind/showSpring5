package org.springframework.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

public class Step6 {
    // 演示如何将 spring.main 前缀的配置属性绑定到 SpringApplication 对象
    public static void main(String[] args) throws IOException {
        // 1. 创建 SpringApplication 实例
        SpringApplication application = new SpringApplication();

        // 2. 创建应用环境对象，用于管理属性源
        ApplicationEnvironment env = new ApplicationEnvironment();

        // 3. 添加属性源到环境中（优先级：后添加的优先级更高）
        // 先添加 step4.properties（优先级较低）
        env.getPropertySources().addLast(new ResourcePropertySource("step4", new ClassPathResource("step4.properties")));
        // 再添加 step6.properties（优先级较高，会覆盖step4中的相同属性）
        env.getPropertySources().addLast(new ResourcePropertySource("step6", new ClassPathResource("step6.properties")));

        // 4. 注释掉的示例代码 - 演示不同的绑定方式：
        // 方式1: 直接绑定到新创建的User对象实例
        // User user = Binder.get(env).bind("user", User.class).get();
        // System.out.println(user);

        // 方式2: 绑定到已存在的User对象实例
        // User user = new User();
        // Binder.get(env).bind("user", Bindable.ofInstance(user));
        // System.out.println(user);

        // 5. 主要的绑定逻辑：
        // 打印绑定前的application状态
        System.out.println(application);

        // 使用Binder将环境中"spring.main"前缀的属性绑定到SpringApplication实例
        // 例如：如果配置文件中包含 spring.main.banner-mode=off，这个配置会被应用到application对象
        Binder.get(env).bind("spring.main", Bindable.ofInstance(application));

        // 打印绑定后的application状态，可以看到配置属性已生效
        System.out.println(application);
    }

    // 内部User类，用于演示属性绑定
    static class User {
        private String firstName;
        private String middleName;
        private String lastName;

        // getter和setter方法
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getMiddleName() { return middleName; }
        public void setMiddleName(String middleName) { this.middleName = middleName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        @Override
        public String toString() {
            return "User{" +
                    "firstName='" + firstName + '\'' +
                    ", middleName='" + middleName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
