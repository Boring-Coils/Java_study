import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TODOList {
    public String name;
    /**
     * 清单id
     * 这个是只读的，不允许修改
     */
    private int id;

    private List<TODOEvent> TODOList = new ArrayList<>();

    /**
     * 构造函数
     * @param name 清单名
     */
    TODOList(String name){
        this.name=name;
        Random rand = new Random();
        id=rand.nextInt(99999)+1;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public List<TODOEvent> getTODOList() {
        return TODOList;
    }

    public void setTODOList(List<TODOEvent> TODOList) {
        this.TODOList = TODOList;
    }


    /**
     * 向清单的末尾添加事项
     * @param event 事项的实例
     */
    public void addEvent(TODOEvent event){
        event.setNo(TODOList.size());
        TODOList.add(event);
    }

    /**
     * 向清单的指定位置插入事项
     * @param event 事项的实例
     * @param no 位置
     */
    public void addEvent(TODOEvent event, int no){
        if(no>TODOList.size()){ no = TODOList.size();}
        else if(no<0){ no = 0;}
        event.setNo(no);
        TODOList.add(no, event);
    }

    /**
     * 向清单的末尾添加事项
     * @param event 事项的内容字符串
     */
    public void addEvent(String event){
        TODOEvent e = new TODOEvent(TODOList.size(), event);
        TODOList.add(e);
    }

    /**
     * 向清单的指定位置插入事项
     * @param event 事项的内容字符串
     * @param no 位置
     */
    public void addEvent(String event, int no){
        if(no>TODOList.size()){ no = TODOList.size();}
        else if(no<0){ no = 0;}
        TODOEvent e = new TODOEvent(no, event);
        TODOList.add(no, e);
    }

}
