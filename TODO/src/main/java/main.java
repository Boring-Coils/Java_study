public class main {

    public static void main(String[] args) {
        TODOList list = new TODOList("默认清单");
        System.out.println(list);
        list.loadTODOList(FileManager.LoadList(0));
        System.out.println(list);

        //list.addEvent("cao");
        //list.addEvent("ni");
        //list.addEvent("ma");
        //System.out.println(list);

    }

}
