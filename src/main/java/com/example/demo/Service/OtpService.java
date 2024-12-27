package com.example.demo.Service;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.User;
import com.example.demo.Entity.UserDetail;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserDetailRepository;
import com.example.demo.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class OtpService {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final Logger logger = LoggerFactory.getLogger(OtpService.class);

    public void generateOTP(int uid) throws MessagingException, UnsupportedEncodingException {
        String tmp = UUID.randomUUID().toString();
        UserDetail userDetail = userDetailRepository.findByUid(uid).get();
        OTP otp = otpRepository.findByUid(uid).get();
        otp.setOtp(tmp);
        long otpExpiry = System.currentTimeMillis() + 300 * 1000;
        otp.setOtpExpiry(Long.toString(otpExpiry));
        otpRepository.save(otp);
        sendOTPEmail(userDetail , otp);
    }

    public boolean sendOTP(String username , Model model) throws MessagingException, UnsupportedEncodingException {
        if (userRepository.findByUsername(username).isEmpty()){
            model.addAttribute("error_username" , "Your username is not exists");
            return false;
        }
        User user = userRepository.findByUsername(username).get();
        if (!user.getStatus().equals("locked")){
            model.addAttribute("wrong_user" , "Your account is not be locked");
            return false;
        }
        generateOTP(userRepository.findByUsername(username).get().getId());
        logger.info("Username: {} generate OTP" , username);
        return true;
    }

    public void clearOTP(OTP userOTP){
        userOTP.setOtp("");
        userOTP.setOtpExpiry("");
        otpRepository.save(userOTP);
    }

    private void sendOTPEmail(UserDetail userDetail , OTP otp) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("hop12345a@gmail.com" , "Admin");
        helper.setTo(userDetail.getEmail());

        String subject = "Your OTP";

        String content = "<p>Hello " + userDetail.getLastName() + "</p>"
                + "<p>For security reason, you're required to use the following "
                + "Your OTP is:</p>"
                + "<p><b>" + otp.getOtp() + "</b></p>"
                + "<br>"
                + "<p>Note: this OTP is set to expire in 5 minutes.</p>";

        helper.setSubject(subject);
        helper.setText(content , true);

        mailSender.send(message);
    }

    public boolean isOtpInvalid(String username , String otp , Model model){
        int uid = userRepository.findByUsername(username).get().getId();
        OTP userOTP = otpRepository.findByUid(uid).get();
        if (!userOTP.getOtp().equals(otp)){
            model.addAttribute("otp_error" , "Your otp is wrong");
            logger.warn("Username: {} wrong otp" , username);
            return false;
        }
        if (Long.parseLong(userOTP.getOtpExpiry()) < System.currentTimeMillis()){
            model.addAttribute("otp_expiry" , "Your otp has expired");
            logger.warn("Username: {} otp expiry" , username);
            return false;
        }
        return true;
    }
}
