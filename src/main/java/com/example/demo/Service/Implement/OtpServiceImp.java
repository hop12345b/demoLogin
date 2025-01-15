package com.example.demo.Service.Implement;

import com.example.demo.Entity.OTP;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@Transactional
@Slf4j
public class OtpServiceImp implements OtpService {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    public OTP getOtp(int uid){
        return otpRepository.findByUid(uid).orElseThrow();
    }

    public void saveNewOtp(int uid){
        OTP otp = new OTP();
        otp.setUid(uid);
        otpRepository.save(otp);
    }

    public void saveOTP(OTP otp){
        otpRepository.save(otp);
    }

    public void sendOTPEmail(String email , OTP otp) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("hop12345a@gmail.com" , "Admin");
        helper.setTo(email);

        String subject = "Your OTP";

        String content = "<p>Hello " + email + "</p>"
                + "<p>For security reason, you're required to use the following "
                + "Your OTP is:</p>"
                + "<p><b>" + otp.getOtp() + "</b></p>"
                + "<br>"
                + "<p>Note: this OTP is set to expire in 5 minutes.</p>";

        helper.setSubject(subject);
        helper.setText(content , true);

        mailSender.send(message);
    }
}
