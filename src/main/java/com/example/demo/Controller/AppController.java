package com.example.demo.Controller;

import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Entity.UserDetail;
import com.example.demo.Service.AppService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;


@Controller
public class AppController {
    @Autowired
    private AppService appService;

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @RequestMapping("/")
    public String toLoginPage(){
        return "redirect:/login";
    }

    @RequestMapping(value = "/home")
    public String toHomePage(HttpSession session , Model model){
        if (!isUserLoggedIn(session)){
            session.setAttribute("not_logged_in_yet" , "not logged in yet");
            return "redirect:/login";
        }
        if (appService.isFirstLogin((String)session.getAttribute("login"))){
            session.setAttribute("first_login" , "first login");
            return "redirect:/change-password";
        }
        if (session.getAttribute("login") != null) {
            model.addAttribute("login_success", "login successful");
        }
        return "home";
    }

    @RequestMapping("/admin-home")
    public String adminHome(Model model , HttpSession session){
        if (!isUserLoggedIn(session)){
            session.setAttribute("not_logged_in_yet" , null);
            return "redirect:/login";
        }
        else{
            model.addAttribute("login_success", "login successful");
        }
        if (appService.isUser((String) session.getAttribute("login"))){
            return "redirect:/home";
        }
        List<UserDetail> userDetailList = appService.listUserDetail();
        model.addAttribute("userDetailList" , userDetailList);
        return "admin-home";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session , Model model){
        logger.info("Username: {} logged out" , session.getAttribute("login"));
        session.setAttribute("login" , null);
        session.setAttribute("logout" , "Logout success");
        return "redirect:/login";
    }
    @RequestMapping(value = "/edit/{id}" , method = RequestMethod.GET)
    public ModelAndView showEditUserPage(@PathVariable(name = "id") int id , HttpSession session) {
        ModelAndView mav = new ModelAndView("edit_user");
        UserDetail userDetail = appService.getUserDetailByID(id);
        Role role = appService.getRoleByID(id);
        User user = appService.getUserByID(id);
        mav.addObject("userDetail", userDetail);
        mav.addObject("role" , role);
        mav.addObject("user",user);
        return mav;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("userDetail") UserDetail userDetail ,  HttpSession session){
        if (!isUserLoggedIn(session)){
            return "redirect:/";
        }
        if (appService.isUser((String) session.getAttribute("login"))){
            return "redirect:/";
        }
        appService.saveUserDetail(userDetail);
        return "redirect:/admin-home";
    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") int id , HttpSession session) {
        if (!isUserLoggedIn(session)){
            return "redirect:/";
        }
        if (appService.isUser((String) session.getAttribute("login"))){
            return "redirect:/";
        }
        appService.delete(id);
        return "redirect:/admin-home";
    }

}
