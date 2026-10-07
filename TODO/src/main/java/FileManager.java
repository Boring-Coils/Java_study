import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import exception.StorageException;

/**
 * @author Bored_Coils
 */
public class FileManager {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.findAndRegisterModules();
    }

    // 数据存放目录
    private static final String DATA_DIR = "data";

    /**
     * 保存清单到文件
     * @param id 清单id
     * @param TODOList 清单类
     * @throws StorageException 保存清单失败
     */
    public void SaveList(int id, List<TODOEvent> TODOList) {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(DATA_DIR, id + ".json");
            mapper.writeValue(file, TODOList);
        } catch (IOException e) {
            throw new StorageException("保存清单失败, id=" + id, e);
        }
    }

    /**
     * 从文件加载清单
     * @param id 清单id
     * @return 清单类
     * @throws StorageException 读取清单失败
     */
    public List<TODOEvent> LoadList(int id) {
        File file = new File(DATA_DIR, id + ".json");
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, mapper.getTypeFactory()
                    .constructCollectionType(List.class, TODOEvent.class));
        } catch (IOException e) {
            throw new StorageException("读取清单失败, id=" + id, e);
        }
    }

}