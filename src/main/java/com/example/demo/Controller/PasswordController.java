package com.example.demo.Controller;

import com.example.demo.Service.AppService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;

@Controller
public class PasswordController {
    @Autowired
    private AppService appService;

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }

    private boolean isUserIsUsing(HttpSession session){
        return session.getAttribute("username") != null;
    }

    @RequestMapping(value = "/change-password" , method = RequestMethod.GET)
    public String toChangePasswordPage(HttpSession session , Model model){
        if (!isUserLoggedIn(session)){
            session.setAttribute("not_logged_in_yet" , "not logged in yet");
            return "redirect:/login";
        }
        if (session.getAttribute("first_login") != null){
            model.addAttribute("first_change_password" , "You have to change password in first login");
            session.setAttribute("first_login" , null);
        }
        return "change-password";
    }

    @RequestMapping(value = "/change-password" , method = RequestMethod.POST)
    public String changePassword(@RequestParam String currentPassword , @RequestParam String newPassword , @RequestParam String confirmPassword , HttpSession session , Model model){
        if (appService.changePassword((String)session.getAttribute("login") , currentPassword , newPassword , confirmPassword , model)){
            session.setAttribute("change_password" , "change password success");
            session.setAttribute("login" , null);
            return "redirect:/login";
        }
        return "change-password";
    }

    @RequestMapping(value = "/forget-password" , method = RequestMethod.GET)
    public String toForgetPasswordPage(){
        return "forget-password";
    }

    @RequestMapping(value = "/forget-password" , method = RequestMethod.POST)
    public String forgetPassword(@RequestParam String username , HttpSession session , Model model) throws MessagingException, UnsupportedEncodingException {
        if (!appService.forgetPassword(username , model)){
            return "forget-password";
        }
        session.setAttribute("username" , username);
        return "redirect:/reset-password";
    }

    @RequestMapping(value = "/reset-password" , method = RequestMethod.GET)
    public String toResetPasswordPage(HttpSession session){
        if (!isUserIsUsing(session)){
            return "redirect:/forget-password";
        }
        return "reset-password";
    }

    @RequestMapping(value = "/reset-password" , method = RequestMethod.POST)
    public String resetPassword(@RequestParam String otp , @RequestParam String newPassword, @RequestParam String confirmPassword , HttpSession session , Model model){
        if(!appService.resetPassword((String)session.getAttribute("username") , otp , newPassword , confirmPassword , model)) return "/reset-password";
        session.setAttribute("reset_password" , "reset password success");
        return "redirect:/login";
    }

}
