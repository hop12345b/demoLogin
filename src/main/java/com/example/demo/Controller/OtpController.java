package com.example.demo.Controller;

import com.example.demo.Service.AppService;
import com.example.demo.Service.OtpService;
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
public class OtpController {
    @Autowired
    private OtpService otpService;

    @RequestMapping(value = "/otp" , method = RequestMethod.GET)
    public String toOtpPage(){
        return "otp";
    }

    @RequestMapping(value = "/otp" , method = RequestMethod.POST)
    public String sendOtp(@RequestParam String username , HttpSession session , Model model) throws MessagingException, UnsupportedEncodingException {
        if (!otpService.sendOTP(username , model)) return "otp";
        session.setAttribute("username" , username);
        return "redirect:/unlock-account";
    }
}
