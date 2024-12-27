package com.example.demo.Service;

import com.example.demo.Entity.Password;
import com.example.demo.Entity.UserDetail;
import com.example.demo.Repository.PasswordRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@Slf4j
public class PasswordService {
    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    public boolean isPasswordAuthentication(int uid , String newPassword , String confirmPassword , Model model){
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("password_dif" , "Your new password and confirm password do not match");
            logger.warn("Uid: {} reset password failed (wrong confirm password)" , uid);
            return false;
        }
        if (!passwordAuthentication(newPassword)) {
            model.addAttribute("not_strong_password" , "Your password is not strong enough");
            logger.warn("Uid: {} reset password failed (new password doesn't meet authentication)" , uid);
            return false;
        }
        if (isOldPassword(uid , newPassword)){
            model.addAttribute("password_used" , "Your password has been used before");
            logger.warn("Uid: {} reset password failed (new password is the same as used password)" , uid);
            return false;
        }
        return true;
    }

    private boolean passwordAuthentication(String password){
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[0-9].*") && password.matches(".*[a-z].*") && password.matches(".*[!@#$%^&*<>].*");
    }

    public String generatePassword(){
        Random random = new Random();
        int tmp = random.nextInt(100000000);
        return String.format("%6d" , tmp);
    }

    private boolean isOldPassword(int uid , String password){
        List<Password> passwordList = passwordRepository.findByUid(uid);
        int size = passwordList.size() - 1;
        int n = size;
        while (n >= 0){
            if (passwordEncoder.matches(password , passwordList.get(n).getPassword())){
                return true;
            }
            n--;
            if (n == size - 3) break;
        }
        return false;
    }

    public void sendPasswordEmail(UserDetail userDetail , String password) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("hop12345a@gmail.com" , "Admin");
        helper.setTo(userDetail.getEmail());

        String subject = "Your password";

        String content = "<p>You have been registered successful </p>"
                + "<p>Your password is:</p>"
                + "<p><b>" + password + "</b></p>";

        helper.setSubject(subject);
        helper.setText(content , true);

        mailSender.send(message);
    }
}
