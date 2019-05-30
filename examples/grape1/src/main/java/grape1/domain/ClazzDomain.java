package grape1.domain;

import lombok.Getter;
import lombok.Setter;
import org.grape.BaseDomain;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@Entity(name = "CLAZZ")
public class ClazzDomain extends BaseDomain {

    @OneToMany(mappedBy = "classId")
    private List<StudentDomain> studentList;


}
