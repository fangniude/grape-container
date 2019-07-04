package grape1.domain;

import grape1.service.StudentService;
import lombok.Getter;
import lombok.Setter;
import org.grape.ReferenceHelper;
import org.grape.BaseDomain;
import org.grape.GrapeApplication;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity(name = "STU")
public class StudentDomain extends BaseDomain {
    private int age;
    private String friend;

    @ManyToOne
    private ClazzDomain classId;

    public void hello() {
        String name = GrapeApplication.getSpringBean(ReferenceHelper.class).getReference(StudentService.class).getName();
        System.out.println(name);
    }
}
