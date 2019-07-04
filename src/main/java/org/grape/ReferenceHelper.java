package org.grape;

import org.apache.dubbo.config.spring.beans.factory.annotation.DubboReferenceHacker;
import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferenceHelper {
    private final ReferenceAnnotationBeanPostProcessor referenceProcessor;

    @Autowired
    ReferenceHelper(ReferenceAnnotationBeanPostProcessor referenceProcessor) {
        this.referenceProcessor = referenceProcessor;
    }

    public <T> T getReference(Class<T> tClass) {
        return DubboReferenceHacker.getReferenceByClass(referenceProcessor, tClass);
    }
}
