package com.example.demo.Controller;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.Password;
import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
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
    private UserService service;

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("username") != null;
    }

    private boolean isUserIsUsing(HttpSession session){
        return session.getAttribute("username") != null;
    }

    private boolean isRegistration = false;
    private boolean isChangePassword = false;
    private boolean isUnlockAccount = false;
    private boolean isLogout = false;
    private boolean isLogin = false;
    private boolean isResetPassword = false;
    private boolean isFirstLogin = false;
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);


    @RequestMapping("/")
    public String toLoginPage(){
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String toLogin(Model model , HttpSession session){
        if (isUserLoggedIn(session)){
            return "redirect:/home";
        }
        if (isRegistration && isChangePassword){
            isRegistration = false;
            model.addAttribute("registered_success" , "Registered successful");
        }
        else if (isChangePassword){
            isChangePassword = false;
            model.addAttribute("change_password_success" , "Change password successful");
        }
        else if (isUnlockAccount){
            isUnlockAccount = false;
            model.addAttribute("unlock_account_success" , "Your account has been unlocked");
        }
        else if (isLogout){
            isLogout = false;
            model.addAttribute("logout" , "logout successful");
        }
        else if (isResetPassword){
            isResetPassword = false;
            model.addAttribute("reset_password_success" , "Your password has been reset");
        }
        session.invalidate();
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username , @RequestParam String password , HttpSession session , Model model){
        if (service.login(username , password , model , session)){
            session.setAttribute("username" , username);
            isLogin = true;
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/home")
    public String toHomePage(HttpSession session , Model model){
        if (!isUserLoggedIn(session) || isRegistration){
            session.invalidate();
            isRegistration = false;
            return "redirect:/login";
        }
        if (service.isFirstLogin((String)session.getAttribute("username"))){
            isFirstLogin = true;
            return "redirect:/change-password";
        }
        if (isLogin) {
            model.addAttribute("login_success", "login successful");
            isLogin = false;
        }
        if (!service.isUser((String)session.getAttribute("username"))){
            return "redirect:/admin-home";
        }
        return "home";
    }

    @PostMapping("/home")
    public String home(Model model){
        return "home";
    }

    @RequestMapping("/admin-home")
    public String adminHome(Model model , HttpSession session){
        if (!isUserIsUsing(session)){
            return "redirect:/login";
        }
        if (service.isUser((String) session.getAttribute("username"))){
            return "redirect:/login";
        }
        List<User> userList = service.listAll();
        model.addAttribute("userList" , userList);
        return "admin-home";
    }

    @RequestMapping(value = "/edit/{id}" , method = RequestMethod.GET)
    public ModelAndView showEditUserPage(@PathVariable(name = "id") int id , HttpSession session) {
        ModelAndView mav = new ModelAndView("edit_user");
        User user = service.getUserByID(id);
        OTP otp = service.getOtpByID(id);
        Password password = service.getPasswordByID(id);
        mav.addObject("user", user);
        return mav;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("user") User user ,  HttpSession session){
        if (!isUserIsUsing(session)){
            return "redirect:/";
        }
        if (service.isUser((String) session.getAttribute("username"))){
            return "redirect:/";
        }
        service.save(user);
        return "redirect:/admin-home";
    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") int id , HttpSession session) {
        if (!isUserIsUsing(session)){
            return "redirect:/";
        }
        if (service.isUser((String) session.getAttribute("username"))){
            return "redirect:/";
        }
        service.delete(id);
        return "redirect:/admin-home";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session){
        isLogout = true;
        logger.info("Username: {} logged out" , session.getAttribute("username"));
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username , @RequestParam String password , @RequestParam String confirmPassword , Model model , HttpSession session){
        if (!service.register(username , password , confirmPassword , model)) return "register";
        isRegistration = true;
        session.setAttribute("username" , username);
        return "redirect:/add-info";
    }

    @GetMapping("/add-info")
    public String toInfoPage(HttpSession session , Model model){
        if (!isUserIsUsing(session)){
            return "redirect:/home";
        }
        return "add-info";
    }

    @PostMapping("/add-info")
    public String addInfo(@RequestParam String email , @RequestParam String phoneNumber, HttpSession session , Model model){
        service.addInfo(session , model , email , phoneNumber);
        if (isUserIsUsing(session)){
            return "redirect:/home";
        }
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/change-password")
    public String toChangePasswordPage(HttpSession session , Model model){
        if (!isUserLoggedIn(session)){
            return "redirect:/login";
        }
        if (isFirstLogin){
            model.addAttribute("first_change_password" , "You have to change password in first login");
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword , @RequestParam String newPassword ,@RequestParam String confirmPassword , HttpSession session , Model model){
        if (service.changePassword((String)session.getAttribute("username") , currentPassword , newPassword , confirmPassword , model)){
            session.invalidate();
            if (isFirstLogin){
                isFirstLogin = false;
            }
            isChangePassword = true;
            return "redirect:/login";
        }
        return "change-password";
    }

    @GetMapping("/forget-password")
    public String toForgetPasswordPage(){
        return "forget-password";
    }

    @PostMapping("/forget-password")
    public String forgetPassword(@RequestParam String username , HttpSession session , Model model){
        if (!service.forgetPassword(username , model)){
            return "forget-password";
        }
        session.setAttribute("username" , username);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String toResetPasswordPage(HttpSession session){
        if (!isUserIsUsing(session)){
            return "redirect:/forget-password";
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String otp , @RequestParam String newPassword, @RequestParam String confirmPassword , HttpSession session , Model model){
        if(!service.resetPassword((String)session.getAttribute("username") , otp , newPassword , confirmPassword , model)) return "/reset-password";
        session.invalidate();
        isResetPassword = true;
        return "redirect:/login";
    }

    @GetMapping("/otp")
    public String toOtpPage(){
        return "otp";
    }

    @PostMapping("/otp")
    public String sendOtp(@RequestParam String username , HttpSession session , Model model){
        if (!service.sendOTP(username , model)) return "otp";
        session.setAttribute("username" , username);
        return "redirect:/unlock-account";
    }

    @GetMapping("/unlock-account")
    public String toUnlockAccountPage(HttpSession session, Model model){
        if (!isUserIsUsing(session)){
            return "redirect:/otp";
        }
        model.addAttribute("otp_sent" , "Your OTP has been send to your device");
        return "unlock-account";
    }

    @PostMapping("/unlock-account")
    public String unlockAccount(@RequestParam String otp, HttpSession session , Model model){
        if(!service.unlockAccount((String) session.getAttribute("username"), otp , model)){
            return "unlock-account";
        }
        session.invalidate();
        isUnlockAccount = true;
        return "redirect:/login";
    }
}
