import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TODOList {
    public String name;
    private int id;

    private List<TODOEvent> TODOList = new ArrayList<>();


    TODOList(String name){
        this.name=name;
        Random rand = new Random();
        id=rand.nextInt(99999)+1;
    }



}
