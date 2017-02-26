package service;

import app.Controller;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Class for work with files.
 * Implements FileService interface
 */
public class FileServiceImpl implements FileService {
    protected File csvFile = new File("parsed.csv");
    private ArrayList<String > headersList = new ArrayList<>();
    private ArrayList<String > csvFileData = new ArrayList<>();
    private static FileServiceImpl fileServiceImpl;

    /**
     * Constructor with creating csv file.
     */
    public FileServiceImpl() {
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                System.out.println("Creating new file.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        csvFile.canWrite();
        csvFile.canRead();
    }

    /**
     * Method returns instance of current object.
     * @return
     */
    public FileServiceImpl getInstance() {
        if (fileServiceImpl == null) {
            fileServiceImpl = new FileServiceImpl();
        }
        return fileServiceImpl;
    }

    /**
     * Parsing xls file to csv file.
     * Add headers from xls file to list.
     *
     * @param file - takes only xls file.
     *
     * @return csv file with parsed data from xls file
     */
    @Override
    public File parse(final File file) {
        String result = "";
        InputStream inputStream = null;
        HSSFWorkbook workBook = null;
        FileWriter fileWriter = null;
        try {
            inputStream = new FileInputStream(file);
            workBook = new HSSFWorkbook(inputStream);
            fileWriter = new FileWriter(csvFile, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        if (it.hasNext()) {
            Row headers = sheet.getRow(0);
            int numOfHeaders = 0;
            if (headers != null) {
                numOfHeaders = headers.getPhysicalNumberOfCells();
            }
            for (int i = 0; i < numOfHeaders; i++) {
                if(headers.getCell(i).getCellType() == HSSFCell.CELL_TYPE_STRING){
                    String name = headers.getCell(i).getStringCellValue();
                    result += name + ",";
                    headersList.add(name);
                }
            }
            result += "\n";
        }
        it.next();
        while (it.hasNext()) {
            Row row = it.next();
            Iterator<Cell> cells = row.iterator();
            while (cells.hasNext()) {
                Cell cell = cells.next();
                int cellType = cell.getCellType();
                switch (cellType) {
                    case Cell.CELL_TYPE_STRING:
                        result += "" + cell.getStringCellValue() + ",";
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        result += "" + cell.getNumericCellValue() + ",";
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        result += "" + cell.getNumericCellValue() + ",";
                        break;
                    default:
                        result += "" + ",";
                        break;
                }
            }
            result += "\n";
        }
        try {
            fileWriter.write(result);
            fileWriter.close();
            workBook.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFile;
    }

    /**
     * Read csv file line by line, and add to list string data.
     *
     * @param csvFile - takes csv file
     * @return ArrayList<String>
     */
    @Override
    public ArrayList<String > readCsvFile(final File csvFile) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))){
            String lineOfCsvFile;
            bufferedReader.readLine();
            while((lineOfCsvFile = bufferedReader.readLine()) != null){
                csvFileData.add(lineOfCsvFile) ;
            }
        } catch (IOException e){
            System.out.println("File not found!");
            e.printStackTrace();
        }
        return csvFileData;
    }

    @Override
    public List<String > getFileHeaders() {
        return headersList;
    }
    @Override
    public ArrayList<String> getCsvFileData() {
        return csvFileData;
    }

    /**
     * Write json object to json file.
     *
     * @param jsonObject - takes json object
     */
    @Override
    public void saveGsonObjectToFile(final JSONObject jsonObject){
        File jsonFile = new File(Controller.getTableName() + "_settings.json");
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        jsonFile.canWrite();
        jsonFile.canRead();
        try {
            FileWriter fileWriter = new FileWriter(jsonFile, true);
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.append("\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
