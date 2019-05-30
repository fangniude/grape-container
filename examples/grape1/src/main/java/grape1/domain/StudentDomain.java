package grape1.domain;

import lombok.Getter;
import lombok.Setter;
import org.grape.BaseDomain;

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
}
