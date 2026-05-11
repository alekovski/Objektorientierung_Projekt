package com.example.ebook_reader;

import FileHandler.FileHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Book;
import models.Library;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelloController {

    // --- UI elements injected from FXML ---
    @FXML private HBox windowHeader;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML public TableColumn<Book, String> cover;
    @FXML private TableView<Book> booksTable;
    @FXML private TextField searchField;

    // --- Application state ---
    private Library library;
    private final FileHandler fileHandler = new FileHandler();
    private final ArrayList<Book> recentlyViewedBooks = new ArrayList<>();

    // --- Window movement and resizing ---
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean maximized = false;
    private double prevX, prevY, prevW, prevH;

    // --- Theme state ---
    private boolean darkModeEnabled = false;

    /**
     * Initializes the main window:
     * - enables custom window dragging and resizing
     * - configures table columns
     * - loads the library and populates the table
     */
    @FXML
    public void initialize() {

        // Enable dragging of the custom window header
        windowHeader.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        windowHeader.setOnMouseDragged(event -> {
            Stage stage = (Stage) windowHeader.getScene().getWindow();
            if (!maximized) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Double-click to toggle maximize
        windowHeader.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize((Stage) windowHeader.getScene().getWindow());
            }
        });

        // Enable resizing once the scene is available
        windowHeader.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                enableResize(newScene, (Stage) windowHeader.getScene().getWindow());
            }
        });

        // Configure table columns
        setupCoverColumn();
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));

        // Load library and populate table
        loadLibrary();
        setupBooksTable();
    }

    // --- Window control buttons ---

    @FXML
    private void minimizeWindow() {
        ((Stage) windowHeader.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void closeWindow() {
        ((Stage) windowHeader.getScene().getWindow()).close();
    }

    @FXML
    private void toggleMaximize() {
        toggleMaximize((Stage) windowHeader.getScene().getWindow());
    }

    /**
     * Toggles between maximized and normal window size.
     */
    private void toggleMaximize(Stage stage) {
        if (!maximized) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevW = stage.getWidth();
            prevH = stage.getHeight();

            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            stage.setX(screen.getMinX());
            stage.setY(screen.getMinY());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());

            maximized = true;
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevW);
            stage.setHeight(prevH);

            maximized = false;
        }
    }

    /**
     * Applies the given stylesheet to all open windows.
     */
    private void applyThemeToAllWindows(String stylesheet) {
        Stage.getWindows().stream()
                .filter(w -> w instanceof Stage)
                .map(w -> (Stage) w)
                .forEach(stage -> {
                    Scene scene = stage.getScene();
                    if (scene != null) {
                        scene.getStylesheets().clear();
                        scene.getStylesheets().add(
                                Objects.requireNonNull(getClass().getResource(stylesheet)).toExternalForm()
                        );
                    }
                });
    }

    // --- Theme switching ---

    @FXML
    private void enableDarkMode() {
        darkModeEnabled = true;
        applyThemeToAllWindows("dark-theme.css");

        Scene scene = booksTable.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("dark-theme.css")).toExternalForm()
        );
    }

    @FXML
    private void enableLightMode() {
        darkModeEnabled = false;
        applyThemeToAllWindows("styles.css");

        Scene scene = booksTable.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm()
        );
    }

    /**
     * Enables custom window resizing by detecting mouse position near edges.
     */
    private void enableResize(Scene scene, Stage stage) {
        final int border = 6;

        scene.setOnMouseMoved(event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            boolean left = x < border;
            boolean right = x > width - border;
            boolean top = y < border;
            boolean bottom = y > height - border;

            if (left && top) scene.setCursor(Cursor.NW_RESIZE);
            else if (left && bottom) scene.setCursor(Cursor.SW_RESIZE);
            else if (right && top) scene.setCursor(Cursor.NE_RESIZE);
            else if (right && bottom) scene.setCursor(Cursor.SE_RESIZE);
            else if (left) scene.setCursor(Cursor.W_RESIZE);
            else if (right) scene.setCursor(Cursor.E_RESIZE);
            else if (top) scene.setCursor(Cursor.N_RESIZE);
            else if (bottom) scene.setCursor(Cursor.S_RESIZE);
            else scene.setCursor(Cursor.DEFAULT);
        });

        scene.setOnMouseDragged(event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            if (scene.getCursor() == Cursor.E_RESIZE || scene.getCursor() == Cursor.NE_RESIZE || scene.getCursor() == Cursor.SE_RESIZE)
                stage.setWidth(x);
            if (scene.getCursor() == Cursor.S_RESIZE || scene.getCursor() == Cursor.SE_RESIZE || scene.getCursor() == Cursor.SW_RESIZE)
                stage.setHeight(y);
            if (scene.getCursor() == Cursor.W_RESIZE || scene.getCursor() == Cursor.NW_RESIZE || scene.getCursor() == Cursor.SW_RESIZE) {
                stage.setX(event.getScreenX());
                stage.setWidth(width - (event.getScreenX() - stage.getX()));
            }
            if (scene.getCursor() == Cursor.N_RESIZE || scene.getCursor() == Cursor.NW_RESIZE || scene.getCursor() == Cursor.NE_RESIZE) {
                stage.setY(event.getScreenY());
                stage.setHeight(height - (event.getScreenY() - stage.getY()));
            }
        });
    }

    /**
     * Configures the cover column to display book cover images.
     */
    private void setupCoverColumn() {
        cover.setCellValueFactory(new PropertyValueFactory<>("coverImagePath"));
        cover.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
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

    /**
     * Loads the library from file or creates default books if empty.
     */
    private void loadLibrary() {
        library = fileHandler.loadLibrary();
        if (library.getBooks().isEmpty()) {
            library = new Library();
            addDefaultBooks();
            fileHandler.saveLibrary(library);
        }
    }

    /**
     * Adds predefined books when no saved library exists.
     */
    private void addDefaultBooks() {
        library.addBook(new Book("The Three-Body Problem", "Cixin Liu",
                "/books/book1.txt", 0, "/covers/three_body.jpg"));
        library.addBook(new Book("100 Years of Solitude", "Gabriel García Márquez",
                "/books/book2.txt", 0, "/covers/hundred_years.jpg"));
        library.addBook(new Book("Moby Dick", "Herman Melville",
                "/books/book3.txt", 0, "/covers/moby_dick.jpg"));
        library.addBook(new Book("Alice's Adventures in Wonderland", "Lewis Carroll",
                "/books/book4.txt", 0, "/covers/wonderland.jpg"));
        library.addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald",
                "/books/book5.txt", 0, "/covers/gatsby.jpg"));
    }

    /**
     * Populates the TableView with books from the library.
     */
    private void setupBooksTable() {
        booksTable.setItems(FXCollections.observableArrayList(library.getBooks()));
    }
    
    // --- Book opening and reading ---

    /**
     * Opens the selected book from the table.
     */
    @FXML
    private void openBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            loadAndShowBook(selectedBook);
        } else {
            showError("No book was selected");
        }
    }

    /**
     * Loads the book content and opens the reader window.
     */
    private void loadAndShowBook(Book book) {
        try {
            String content = loadBookContent(book);
            showBookContent(book, content);
            addToRecentlyViewed(book);
        } catch (Exception e) {
            showError("Error while opening a book: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the text file of the selected book from the resources folder.
     */
    private String loadBookContent(Book book) throws IOException, URISyntaxException {
        URI fileUri = Objects.requireNonNull(getClass().getResource(book.getFilePath())).toURI();
        return new String(Files.readAllBytes(Paths.get(fileUri)));
    }

    /**
     * Creates and displays the reader window with:
     * - custom header (drag, minimize, maximize, close)
     * - book cover
     * - scrollable text area
     * - theme support (light/dark)
     */
    private void showBookContent(Book book, String content) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        // --- Header ---
        HBox header = new HBox();
        header.getStyleClass().add("window-header");

        Label title = new Label(book.getTitle());
        title.getStyleClass().add("window-title");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimize = new Button("—");
        minimize.getStyleClass().add("window-button");

        Button maximize = new Button("⬜");
        maximize.getStyleClass().add("window-button");

        Button close = new Button("✕");
        close.getStyleClass().addAll("window-button", "window-button-close");

        header.getChildren().addAll(title, spacer, minimize, maximize, close);

        // --- Content area ---
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Cover image
        try (InputStream stream = getClass().getResourceAsStream(book.getCoverImagePath())) {
            ImageView coverImage = new ImageView(new Image(stream));
            coverImage.setFitWidth(200);
            coverImage.setPreserveRatio(true);
            contentBox.getChildren().add(coverImage);
        } catch (Exception ignored) {}

        // TextArea (reader)
        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setFocusTraversable(false);

        textArea.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-control-inner-background: transparent;"
        );

        textArea.setPrefRowCount(20);

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        contentBox.getChildren().add(scrollPane);

        // Root layout
        VBox root = new VBox(header, contentBox);

        // Scene size (90% of screen)
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(
                root,
                screen.getWidth() * 0.9,
                screen.getHeight() * 0.9
        );

        stage.setScene(scene);

        // Apply theme
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(darkModeEnabled ? "dark-theme.css" : "styles.css")
                ).toExternalForm()
        );

        // Header interactions
        header.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        header.setOnMouseDragged(e -> {
            if (!maximized) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });

        header.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) toggleMaximize(stage);
        });

        minimize.setOnAction(e -> stage.setIconified(true));
        close.setOnAction(e -> stage.close());
        maximize.setOnAction(e -> toggleMaximize(stage));

        // Enable resizing
        enableResize(scene, stage);

        stage.show();
    }

    /**
     * Adds a book to the recently viewed list (max 10 items).
     */
    private void addToRecentlyViewed(Book book) {
        recentlyViewedBooks.remove(book);
        recentlyViewedBooks.add(book);
        if (recentlyViewedBooks.size() > 10) {
            recentlyViewedBooks.remove(0);
        }
    }

    // --- Recently Viewed window ---

    /**
     * Opens a window showing the last 10 viewed books.
     */
    @FXML
    private void showRecentlyViewed() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("window-header");

        Label title = new Label("Recently Viewed");
        title.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().addAll("window-button", "window-button-close");
        close.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, spacer, close);

        // ListView
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

        // Root
        VBox root = new VBox(header, listView);
        Scene scene = new Scene(root, 400, 300);

        // Apply theme
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(darkModeEnabled ? "dark-theme.css" : "styles.css")
                ).toExternalForm()
        );

        // Dragging
        header.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        header.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        stage.setScene(scene);
        stage.show();
    }

    // --- Add / Edit / Delete books ---

    /**
     * Opens a custom window to add a new book.
     */
    @FXML
    private void addBook() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("window-header");

        Label title = new Label("Add Book");
        title.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().addAll("window-button", "window-button-close");
        close.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, spacer, close);

        // Input fields
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField fileField = new TextField();
        TextField coverField = new TextField();

        titleField.setPromptText("Title");
        authorField.setPromptText("Author");
        fileField.setPromptText("/books/bookX.txt");
        coverField.setPromptText("/covers/coverX.jpg");

        VBox fields = new VBox(10, titleField, authorField, fileField, coverField);
        fields.setPadding(new Insets(10));

        // Buttons
        Button ok = new Button("OK");
        ok.getStyleClass().add("modern-button");

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("modern-button");
        cancel.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, ok, cancel);
        buttons.setPadding(new Insets(10));

        // Root
        VBox root = new VBox(header, fields, buttons);

        Scene scene = new Scene(root, 400, 260);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(darkModeEnabled ? "dark-theme.css" : "styles.css")
                ).toExternalForm()
        );

        // Dragging
        header.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        header.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        // OK action
        ok.setOnAction(e -> {
            Book book = new Book(
                    titleField.getText(),
                    authorField.getText(),
                    fileField.getText(),
                    0,
                    coverField.getText()
            );
            library.addBook(book);
            booksTable.getItems().add(book);
            fileHandler.saveLibrary(library);
            stage.close();
        });

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Deletes the selected book from the library.
     */
    @FXML
    private void deleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No book selected");
            return;
        }

        library.getBooks().remove(selected);
        booksTable.getItems().remove(selected);
        fileHandler.saveLibrary(library);
    }

    /**
     * Opens a custom window to edit the selected book.
     */
    @FXML
    private void editBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No book selected");
            return;
        }

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("window-header");

        Label title = new Label("Edit Book");
        title.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().addAll("window-button", "window-button-close");
        close.setOnAction(e -> stage.close());

        header.getChildren().addAll(title, spacer, close);

        // Input fields
        TextField titleField = new TextField(selected.getTitle());
        TextField authorField = new TextField(selected.getAuthor());
        TextField fileField = new TextField(selected.getFilePath());
        TextField coverField = new TextField(selected.getCoverImagePath());

        VBox fields = new VBox(10, titleField, authorField, fileField, coverField);
        fields.setPadding(new Insets(10));

        // Buttons
        Button ok = new Button("OK");
        ok.getStyleClass().add("modern-button");

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("modern-button");
        cancel.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, ok, cancel);
        buttons.setPadding(new Insets(10));

        // Root
        VBox root = new VBox(header, fields, buttons);

        Scene scene = new Scene(root, 400, 260);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        getClass().getResource(darkModeEnabled ? "dark-theme.css" : "styles.css")
                ).toExternalForm()
        );

        // Dragging
        header.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        header.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        // OK action
        ok.setOnAction(e -> {
            selected.setTitle(titleField.getText());
            selected.setAuthor(authorField.getText());
            selected.setFilePath(fileField.getText());
            selected.setCoverImagePath(coverField.getText());

            booksTable.refresh();
            fileHandler.saveLibrary(library);
            stage.close();
        });

        stage.setScene(scene);
        stage.show();
    }

    // --- Search ---

    /**
     * Filters the book list by title or author.
     */
    @FXML
    private void searchBooks() {
        String query = searchField.getText().toLowerCase().trim();

        if (query.isEmpty()) {
            booksTable.setItems(FXCollections.observableArrayList(library.getBooks()));
            return;
        }

        booksTable.setItems(
                FXCollections.observableArrayList(
                        library.getBooks().stream()
                                .filter(book ->
                                        book.getTitle().toLowerCase().contains(query) ||
                                        book.getAuthor().toLowerCase().contains(query))
                                .collect(Collectors.toList())
                )
        );
    }

    // --- Error dialogs ---

    /**
     * Shows a simple error dialog with a message.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error dialog including exception details.
     */
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\n" + e.getMessage());
        alert.showAndWait();
    }
}