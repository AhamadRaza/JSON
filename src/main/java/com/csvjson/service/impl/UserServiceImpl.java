package com.csvjson.service.impl;

import com.csvjson.model.User;
import com.csvjson.repo.UserRepository;
import com.csvjson.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired private UserRepository userRepository;
    @Override
    public List<User> findAll() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public boolean createJson(List<User> users, ServletContext context) {
        String filePath = context.getRealPath("/resources/reports");
        boolean exists = new File(filePath).exists();
        if(!exists){
            new File(filePath).mkdirs();
        }
        File file = new File(filePath+"/"+File.separator+"users.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, users);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean createcsv(List<User> users, ServletContext context) {
        String filePath = context.getRealPath("/resources/reports");
        boolean exists = new File(filePath).exists();
        if(!exists){
            new File(filePath).mkdirs();
        }
        File file = new File(filePath+"/"+File.separator+"users.csv");
        try{
            FileWriter fileWriter = new FileWriter(file);
            CSVWriter writer = new CSVWriter(fileWriter);
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"First Nmae","Last Name","Email","Mobile"});
            for (User user : users){
                data.add(new String[]{user.getFirstName(),user.getLastName(),user.getEmail(),user.getMobile()});
            }
            writer.writeAll(data);
            writer.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean saveDataFromUploadFile(MultipartFile file) {
        boolean isFlag = false;
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(extension.equalsIgnoreCase("json")){
            isFlag = readDadaFromJson(file);
        }
       else if(extension.equalsIgnoreCase("csv")){
            isFlag = readDadaFromCsv(file);
        }
        else if(extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")){
            isFlag = readDadaFromExcel(file);
        }
        return isFlag;
    }

    private boolean readDadaFromExcel(MultipartFile file) {
        Workbook workbook = getWorkBook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        rows.next();
        while (rows.hasNext()){
            Row row = rows.next();
            User user = new User();
            if(row.getCell(0).getCellType() == Cell.CELL_TYPE_STRING){
                user.setFirstName(row.getCell(0).getStringCellValue());
            }
            if(row.getCell(1).getCellType() == Cell.CELL_TYPE_STRING){
                user.setLastName(row.getCell(1).getStringCellValue());
            }
            if(row.getCell(2).getCellType() == Cell.CELL_TYPE_STRING){
                user.setEmail(row.getCell(2).getStringCellValue());
            }
            if(row.getCell(3).getCellType() == Cell.CELL_TYPE_NUMERIC){
                String mobile = NumberToTextConverter.toText(row.getCell(3).getNumericCellValue());
                user.setMobile(mobile);
            }
            else  if(row.getCell(3).getCellType() == Cell.CELL_TYPE_STRING){
                user.setMobile(row.getCell(3).getStringCellValue());
            }
            user.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
            userRepository.save(user);
        }
      return true;
    }

    private Workbook getWorkBook(MultipartFile file) {
        Workbook workbook = null;
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        try {
            if(extension.equalsIgnoreCase("xlsx")){
                workbook = new XSSFWorkbook(file.getInputStream());
            }
            else if(extension.equalsIgnoreCase("xls")){
                workbook = new HSSFWorkbook(file.getInputStream());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return workbook;
    }

    private boolean readDadaFromCsv(MultipartFile file) {
        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows ){
                userRepository.save(new User(row[0],row[1],row[2],row[3],FilenameUtils.getExtension(file.getOriginalFilename())));
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    private boolean readDadaFromJson(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            List<User> users = Arrays.asList(mapper.readValue(inputStream, User[].class));
            if(users!=null && users.size()>0) {
                for (User user : users) {
                    user.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
                    userRepository.save(user);
                }
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}