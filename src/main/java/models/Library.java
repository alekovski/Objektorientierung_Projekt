package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Library implements Serializable {

    private final List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> getBooks() {
        return books;
    }
    }