import exception.AddNullEventException;
import exception.StorageException;
import exception.loadTODOListException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Bored_Coils
 */
public class TODOList {
    private String name;
    /**
     * 清单id
     * 这个是只读的，不允许修改
     */
    private final int id = 0;

    private List<TODOEvent> TODOList = new ArrayList<>();

    //FileManager fileManager = new FileManager();

    /**
     * 构造函数
     * @param name 清单名
     */
    TODOList(String name){
        this.name=name;
        //Random rand = new Random();
        //id=rand.nextInt(99999)+1;
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
     * @param TODOList 现成的TODOList列表
     */
    public void loadTODOList(List<TODOEvent> TODOList) {
        try {
            Objects.requireNonNull(this.TODOList);
        }catch (NullPointerException e) {
            throw new loadTODOListException(e.getMessage(), e);
        }
        this.TODOList = List.copyOf(TODOList);
        for (int i = 0; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
        save();
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
        save();
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
        save();
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
        save();
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
        for (int i = no; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
        save();
    }

    /**
     * 设置某个事项已完成
     * @param no 事项编号
     */
    public void setDone (int no){
        if(no>=TODOList.size()){ return;}
        if(no<0){ return;}
        TODOList.get(no).setStatus(true);
        save();
    }

    /**
     * 设置某个事项未完成
     * @param no 事项编号
     */
    public void setNotDone (int no){
        if(no>=TODOList.size()){ return;}
        if(no<0){ return;}
        TODOList.get(no).setStatus(false);
        save();
    }

    /**
     * 根据索引删除指定的事件，并重新排列后续事件的编号
     * @param no 要删除的事件在清单中的索引
     */
    public void removeEvent(int no){
        if(no>=TODOList.size()){ return;}
        if(no<0){ return;}
        TODOList.remove(no);
        for (int i = no; i < this.TODOList.size(); i++){
            TODOList.get(i).setNo(i);
        }
        save();
    }

    /**
     * 清空清单内容，删除所有事件
     */
    public void clear(){
        TODOList.clear();
        save();
    }

    public void save(){
        try{
            FileManager.SaveList(id, TODOList);
        }catch (StorageException e){
            throw new StorageException("清单保存失败：" + id,e);
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(name + "\n");
        for (int i = 0; i < TODOList.size(); i++){
            sb.append(TODOList.get(i).toString() + "\n");
        }
        return sb.toString();
    }




}