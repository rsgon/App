package service;

import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for work with files.
 */
public interface FileService {
    File parse(File file);
    ArrayList<String > readCsvFile(final File csvFile);
    ArrayList<String> getCsvFileData();
    void saveGsonObjectToFile(final JSONObject jsonObject);
    List<String > getFileHeaders();
    FileService getInstance();
}
