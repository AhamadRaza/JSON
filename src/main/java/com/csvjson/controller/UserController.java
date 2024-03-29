package com.csvjson.controller;

import com.csvjson.model.User;
import com.csvjson.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
public class UserController {
    //user
    @Autowired private UserService userService;
    @Autowired private ServletContext context;

    @GetMapping(value = "/")
    public String getUser(Model model){
        model.addAttribute("user", new User());
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
            fileDownload(fullPath, response, "users.json");
        }
    }
    @GetMapping(value = "/createcsv")
    public void createcsv(HttpServletRequest request, HttpServletResponse response){
        List<User> users = userService.findAll();
        boolean isFLag = userService.createcsv(users, context);
        if(isFLag){
            String fullPath = request.getServletContext().getRealPath("/resources/reports/"+"users.csv");
            fileDownload(fullPath, response, "users.csv");
        }
    }

    @PostMapping(value = "/fileupload")
    public String uploadFile(@ModelAttribute User user, RedirectAttributes redirectAttributes){
        boolean isFlag = userService.saveDataFromUploadFile(user.getFile());
        if(isFlag){
            redirectAttributes.addFlashAttribute("successmessage", "file uploaded sucessfully!");
        }
        else {
            redirectAttributes.addFlashAttribute("errormessage", "file not uploaded , Please try later!");
        }
        return "redirect:/";
    }

    private void fileDownload(String fullPath, HttpServletResponse response, String filename) {
        File file = new File(fullPath);
        final int BUFFER_SIZE = 4096;
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("Content-disposition", "attachment; filename=" + filename);
                OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int byteRead = -1;
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }
                inputStream.close();
                outputStream.close();
                file.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}