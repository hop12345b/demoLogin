package com.example.demo.Controller;

import com.example.demo.Entity.UserDetail;
import com.example.demo.Service.AppService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserDetailController {
    @Autowired
    private AppService appService;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailController.class);

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") == null;
    }

    @RequestMapping(value = "/add-info" , method = RequestMethod.GET)
    public String toInfoPage(HttpSession session , Model model , RedirectAttributes redirectAttributes){
        if (isUserLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        String username = session.getAttribute("login").toString();
        UserDetail userDetail = appService.getUserDetail(appService.getUser(username).getId());
        model.addAttribute("userDetail", userDetail);
        return "add-info";
    }

    @RequestMapping(value = "/add-info" , method = RequestMethod.POST)
    public String addInfo(@RequestParam String firstName , @RequestParam String lastName , @RequestParam String phoneNumber, HttpSession session , Model model){
        String username = (String)session.getAttribute("login");
        UserDetail userDetail = appService.getUserDetail(appService.getUser(username).getId());
        userDetail.setFirstName(firstName);
        userDetail.setLastName(lastName);
        userDetail.setPhoneNumber(phoneNumber);

        appService.saveUserDetail(userDetail);
        model.addAttribute("success" , "Add info successful");
        logger.info("Username: {} add information" , username);

        return "redirect:/home";
    }
}
