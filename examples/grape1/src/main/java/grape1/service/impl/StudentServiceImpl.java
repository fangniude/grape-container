package grape1.service.impl;

import grape1.domain.StudentDomain;
import grape1.service.StudentService;
import org.apache.dubbo.config.annotation.Service;
import org.grape.BaseCrudService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Service(interfaceClass = StudentService.class)
@Component
public class StudentServiceImpl extends BaseCrudService<StudentDomain> implements StudentService {

    public StudentServiceImpl(Class<StudentDomain> domainClass) {
        super(StudentDomain.class);
    }

    @PostConstruct
    private void init() {
        System.out.println("student");
    }

    @Override
    public String getName() {
        return "hello";
    }
}
