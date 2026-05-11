package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the entire collection of books in the application.
 * This class is serializable so it can be saved and loaded as a single object.
 */
public class Library implements Serializable {

    // Internal list storing all books in the library
    private List<Book> books = new ArrayList<>();

    // Ensures stable serialization compatibility across versions
    private static final long serialVersionUID = 1L;

    /**
     * Returns the list of all books.
     * Used by the controller to populate the TableView.
     */
    public List<Book> getBooks() {
        return books;
    }

    /**
     * Adds a new book to the library.
     */
    public void addBook(Book book) {
        books.add(book);
    }

    public boolean contains(Book book) {
        return books.contains(book);
    }

    /**
     * Replaces the entire book list (used when loading saved data).
     */
    public void setBooks(List<Book> books) {
        this.books = books;
    }

    /**
    * Removes the given book from the library.
    * Used by the controller when deleting a book.
    */
    public void removeBook(Book book) {
        books.remove(book);
    }
}