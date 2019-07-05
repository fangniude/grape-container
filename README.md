`grape-container` 是一个基于`SpringBoot`、`Ebean`、`dubbo`等技术构建的Java服务端运行环境，其目标是让Java服务端开发「开箱即用」。

# 1. 概览

- 一个基于`SpringBoot`、`Ebean`、`dubbo`等技术构建的Java模块化运行环境
- 基于Jdk8+，用较现代的方式书写Java
- 基于SpringBoot，最流行的技术
- 选用Ebean作为持久化层，简单、高效
- 使用ServiceLoader加载插件
- 每一个插件为一个jar包
  - 每个插件均提供rest服务(controller) 和 dubbo服务(service)
  - 每个插件有自己的数据库，多个插件可以共用一个数据库连接
  - 插件管理自己的数据库迁移脚本
  - 插件间调用走dubbo服务
- grape-container启动时，根据配置加载classpath下的全部或部分插件，执行数据库迁移脚本，启动SpringBoot
- 打包时，通过classpath下插件的多少 和 配置文件，控制应用的功能



# 2. 目标

- 不同的业务(插件)，可以跑在同一个容器(container)中
- 通用的插件，可以很方便的重用，而不需要做进平台
- 在规模较小，业务量不大的情况下，所有业务只用一个应用即可
- 当业务量大了，想使用微服务部署，只需要修改打包部署方式
- 提供标准的运行环境，从0开始搭一套环境变的更简单
- 几乎不需要手工处理线上数据库升级



# 3. 主要功能

- [x] 插件加载运行平台
- [x] 支持插件生命周期管理
- [x] 根据配置加载插件
- [x] 多datasource管理
- [x] 启动时ebean的`domain`动态增强
- [x] 生成DDL模型和SQL
- [x] 启动时数据库自动迁移
- [x] 提供domain基类，定义常用的操作
- [x] 集成dubbo并提供动态获取`reference`



# 4. 第一个插件

## 4.1 创建插件项目

创建一个`maven`项目，并添加依赖：

```xml
    <artifactId>grape1</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.grape</groupId>
            <artifactId>grape-container</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
```

基本项目结构如下：

![image-20190706050716351](http://ww4.sinaimg.cn/large/006tNc79ly1g4pnj88mp4j309v0hxwfm.jpg)

每一个插件都有一个名字，即包名，如`grape1`。

**注：** 包名 === 插件名，后文中两个名词可能会混用。



## 4.2 插件类

每一个插件都有一个插件类，放在插件包下，如`Grape1Plugin`：

```java
package grape1;

import com.google.auto.service.AutoService;
import org.grape.Plugin;

@AutoService(Plugin.class)
public class Grape1Plugin extends Plugin {
}
```

可以重写`Plugin`的方法，在插件的生命周期做些事情。



## 4.3 领域模型

强制使用`domain`包名，存放`领域模型`，`domain`类强制继承`BaseDomain`，表名建议使用包名前缀，如`GRAPE1_`。Ebean相关的技术，参考[ebean官网](https://ebean.io):

```java
package grape1.domain;

import grape1.service.StudentService;
import lombok.Getter;
import lombok.Setter;
import org.grape.BaseDomain;
import org.grape.GrapeApplication;
import org.grape.ReferenceHelper;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity(name = "GRAPE1_STUDENT")
public class StudentDomain extends BaseDomain {
    private int age;
    private String friend;

    @ManyToOne
    private ClazzDomain classId;
}
```

建议使用`controller`,`service`,`dto`等子包名，不强制，Spring和Dubbo均会扫描插件包`grape1`及其子包。

在`resources`中必须有`ebean.mf`文件，配置ebean增强相关配置：

```properties
entity-packages: grape1.domain
transactional-packages: grape1.domain
querybean-packages: grape1.domain
```

`dbmigration.grape1`下面的`数据库模型`和`迁移脚本`由`DbMigrationTest`生成：

```java
package grape1;

import org.grape.GrapeDbMigration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DbMigrationTest {

    @Test
    public void test() throws IOException {
        GrapeDbMigration dbm = new GrapeDbMigration();
        dbm.generate("grape1", "1.0.0");
    }
}
```



## 4.4 服务

Service接口，建议继承自`CrudService`：

```java
package grape1.service;

import grape1.domain.StudentDomain;
import org.grape.CrudService;

public interface StudentService extends CrudService<StudentDomain> {
    String getName();
}
```



Service实现类，使用`dubbo`的`Service`注解，提供`dubbo`服务，使用`Reference`注解使用其他插件提供的`dubbo`服务：

```java
package grape1.service.impl;

import grape1.domain.StudentDomain;
import grape1.service.StudentService;
import grape2.service.HelpService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.grape.BaseCrudService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Service(interfaceClass = StudentService.class)
@Component
public class StudentServiceImpl extends BaseCrudService<StudentDomain> implements StudentService {
    @Reference
    private HelpService teacherService;

    public StudentServiceImpl(Class<StudentDomain> domainClass) {
        super(StudentDomain.class);
    }

    @Override
    public String getName() {
        return "hello";
    }
}

```

需要动态使用`SpringBean`或`dubbo reference`时，可以使用以下方法：

```java
// 取spring bean
StudentService studentService = GrapeApplication.getSpringBean(StudentService.class);

// 取reference
HelpService reference = ReferenceHelper.reference(HelpService.class);
```



## 4.5 配置文件

配置文件`application.properties`:

```properties
#
# http server
#
server.port=8000
#
# enable grapes, default all
# example: grape1,grape2,grape3
#
plugins.enable=grape1
#
# data source
#
datasource.default.username=root
datasource.default.password=123456
datasource.default.databaseUrl=jdbc:mysql://localhost:3306/grape?useUnicode=true&characterEncoding=UTF-8
datasource.default.databaseDriver=com.mysql.jdbc.Driver
datasource.default.heartbeatSql=select 1
#
# data source reference
#
datasource.grape1=default
#
# dubbo
#
dubbo.application.name=test
dubbo.protocol.name=dubbo
dubbo.protocol.port=20881
#dubbo.registry.address=nacos://127.0.0.1:8848
dubbo.registry.address=multicast://224.5.6.7:1234?unicast=false
#
# spring debug
#
debug=true
```

仅作为参考，`SpingBoot`, `dubbo`等配置项非常多，请参考各自的官方文档。



## 4.6 启动

插件启动配置：

![image-20190706052807009](http://ww2.sinaimg.cn/large/006tNc79ly1g4po4wq51dj30kp0iptam.jpg)



# 5. 实现思路

- 通过ServiceLoader加载插件
- 多处用到了`插件名=包名`
- Spring启动参数中，配置所有启动的包名
- 启动时动态修改配置文件，加入dubbo扫描路径
- 使用ebean的`DB Migration`生成DDL，并在启动时升级数据库
- 启动时解析配置文件，获取所有的datasource，并根据名称找到插件的datasource，如果没有使用default
- 插件类中提供插件生命周期管理



# 6. 命名约定

- 每一个插件都有一个名字，即包名，用名词，全部小写，如`grape1`
- 子包名统一使用：`domain`,`controller`,`service`,`service.impl`等
- 包下面有一个插件类文件，命名格式为插件名+Plugin，如：`Grape1Plugin`
- domain中，表名统一使用 `插件名 + domain名`，如`GRAPE1_STUDENT`
- 其他通用命名参考阿里云《Java开发手册》



























