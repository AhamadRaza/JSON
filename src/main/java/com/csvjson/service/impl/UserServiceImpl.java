package com.csvjson.service.impl;

import com.csvjson.model.User;
import com.csvjson.repo.UserRepository;
import com.csvjson.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.opencsv.CSVWriter;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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
            List<String[]> data = new ArrayList<String[]>();
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
}