package com.itheima.a23;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

import java.util.Date;

/**
 * Spring MVC 数据绑定工厂测试类
 * 演示了Spring中如何将HTTP请求参数绑定到Java对象的多种方式
 */
public class TestServletDataBinderFactory {
    public static void main(String[] args) throws Exception {
        // 创建模拟的HTTP请求对象，MockHttpServletRequest是Spring提供的测试工具类
        // 它模拟了真实的HttpServletRequest，可以在非Web环境下测试Web相关功能
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 设置日期参数，这个参数需要从字符串"1999|01|02"转换为Date类型
        // 这里使用了自定义的日期格式"yyyy|MM|dd"，而不是标准的"yyyy-MM-dd"
        // 这需要Spring能够识别并正确转换这种非标准格式
        request.setParameter("birthday", "1999|01|02");

        // 设置嵌套对象参数，address.name表示绑定到target对象的address属性的name字段
        // Spring支持点号表示法的属性路径，可以自动处理嵌套对象的绑定
        // 这里会创建Address对象并设置其name属性为"西安"
        request.setParameter("address.name", "西安");

        // 创建目标对象，这是数据绑定的目标，所有请求参数最终都会设置到这个对象的属性中
        // User对象包含birthday(Date类型)和address(Address对象)两个属性
        User target = new User();

        // =================================================================
        // 数据绑定的五种策略（每次只启用一种进行测试）
        // 这五种策略展示了Spring数据绑定的演进过程和不同使用场景
        // =================================================================

        // -----------------------------------------------------------------
        // 策略1：最基本的数据绑定，没有任何类型转换功能
        // -----------------------------------------------------------------
        // "1. 用工厂, 无转换功能"
        // 创建最简单的数据绑定工厂，两个参数都为null表示：
        // - 第一个null：不注册任何@InitBinder方法（Controller级别的自定义绑定逻辑）
        // - 第二个null：不使用任何WebBindingInitializer（全局级别的绑定配置）
        // 这种配置下，只能处理基本的String到简单类型（int, long等）的转换
        // 对于复杂的Date类型转换和嵌套对象绑定会失败
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, null);

        // -----------------------------------------------------------------
        // 策略2：使用@InitBinder注解进行类型转换（传统PropertyEditor方式）
        // -----------------------------------------------------------------
        // "2. 用 @InitBinder 转换"
        // 通过Controller中的@InitBinder方法注册自定义属性编辑器
        // PropertyEditorRegistry 是属性编辑器注册表接口
        // PropertyEditor 是JDK标准的属性编辑器，Spring对其进行了扩展
        //
        // 创建可调用的处理器方法，包装MyController中的aaa方法
        // InvocableHandlerMethod可以将普通方法包装为可执行的处理方法
        // 这里包装了MyController的aaa方法，该方法使用@InitBinder注解
//        InvocableHandlerMethod method = new InvocableHandlerMethod(
//            new MyController(),  // 创建Controller实例
//            MyController.class.getMethod("aaa", WebDataBinder.class)  // 通过反射获取aaa方法
//        );
        // 创建数据绑定工厂，传入@InitBinder方法列表，第二个参数为null表示不使用全局配置
        // 这种方式允许在Controller级别定制数据绑定规则
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(List.of(method), null);

        // -----------------------------------------------------------------
        // 策略3：使用ConversionService进行类型转换（现代Formatter方式）
        // -----------------------------------------------------------------
        // "3. 用 ConversionService 转换"
        // 使用ConversionService和Formatter进行类型转换，这是Spring 3.0+推荐的方式
        // ConversionService 是统一的类型转换服务接口，替代了分散的PropertyEditor
        // Formatter 是专门为格式化设计接口，比PropertyEditor更专注、更安全
        //
        // 创建格式化转换服务，这是ConversionService的一个实现
        // FormattingConversionService支持Formatter和Converter的注册
//        FormattingConversionService service = new FormattingConversionService();
        // 向转换服务注册自定义的日期格式化器
        // MyDateFormatter负责将字符串"1999|01|02"转换为Date对象
//        service.addFormatter(new MyDateFormatter("用 ConversionService 方式扩展转换功能"));
        // 创建可配置的Web绑定初始化器，用于全局配置数据绑定规则
        // ConfigurableWebBindingInitializer可以在多个Controller间共享配置
//        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        // 设置转换服务，这样所有使用该initializer的Controller都能享受到类型转换功能
//        initializer.setConversionService(service);
        // 创建数据绑定工厂，第一个参数为null表示不使用@InitBinder方法
        // 第二个参数传入配置好的initializer，使用ConversionService进行类型转换
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, initializer);

        // -----------------------------------------------------------------
        // 策略4：同时使用@InitBinder和ConversionService（组合方式）
        // -----------------------------------------------------------------
        // "4. 同时加了 @InitBinder 和 ConversionService"
        // 组合使用两种转换机制，@InitBinder的优先级高于ConversionService
        // 这种配置提供了最大的灵活性：全局配置 + 局部定制
        //
        // 创建@InitBinder方法（同策略2）
        // 这提供了Controller级别的定制能力
//        InvocableHandlerMethod method = new InvocableHandlerMethod(new MyController(), MyController.class.getMethod("aaa", WebDataBinder.class));
        //
        // 创建ConversionService（同策略3）
        // 这提供了全局的类型转换能力
//        FormattingConversionService service = new FormattingConversionService();
//        service.addFormatter(new MyDateFormatter("用 ConversionService 方式扩展转换功能"));
//        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
//        initializer.setConversionService(service);
        //
        // 创建数据绑定工厂，同时传入@InitBinder方法和WebBindingInitializer
        // 当两种转换机制都存在时，@InitBinder中注册的转换器优先级更高
        // 这种设计体现了Spring的"约定优于配置"和"可扩展性"原则
//        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(List.of(method), initializer);

        // -----------------------------------------------------------------
        // 策略5：使用Spring Boot默认的ConversionService（生产环境推荐）
        // -----------------------------------------------------------------
        // "5. 使用默认 ConversionService 转换"
        // 使用ApplicationConversionService，这是Spring Boot提供的开箱即用解决方案
        // 它预注册了大量常见的转换器，包括日期、数字、集合等类型的转换
        //
        // 创建应用转换服务，这是Spring Boot的默认实现
        // ApplicationConversionService继承了FormattingConversionService并预配置了常用转换器
        ApplicationConversionService service = new ApplicationConversionService();

        // 创建可配置的Web绑定初始化器
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();

        // 将ApplicationConversionService设置到初始化器中
        // 这样所有通过这个initializer创建的数据绑定器都能使用预配置的转换功能
        initializer.setConversionService(service);

        // 创建数据绑定工厂
        // 第一个参数为null：不使用任何@InitBinder方法（依赖全局配置）
        // 第二个参数传入配置好的initializer：使用ApplicationConversionService进行类型转换
        // 这种配置方式简单且功能强大，是生产环境中的常用选择
        ServletRequestDataBinderFactory factory = new ServletRequestDataBinderFactory(null, initializer);

        // 创建数据绑定器，这是实际执行数据绑定的核心对象
        // factory.createBinder()方法创建WebDataBinder实例，它负责具体的绑定逻辑
        // 参数说明：
        // - new ServletWebRequest(request): 将MockHttpServletRequest包装为ServletWebRequest
        // - target: 数据绑定的目标对象（User实例）
        // - "user": 对象名称，用于在错误消息和日志中标识这个对象
        WebDataBinder dataBinder = factory.createBinder(new ServletWebRequest(request), target, "user");

        // 执行实际的数据绑定操作
        // ServletRequestParameterPropertyValues从请求中提取所有参数
        // dataBinder.bind()方法将参数值绑定到target对象的对应属性上
        // 这个过程包括：类型转换、嵌套对象处理、数据验证等
        dataBinder.bind(new ServletRequestParameterPropertyValues(request));

        // 输出绑定结果，检查数据是否正确绑定
        // 如果一切正常，应该输出：
        // User{birthday=Sat Jan 02 00:00:00 CST 1999, address=Address{name='西安'}}
        System.out.println(target);
    }

    /**
     * 模拟Controller类，包含@InitBinder方法
     * 在真实的Spring MVC中，这类方法会在控制器处理请求前被调用
     */
    static class MyController {
        /**
         * @InitBinder 注解的方法，用于初始化WebDataBinder
         * 可以注册自定义的属性编辑器、格式化器等
         * @param dataBinder 数据绑定器，用于配置绑定规则
         */
        @InitBinder
        public void aaa(WebDataBinder dataBinder) {
            // 扩展 dataBinder 的转换器
            // 这里添加一个自定义的日期格式化器
            dataBinder.addCustomFormatter(new MyDateFormatter("用 @InitBinder 方式扩展的"));
        }
    }

    /**
     * 用户实体类，演示数据绑定的目标对象
     */
    public static class User {
        // 使用@DateTimeFormat注解指定日期格式
        // Spring会根据这个注解自动进行日期格式转换
        @DateTimeFormat(pattern = "yyyy|MM|dd")
        private Date birthday;
        // 嵌套对象属性，Spring支持复杂对象的绑定
        private Address address;

        // Getter和Setter方法
        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        @Override
        public String toString() {
            return "User{" +
                    "birthday=" + birthday +
                    ", address=" + address +
                    '}';
        }
    }

    /**
     * 地址实体类，作为User的嵌套属性
     * 演示Spring对嵌套属性绑定的支持
     */
    public static class Address {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}