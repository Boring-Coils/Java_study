import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileManager {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.findAndRegisterModules();
    }

    // 数据存放目录
    private static final String DATA_DIR = "data";


    public void SaveList(int id, List<TODOEvent> TODOList) {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(DATA_DIR, id + ".json");
            mapper.writeValue(file, TODOList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TODOEvent> LoadList(int id) {
        File file = new File(DATA_DIR, id + ".json");
        if (!file.exists()) {
            return null;
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory()
                    .constructCollectionType(List.class, TODOEvent.class));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}