package com.csvjson.service;

import com.csvjson.model.User;

import javax.servlet.ServletContext;
import java.util.List;

public interface UserService {
    public List<User> findAll();
    public boolean createJson(List<User> users, ServletContext context);

    //boolean createcsv(List<User> users, ServletContext context);
}