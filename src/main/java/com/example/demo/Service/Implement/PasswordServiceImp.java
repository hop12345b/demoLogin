package com.example.demo.Service.Implement;

import com.example.demo.Entity.Password;
import com.example.demo.Entity.UserDetail;
import com.example.demo.Repository.PasswordRepository;
import com.example.demo.Service.PasswordService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@Transactional
@Slf4j
public class PasswordServiceImp implements PasswordService {
    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Password> getPasswordList(int uid){
        return passwordRepository.findByUid(uid);
    }

    public void savePassword(Password password){
        passwordRepository.save(password);
    }

    public boolean checkOldPassword(int uid , String password){
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

    public void savePassword(int uid , String password){
        Password passwords = new Password();
        passwords.setUid(uid);
        passwords.setPassword(passwordEncoder.encode(password));
        passwordRepository.save(passwords);
    }
}
