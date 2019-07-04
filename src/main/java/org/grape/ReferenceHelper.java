package org.grape;

import org.apache.dubbo.config.spring.beans.factory.annotation.DubboReferenceHacker;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ReferenceHelper {
    private static ReferenceHelper instance;
    private final ReferenceAnnotationBeanPostProcessor referenceProcessor;

    @PostConstruct
    private void init() {
        instance = this;
    }

    @Autowired
    ReferenceHelper(ReferenceAnnotationBeanPostProcessor referenceProcessor) {
        this.referenceProcessor = referenceProcessor;
    }

    public <T> T getReference(Class<T> tClass) {
        return DubboReferenceHacker.getReferenceByClass(referenceProcessor, tClass);
    }

    public static <T> T reference(Class<T> tClass) {
        return instance.getReference(tClass);
    }
}
