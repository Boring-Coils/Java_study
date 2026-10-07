import exception.AddNullEventException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        return List.copyOf(TODOList);
    }

    /**
     * 通过现成的列表生成，调用该方法会直接覆盖原有内容<br>
     * 该方法会自动依照事项在列表中的位置重写事项的{@code no}属性
     * @param TODOList
     */
    public void loadTODOList(List<TODOEvent> TODOList) {
        this.TODOList = TODOList;
        for (int i = 0; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
    }


    /**
     * 向清单的末尾添加事项
     * @param event 事项的实例
     */
    public void addEvent(TODOEvent event){
        try{
            Objects.requireNonNull(event, "event 不能为空");
        }catch (NullPointerException e){
            throw new AddNullEventException(e.getMessage(),e);
        }
        event.setNo(TODOList.size());
        TODOList.add(event);
    }

    /**
     * 向清单的指定位置插入事项
     * @param event 事项的实例
     * @param no 位置
     */
    public void addEvent(TODOEvent event, int no){
        try{
            Objects.requireNonNull(event, "event 不能为空");
        }catch (NullPointerException e){
            throw new AddNullEventException(e.getMessage(),e);
        }
        if(no>TODOList.size()){ no = TODOList.size();}
        else if(no<0){ no = 0;}
        event.setNo(no);
        TODOList.add(no, event);
        for (int i = no; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
    }

    /**
     * 向清单的末尾添加事项
     * @param event 事项的内容字符串
     */
    public void addEvent(String event){
        try{
            Objects.requireNonNull(event, "event 不能为空");
        }catch (NullPointerException e){
            throw new AddNullEventException(e.getMessage(),e);
        }
        TODOEvent e = new TODOEvent(TODOList.size(), event);
        TODOList.add(e);
    }

    /**
     * 向清单的指定位置插入事项
     * @param event 事项的内容字符串
     * @param no 位置
     */
    public void addEvent(String event, int no){
        try{
            Objects.requireNonNull(event, "event 不能为空");
        }catch (NullPointerException e){
            throw new AddNullEventException(e.getMessage(),e);
        }
        if(no>TODOList.size()){ no = TODOList.size();}
        else if(no<0){ no = 0;}
        TODOEvent e = new TODOEvent(no, event);
        TODOList.add(no, e);
        for (int i = 0; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
    }

}
