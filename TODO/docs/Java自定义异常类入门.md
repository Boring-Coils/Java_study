# Java 自定义异常类入门

> 面向刚写完第一个 Java 程序的人（本文以本项目 `TODO` 的 `FileManager` 为例子）
>
> 阅读前提：已经知道 `try / catch / throw` 大概是怎么回事。

---

## 0. 先把神秘感去掉

异常类是一个**普通类**，只是它继承的不是 `Object`，而是 `Exception` 或 `RuntimeException`。

除此之外，它和 `TODOEvent` 没有任何区别：一样有字段、有构造方法、有方法。
所以"怎么写异常类"这个问题的真实答案是——**你只需要写一个构造方法，方法体里调一次 `super` 就完事了。**

---

## 1. 先问自己：我到底要不要自己写一个？

很多新手一上来就新建异常类，其实 JDK 自带的足够覆盖大部分场景。**先查现成的，不够用再造新的。**

| 现成的异常 | 什么时候用它 |
| --- | --- |
| `IllegalArgumentException` | 调用方传进来的参数不合法，比如 `id` 是负数、`event` 是空字符串 |
| `IllegalStateException` | 当前状态不允许这个操作，比如清单还没创建就要往里加事项 |
| `UnsupportedOperationException` | 方法故意不实现，先占个位置 |
| `NullPointerException` | 不要主动抛它，让它自然发生说明你代码有 bug |
| `RuntimeException` | 临时过渡用。想快速让错误冒出来又懒得定义新类，直接包一层 |

**结论**：参数校验、状态判断这类，直接用现成的，别自造。

---

## 2. 什么时候值得自己写一个？

满足下面任意一条，就值得：

1. **上层想精确地只 catch 这一种错。**
   `catch (StorageException e)` 比 `catch (Exception e)` 精准得多——后者会顺手把空指针、数组越界一起吞掉，然后你就再也找不到 bug 在哪。

2. **光看名字就该知道是谁抛的。**
   以后堆栈里出现 `StorageException`，你立刻知道问题出在文件层，不用点进去看代码。

3. **需要扛着额外信息走。**
   比如要把"是哪个 id 出的错"一路带到最顶层显示给用户，可以在异常类里多存一个字段。

**反面例子**：把项目里所有异常都塞进一个 `MyException`。那样它和 `catch (Exception e)` 没有区别，等于白定义。

---

## 3. 最小可用的写法

```java
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

就这些。类体里除了两行 `super` 调用，**通常一个字都不需要写**。

如果需要额外信息（可选）：

```java
public class StorageException extends RuntimeException {

    private final int listId;

    public StorageException(String message, int listId, Throwable cause) {
        super(message, cause);
        this.listId = listId;
    }

    public int getListId() {
        return listId;
    }
}
```

---

## 4. 为什么偏偏是这两个构造方法？

| 构造方法 | 作用 |
| --- | --- |
| `StorageException(String message)` | 只想说"出什么事了"，信息由你自己写，没有底层堆栈 |
| `StorageException(String message, Throwable cause)` | 在 `catch` 块里包别人的异常时用。`cause` 就是那个原始的 `IOException` |

**本项目用的正是第二种**：

```java
} catch (IOException e) {
    throw new StorageException("保存清单失败, id=" + id, e);
    //                                                     ↑ 这个 e 千万不能省
}
```

省略 `cause` 的后果：堆栈就在你这一层断掉，你只知道"保存失败了"，不知道是权限问题、路径问题还是磁盘满了。**这是最常见也最亏的一个错误。**

---

## 5. 受检（checked）还是非受检（unchecked）？

| 父类 | 类型 | 含义 | 对你代码的影响 |
| --- | --- | --- | --- |
| `extends RuntimeException` | 非受检 | 调用方不处理也能编译 | 方法签名不用改，改动面最小。**新手推荐先用这个** |
| `extends Exception` | 受检 | 编译器强制每个调用点要么 `try` 要么 `throws` | 语义更严谨，但会一路连锁报错，拖慢进度 |
| `extends Throwable` | —— | 太底层 | 不要直接继承它，它不是给业务代码用的 |

**给本项目的建议**：先用 `RuntimeException`。等整个 CLI 跑通了、你对异常传播有感觉了，再考虑要不要改成受检。

---

## 6. 动手步骤（结合本项目）

1. **定名**
   以 `Exception` 结尾，前缀写清是谁的错。`StorageException` 就够直白；`StorageError`、`DataProblem` 这类名字别人扫代码时认不出是异常。

2. **选父类**
   现在选 `RuntimeException`。

3. **写构造方法**
   照着第 3 节抄两个构造方法，方法体里只调 `super`。

4. **建文件**
   本项目**没有 package 声明**，所以直接放在：

   ```
   TODO/src/main/java/StorageException.java
   ```

   和 `FileManager.java` 同级即可。文件名必须和类名**完全一致**，大小写都不能差。

5. **回到 `FileManager` 改 catch**
   把 `e.printStackTrace()` 换成 `throw new StorageException("...", e)`。

6. **在最外层接住**
   菜单循环那里 `catch (StorageException e)`，打印一句人话给用户，然后**回到菜单而不是退出**。

---

## 7. 常见坑

- [ ] **类名不以 `Exception` 结尾** —— 别人认不出这是异常。
- [ ] **只写 message 不传 `cause`** —— 堆栈断掉，排查全靠猜。
- [ ] **`new` 出来忘了 `throw`** —— `new StorageException("...")` 单独一行是合法语句，编译器不报错，程序继续往下走，然后你一脸问号。
- [ ] **在异常类里写业务逻辑** —— 异常类只装信息，不干活。别重写 `getMessage()` 去拼字符串。
- [ ] **文件名和类名不一致** —— `public` 类的文件名必须完全相同，不符直接编译不过。
- [ ] **所有异常共用一个自定义类** —— 等于 `catch (Exception e)`，白写。
- [ ] **`catch` 里只打印不重新抛出** —— 这正是本项目当前的问题：异常被"标记为已处理"，编译器和调用方都以为一切正常。
- [ ] **用 `e.getMessage()` 当唯一信息** —— `IOException` 的 message 经常只有路径没有原因，必须靠 `cause` 保留原始异常。

---

## 8. 怎么验证改对了

| 场景 | 期望结果 |
| --- | --- |
| 正常存取 | 行为与改动前完全一致，`data/` 下的 JSON 内容不变 |
| 保存时故意让目录只读 | 程序给出清晰提示，而不是静默无事发生 |
| 手动把某个 JSON 改残再读 | 报"读取失败"，而不是返回一个空列表骗你 |
| 读一个不存在的 id | 安静地拿到空列表，**不抛异常** —— 这是"可预期"与"真出事"的分界 |

最后一行的原则值得单独记住：

> **可预期的正常情况用返回值表达（空列表 / Optional），真出事了才抛异常。**

---

## 9. 一层一层看：异常该在哪一层被处理

```
顶层 CLI 菜单        ← 在这里 catch，打印人话，继续循环
      ↑
TODOList（业务）     ← 一般不 catch，让它穿过去
      ↑
FileManager（存储）  ← 在这里 catch IOException，包装成 StorageException 抛出去
      ↑
java.io              ← 别人写的，管不了
```

原则：**底层只管喊救命，顶层负责安抚用户。中间任何一层都不该把错误悄悄咽下去。**

---

## 10. 最后一句实话

自定义异常类是可选项，不是必修课。

你完全可以先直接：

```java
throw new RuntimeException("保存清单失败, id=" + id, e);
```

把"异常被吞掉"这个问题解决掉，跑通之后再回来补一个正经的 `StorageException`。

**先让错误别被吞掉，比先取个好名字重要得多。**
