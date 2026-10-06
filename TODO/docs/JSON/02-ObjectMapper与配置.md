# 02 ObjectMapper 与配置

`ObjectMapper` 是 Jackson 数据绑定的**唯一核心入口**，负责序列化、反序列化、JsonNode 树构建等几乎全部操作。它是一个线程安全、可复用的对象，通常在整个应用中以单例/Bean 形式存在。

## 创建 ObjectMapper

```java
ObjectMapper mapper = new ObjectMapper();              // 默认配置

// 从 factory 创建（复杂场景）
JsonFactory factory = JsonFactory.builder().build();
ObjectMapper mapper = new ObjectMapper(factory);
```

## 推荐统一注册 Java 8 模块

```java
ObjectMapper mapper = new ObjectMapper();
mapper.findAndRegisterModules(); // 自动发现 Classpath 中所有 Jackson 模块（jsr310、parameter-names、jdk8 等）
```

也可以手动逐个注册：

```java
mapper.registerModule(new JavaTimeModule());
mapper.registerModule(new ParameterNamesModule());
mapper.registerModule(new Jdk8Module());
// 需要额外引入对应依赖
```

## 常用配置方法类别

| 方法 | 作用 |
| --- | --- |
| `configure(SerializationFeature, boolean)` | 配置序列化行为 |
| `configure(DeserializationFeature, boolean)` | 配置反序列化行为 |
| `configure(JsonParser.Feature, boolean)` | 配置底层解析器（读取侧） |
| `configure(JsonGenerator.Feature, boolean)` | 配置底层生成器（写出侧） |
| `configure(MapperFeature, boolean)` | 配置通用映射行为 |
| `setSerializationInclusion(JsonInclude.Include)` | 设置序列化字段包含规则（空值过滤） |
| `setDateFormat(DateFormat)` | 设置日期格式 |
| `setTimeZone(TimeZone)` | 设置时区 |
| `setPropertyNamingStrategy(...)` | 设置命名策略（camelCase/snake_case 等） |
| `setVisibility(...)` | 设置可见性规则 |
| `setAnnotationIntrospector(...)` | 自定义注解内省器 |
| `registerModule(Module)` | 注册模块 |
| `addMixIn(Class, Class)` | 注册 MixIn |
| `getSerializationConfig()` / `getDeserializationConfig()` | 获取当前配置快照 |

### 链式风格（新版本推荐）

从 2.x 后期开始支持 builder 风格配置：

```java
ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .serializationInclusion(JsonInclude.Include.NON_NULL);
```

> 注意：`serializationInclusion()` 等返回新实例的"链式"风格实际上也会修改同一 mapper（databind 内部返回自身并应用变更）。

## SerializationFeature（序列化特性）

| 特性 | 默认 | 说明 |
| --- | --- | --- |
| `WRAP_ROOT_VALUE` | false | 是否将根值包装在命名属性中（需配合 `@JsonRootName`） |
| `INDENT_OUTPUT` | false | 输出格式化缩进（美化 JSON） |
| `FAIL_ON_EMPTY_BEANS` | true | 遇到没有任何可序列化属性的 Bean 时是否抛错 |
| `WRITE_DATES_AS_TIMESTAMPS` | true | 时间是否写成时间戳数字（如 `1664515200000`） |
| `ORDER_MAP_ENTRIES_BY_KEYS` | false | 序列化 Map 时按键排序 |
| `WRITE_DATES_WITH_ZONE_ID` | false | 写出时是否带时区 ID |
| `WRITE_SELF_REFERENCES_AS_NULL` | true | 处理自引用时写出 `null` 而非无限循环 |
| `WRITE_NULL_MAP_VALUES` | true | 写出 `null` 的 Map 值 |
| `WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED` | false | 单个元素的 List/数组是否不包数组 |
| `WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS` | false | char[] 是否写成 JSON 数组而非字符串 |
| `CLOSE_CLOSEABLE` | false | 序列化完成后是否关闭 `Closeable` 输入数据源 |

```java
mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
```

## DeserializationFeature（反序列化特性）

| 特性 | 默认 | 说明 |
| --- | --- | --- |
| `FAIL_ON_UNKNOWN_PROPERTIES` | true | JSON 中出现目标类没有的属性是否抛异常 |
| `FAIL_ON_NULL_FOR_PRIMITIVES` | false | JSON 给基本类型赋 `null` 时是否抛异常 |
| `FAIL_ON_IGNORED_PROPERTIES` | false | JSON 字段被 `@JsonIgnore` 忽略且未提供时是否抛异常 |
| `ACCEPT_EMPTY_STRING_AS_NULL_OBJECT` | false | 空字符串当作 `null`（对象属性） |
| `ACCEPT_SINGLE_VALUE_AS_ARRAY` | false | 单个值是否可反序列化为单元素数组/集合 |
| `ACCEPT_FLOAT_AS_INT` | true | 浮点数值是否能反序列化为整型 |
| `USE_BIG_DECIMAL_FOR_FLOATS` | false | 浮点是否始终使用 `BigDecimal` |
| `USE_BIG_INTEGER_FOR_INTS` | false | 整型是否始终使用 `BigInteger` |
| `WRAP_EXCEPTIONS` | true | 是否将检查异常包装为 `RuntimeException` |
| `FAIL_ON_TRAILING_TOKENS` | false | JSON 末尾有多余内容时是否抛异常 |
| `FAIL_ON_READING_DUP_TREE_KEY` | false | JsonNode 中出现重复键时是否报错 |
| `UNWRAP_ROOT_VALUE` | false | 反序列化时是否解包根命名属性 |
| `USE_LONG_FOR_INTS` | false | 整型值是否始终使用 `long` |

```java
// 最常见的：忽略 JSON 中多余字段，避免 FAIL_ON_UNKNOWN_PROPERTIES 异常
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

## MapperFeature（通用映射特性）

常见的有：

| 特性 | 说明 |
| --- | --- |
| `ACCEPT_CASE_INSENSITIVE_PROPERTIES` | 属性名大小写不敏感匹配 |
| `ACCEPT_CASE_INSENSITIVE_ENUMS` | 枚举值大小写不敏感匹配 |
| `SORT_PROPERTIES_ALPHABETICALLY` | 序列化时按字母序输出属性 |
| `DEFAULT_VIEW_INCLUSION` | 是否按默认视图包含全部属性 |

```java
mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
```

## JsonParser.Feature（读取侧）

| 特性 | 默认 | 说明 |
| --- | --- | --- |
| `ALLOW_COMMENTS` | false | 允许 JSON 中带 `//`、`/* */` 注释 |
| `ALLOW_SINGLE_QUOTES` | false | 允许使用单引号包裹字段/字符串 |
| `ALLOW_UNQUOTED_FIELD_NAMES` | false | 允许键名不加引号 |
| `ALLOW_TRAILING_COMMA` | false | 允许数组/对象末尾多余逗号 |
| `ALLOW_NUMERIC_LEADING_ZEROS` | false | 允许前导零的数值 |
| `ALLOW_NON_NUMERIC_NUMBERS` | false | 允许 `NaN`、`Infinity` 等非标准数值 |

```java
// 允许单引号
mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
```

## JsonGenerator.Feature（写出侧）

| 特性 | 默认 | 说明 |
| --- | --- | --- |
| `QUOTE_FIELD_NAMES` | true | 键名是否加引号 |
| `ESCAPE_NON_ASCII` | false | 是否转义非 ASCII 字符（取决于配置） |
| `AUTO_CLOSE_TARGET` | true | 写完后是否自动关闭目标流 |
| `WRITE_NUMBERS_AS_STRINGS` | false | 数字是否写成字符串 |

```java
mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
```

## 属性包含规则（空值/空集合过滤）

使用 `setSerializationInclusion` 或链式 `serializationInclusion`：

```java
import com.fasterxml.jackson.annotation.JsonInclude;

mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
```

| `JsonInclude.Include` 取值 | 含义 |
| --- | --- |
| `ALWAYS` | 总是包含（默认） |
| `NON_NULL` | 仅当值非 `null` 时包含 |
| `NON_ABSENT` | 排除 `null` 与 `Optional` 空值 |
| `NON_EMPTY` | 排除 `null`、空字符串、空集合、空数组 |
| `NON_DEFAULT` | 排除与类型默认值相同的字段（0、false、空串等） |

## 属性命名策略

```java
mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
```

| 策略 | 示例（字段 `userName`） |
| --- | --- |
| `LOWER_CAMEL_CASE` | `userName`（默认） |
| `UPPER_CAMEL_CASE` | `UserName` |
| `SNAKE_CASE` | `user_name` |
| `UPPER_CAMEL_CASE` | `UserName` |
| `KEBAB_CASE` | `user-name` |
| `LOWER_CASE` | `username` |

> 运行时也可通过 `findSetter` 等实现自定义 `NamingStrategy`，继承 `PropertyNamingStrategies.NamingBase`。

## 配置查看与复制

```java
// 导出配置快照（可用于深拷贝 mapper）
SerializationConfig sConf = mapper.getSerializationConfig();
DeserializationConfig dConf = mapper.getDeserializationConfig();

// 在已有 mapper 基础上创建新 mapper
ObjectMapper copy = ObjectMapperFactory.builder(mapper).build(); // 2.x 后期版本
// 或：
ObjectReader reader = mapper.reader().forType(Foo.class);         // 只读
ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();    // 只写
```

## 大厂最佳实践小结

1. **复用同一个 `ObjectMapper`**（线程安全），避免每次 new。
2. 开启 `INDENT_OUTPUT` 只用于调试/日志；生产通常关闭。
3. 真实场景常关闭 `FAIL_ON_UNKNOWN_PROPERTIES`，容忍后端新增字段。
4. 需要微服务间精确的空值语义时，优先明确设置 `Inclusion` 规则。
5. 时间类型统一注册 `JavaTimeModule` 并关闭 `WRITE_DATES_AS_TIMESTAMPS`。