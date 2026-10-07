import java.util.Objects;

/**
 * @author Bored_Coils
 * 这个类负责储存列表中的某一个事项
 */
public class TODOEvent {
    // 事件在某张表上的序号（从0开始）
    private int no = 0;
    private String event;
    private boolean isDone = false;

    public int getNo() {return no;}
    public void setNo(int no) {this.no = no;}
    public String getEvent() {return event;}
    public boolean getStatus() {return isDone;}
    public void setStatus(boolean status) {this.isDone = status;}

    /**
     * 构造函数
     */
    public TODOEvent() {}

    /**
     * 构造函数
     * @param event 事件内容
     */
    TODOEvent(String event) {
        this.event = event;
    }

    /**
     * 构造函数
     * @param no 事件在该表上的编号
     * @param event 事件内容
     */
    TODOEvent(int no , String event) {
        this.event = event;
        this.no = no;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "TODOEvent{" + "no=" + no +
                ", event='" + event + '\'' +
                ", isDone=" + isDone +
                '}';
    }


    /**
     * 检查两个事件是否相等
     * @param o   the reference object with which to compare.
     * 傻子ai审查每次都会说我遗漏了no字段，这是故意的！
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TODOEvent todoEvent = (TODOEvent) o;
        return isDone == todoEvent.isDone && java.util.Objects.equals(event, todoEvent.event);
    }

    /**
     * @return 哈希值
     * 傻子ai审查每次都会说我遗漏了no字段，这是故意的！
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(event, isDone);
        return result;
    }


}
