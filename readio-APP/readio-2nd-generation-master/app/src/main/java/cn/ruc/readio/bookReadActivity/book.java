package cn.ruc.readio.bookReadActivity;

import android.util.Pair;

import java.util.ArrayList;


public class book {
    private String bookName;
    private String authorName;
    private String bookAbstract;
    private String bookId;
    private int size;
    private int progress;
    private ArrayList<Pair<String, String>> content;

    public book() {
    }

    public void setBookAbstract(String bookAbstract) {
        this.bookAbstract = bookAbstract;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int page) {
        progress = page;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public ArrayList<Pair<String, String>> getContent() {
        return content;
    }

    public void setContent(ArrayList<Pair<String, String>> content) {
        this.content = content;
    }

    public String getChapterName(int i) {
        return content.get(i).first;
    }

    public String getChapter(int i) {
        return content.get(i).second;
    }
}
