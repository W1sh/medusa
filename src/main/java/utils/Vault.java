package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Vault {

    private static final String file = "/home/bruno/IdeaProjects/jdbot/src/main/resources/config.properties";

    private Vault(){}

    public static void store(String key, String value, String comments){
        Properties prop = new Properties();
        prop.setProperty(key, value);

        try(FileWriter fileWriter = new FileWriter(file)) {
            prop.store(fileWriter, comments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String fetch(String key){
        Properties prop = new Properties();
        try(FileReader fileReader = new FileReader(file)) {
            prop.load(fileReader);
            return prop.getProperty(key);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
