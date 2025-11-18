package com.itheima.a20;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

// 声明当前类为配置类，用于定义Bean和配置Spring容器
@Configuration
// 启用组件扫描，自动扫描并注册当前包及其子包下的@Component、@Service、@Repository等注解的类
@ComponentScan
// 指定要加载的配置文件，classpath:表示从类路径下加载application.properties文件
@PropertySource("classpath:application.properties")
// 启用配置属性绑定功能，将配置文件中的属性值绑定到WebMvcProperties和ServerProperties类的实例中
@EnableConfigurationProperties({WebMvcProperties.class, ServerProperties.class})
public class WebConfig {

    // ⬅️内嵌 web 容器工厂
    // 定义Tomcat Servlet Web服务器工厂Bean，用于创建和配置内嵌的Tomcat服务器
    @Bean
    // 方法参数注入ServerProperties配置属性，用于获取服务器相关配置
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(ServerProperties serverProperties) {
        // 创建TomcatServletWebServerFactory实例，并设置端口号（从配置文件中读取）
        return new TomcatServletWebServerFactory(serverProperties.getPort());
    }

    // ⬅️创建 DispatcherServlet
    // 定义Spring MVC的核心控制器DispatcherServlet的Bean
    @Bean
    public DispatcherServlet dispatcherServlet() {
        // 创建DispatcherServlet实例，它是Spring MVC的前端控制器，负责请求的分发和处理
        return new DispatcherServlet();
    }

    // ⬅️注册 DispatcherServlet, Spring MVC 的入口
    // 定义DispatcherServlet注册Bean，用于将DispatcherServlet注册到Servlet容器中
    @Bean
    // 方法参数注入DispatcherServlet实例和WebMvcProperties配置属性
    public DispatcherServletRegistrationBean dispatcherServletRegistrationBean(
            DispatcherServlet dispatcherServlet, WebMvcProperties webMvcProperties) {
        // 创建DispatcherServletRegistrationBean实例，将DispatcherServlet映射到根路径("/")
        DispatcherServletRegistrationBean registrationBean = new DispatcherServletRegistrationBean(dispatcherServlet, "/");
        // 设置Servlet的启动顺序（从配置文件中读取）
        registrationBean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        // 返回配置完成的注册Bean
        return registrationBean;
    }

    // 如果用 DispatcherServlet 初始化时默认添加的组件, 并不会作为 bean, 会直接把requestMappingHandlerMapping
    // 当作DispatcherServlet的成员变量，给测试带来困扰
    // ⬅️1. 加入RequestMappingHandlerMapping
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    // ⬅️2. 继续加入RequestMappingHandlerAdapter, 会替换掉 DispatcherServlet 默认的 4 个 HandlerAdapter
    @Bean
    public MyRequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        TokenArgumentResolver tokenArgumentResolver = new TokenArgumentResolver();
        YmlReturnValueHandler ymlReturnValueHandler = new YmlReturnValueHandler();
        MyRequestMappingHandlerAdapter handlerAdapter = new MyRequestMappingHandlerAdapter();
        handlerAdapter.setCustomArgumentResolvers(List.of(tokenArgumentResolver));
        handlerAdapter.setCustomReturnValueHandlers(List.of(ymlReturnValueHandler));
        return handlerAdapter;
    }

    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters();
    }

    // ⬅️3. 演示 RequestMappingHandlerAdapter 初始化后, 有哪些参数、返回值处理器

    // ⬅️3.1 创建自定义参数处理器

    // ⬅️3.2 创建自定义返回值处理器

}
