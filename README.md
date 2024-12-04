## 接口文档

[接口文档](https://qjn0z3k3py.apifox.cn/)

## 项目结构

```
│  pom.xml
│  README.md
│
└─src
   ├─main
   │  ├─java
   │  │  └─org
   │  │      └─webapp
   │  │          │  Application.java
   │  │          │
   │  │          ├─config
   │  │          │      JakartaEndpointConfig.java
   │  │          │      JsonSerializationConfig.java
   │  │          │      MyBatisPlusConfig.java
   │  │          │      RedisConfig.java
   │  │          │      SpringSecurityConfig.java
   │  │          │      SpringWebSocketConfig.java
   │  │          │
   │  │          ├─controller
   │  │          │      ContactController.java
   │  │          │      IncompleteController.java            // 开发中，不开放接口
   │  │          │      InteractionController.java
   │  │          │      JakartaWebSocketServer.java
   │  │          │      NettyWebSocketServer.java            // 开发中，不开放接口
   │  │          │      UserController.java
   │  │          │      VideoController.java
   │  │          │
   │  │          ├─handler
   │  │          │      JakartaWebSocketDecoder.java
   │  │          │      JakartaWebSocketEncoder.java
   │  │          │      JwtAuthenticationFilter.java
   │  │          │      NettyMessageHandler.java
   │  │          │      NoPermissionHandler.java             // 权限不足的处理
   │  │          │      RequestVerifyHandler.java
   │  │          │      UnauthorizedHandler.java             // 令牌校验失败的处理
   │  │          │
   │  │          ├─mapper
   │  │          │      CommentMapper.java
   │  │          │      CommentMapper.xml
   │  │          │      GroupMapper.java
   │  │          │      LikeMapper.java
   │  │          │      LikeMapper.xml
   │  │          │      MemberMapper.java
   │  │          │      MessageMapper.java
   │  │          │      UserMapper.java
   │  │          │      VideoMapper.java
   │  │          │
   │  │          ├─pojo
   │  │          │      CommentDO.java
   │  │          │      GroupDO.java
   │  │          │      LikeDO.java
   │  │          │      MemberDO.java
   │  │          │      MessageDO.java
   │  │          │      MessageDTO.java
   │  │          │      ResponseVO.java
   │  │          │      UserDO.java
   │  │          │      UserVO.java
   │  │          │      VideoDO.java
   │  │          │
   │  │          ├─service
   │  │          │      ChatService.java
   │  │          │      ChatServiceImpl.java
   │  │          │      CommentService.java
   │  │          │      CommentServiceImpl.java
   │  │          │      LikeService.java
   │  │          │      LikeServiceImpl.java
   │  │          │      UserService.java
   │  │          │      UserServiceImpl.java
   │  │          │      VideoService.java
   │  │          │      VideoServiceImpl.java
   │  │          │
   │  │          └─utils
   │  │                  CustomizeUtils.java                 // 自定义工具类，包括Map与POJO的互转，自定义ObjectMapper类对象等
   │  │                  FileUtils.java                      // 自定义工具类，处理文件存储
   │  │                  JwtUtils.java
   │  │                  RedisUtils.java
   │  │
   │  └─resources
   │      │  application.yaml
   │      │  log4j2.xml
   │      │  mybatis-config.xml
   │      │
   │      ├─static
   │      └─templates
   └─test
       └─java
           └─org
               └─webapp
                   └─controller
                           ContactControllerTests.java
                           InteractionControllerTests.java
                           UserControllerTests.java
                           VideoControllerTests.java
```

## 注意事项

1. 部分接口仍在开发或测试中，恕不保证响应正确。
2. 因Windows下C盘创建文件需要管理员权限，故使用D盘保存数据。
3. 目前Windows下暂无法生成日志文件，未来将修复。
4. 单元测试仍在编写中。