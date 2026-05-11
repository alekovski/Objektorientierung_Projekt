package models;

import java.io.Serializable;

/**
 * Represents a single book in the library.
 * This class is a simple data container (model) used by the application.
 * It is serializable so it can be saved to and loaded from a file.
 */
public class Book implements Serializable {

    // The title of the book (displayed in the UI)
    private String title;

    // The author of the book
    private String author;

    // Path to the text file stored inside the resources folder
    private String filePath;

    // Current reading progress (used for future features)
    private int currentPage;

    // Path to the cover image stored inside the resources folder
    private String coverImagePath;

    // Ensures stable serialization compatibility across versions
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new Book object with all required information.
     * This constructor is used when loading default books or reading saved data.
     */
    public Book(String title, String author, String filePath, int currentPage, String coverImagePath) {
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.currentPage = currentPage;
        this.coverImagePath = coverImagePath;
    }

    // --- Getters and setters (required by JavaFX and serialization) ---

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

    /**
     * Returns a readable text representation of the book.
     * Used for debugging and for displaying items in ListView.
     */
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