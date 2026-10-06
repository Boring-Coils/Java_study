# 06 树模型 JsonNode

Tree Model 把 JSON 解析成一棵由 **`JsonNode`** 组成的树，适合处理**结构未知**、**字段动态**或只想**局部修改**的数据，无需定义 POJO。核心类型为 `com.fasterxml.jackson.databind.JsonNode`。

## 解析为 JsonNode

```java
ObjectMapper mapper = new ObjectMapper();
String json = "{\"name\":\"Alice\",\"age\":30,\"tags\":[\"a\",\"b\"]}";

JsonNode root = mapper.readTree(json);
```

从文件 / 流 / 字节数组解析：

```java
JsonNode node = mapper.readTree(new File("data.json"));
JsonNode node = mapper.readTree(is);  // InputStream
JsonNode node = mapper.readTree(bytes);
```

> `readTree` 内部把流 `JsonParser` 转成树。对乱/非法 JSON 会抛 `JsonMappingException`。

## JsonNode 的取值方法

对字段/值取公开方法：

```java
root.get("name").asText();      // "Alice"
root.get("age").asInt();        // 30
root.get("age").asLong();
root.get("age").asDouble();
root.path("missing").asText();  // 不存在返回 ""，不抛异常
root.has("age");                // true
root.hasNonNull("age");         // true（存在且非 null）
root.isNull();                  // 是否 null 节点
root.isObject();                // 是否对象
root.isArray();                 // 是否数组
root.isTextual();
root.isNumber();
root.isBoolean();
root.size();                    // 子节点数量 / 数组长度
root.fieldNames();              // 遍历对象字段名
root.elements();                // 遍历子节点
root.properties();              // 遍历 Map.Entry<String,JsonNode>
```

字段访问相关：

| 方法 | 说明 |
| --- | --- |
| `get(String)` | 按字段名取子节点；不存在返回 `null` |
| `get(int)` | 按索引取数组元素；越界返回 `null` |
| `path(String)` | 按字段名取；不存在返回 `MissingNode`（不抛异常） |
| `path(int)` | 按索引取；越界返回 `MissingNode` |
| `at("/a/b")` | 按 JSON Pointer 路径取（见第 08 章） |
| `findValue(String)` | 深度优先查找第一个同名节点 |
| `findValues(String)` | 递归查找所有同名节点，返回 List |
| `findPath(String)` | findValue 的宽松版，找不到返回 MissingNode |
| `findAllValues(String)` | 递归查找所有同名节点（含数组） |

## 数组节点操作

```java
JsonNode arr = root.get("tags");
if (arr.isArray()) {
    for (JsonNode e : arr) {
        System.out.println(e.asText());
    }
}
System.out.println(arr.size());    // 数组长度
System.out.println(arr.get(0).asText()); // 首元素
```

## 构造 / 修改 JsonNode

### 用 ObjectNode 构建（构建 JSON）

```java
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

ObjectNode obj = mapper.createObjectNode();
obj.put("name", "Bob");                // 字符串
obj.put("age", 25);                    // 数值（int）
obj.set("address", mapper.createObjectNode().put("city", "Beijing"));

ArrayNode arr = mapper.createArrayNode();
arr.add(1).add(2).add(3);
arr.add("hello");
obj.set("nums", arr);

String json = mapper.writeValueAsString(obj);
// {"name":"Bob","age":25,"address":{"city":"Beijing"},"nums":[1,2,3]}
```

> 字段可任意放置、可重复、可删：`obj.remove("age")`、`obj.replace("name", node)`。

### 修改已有树

```java
((ObjectNode) root).put("age", 31);          // 修改对象字段
((ObjectNode) root).remove("name");          // 删除
((ArrayNode) root.get("tags")).add("c");     // 数组追加
```

## 数值 / 文本 自动填充与 `asXxx`

`asInt()`、`asLong()`、`asDouble()`、`asBoolean()`、`asText()` 在**节点不匹配时返回默认值**，非常稳健；也可给出默认值：

```java
node.asInt(-1);          // 无法解析返回 -1
node.asText("unknown");  // 无法解析返回 "unknown"
node.asBoolean(false);
```

### 大数字处理

```java
BigDecimal d = root.get("price").decimalValue(); // 高精度
BigInteger bi = root.get("big").bigIntegerValue();
```

## JsonNode 与 POJO 互转

树 → POJO：

```java
User user = mapper.treeToValue(root, User.class);
```

POJO → 树：

```java
JsonNode node = mapper.valueToTree(user);
```

## 复制与查询

```java
JsonNode copy = root.deepCopy();        // 深拷贝（互不影响）
JsonNode merged = ...;                  // ObjectNode.mergeIn / 手动合并
Set<String> fields = mapper.treeToValue(root, new TypeReference<Set<String>>() {});
```

使用 `ObjectReader` 也可复用树的读取配置。

## 常用场景示例

### 1. 泛化的动态配置解析

```java
JsonNode cfg = mapper.readTree("{\"enabled\":true,\"timeout\":3000}");
boolean on = cfg.path("enabled").asBoolean(false);
int timeout = cfg.path("timeout").asInt(1000);
```

### 2. 遍历整个树

```java
Iterator<Map.Entry<String, JsonNode>> it = root.fields();
while (it.hasNext()) {
    Map.Entry<String, JsonNode> e = it.next();
    System.out.println(e.getKey() + " = " + e.getValue().asText());
}
```

### 3. 过滤修改后重新序列化

```java
ObjectNode on = (ObjectNode) root;
for (String key : List.of("passwordHash", "token")) {
    on.remove(key);
}
return mapper.writeValueAsString(on);
```

## 节点类型常量

`JsonNodeType` 枚举：`ARRAY`、`OBJECT`、`STRING`、`NUMBER`、`BOOLEAN`、`NULL`、`MISSING`、`BINARY`、`POJO`。

```java
if (root.getNodeType() == JsonNodeType.OBJECT) { ... }
```

## 最佳实践

1. **能定义 POJO 就优先用 POJO**（类型安全、可读性好）；Tree Model 用于动态/未知结构。
2. 读取不存在字段一律用 `path()`（返回 MissingNode）而不是 `get()`（返回 null），避免 NPE。
3. 修改共享节点前先 `deepCopy()`，避免污染原数据。
4. 输出前用 `ObjectNode.remove` 过滤敏感字段，比序列化多个 DTO 更省事，但注意安全与可维护性。