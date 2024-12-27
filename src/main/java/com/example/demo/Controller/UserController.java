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
public class UserController {
    @Autowired
    private AppService appService;

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }
    private boolean isUserIsUsing(HttpSession session){
        return session.getAttribute("username") != null;
    }

    @RequestMapping(value = "/login" , method = RequestMethod.GET)
    public String toLogin(Model model , HttpSession session){
        if (isUserLoggedIn(session)){
            return "redirect:/home";
        }
        if (session.getAttribute("register") != null){
            model.addAttribute("registered_success" , "Registered successful");
            session.setAttribute("register" , null);
        }
        else if (session.getAttribute("change_password") != null){
            model.addAttribute("change_password_success" , "Change password successful");
            session.setAttribute("change_password" , null);
        }
        else if (session.getAttribute("unlock_account") != null){
            model.addAttribute("unlock_account_success" , "Your account has been unlocked");
            session.setAttribute("unlock_account" , null);
        }
        else if (session.getAttribute("logout") != null){
            model.addAttribute("logout" , "logout successful");
            session.invalidate();
        }
        else if (session.getAttribute("reset_password") != null){
            model.addAttribute("reset_password_success" , "Your password has been reset");
            session.setAttribute("reset_password" , null);
        }
        else if (session.getAttribute("not_logged_in_yet") != null){
            model.addAttribute("not_logged_in_yet" , "not logged in yet");
            session.setAttribute("not_logged_in_yet" , null);
        }
        return "login";
    }

    @RequestMapping(value = "/login" , method = RequestMethod.POST)
    public String login(@RequestParam String username , @RequestParam String password , HttpSession session , Model model){
        if (appService.login(username , password , model)){
            if (appService.isUser(username)) {
                session.setAttribute("login", username);
                return "redirect:/home";
            }
            else {
                session.setAttribute("login" , username);
                return "redirect:/admin-home";
            }
        }
        return "login";
    }

    @RequestMapping(value = "/register" , method = RequestMethod.GET)
    public String registerPage(){
        return "register";
    }

    @RequestMapping(value = "/register" , method = RequestMethod.POST)
    public String register(@RequestParam String username , @RequestParam String email , Model model , HttpSession session) throws MessagingException, UnsupportedEncodingException {
        if (!appService.register(username , email , model)) return "register";
        session.setAttribute("register" , "register success");
        return "redirect:/login";
    }

    @RequestMapping(value = "/unlock-account" , method = RequestMethod.GET)
    public String toUnlockAccountPage(HttpSession session, Model model){
        if (!isUserIsUsing(session)){
            return "redirect:/otp";
        }
        model.addAttribute("otp_sent" , "Your OTP has been send to your device");
        return "unlock-account";
    }

    @RequestMapping(value = "/unlock-account" , method = RequestMethod.POST)
    public String unlockAccount(@RequestParam String otp, HttpSession session , Model model){
        if(!appService.unlockAccount((String) session.getAttribute("username"), otp , model)){
            return "unlock-account";
        }
        session.setAttribute("unlock_account" , "unlock account success");
        return "redirect:/login";
    }

}
