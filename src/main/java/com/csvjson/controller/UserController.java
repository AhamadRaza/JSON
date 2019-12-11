package com.csvjson.controller;

import com.csvjson.model.User;
import com.csvjson.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
public class UserController {
    @Autowired private UserService userService;
    @Autowired private ServletContext context;

    @GetMapping(value = "/")
    public String getUser(Model model){
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "view/user";
    }
    @GetMapping(value = "/createjson")
    public void createJson(HttpServletRequest request, HttpServletResponse response){
        List<User> users = userService.findAll();
        boolean isFLag = userService.createJson(users, context);
        if(isFLag){
            String fullPath = request.getServletContext().getRealPath("/resources/reports/"+"users.json");
            fileDownload(fullPath, "user.json", response);
        }
    }
    /*@GetMapping(value = "/createcsv")
    public void createcsv(HttpServletRequest request, HttpServletResponse response){
        List<User> users = userService.findAll();
        boolean isFLag = userService.createcsv(users, context);
        if(isFLag){
            String fullPath = request.getServletContext().getRealPath("/resources/reports/"+"user.csv");
            fileDownload(fullPath, response, "user.csv");
        }
    }*/

    private void fileDownload(String fullPath, String filename, HttpServletResponse response) {
        File file = new File(fullPath);
        final int BUFFER_SIZE = 4096;
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("Content-disposition", "attachment; filename" + filename);
                OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int byteRead = -1;
                while ((byteRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }
                fileInputStream.close();
                outputStream.close();
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}