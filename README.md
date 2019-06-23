`grape-container` 是一个基于`SpringBoot`、`Ebean`、`dubbo`等技术构建的Java服务端运行环境，其目标是让Java服务端开发「开箱即用」。

# grape-container是什么？

- 一个基于`SpringBoot`、`Ebean`、`dubbo`等技术构建的Java模块化运行环境
- 支持Jdk8+，用较现代的方式书写Java
- 基于SpringBoot，最流行的技术
- 选用Ebean作为持久化层
- 使用ServiceLoader支持模块化加载运行
- 每一个模块为一颗葡萄(grape)，即一个jar包
  - 每颗葡萄均提供rest服务 和 dubbo服务
  - 每颗葡萄有自己的数据库，多个葡萄可以共用一个数据库连接
  - 同时管理自己的数据库迁移脚本
  - 模块间调用走dubbo服务
- grape-container启动时，加载classpath下的所有grape，执行数据库迁移脚本，启动SpringBoot
- 部署时，通过classpath下grape的多少控制服务的大小

# grape-container试图解决什么问题？

- 不同的业务(grape)，可以跑在同一个平台(container)上
- 通用的grape，可以很方便的重用，而不需要做进平台
- 在规模较小，业务量不大的情况下，所有业务只用一个应用即可
- 当业务量大了，想使用微服务部署，则只需要改部署方式
- 提供标准的运行环境，从0开始搭一套环境变的更简单
- 几乎不需要手工处理线上数据库迁移

# 主要功能

- [x] 插件加载运行平台
- [x] 支持葡萄生命周期管理
- [x] 根据配置加载葡萄
- [x] 多datasource管理
- [x] ebean启动时动态增强
- [x] 启动时数据库自动迁移
- [x] 提供domain基类，定义常用的操作
- [x] 集成dubbo
- [x] 集成swagger-ui
- [x] 集成errorprone等质量工具
- [x] undertow替代tomcat
- [ ] 提供pom-bom包
- [ ] release到maven仓库