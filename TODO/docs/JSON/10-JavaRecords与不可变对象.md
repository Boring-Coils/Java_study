# 10 Java Records 与不可变对象

自 Java 17（乃至 record 的 Java 16）以来，**record** 成为不可变对象的首选写法。Jackson 对 record 支持良好，你可利用规范构造函数自动绑定，无需额外样板代码。

## 1. 基本使用

```java
public record User(String name, int age) {}

ObjectMapper mapper = new ObjectMapper();

// 反序列化：自动调用规范构造函数
User u = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", User.class);

// 序列化：自动取 record component
String json = mapper.writeValueAsString(u); // {"name":"Alice","age":30}
```

> Jackson **2.12+** 会自动识别 record 的规范构造函数（canonical constructor），按声明顺序绑定属性，**无需** `@JsonCreator` 或 `@JsonProperty`（除非重命名）。

## 2. 属性重命名 / 自定义

record 组件同样支持注解：

```java
public record User(
        @JsonProperty("user_id") String userId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthday) {}
```

反序列化按 JSON 键匹配 record 组件。未知属性默认抛错，`@JsonIgnoreProperties(ignoreUnknown = true)` 同样适用 record：

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(String name, int age) {}
```

## 3. record 与构造函数参数名模块

record 的规范构造函数参数名本身就是组件名，**无需** `ParameterNamesModule`。该模块主要解决的是**普通类带参构造**的参数名引用问题：

```java
public class LegacyUser {
    private final String name;
    private final int age;

    public LegacyUser(String name, int age) {    // 参数名需通过 -parameters 或 @JsonProperty 保留
        this.name = name;
        this.age = age;
    }
}
// 注册 ParameterNamesModule 且开启 -parameters 后，方可无需 @JsonProperty
```

若末开启 `-parameters`，普通带参构造需显式 `@JsonCreator` + `@JsonProperty`。

## 4. 默认值 / 可空（Complementarity with Optional）

record 可以与 `Optional`、默认实参等配合，但注意序列化输出差异：

```java
public record Config(String name, Optional<String> note) {}

// 序列化：Optional 默认输出为内容本身（需要 Jdk8Module 或较新 databind）
String json = mapper.writeValueAsString(new Config("a", Optional.empty()));
// 可能输出 {"name":"a","note":null}，具体受模块与 Inclusion 影响
```

如需彻底控制 null/empty，可给组件加 `@JsonInclude(NON_ABSENT)`：

```java
public record Config(
        @JsonInclude(JsonInclude.Include.NON_ABSENT) Optional<String> note) {}
```

## 5. Builder 模式（配合 Lombok @Jacksonized）

复杂/可选字段多的 record，常用 Builder，配合 Jackson：

```java
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized               // 让 Jackson 通过 builder 反序列化
public record Person(String name, int age, String email) {}
```

## 6. 自定义访问器（compact constructor / 组件校验）

record 可自定义方法（如验证、派生字段），Jackson 序列化只认 **record components**，不会输出额外方法返回的字段（除非标注）：

```java
public record Money(BigDecimal amount, String currency) {
    public Money {              // compact constructor，校验
        if (amount == null) throw new IllegalArgumentException();
    }

    @JsonIgnore                  // 即使存在也不序列化
    public String formatted() { return amount + " " + currency; }
}
```

## 7. 与树/泛型互转

record 同样支持 `TypeReference`、`readTree`、`treeToValue`：

```java
List<User> users = mapper.readValue(jsonArr, new TypeReference<List<User>>() {});

JsonNode node = mapper.valueToTree(user);
User back = mapper.treeToValue(node.get(0), User.class);
```

## 8. 配置速查

| 需求 | 配置 |
| --- | --- |
| record 自动绑定构造函数 | Jackson 2.12+ 默认支持，无需处理 |
| 重命名字段 | 组件上 `@JsonProperty` |
| 容忍未知字段 | record 上 `@JsonIgnoreProperties(ignoreUnknown=true)` |
| 普通带参构造无注解 | 注册 `ParameterNamesModule` + 开启 `-parameters` |
| 过滤 Optional 空值 | `@JsonInclude(NON_ABSENT)` |
| Builder 反序列化 | Lombok `@Jacksonized` |

## 9. 注意事项

- **record 是 final 的**，无法通过 setter 注入，只能走构造函数绑定；因此参数名别名、默认值等要提前设计好。
- compact constructor 抛异常会在反序列化时被 Jackson 包装（一般是 `RuntimeJsonMappingException` 或 `ValueInstantiationException`）。
- 若同时存在自定义构造器与组件默认构造器，Jackson 优先使用规范构造函数，请勿手写重载构造器（除非明确使用 `@JsonCreator` 指定）。