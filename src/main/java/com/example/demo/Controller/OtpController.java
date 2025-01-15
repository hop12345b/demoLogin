package com.example.demo.Controller;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.User;
import com.example.demo.Service.AppService;
import com.example.demo.Service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Controller
public class OtpController {
    @Autowired
    private OtpService otpService;

    @Autowired
    private AppService appService;

    private final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @RequestMapping(value = "/otp" , method = RequestMethod.GET)
    public String toOtpPage(){
        return "otp";
    }

    @RequestMapping(value = "/otp" , method = RequestMethod.POST)
    public String sendOtp(@RequestParam String username , HttpSession session , Model model) throws MessagingException, UnsupportedEncodingException {
        if (appService.getUser(username) == null){
            model.addAttribute("error_username" , "Your username is not exists");
            return "otp";
        }
        User user = appService.getUser(username);
        if (!user.getStatus().equals("locked")){
            model.addAttribute("wrong_user" , "Your account is not be locked");
            return "otp";
        }

        String tmp = UUID.randomUUID().toString();
        String email = appService.getUserDetail(user.getId()).getEmail();
        OTP otp = otpService.getOtp(user.getId());
        otp.setOtp(tmp);
        long otpExpiry = System.currentTimeMillis() + 300 * 1000;
        otp.setOtpExpiry(Long.toString(otpExpiry));
        otpService.saveOTP(otp);
        otpService.sendOTPEmail(email , otp);

        logger.info("Username: {} generate OTP" , username);
        session.setAttribute("username" , username);
        return "redirect:/unlock-account";
    }
}
