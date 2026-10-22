# Eureka 注册与发现笔记（campus-food）

> 对照当前代码。注册中心**不是**你手写的一张表，而是 Spring Cloud Netflix Eureka：Server 收实例，Client 启动后自己报到 Server。Feign / Gateway 再用服务名去查实例。

---

## 1. 要解决什么问题

拆成多个进程之后（用户 8081、美食 8082、商家 8083），调用方不能写死 `localhost:8081`：端口会变、机器会变。

约定：

| 角色 | 职责 |
|------|------|
| Eureka Server | 记「哪个服务名 → 哪些 host:port」，提供控制台 |
| Eureka Client | 启动时把自己注册上去，并定期拉注册表、发心跳 |
| 调用方 | 只认服务名，例如 `user-service`，不认具体 IP |

本仓库服务名来自各模块 `spring.application.name`：

| 模块 | 服务名 | 端口 |
|------|--------|------|
| `eureka-server` | `eureka-server`（不注册自己） | 8761 |
| `api-gateway` | `api-gateway` | 8080 |
| `user-service` | `user-service` | 8081 |
| `food-service` | `food-service` | 8082 |
| `merchant-service` | `merchant-service` | 8083 |

---

## 2. 本项目里的文件（你写的部分）

业务仓库里**没有** `register()` 实现类。你写的是开关注解 + yml。

### 2.1 注册中心

| 文件 | 职责 |
|------|------|
| `eureka-server/pom.xml` | 依赖 `spring-cloud-starter-netflix-eureka-server` |
| `EurekaServerApplication` | `@EnableEurekaServer` 打开注册中心 |
| `eureka-server/.../application.yml` | 端口 8761；自己不当 Client |

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

`@EnableEurekaServer`：让 Spring 装配 Netflix Eureka 的 HTTP 接口（默认 `http://localhost:8761/eureka/`）。接收注册、心跳、下线的代码在依赖包里。

```yaml
server:
  port: 8761
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false   # Server 不要再注册到自己
    fetch-registry: false         # 单节点也不用拉别人的注册表
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

控制台：浏览器打开 `http://localhost:8761`。

### 2.2 各业务服务（Client）

每个要被发现的模块：

| 位置 | 写什么 |
|------|--------|
| `pom.xml` | `spring-cloud-starter-netflix-eureka-client` |
| `application.yml` | `spring.application.name` + `eureka.client.service-url.defaultZone` |
| `*Application.java` | **不必**再写 `@EnableEurekaClient`（新版有依赖就会自动注册） |

以 `user-service` 为例：

```yaml
spring:
  application:
    name: user-service   # 注册到 Eureka 上的名字；Feign/Gateway 也用这个
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

`food-service`、`merchant-service`、`api-gateway` 同样指向 `8761`，只是 `name` 和 `port` 不同。

### 2.3 业务服务到底是怎么注册的（yml 干什么）

**yml 不会自己去注册。** 它只是配置；真正发 HTTP 的是启动后的 Eureka Client。

三样缺一不可：

| 条件 | 作用 | 没有会怎样 |
|------|------|------------|
| `pom.xml` 里 `spring-cloud-starter-netflix-eureka-client` | 引入 Client，Spring Boot **自动配置** `DiscoveryClient` | 进程起来了也不会向 8761 报到 |
| `application.yml` 里的 `name` + `defaultZone` | 告诉 Client：**用什么名字**、**报到哪台 Server** | 不知道往哪注册，或名字不对，Feign/Gateway 找不到 |
| 启动 `*Application` 的 `main` | 进程起来后 Client 才读 yml、发注册请求 | 文件躺在磁盘上，控制台不会出现这个服务 |

所以：**不是「yml 把服务注册上去」，而是「进程启动 → Client 读 yml → POST 到 Eureka Server」。**

```
food-service/pom.xml          有 eureka-client 依赖
food-service/application.yml  name=food-service，defaultZone=8761
        │
        ▼
启动 FoodServiceApplication.main()
        │
        ▼
Spring Boot 自动配置 Eureka Client（不用写 @EnableEurekaClient）
        │  读取：
        │    spring.application.name  → 服务名 food-service
        │    server.port              → 8082
        │    eureka.client.service-url.defaultZone → http://localhost:8761/eureka/
        ▼
Client HTTP POST 到 Eureka Server（框架里的 DiscoveryClient.register()）
        │
        ▼
Server 写入内存注册表 → 打开 http://localhost:8761 能看到 FOOD-SERVICE
```

`user-service`、`merchant-service`、`api-gateway` 走同一条路，只是 yml 里的 `name` / `port` 不同。

对比：`eureka-server` 的 yml 写了 `register-with-eureka: false`，所以 **Server 进程起来也不会把自己当实例注册进去**。

业务代码（`FoodServiceImpl`、`UserFeignClient` 等）**不参与注册**。Feign 只在调用时按服务名去查已经注册好的表。

---

## 3. 一次注册发生了什么（时序）

```
1. 先启动 EurekaServerApplication（8761 已监听）

2. 再启动 UserServiceApplication
      Spring Boot 自动配置 Eureka Client
        → 读 application.name = user-service
        → 读本机端口 8081
        → HTTP POST http://localhost:8761/eureka/ 提交实例

3. Server 把实例放进内存注册表
      控制台出现 user-service  UP

4. 之后 Client 定时心跳（renew）
      超时不心跳 → Server 认为挂了，剔除
```

美食、商家、网关同理。启动顺序建议：**Eureka → 三个业务服务 → Gateway**（Gateway 的 `lb://` 依赖注册表里已有实例）。

---

## 4. 注册之后谁在用这张表

### 4.1 OpenFeign（服务间 HTTP）

[`UserFeignClient`](../../campus-food-common/src/main/java/com/campus/food/campusfood/feign/UserFeignClient.java)：

```java
@FeignClient(name = "user-service", path = "/internal", fallback = UserFeignFallback.class)
```

`name = "user-service"` 不是 Java 包名，是 Eureka 上的服务名。调用 `getUserInfo` 时：

1. LoadBalancer 向本地缓存的注册表问：`user-service` 有哪些 UP 实例
2. 选出一台（课设通常只有 `localhost:8081`）
3. 发 HTTP：`GET http://那台机:8081/internal/users/{id}`

`food-service` / `merchant-service` 启动类上的 `@EnableFeignClients(basePackages = "com.campus.food.campusfood.feign")` 才会扫到这个接口。fallback 在用户服务挂掉时走 [`UserFeignFallback`](../../campus-food-common/src/main/java/com/campus/food/campusfood/feign/UserFeignFallback.java)，配合 `spring-cloud-starter-circuitbreaker-resilience4j`（课设任务书里的 Hystrix 已停更，用这一套替代）。

### 4.2 Gateway（浏览器只打 8080）

[`api-gateway` 的 yml](../../api-gateway/src/main/resources/application.yml)：

```yaml
uri: lb://user-service
predicates:
  - Path=/auth/**,/user/**
```

`lb://` = load balancer，同样用 Eureka 服务名。`/internal/**` **没有**配进 Gateway，内部 Feign 直连 8081，Apifox 测 internal 不要打 8080。

---

## 5. 和「你要写的业务代码」的边界

| 层次 | 在不在本仓库 | 说明 |
|------|----------------|------|
| `@EnableEurekaServer`、client 依赖、yml | **在** | 课设要能指给老师看的就是这些 |
| `DiscoveryClient.register()`、Server 注册表 | **Maven 依赖** | `com.netflix.eureka`、`com.netflix.discovery` |
| 登录、美食列表、Redis | **在** | 和 Eureka 无关；Eureka 只解决「进程互相找到」 |

答辩可以说：注册由 **Eureka Client 自动配置**完成；业务代码通过 **服务名** 调用，不写死端口。

---

## 6. 本地怎么验证

1. 启动 `EurekaServerApplication`，打开 `http://localhost:8761`。
2. 再启动 user / food / merchant（以及可选 gateway）。
3. 控制台 **Instances currently registered with Eureka** 里应出现对应 `application.name`。
4. food 调 Feign 成功，说明不仅注册了，发现也通了。

常见失败：先起业务再起 Eureka → Client 连不上 `8761`，过一会儿会重试，但控制台会空一段时间。

---

## 7. 和任务书用词的对应

| 任务书 | 本项目 |
|--------|--------|
| Eureka 服务注册与发现 | 本节：Server 8761 + 各服务 Client |
| Ribbon | `spring-cloud-starter-loadbalancer`（Ribbon 已从 Spring Cloud 移除） |
| Feign | OpenFeign `@FeignClient` |
| Hystrix | Resilience4j + `UserFeignFallback` |
