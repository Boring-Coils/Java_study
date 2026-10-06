/**
 * @author Bored_Coils
 */
public class Book {
    //书名
    private String title;
    //作者
    private String author;
    //书本在图书馆中的id，也是唯一的
    private Long id = 0L;
    //ISBN
    private String isbn;
    //书本的状态
    private BookStatus status = BookStatus.AVAILABLE;

    /**
     * @param title 书本的标题
     * @param author 书本的作者
     * @param id 书本在图书馆内的id
     * @param isbn 书本的ISBN
     */
    Book(String title, String author, Long id, String isbn) {
        this.title = title;
        this.author = author;
        this.id = id;
        this.isbn = isbn;
    }

    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public Long getId() {return id;}
    public String getIsbn() {return isbn;}
    public BookStatus getStatus() {return status;}
    public void setStatus(BookStatus status) {this.status = status;}

}
