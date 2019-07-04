package org.apache.dubbo.config.spring.beans.factory.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.config.annotation.Reference;
import org.grape.GrapeException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InjectionMetadata;

import java.lang.annotation.Annotation;

@Slf4j
public class ReferenceHelper {
    private ReferenceAnnotationBeanPostProcessor referenceProcessor;

    public ReferenceHelper(ReferenceAnnotationBeanPostProcessor referenceProcessor) {
        this.referenceProcessor = referenceProcessor;
    }

    public <T> T getReference(Class<T> tClass) {
        try {
            @SuppressWarnings({"unchecked", "ConstantConditions"})
            Object object = referenceProcessor.getInjectedObject(new ClassReference(tClass), this, tClass.getName(), tClass, new InjectionMetadata.InjectedElement(null, null) {
                @SuppressWarnings("NullableProblems")
                @Override
                protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
                    super.inject(target, requestingBeanName, pvs);
                }
            });

            return tClass.cast(object);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GrapeException(e);
        }
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class ClassReference<T> implements Reference {
        private Class<T> tClass;

        ClassReference(Class<T> tClass) {
            this.tClass = tClass;
        }

        @Override
        public Class<?> interfaceClass() {
            return tClass;
        }

        @Override
        public String interfaceName() {
            return "";
        }

        @Override
        public String version() {
            return "";
        }

        @Override
        public String group() {
            return "";
        }

        @Override
        public String url() {
            return "";
        }

        @Override
        public String client() {
            return "";
        }

        @Override
        public boolean generic() {
            return false;
        }

        @Override
        public boolean injvm() {
            return true;
        }

        @Override
        public boolean check() {
            return true;
        }

        @Override
        public boolean init() {
            return false;
        }

        @Override
        public boolean lazy() {
            return false;
        }

        @Override
        public boolean stubevent() {
            return false;
        }

        @Override
        public String reconnect() {
            return "";
        }

        @Override
        public boolean sticky() {
            return false;
        }

        @Override
        public String proxy() {
            return "";
        }

        @Override
        public String stub() {
            return "";
        }

        @Override
        public String cluster() {
            return "";
        }

        @Override
        public int connections() {
            return 0;
        }

        @Override
        public int callbacks() {
            return 0;
        }

        @Override
        public String onconnect() {
            return "";
        }

        @Override
        public String ondisconnect() {
            return "";
        }

        @Override
        public String owner() {
            return "";
        }

        @Override
        public String layer() {
            return "";
        }

        @Override
        public int retries() {
            return 2;
        }

        @Override
        public String loadbalance() {
            return "";
        }

        @Override
        public boolean async() {
            return false;
        }

        @Override
        public int actives() {
            return 0;
        }

        @Override
        public boolean sent() {
            return false;
        }

        @Override
        public String mock() {
            return "";
        }

        @Override
        public String validation() {
            return "";
        }

        @Override
        public int timeout() {
            return 0;
        }

        @Override
        public String cache() {
            return "";
        }

        @Override
        public String[] filter() {
            return new String[0];
        }

        @Override
        public String[] listener() {
            return new String[0];
        }

        @Override
        public String[] parameters() {
            return new String[0];
        }

        @Override
        public String application() {
            return "";
        }

        @Override
        public String module() {
            return "";
        }

        @Override
        public String consumer() {
            return "";
        }

        @Override
        public String monitor() {
            return "";
        }

        @Override
        public String[] registry() {
            return new String[0];
        }

        @Override
        public String protocol() {
            return "";
        }

        @Override
        public Method[] methods() {
            return new Method[0];
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Reference.class;
        }
    }
}
