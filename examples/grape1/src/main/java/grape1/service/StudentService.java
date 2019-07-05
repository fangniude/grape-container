package grape1.service;

import grape1.domain.StudentDomain;
import org.grape.CrudService;

public interface StudentService extends CrudService<StudentDomain> {
    String getName();
}
