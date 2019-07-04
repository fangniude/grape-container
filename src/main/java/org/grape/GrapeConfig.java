package org.grape;

import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrapeConfig {
    @Bean
    public ReferenceHelper referenceHelper(ReferenceAnnotationBeanPostProcessor processor) {
        return new ReferenceHelper(processor);
    }
}
