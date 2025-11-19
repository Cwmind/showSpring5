package com.itheima.a41;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

public class TestDataSourceAuto {
    @SuppressWarnings("all")
    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new SimpleCommandLinePropertySource(
                "--spring.datasource.url=jdbc:mysql://localhost:3306/test",
                "--spring.datasource.username=root",
                "--spring.datasource.password=root"
        ));
        context.setEnvironment(env);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context.getDefaultListableBeanFactory());
        context.registerBean(Config.class);

        String packageName = TestDataSourceAuto.class.getPackageName();
        System.out.println("当前包名:" + packageName);
        AutoConfigurationPackages.register(context.getDefaultListableBeanFactory(),
                packageName);

        context.refresh();
        for (String name : context.getBeanDefinitionNames()) {
            String resourceDescription = context.getBeanDefinition(name).getResourceDescription();
            if (resourceDescription != null)
                System.out.println(name + " 来源:" + resourceDescription);
        }
        MyDataSourceAutoConfiguration.PooledSubClass pooled = new MyDataSourceAutoConfiguration.PooledSubClass();
        Object bean = context.getBean(pooled.getClass().getSuperclass());

        System.out.println(bean);
    }

    @Configuration
    @Import(MyImportSelector.class)
    static class Config {

    }

    static class MyImportSelector implements DeferredImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[]{
                    DataSourceAutoConfiguration.class.getName(),
                    MybatisAutoConfiguration.class.getName(),
                    DataSourceTransactionManagerAutoConfiguration.class.getName(),
                    TransactionAutoConfiguration.class.getName()
            };
        }
    }

//    static  class myDataSourceAutoConfiguration extends DataSourceAutoConfiguration {
//        DataSourceAutoConfiguration.PooledDataSourceConfiguration pooledDataSourceConfiguration
//                = new DataSourceAutoConfiguration.PooledDataSourceConfiguration();
//    }




    public class MyDataSourceAutoConfiguration extends DataSourceAutoConfiguration {

        // 定义PooledDataSourceConfiguration的子类（必须是protected或public，且同包/子类可访问）
        protected static class PooledSubClass extends DataSourceAutoConfiguration.PooledDataSourceConfiguration {
            // 子类的构造方法可以直接调用父类的protected构造方法
            public PooledSubClass() {
                super(); // 此处调用父类的protected构造方法，合法
            }
        }
        // 通过子类实例化（间接拿到PooledDataSourceConfiguration的实例
    }



}
