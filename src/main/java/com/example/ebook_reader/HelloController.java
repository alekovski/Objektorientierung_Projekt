package com.example.ebook_reader;

import FileHandler.FileHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Book;
import models.Library;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelloController {

    @FXML
    public TableColumn<Book, String> cover;
    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TextField searchField;
    @FXML
    private ProgressBar progressBar;

    private Library library;
    private final FileHandler fileHandler = new FileHandler();
    private final ArrayList<Book> recentlyViewedBooks = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("Initializing controller...");
        setupCoverColumn();
        loadLibrary();
        setupBooksTable();
    }

    private void setupCoverColumn() {
        System.out.println("Setting up cover column...");
        cover.setCellValueFactory(new PropertyValueFactory<>("coverImagePath"));
        cover.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    System.out.println(imagePath);
                        Image image = new Image(getClass().getResource(imagePath).toExternalForm());
                        imageView.setImage(image);
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(120);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                    }
                }
        });
    }

    private void loadLibrary() {
        System.out.println("Loading library...");
        library = fileHandler.loadLibrary("library.dat");
        if (library == null || library.getBooks().isEmpty()) {
            System.out.println("Creating new library with default books...");
            library = new Library();
            addDefaultBooks();
            fileHandler.saveLibrary(library, "library.dat");
        }
        System.out.println("Library contains " + library.getBooks().size() + " books");
    }

    private void addDefaultBooks() {
        Book book1 = new Book("Проблема трьох тіл", "Лю Цисінь",
                "/books/book1.txt", 0, "/covers/three_body.jpg");
        Book book2 = new Book("Сто років самотності", "Габріель Гарсіа Маркес",
                "/books/book2.txt", 0, "/covers/hundred_years.jpg");
        Book book3 = new Book("Moby Dick", "Herman Melville",
                "/books/book3.txt", 0, "/covers/moby_dick.jpg");
        Book book4 = new Book("Alice's Adventures in Wonderland", "Lewis Carroll",
                "/books/book3.txt", 0, "/covers/wonderland.jpg");
        Book book5 = new Book("The Great Gatsby", "F. Scott Fitzgerald",
                "/books/book3.txt", 0, "/covers/gatsby.jpg");
        library.addBook(book1);
        library.addBook(book2);
        library.addBook(book3);
        library.addBook(book4);
        library.addBook(book5);
    }

    private void setupBooksTable() {
        System.out.println(library.getBooks());
        System.out.println("Setting up books table...");
        booksTable.setItems(FXCollections.observableArrayList(library.getBooks()));
        booksTable.setRowFactory(tv -> new TableRow<Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (book == null || empty) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: #f8f8f8;");
                }
            }
        });
    }

    @FXML
    private void openBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            loadAndShowBook(selectedBook);
        } else {
            showError("No book was selected");
        }
    }

    private void loadAndShowBook(Book book) {
        try {
            String content = loadBookContent(book);
            showBookContent(book, content);
            addToRecentlyViewed(book);
        } catch (IOException | URISyntaxException e) {
            showError("Error while opening a book: " + e.getMessage(), e);
        }
    }

    private String loadBookContent(Book book) throws IOException, URISyntaxException {
        URI fileUri = null;
        try {
            fileUri = Objects.requireNonNull(getClass().getResource(book.getFilePath())).toURI();
            System.out.println("File URI: " + fileUri); // Додано налагодження
            return new String(Files.readAllBytes(Paths.get(fileUri)));
        } catch (NullPointerException e) {
            System.err.println("Error loading book content: " + book.getFilePath());
            e.printStackTrace();
            throw e;
        }
    }

    private void showBookContent(Book book, String content) {
        try {
            Stage bookStage = new Stage();
            bookStage.setTitle(book.getTitle());

            VBox root = new VBox(10);

            try (InputStream stream = getClass().getResourceAsStream(book.getCoverImagePath())) {
                if (stream == null) {
                    throw new IOException("Cover image not found: " + book.getCoverImagePath());
                }
                ImageView coverImage = new ImageView(new Image(stream));
                coverImage.setFitWidth(200);
                coverImage.setPreserveRatio(true);
                root.getChildren().add(coverImage);
            }

            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            root.getChildren().add(textArea);

            root.setPadding(new Insets(15));
            root.setAlignment(Pos.TOP_CENTER);

            Scene scene = new Scene(root, 800, 600);
            bookStage.setScene(scene);
            bookStage.show();
        } catch (Exception e) {
            showError("Error showing book content", e);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private void addToRecentlyViewed(Book book) {
        if (recentlyViewedBooks.contains(book)) {
            recentlyViewedBooks.remove(book);
        }
        recentlyViewedBooks.add(book);
        if (recentlyViewedBooks.size() > 10) {
            recentlyViewedBooks.remove(0);
        }
    }

    @FXML
    private void showRecentlyViewed() {
        Stage stage = new Stage();
        stage.setTitle("Recently Viewed");

        ListView<Book> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(recentlyViewedBooks));

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = listView.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    loadAndShowBook(selectedBook);
                    stage.close();
                }
            }
        });

        stage.setScene(new Scene(new VBox(listView), 400, 300));
        stage.show();
    }

    @FXML
    private void addBook() {
        showError("Add book functionality not implemented yet.");
    }

    @FXML
    private void deleteBook() {
        showError("Delete book functionality not implemented yet.");
    }

    @FXML
    private void editBook() {
        showError("Edit book functionality not implemented yet.");
    }

    @FXML
    private void searchBooks() {
        String searchText = searchField.getText().toLowerCase();
        List<Book> filteredBooks = library.getBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        booksTable.setItems(FXCollections.observableArrayList(filteredBooks));
    }
}