package com.autotasks.jar.spring;

import com.autotasks.jar.exec.HybridThreadManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoThreadAutoConfiguration {

    // ✅ Static bean to avoid BeanPostProcessor warnings
    @Bean
    public static HybridThreadManager hybridThreadManager() {
        return new HybridThreadManager();
    }

    @Bean
    public static SmartTaskBeanPostProcessor smartTaskBeanPostProcessor(HybridThreadManager manager) {
        return new SmartTaskBeanPostProcessor(manager);
    }
}
