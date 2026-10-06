import java.util.Objects;

/**
 * @author Bored_Coils
 * 这个类负责储存列表中的某一个事项
 */
public class TODOEvent {
    // 事件在某张表上的序号
    private int no;
    private String event;
    private boolean isDone = false;

    public int getNo() {return no;}
    public void setNo(int no) {this.no = no;}
    public String getEvent() {return event;}
    public boolean getStatus() {return isDone;}
    public void setStatus(boolean status) {this.isDone = status;}

    public TODOEvent() {}

    /**
     * 构造函数
     * @param no 事件在该表上的编号
     * @param event 事件内容
     */
    TODOEvent(int no, String event) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TODOEvent todoEvent = (TODOEvent) o;
        return isDone == todoEvent.isDone && java.util.Objects.equals(event, todoEvent.event);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(event, isDone);
        return result;
    }


}
