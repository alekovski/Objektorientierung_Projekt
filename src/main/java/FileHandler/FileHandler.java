package FileHandler;

import models.Library;
import java.io.*;

public class FileHandler {

    public void saveLibrary(Library library, String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("library.dat"))) {
            oos.writeObject(library);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Library loadLibrary(String path) {
        File file = new File("library.dat");
        if (!file.exists()) {
            return new Library();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("library.dat"))) {
            return (Library) ois.readObject();
        } catch (Exception e) {
            return new Library();
        }
    }
}