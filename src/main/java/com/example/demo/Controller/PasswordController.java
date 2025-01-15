package com.example.demo.Controller;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.Password;
import com.example.demo.Entity.User;
import com.example.demo.Entity.UserDetail;
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
import java.util.UUID;

@Controller
public class PasswordController {
    @Autowired
    private AppService appService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean isUserLoggedIn(HttpSession session){
        return session.getAttribute("login") != null;
    }

    private boolean isUserIsUsing(HttpSession session){
        return session.getAttribute("username") != null;
    }

    @RequestMapping(value = "/change-password" , method = RequestMethod.GET)
    public String toChangePasswordPage(HttpSession session , Model model , RedirectAttributes redirectAttributes){
        if (!isUserLoggedIn(session)){
            redirectAttributes.addFlashAttribute("not_logged_in_yet" , "You have to login first.");
            return "redirect:/login";
        }
        if (session.getAttribute("first_login") != null){
            model.addAttribute("first_change_password" , "You have to change password in first login.");
            session.setAttribute("first_login" , null);
        }
        String username =(String)session.getAttribute("login");
        UserDetail userDetail = appService.getUserDetail(appService.getUser(username).getId());
        model.addAttribute("userDetail" , userDetail);
        return "change-password";
    }

    @RequestMapping(value = "/change-password" , method = RequestMethod.POST)
    public String changePassword(@RequestParam String currentPassword , @RequestParam String newPassword , @RequestParam String confirmPassword , HttpSession session , Model model , RedirectAttributes redirectAttributes){
        String username =(String)session.getAttribute("login");
        User user = appService.getUser(username);
        int uid = user.getId();
        if (appService.getUser(username).getRole().getRoleName().equals("user")) {
            List<Password> passwordList = passwordService.getPasswordList(uid);
            if (!passwordEncoder.matches(currentPassword , passwordList.getLast().getPassword())) {
                model.addAttribute("currentPassword_wrong", "Your current password is incorrect");
                logger.warn("Username: {} change password failed (wrong current password)", username);
                return "change-password";
            }
            else if (!newPassword.equals(confirmPassword)){
                model.addAttribute("password_dif" , "Your new password and confirm password do not match");
                logger.warn("Uid: {} reset password failed (wrong confirm password.)" , uid);
                return "change-password";
            }
            else if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[0-9].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*[!@#$%^&*<>].*")) {
                model.addAttribute("not_strong_password" , "Your new password is not strong enough");
                logger.warn("Uid: {} reset password failed (new password doesn't meet authentication.)" , uid);
                return "change-password";
            }
            else if (passwordService.checkOldPassword(uid , newPassword)){
                model.addAttribute("password_used" , "Your new password has used before");
                logger.warn("Uid: {} reset password failed (new password is the same as used password.)" , uid);
                return "change-password";
            }
        }
        Password passwords = new Password();
        passwords.setPassword(passwordEncoder.encode(newPassword));
        passwords.setUid(uid);
        passwords.setUpdateTime(dateFormat.format(Calendar.getInstance().getTime()));
        user.setStatus("");

        passwordService.savePassword(passwords);
        appService.saveUser(user);

        logger.info("Username: {} changed password" , username);
        model.addAttribute("success", "Your password has been changed successfully");
        redirectAttributes.addFlashAttribute("change_password_success", "Your password has been changed successfully");
        session.setAttribute("login" , null);
        return "redirect:/login";
    }

    @RequestMapping(value = "/forget-password" , method = RequestMethod.GET)
    public String toForgetPasswordPage(){
        return "forget-password";
    }

    @RequestMapping(value = "/forget-password" , method = RequestMethod.POST)
    public String forgetPassword(@RequestParam String username , HttpSession session , Model model) throws MessagingException, UnsupportedEncodingException {
        if (appService.getUser(username) == null) {
            model.addAttribute("wrong_username" , "Your username is not exists");
            return "forget-password";
        }
        if (appService.getUser(username).getStatus().equals("locked")){
            model.addAttribute("username_locked" , "Your account has been locked. Please contact support");
            return "forget-password";
        }

        int id = appService.getUser(username).getId();
        String tmp = UUID.randomUUID().toString();
        String email = appService.getUserDetail(id).getEmail();
        OTP otp = otpService.getOtp(id);
        otp.setOtp(tmp);
        long otpExpiry = System.currentTimeMillis() + 300 * 1000;
        otp.setOtpExpiry(Long.toString(otpExpiry));
        otpService.saveOTP(otp);
        otpService.sendOTPEmail(email , otp);

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
    public String resetPassword(@RequestParam String otp , @RequestParam String newPassword, @RequestParam String confirmPassword , HttpSession session , Model model , RedirectAttributes redirectAttributes) {
        String username =(String)session.getAttribute("username");
        User user = appService.getUser(username);
        int uid = user.getId();
        OTP userOTP = otpService.getOtp(uid);
        if (!userOTP.getOtp().equals(otp)){
            model.addAttribute("otp_error" , "Your OTP is incorrect");
            logger.warn("Username: {} otp incorrect" , username);
            user.setFAILED_ATTEMPTS(user.getFAILED_ATTEMPTS() + 1);
            appService.saveUser(user);
            if (user.getFAILED_ATTEMPTS() == MAX_FAILED_ATTEMPTS){
                user.setStatus("locked");
                appService.saveUser(user);
                logger.warn("Username: {} be locked (invalid OTP)" , username);
                model.addAttribute("account_locked", "Your account has been locked due to multiple failed attempts.");
            }
            return "redirect:/reset-password";
        }
        if (Long.parseLong(userOTP.getOtpExpiry()) < System.currentTimeMillis()){
            model.addAttribute("otp_expiry" , "Your OTP was expired");
            logger.warn("Username: {} otp expiry" , username);
            return "redirect:/reset-password";
        }
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("password_dif" , "Your new password and confirm password do not match");
            logger.warn("Uid: {} reset password failed (wrong confirm password)" , uid);
            return "redirect:/reset-password";
        }
        if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[0-9].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*[!@#$%^&*<>].*")) {
            model.addAttribute("not_strong_password" , "Your new password is not strong enough");
            logger.warn("Uid: {} reset password failed (new password doesn't meet authentication)" , uid);
            return "redirect:/reset-password";
        }
        if (passwordService.checkOldPassword(uid , newPassword)){
            model.addAttribute("password_used" , "Your new password has been used before");
            logger.warn("Uid: {} reset password failed (new password is the same as used password)" , uid);
            return "redirect:/reset-password";
        }

        Password passwords = new Password();
        passwords.setPassword(passwordEncoder.encode(newPassword));
        passwords.setUpdateTime(dateFormat.format(Calendar.getInstance().getTime()));

        userOTP.setOtp("");
        userOTP.setOtpExpiry("");

        otpService.saveOTP(userOTP);
        passwordService.savePassword(passwords);
        appService.saveUser(user);

        logger.info("Username: {} reset password", username);
        model.addAttribute("success", "Your password has been reset");
        redirectAttributes.addFlashAttribute("reset_password_success", "Your password has been reset");
        return "redirect:/login";
    }

}
