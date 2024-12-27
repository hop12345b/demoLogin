package com.example.demo.Service;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AppService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordService passwordService;

    private final Logger logger = LoggerFactory.getLogger(AppService.class);
    private final int MAX_FAILED_ATTEMPTS = 5;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void saveUserDetail(UserDetail userDetail){
        userDetailRepository.save(userDetail);
    }

    public void delete(int id){
        Role role = roleRepository.findById(id).get();
        if (!role.getRoleName().equals("admin")){
            userRepository.deleteById(id);
        }
    }

    public boolean login(String username , String password , Model model){
        if (!isUsernameExist(username)){
            model.addAttribute("error_username" , "Your username is not exists");
            logger.warn("Failed login");
            return false;
        }
        User user = getUser(username);
        int uid = user.getId();
        if (user.getStatus().equals("locked")){
            model.addAttribute("account_locked", "Account is locked. Please contact support.");
            logger.warn("Username: {} login failed(account locked)" , username);
            return false;
        }
        List<Password> passwordList = passwordRepository.findByUid(uid);
        if (!passwordEncoder.matches(password , passwordList.get(passwordList.size() - 1).getPassword())){
            user.setFAILED_ATTEMPTS(user.getFAILED_ATTEMPTS() + 1);
            logger.warn("Username: {} login failed (wrong password)" , username);
            userRepository.save(user);
            if (user.getFAILED_ATTEMPTS() == MAX_FAILED_ATTEMPTS && !user.getStatus().equals("first_login")){
                lockAccount(username);
                logger.warn("Username: {} be locked (invalid password)" , username);
                model.addAttribute("account_locked", "Account is locked. Please contact support.");
                return false;
            }
            model.addAttribute("error", "Invalid username or password");
            return false;
        }
        user.setFAILED_ATTEMPTS(0);
        userRepository.save(user);
        logger.info("Username: {} logged in" , username);
        return true;
    }

    public boolean register(String username , String email , Model model) throws MessagingException, UnsupportedEncodingException {
        if (isUsernameExist(username)){
            model.addAttribute("error_username" , "Your username is exists");
            return false;
        }
        String creationDate = dateFormat.format(Calendar.getInstance().getTime());
        String password = passwordService.generatePassword();

        User user = new User();
        user.setUsername(username);
        user.setStatus("first_login");
        userRepository.save(user);

        int uid = user.getId();

        OTP otp = new OTP();
        otp.setUid(uid);
        otpRepository.save(otp);

        Password passwords = new Password();
        passwords.setUid(uid);
        passwords.setPassword(passwordEncoder.encode(password));
        passwordRepository.save(passwords);

        Role role = new Role();
        role.setUid(uid);
        role.setRoleName("user");
        if (username.equals("admin") || username.equals("hop1")) role.setRoleName("admin");
        roleRepository.save(role);

        UserDetail userDetail = new UserDetail();
        userDetail.setUid(uid);
        userDetail.setUsername(username);
        userDetail.setEmail(email);
        userDetail.setCreationDate(creationDate);
        userDetail.setRole(role.getRoleName());
        userDetailRepository.save(userDetail);

        passwordService.sendPasswordEmail(userDetail , password);
        logger.warn("Username: {} , registered successful" , username);
        return true;
    }

    public boolean changePassword(String username , String currentPassword , String newPassword , String confirmPassword , Model model){
        User user = getUser(username);
        int uid = user.getId();
        if (isUser(username)) {
            List<Password> passwordList = passwordRepository.findByUid(uid);
            if (!passwordEncoder.matches(currentPassword , passwordList.get(passwordList.size() - 1).getPassword())) {
                model.addAttribute("currentPassword_wrong", "Your password has been used before");
                logger.warn("Username: {} change password failed (wrong current password)", username);
                return false;
            }
            if (!passwordService.isPasswordAuthentication(uid, newPassword, confirmPassword, model)) {
                return false;
            }
        }
        Password passwords = new Password();
        passwords.setPassword(passwordEncoder.encode(newPassword));
        passwords.setUid(uid);
        passwords.setUpdateTime(dateFormat.format(Calendar.getInstance().getTime()));
        user.setStatus("");
        passwordRepository.save(passwords);
        userRepository.save(user);
        logger.info("Username: {} changed password" , username);
        model.addAttribute("success", "Password changed successfully");
        return true;
    }

    public boolean resetPassword(String username , String otp , String newPassword, String confirmPassword , Model model){
        User user = getUser(username);
        int uid = user.getId();
        OTP userOTP = otpRepository.findByUid(uid).get();
        if (!otpService.isOtpInvalid(username , otp , model)){
            logger.warn("Username: {} otp incorrect ", username);
            user.setFAILED_ATTEMPTS(user.getFAILED_ATTEMPTS() + 1);
            userRepository.save(user);
            if (user.getFAILED_ATTEMPTS() == MAX_FAILED_ATTEMPTS){
                lockAccount(username);
                logger.warn("Username: {} be locked (invalid OTP)" , username);
                model.addAttribute("account_locked", "Account is locked. Please contact support.");
                return false;
            }
            return false;
        }
        if (!passwordService.isPasswordAuthentication(uid , newPassword , confirmPassword , model)){
            return false;
        }
        Password passwords = new Password();
        passwords.setPassword(passwordEncoder.encode(newPassword));
        passwords.setUpdateTime(dateFormat.format(Calendar.getInstance().getTime()));
        userOTP.setOtp("");
        userOTP.setOtpExpiry("");
        otpRepository.save(userOTP);
        passwordRepository.save(passwords);
        userRepository.save(user);
        logger.info("Username: {} reset password", username);
        model.addAttribute("success", "Reset password successfully");
        return true;
    }

    public boolean forgetPassword(String username , Model model) throws MessagingException, UnsupportedEncodingException {
        if (!isUsernameExist(username)) {
            model.addAttribute("wrong_username" , "Your username is not exists");
            return false;
        }
        if (userRepository.findByUsername(username).get().getStatus().equals("locked")){
            model.addAttribute("username_locked" , "Your username has been locked.");
            return false;
        }
        otpService.generateOTP(userRepository.findByUsername(username).get().getId());
        return true;
    }

    public boolean unlockAccount(String username , String otp , Model model){
        OTP userOTP = otpRepository.findByUid(userRepository.findByUsername(username).get().getId()).get();
        if (!otpService.isOtpInvalid(username , otp , model)){
            return false;
        }
        unlock(username);
        otpService.clearOTP(userOTP);
        logger.info("Username: {} account unlocked" , username);
        model.addAttribute("success" , "Unlocked account");
        return true;
    }

    private void lockAccount(String username){
        User user = getUser(username);
        user.setStatus("locked");
        userRepository.save(user);
    }

    private void unlock(String username){
        User user = getUser(username);
        user.setStatus("");
        userRepository.save(user);
    }

    private boolean isUsernameExist(String username){
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isFirstLogin(String username){
        return userRepository.findByUsername(username).get().getStatus().equals("first_login");
    }

    public void addInfo(String username , String firstName , String lastName , String phoneNumber , Model model){
        UserDetail userDetail = userDetailRepository.findByUid(userRepository.findByUsername(username).get().getId()).get();
        userDetail.setFirstName(firstName);
        userDetail.setLastName(lastName);
        userDetail.setPhoneNumber(phoneNumber);
        userDetailRepository.save(userDetail);
        model.addAttribute("success" , "Add info successful");
        logger.info("Username: {} add information" , userDetail.getUid());
    }

    public boolean isUser(String username){
        if (roleRepository.findByUid(userRepository.findByUsername(username).get().getId()).get().getRoleName().equals("admin")){
            return false;
        }
        return true;
    }

    public List<UserDetail> listUserDetail(){
        return userDetailRepository.findAll();
    }

    public User getUser(String username){
        return userRepository.findByUsername(username).get();
    }

    public User getUserByID(int id){
        return userRepository.findById(id).get();
    }

    public UserDetail getUserDetailByID(int id){
        return userDetailRepository.findById(id).get();
    }

    public Role getRoleByID(int id){
        return roleRepository.findById(id).get();
    }

}
