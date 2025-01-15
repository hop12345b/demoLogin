package com.example.demo.Controller;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.Password;
import com.example.demo.Entity.User;
import com.example.demo.Service.AppService;
import com.example.demo.Service.OtpService;
import com.example.demo.Service.PasswordService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

@Controller
public class UserController {
    @Autowired
    private AppService appService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private OtpService otpService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }
    private boolean isUserIsUsing(HttpSession session){
        return session.getAttribute("username") != null;
    }

    @RequestMapping(value = "/login" , method = RequestMethod.GET)
    public String toLogin(Model model , HttpSession session , RedirectAttributes redirectAttributes){
        if (isUserLoggedIn(session)){
            return "redirect:/home";
        }
        if (session.getAttribute("logout") != null){
            session.invalidate();
        }
        return "login";
    }

    @RequestMapping(value = "/login" , method = RequestMethod.POST)
    public String login(@RequestParam String username , @RequestParam String password , HttpSession session , Model model , RedirectAttributes redirectAttributes){
        if (appService.getUser(username) == null){
            model.addAttribute("error_username" , "Your username does not exist");
            logger.warn("Failed login");
            return "login";
        }
        User user = appService.getUser(username);
        int uid = user.getId();
        List<Password> passwordList = passwordService.getPasswordList(uid);
        if (user.getStatus().equals("locked")){
            model.addAttribute("account_locked", "Account is locked. Please contact support.");
            logger.warn("Username: {} login failed(account locked)" , username);
        }
        else if (!passwordEncoder.matches(password , passwordList.getLast().getPassword())){
            user.setFAILED_ATTEMPTS(user.getFAILED_ATTEMPTS() + 1);
            logger.warn("Username: {} login failed (wrong password)" , username);
            appService.saveUser(user);
            if (user.getFAILED_ATTEMPTS() == MAX_FAILED_ATTEMPTS && !user.getStatus().equals("first_login")){
                user.setStatus("locked");
                appService.saveUser(user);
                logger.warn("Username: {} be locked (invalid password)" , username);
                model.addAttribute("account_locked", "Your account has been locked due to multiple failed attempts. Please contact support.");
            }
            model.addAttribute("error", "Invalid username or password");
        }
        else{
            user.setFAILED_ATTEMPTS(0);
            appService.saveUser(user);
            logger.info("Username: {} logged in" , username);
            session.setAttribute("login", username);
            redirectAttributes.addFlashAttribute("login_success" , "You has been logged in");
            if (appService.getUser(username).getRole().getRoleName().equals("user")) return "redirect:/home";
            else return "redirect:/admin-home/0";
        }
        return "login";
    }

    @RequestMapping("/")
    public String toLoginPage(){
        return "redirect:/login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session , RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("logout" , "You have been logged out.");
        logger.info("Username: {} logged out" , session.getAttribute("login"));
        session.setAttribute("logout" , "Logout success");
        return "redirect:/login";
    }

    @RequestMapping(value = "/home")
    public String toHomePage(HttpSession session , Model model , RedirectAttributes redirectAttributes){
        if (!isUserLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        String username =(String)session.getAttribute("login");
        if (appService.getUser(username).getStatus().equals("first_login")){
            session.setAttribute("first_login" , "first login");
            return "redirect:/change-password";
        }
        return "home";
    }

    @RequestMapping(value = "/register" , method = RequestMethod.GET)
    public String registerPage(){
        return "register";
    }

    @RequestMapping(value = "/register" , method = RequestMethod.POST)
    public String register(@RequestParam String username , @RequestParam String email , Model model , HttpSession session , RedirectAttributes redirectAttributes) throws MessagingException, UnsupportedEncodingException {
        if (appService.getUser(username) != null){
            model.addAttribute("error_username" , "Your username is existed.");
            return "register";
        }
        appService.saveUser(username);
        int uid = appService.getUser(username).getId();
        otpService.saveNewOtp(uid);

        Random random = new Random();
        int tmp = random.nextInt(100000000);
        String password = String.format("%6d" , tmp);
        passwordService.savePassword(uid , password);

        appService.saveRole(uid , username);

        String creationDate = dateFormat.format(Calendar.getInstance().getTime());
        appService.saveUserDetail(uid , username , email , creationDate , appService.getRole(uid));

        passwordService.sendPasswordEmail(appService.getUserDetail(uid), password);
        logger.warn("Username: {} , registered successful" , username);

        redirectAttributes.addFlashAttribute("register_success" , "You have registered successful. Your password has been sent to your email.");
        return "redirect:/login";
    }

    @RequestMapping(value = "/unlock-account" , method = RequestMethod.GET)
    public String toUnlockAccountPage(HttpSession session, Model model){
        if (!isUserIsUsing(session)){
            return "redirect:/otp";
        }
        model.addAttribute("otp_sent" , "Your OTP has been sent to your device");
        return "unlock-account";
    }

    @RequestMapping(value = "/unlock-account" , method = RequestMethod.POST)
    public String unlockAccount(@RequestParam String otp, HttpSession session , Model model , RedirectAttributes redirectAttributes){
        String username =(String)session.getAttribute("username");
        OTP userOTP = otpService.getOtp(appService.getUser(username).getId());
        if (!userOTP.getOtp().equals(otp)){
            model.addAttribute("wrong_otp" , "Your OTP is incorrect or expired");
            logger.warn("Username: {} wrong otp" , username);
            return "unlock-account";
        }
        if (Long.parseLong(userOTP.getOtpExpiry()) < System.currentTimeMillis()){
            model.addAttribute("wrong_otp" , "Your OTP is incorrect or expired");
            logger.warn("Username: {} otp expiry" , username);
            return "unlock-account";
        }
        User user = appService.getUser(username);
        user.setStatus("");
        appService.saveUser(user);

        userOTP.setOtp("");
        userOTP.setOtpExpiry("");
        otpService.saveOTP(userOTP);

        logger.info("Username: {} account unlocked" , username);
        redirectAttributes.addFlashAttribute("unlock_account_success" , "Your account has been unlocked.");
        return "redirect:/login";
    }

}
