package com.autotasks.jar.spring;

import com.autotasks.jar.exec.HybridThreadManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoThreadAutoConfiguration {

    @Bean
    public HybridThreadManager hybridThreadManager() {
        return new HybridThreadManager();
    }

    @Bean
    public SmartTaskBeanPostProcessor smartTaskBeanPostProcessor(HybridThreadManager manager) {
        return new SmartTaskBeanPostProcessor(manager);
    }
}
