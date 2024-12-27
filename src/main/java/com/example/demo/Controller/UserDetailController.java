package com.example.demo.Controller;

import com.example.demo.Service.AppService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserDetailController {
    @Autowired
    private AppService appService;

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }

    @RequestMapping(value = "/add-info" , method = RequestMethod.GET)
    public String toInfoPage(HttpSession session , Model model){
        if (!isUserLoggedIn(session)){
            session.setAttribute("not_logged_in_yet" , "not logged in yet");
            return "redirect:/login";
        }
        return "add-info";
    }

    @RequestMapping(value = "/add-info" , method = RequestMethod.POST)
    public String addInfo(@RequestParam String firstName , @RequestParam String lastName , @RequestParam String phoneNumber, HttpSession session , Model model){
        if (isUserLoggedIn(session)){
            String username = (String)session.getAttribute("login");
            appService.addInfo(username , firstName , lastName , phoneNumber , model);
            return "redirect:/home";
        }
        session.setAttribute("not_logged_in_yet" , "not logged in yet");
        return "redirect:/login";
    }
}
