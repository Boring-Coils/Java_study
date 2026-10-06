# 08 JSON Pointer 与路径访问

JSON Pointer 是一套在 JSON 文档中定位特定节点的**标准字符串地址**（RFC 6901），Jackson 在 `JsonNode` 上通过 `at()` 方法提供支持，语法与 XPath 相似但更简洁。

## 基本语法

| 语法 | 含义 |
| --- | --- |
| `/a/b/c` | 从根对象依次取字段 `a` → `b` → `c` |
| `/arr/0` | 数组按索引取元素 |
| `/a/0/name` | 混合取对象字段与数组索引 |
| `""` 或 `/` | 根节点 |
| `~0` | 转义 `~` |
| `~1` | 转义 `/` |
| `-` | 数组末尾（写入场景，读取时通常不适用） |

## 使用 at()

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree("""
        {
          "user": {
            "id": 1001,
            "profile": { "name": "Alice", "tags": ["dev", "java"] }
          }
        }
        """);

JsonNode name = root.at("/user/profile/name");   // "Alice"
JsonNode tag0 = root.at("/user/profile/tags/0"); // "dev"
```

- 路径**不存在**时返回 `MissingNode`（**不抛异常**）。
- 可用 `isMissingNode()` 判断节点是否存在。

```java
JsonNode node = root.at("/nope/not/found");
if (node.isMissingNode()) {
    System.out.println("路径不存在");
}
```

## 字段名含特殊字符

当字段名包含 `~` 或 `/` 时需转义：`~` → `~0`，`/` → `~1`。

```java
// 字段名 "a/b"，路径写作 "/a~1b"
JsonNode n = root.at("/a~1b");
```

## JsonPointer 类型

`JsonPointer` 是独立的类（`com.fasterxml.jackson.core.JsonPointer`），可先编译复用，或直接用 `at`：

```java
import com.fasterxml.jackson.core.JsonPointer;

JsonPointer ptr = JsonPointer.compile("/user/profile/name");
JsonNode name = root.at(ptr);

// 从根访问
JsonNode n2 = root.at("/user/id");

// 相对（从某节点偏移）
JsonPointer sub = ptr.tail();       // 去掉第一段 "/user"
JsonNode n3 = root.get("user").at(sub); // 等价于 root.at("/profile/name")?
```

## 写入/修改结合 JSON Pointer（ObjectNode）

配合 `ObjectNode`/`ArrayNode` 可以实现路径定位修改，但需手动导航：

```java
JsonNode root2 = mapper.readTree("{\"user\":{\"profile\":{\"name\":\"Alice\"}}}");
ObjectNode user = (ObjectNode) root2.at("/user");
ObjectNode profile = (ObjectNode) user.get("profile");
profile.put("age", 31);

System.out.println(mapper.writeValueAsString(root2));
// {"user":{"profile":{"name":"Alice","age":31}}}
```

> Jackson 的 `ObjectNode` 有 `at()` 来返回指定路径的子节点，可直接转型后修改。

## 常用查询 API 对比

| 方法 | 返回不存在的节点 | 说明 |
| --- | --- | --- |
| `at("/a/b")` | `MissingNode` | 按 JSON Pointer 路径，最精确 |
| `path("a")` | `MissingNode` | 单层字段访问 |
| `findValue("x")` | `null`（找不到返回 null） | 深度优先第一次匹配 |
| `findPath("x")` | `MissingNode` | findValue 的宽松版 |
| `findValues("x")` | 空列表 | 递归收集所有同名字段 |
| `findAllValues("x")` | 空列表 | 递归收集（含数组内） |

```java
// 递归查找所有 "name" 字段
List<JsonNode> names = root.findValues("name");
```

## 与 JsonPath（第三方）区别

- **JSON Pointer（RFC 6901）**：`/a/b` 语法，Jackson 内置 `at()`，只支持查询导航，无过滤/通配。
- **JsonPath（第三方库如 Jayway JsonPath）**：`$.user[*].name`，支持通配、过滤（`[?(@.age>18)]`）等更复杂表达式。

若需要强大过滤与通配，请引入第三方 JsonPath 库；若只是精确定位某字段，JSON Pointer 足够且零依赖。

## 完整示例

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree(json);

// 直接取深层字段
String city = root.at("/user/address/city").asText("unknown");

// 批量取多个字段（快速、无异常）
JsonNode idN = root.at("/user/id");
if (idN.isMissingNode()) {
    throw new IllegalArgumentException("缺少 id 字段");
}
long id = idN.asLong();

// 修改后输出
ObjectNode container = (ObjectNode) root;
((ObjectNode) container.get("user")).put("verified", true);
return mapper.writeValueAsString(container);
```

## 最佳实践

1. 优先用 `at("/path")` 做**精确、可空安全**的字段读取。
2. 读取前先 `isMissingNode()` / `asText("default")` 做兜底。
3. 需要复杂过滤（大于、通配等）再考虑第三方 JsonPath。
4. 反复使用同一路径时先 `JsonPointer.compile(...)` 缓存，避免重复解析。