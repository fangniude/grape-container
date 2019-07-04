package grape1.reference;

import grape1.service.StudentService;
import lombok.Getter;
import org.grape.ReferenceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Component
public class Grape1Reference {
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
                StudentService reference = helper.getReference(StudentService.class);
                System.out.println(reference);
                assert reference != null;
            }
        });
    }
}
