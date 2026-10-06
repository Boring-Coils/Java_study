# Jackson 与 JSON 操作

本文档系统整理 Java 生态中最流行的 JSON 处理库 **Jackson**（FasterXML）中与 JSON 操作相关的所有主要内容，配套代码示例基于本项目配置编写：

- JDK：**Java 21**
- Jackson：**jackson-databind 2.17.2**

## 目录

| 章节 | 内容 | 说明 |
| --- | --- | --- |
| [01-概述与依赖](01-概述与依赖.md) | 库结构、三大核心包、Maven 依赖 | 认识 Jackson 的整体架构 |
| [02-ObjectMapper 与配置](02-ObjectMapper与配置.md) | ObjectMapper、特性开关、配置项 | 唯一核心入口，最常用 |
| [03-序列化](03-序列化.md) | 对象 → JSON | 写出到字符串/文件/流 |
| [04-反序列化](04-反序列化.md) | JSON → 对象 | 读取与绑定 |
| [05-注解](05-注解.md) | 全部常用注解 | 控制序列化/反序列化行为 |
| [06-树模型 JsonNode](06-树模型JsonNode.md) | JsonNode 的读取与构建 | 无需 POJO 的动态处理 |
| [07-流式 API](07-流式API.md) | JsonParser / JsonGenerator | 底层高性能读写 |
| [08-JSON Pointer 与路径](08-JSONPointer与路径.md) | JsonPointer、at() | 嵌套数据的精确访问 |
| [09-日期时间处理](09-日期时间处理.md) | JavaTimeModule、JSR-310 | LocalDate/LocalDateTime/Instant |
| [10-Java Records 与不可变对象](10-JavaRecords与不可变对象.md) | Records、Optional、模块注册 | Java 17+ 新特性的绑定 |

## 快速对照表

| 需求 | API |
| --- | --- |
| 对象转 JSON 字符串 | `mapper.writeValueAsString(obj)` |
| 对象转字节数组 | `mapper.writeValueAsBytes(obj)` |
| 对象写入文件 | `mapper.writeValue(file, obj)` |
| JSON 字符串转对象 | `mapper.readValue(json, Foo.class)` |
| JSON 文件转对象 | `mapper.readValue(file, Foo.class)` |
| 泛型 List / Map | `TypeReference<T>` |
| 不确定结构 | `mapper.readTree(json)` → `JsonNode` |
| 流式高效读写 | `JsonGenerator` / `JsonParser` |

## 核心依赖速查

`pom.xml` 中本项目仅需（`jackson-databind` 已传递依赖 `jackson-core` 与 `jackson-annotations`）：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.2</version>
</dependency>
```

> **注意**：如需处理 Java 8 时间类型（`LocalDateTime` 等）或让构造函数参数名自动绑定，在 Jackson **2.x** 中仍需额外引入 `jackson-datatype-jsr310` 与 `jackson-module-parameter-names`（详见各章节）。这些模块在 Jackson **3.x** 中才被内置进 `jackson-databind`。