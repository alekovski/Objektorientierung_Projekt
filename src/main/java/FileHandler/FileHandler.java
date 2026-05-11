package FileHandler;

import models.Library;
import java.io.*;

/**
 * Provides simple persistence for the Library object using Java serialization.
 * The library is stored in a fixed file named "library.dat" in the working directory.
 */
public class FileHandler {

    // Name of the file used to store the serialized library
    private static final String FILE_NAME = "library.dat";

    /**
     * Saves the given Library object to disk using Java serialization.
     */
    public void saveLibrary(Library library) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(library);
        } catch (IOException e) {
            System.err.println("Error saving library: " + e.getMessage());
        }
    }

    /**
     * Loads the Library object from disk.
     * If the file does not exist or cannot be read, a new empty Library is returned.
     */
    public Library loadLibrary() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new Library();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Library) ois.readObject();
        } catch (Exception e) {
            return new Library();
        }
    }
}