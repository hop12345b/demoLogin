package com.example.demo.Controller;

import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Entity.UserDetail;
import com.example.demo.Service.AppService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class AdminController {
    @Autowired
    private AppService appService;

    private boolean isUserNotLoggedIn(HttpSession session){
        return session.getAttribute("login") == null;
    }

    @RequestMapping(value = "/admin-home/{page}")
    public String adminHome(Model model , RedirectAttributes redirectAttributes, HttpSession session , @PathVariable(name = "page") int page){
        if (isUserNotLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        String username = (String)session.getAttribute("login");
        if (appService.getUser(username).getRole().getRoleName().equals("user")){
            return "redirect:/home";
        }
        Pageable pageable = PageRequest.of(page , 5);
        List<UserDetail> userDetailListByPage = appService.userDetailListByPage(pageable);
        List<UserDetail> userDetailList = appService.userDetailList();

        model.addAttribute("userDetailListByPage" , userDetailListByPage);
        model.addAttribute("userDetailList" , userDetailList);
        return "admin-home";
    }

    @RequestMapping("admin-home")
    public String adminHome(){
        return "redirect:/admin-home/0";
    }

    @RequestMapping(value = "/edit/{id}")
    public String showEditUserPage(@PathVariable(name = "id") int id , HttpSession session , Model model , RedirectAttributes redirectAttributes){
        String username = (String)session.getAttribute("login");
        if (isUserNotLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        if (appService.getUser(username).getRole().getRoleName().equals("user")){
            return "redirect:/";
        }
        UserDetail userDetail = appService.getUserDetail(id);
        Role role = appService.getRole(id);
        User user = appService.getUser(username);

        model.addAttribute("userDetail", userDetail);
        model.addAttribute("role" , role);
        model.addAttribute("user",user);
        return "edit-user";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("userDetail") UserDetail userDetail ,  HttpSession session , RedirectAttributes redirectAttributes){
        if (isUserNotLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        String username = (String)session.getAttribute("login");
        if (appService.getUser(username).getRole().getRoleName().equals("user")){
            return "redirect:/";
        }
        appService.saveUserDetail(userDetail);
        return "redirect:/admin-home";
    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") int id , HttpSession session , RedirectAttributes redirectAttributes) {
        if (isUserNotLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        String username = (String)session.getAttribute("login");
        if (appService.getUser(username).getRole().getRoleName().equals("user")){
            return "redirect:/";
        }
        appService.delete(id);
        return "redirect:/admin-home";
    }

}
