package models;

import java.io.Serializable;

public class Book implements Serializable {
    private String title;
    private String author;
    private String filePath;
    private int currentPage;
    private String coverImagePath;

    public Book(String title, String author, String filePath, int currentPage, String coverImagePath) {
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.currentPage = currentPage;
        this.coverImagePath = coverImagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", filePath='" + filePath + '\'' +
                ", currentPage=" + currentPage +
                ", coverImagePath='" + coverImagePath + '\'' +
                '}';
    }
}