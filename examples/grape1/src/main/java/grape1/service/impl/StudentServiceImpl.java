package grape1.service.impl;

import grape1.service.StudentService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Service(interfaceClass = StudentService.class)
@Component
public class StudentServiceImpl implements StudentService {
    @PostConstruct
    private void init() {
        System.out.println("student");
    }

    @Override
    public String getName() {
        return "hello";
    }
}
