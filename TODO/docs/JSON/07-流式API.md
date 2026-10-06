# 07 流式 API（JsonParser / JsonGenerator）

Streaming API 是 Jackson 最底层的接口，逐 token 读取或写入 JSON，**内存占用极小、性能最高**，适合处理超大 JSON 文件或需要 token 级控制的场景。核心类位于 `com.fasterxml.jackson.core`：

- 读取：`JsonParser`
- 写出：`JsonGenerator`
- 工厂：`JsonFactory`

> Data Binding（ObjectMapper）与 Tree Model（JsonNode）都是在流式 API 之上构建的。

## 1. 写入：JsonGenerator

```java
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.StringWriter;

JsonFactory factory = new JsonFactory();
StringWriter sw = new StringWriter();
try (JsonGenerator gen = factory.createGenerator(sw)) {
    gen.writeStartObject();              // {
    gen.writeStringField("name", "Alice");
    gen.writeNumberField("age", 30);
    gen.writeArrayFieldStart("roles");   // "roles":[
    gen.writeString("ADMIN");
    gen.writeString("USER");
    gen.writeEndArray();                 // ]
    gen.writeObjectField("meta", mapper.valueToTree(...)); // 任意字段
    gen.writeEndObject();                // }
}
String json = sw.toString();
// {"name":"Alice","age":30,"roles":["ADMIN","USER"]}
```

常用写出方法：

| 方法 | 作用 |
| --- | --- |
| `writeStartObject()` / `writeEndObject()` | 开始/结束对象 |
| `writeStartArray()` / `writeEndArray()` | 开始/结束数组 |
| `writeStringField(name, value)` | 字符串字段 |
| `writeNumberField(name, value)` | 数值字段 |
| `writeBooleanField(name, value)` | 布尔字段 |
| `writeNullField(name)` | null 字段 |
| `writeArrayFieldStart(name)` | 数组字段开始 |
| `writeString(value)` / `writeNumber(value)` / ... | 直接写值 |
| `writeObject(value)` | 写任意对象（交给序列化器） |
| `writeTree(JsonNode)` | 写 JsonNode |
| `flush()` | 刷出缓冲 |

## 2. 读取：JsonParser

逐 token 遍历 JSON。每个 token 由 `nextToken()` 获取，`JsonToken` 枚举表示当前类型：

```java
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

JsonFactory factory = new JsonFactory();
String json = "{\"name\":\"Alice\",\"age\":30}";

try (JsonParser parser = factory.createParser(json)) {
    while (parser.nextToken() != null) {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.FIELD_NAME) {
            String field = parser.currentName();
            parser.nextToken();  // 读字段值
            if (field.equals("name")) {
                System.out.println("name = " + parser.getText());
            } else if (field.equals("age")) {
                System.out.println("age = " + parser.getIntValue());
            }
        }
    }
}
```

`JsonToken` 枚举值：`START_OBJECT`、`END_OBJECT`、`START_ARRAY`、`END_ARRAY`、`FIELD_NAME`、`VALUE_STRING`、`VALUE_NUMBER_INT`、`VALUE_NUMBER_FLOAT`、`VALUE_TRUE`、`VALUE_FALSE`、`VALUE_NULL`、`VALUE_EMBEDDED_OBJECT`、`NOT_AVAILABLE`。

## 3. 获取值的便捷方式

在读到值 token 后：

```java
parser.getText();        // 字符串表示
parser.getIntValue();    // int
parser.getLongValue();
parser.getDoubleValue();
parser.getEmbeddedObject(); // 原始对象
parser.getValueAsString();
parser.currentToken();    // 当前 token
parser.currentName();     // 当前字段名（在 FIELD_NAME 时）
```

## 4. 创建 JsonParser / JsonGenerator 的输入源

`createParser` 支持：

- `String`
- `char[]`（含 offset/len）
- `byte[]`（自动检测 UTF-8 / UTF-16）
- `InputStream`
- `Reader`
- `File`
- `URL`

`createGenerator` 支持：

- `OutputStream`（常用）
- `Writer`
- `File`
- `StringBuilder`（`JsonFactory.stringGenerator()` 或 `createGenerator(StringBuilder)`）

```java
try (JsonParser p = JsonFactory.builder().build().createParser(new File("big.json"))) { ... }
try (JsonGenerator g = JsonFactory.builder().build().createGenerator(System.out)) { ... }
```

## 5. 性能调优与特性

### 配置特性

```java
JsonFactory f = JsonFactory.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
        .build();
```

- 读取侧：`StreamReadFeature.*`（如 `STRICT_DUPLICATE_DETECTION` 检测重复键、`IGNORE_UNKNOWN` 等）。
- 写出侧：`StreamWriteFeature.*`（如 `QUOTE_FIELD_NAMES`、`WRITE_NUMBERS_AS_STRINGS`）。

### 复用与缓冲

```java
// 复用同一 JsonFactory（线程安全）减少构造开销
JsonFactory f = new JsonFactory();

// 显式缓冲，减少 IO 次数
BufferedInputStream bis = new BufferedInputStream(is, 64 * 1024);
try (JsonParser p = f.createParser(bis)) { ... }
```

## 6. 三者的协作

```java
// JsonParser / Generator 也可以直接用于 ObjectMapper
ObjectMapper mapper = new ObjectMapper();

try (JsonParser p = mapper.getFactory().createParser(json)) {
    User u = mapper.readValue(p, User.class);      // 流式 parser 交给 databind
}

try (JsonGenerator g = mapper.getFactory().createGenerator(sw)) {
    mapper.writeValue(g, user);                    // databind 输出到 generator
}
```

## 7. 流式读取超大文件示例（逐记录消费）

```java
JsonFactory f = new JsonFactory();
try (JsonParser p = f.createParser(new File("huge.json"))) {
    // 期望顶层是数组
    p.nextToken();                 // START_ARRAY
    while (p.nextToken() != JsonToken.END_ARRAY) {
        // 每个对象作为 value 交给 databind
        User u = mapper.readValue(p, User.class);
        process(u);
    }
}
```

## 最佳实践

1. 需要处理 **GB 级 JSON** 或最大化吞吐时才用流式 API；否则数据绑定即可。
2. **必须关闭** parser / generator（尽量用 try-with-resources）以释放底层 IO。
3. 复用 `JsonFactory` 是线程安全的，不要每次 new。
4. 使用字段名常量、避免频繁 `getText()`/`currentName()` 的重复解析损耗。