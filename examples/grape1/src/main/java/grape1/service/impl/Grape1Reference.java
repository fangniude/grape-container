package grape1.service.impl;

import grape1.service.StudentService;
import lombok.Getter;
import org.grape.GrapeApplication;
import org.grape.ReferenceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Component
public class Grape1Reference {
    private StudentService studentService;
    private ReferenceHelper helper;

    @Autowired
    public Grape1Reference(ReferenceHelper helper) {
        this.helper = helper;
    }

    @PostConstruct
    private void init() {
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                StudentService studentService = GrapeApplication.getSpringBean(StudentService.class);
                StudentService ref = GrapeApplication.getSpringBean(ReferenceHelper.class).getReference(StudentService.class);
                StudentService reference = ReferenceHelper.reference(StudentService.class);
                System.out.println(reference);
                assert reference != null;
            }
        });
    }
}
