package com.example.demo.Service;

import com.example.demo.Entity.OTP;
import com.example.demo.Entity.Password;
import com.example.demo.Entity.User;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.PasswordRepository;
import com.example.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public void save(User user){
        userRepository.save(user);
    }

    public void savePassword(Password password){
        passwordRepository.save(password);
    }

    public void delete(int id){
        User user = getUserByID(id);
        if (!user.getRole().equals("admin")){
            userRepository.deleteById(id);
        }
    }

    public boolean login(String username , String password , Model model , HttpSession session){
        if (!isUsernameExist(username)){
            model.addAttribute("error_username" , "Your username is not exists");
            logger.warn("Failed login");
            return false;
        }
        User user = getUser(username);
        if (user.isLocked()){
            model.addAttribute("account_locked", "Account is locked. Please contact support.");
            logger.warn("Username: {} login failed(account locked)" , username);
            return false;
        }
        if (!passwordRepository.findByUsername(username).get().getCurrentPassword().equals(password)){
            user.setFAILED_ATTEMPTS(user.getFAILED_ATTEMPTS() + 1);
            logger.warn("Username: {} login failed (wrong password)" , username);
            userRepository.save(user);
            if (user.getFAILED_ATTEMPTS() == 3){
                lockAccount(username);
                logger.warn("Username: {} be locked " , username);
                model.addAttribute("account_locked", "Account is locked. Please contact support.");
                return false;
            }
            model.addAttribute("error", "Invalid username or password");
            return false;
        }
        session.setAttribute("username" , username);
        user.setFAILED_ATTEMPTS(0);
        userRepository.save(user);
        logger.info("Username: {} logged in" , username);
        return true;
    }

    public boolean register(String username , String password , String confirmPassword , Model model){
        if (isUsernameExist(username)){
            model.addAttribute("error_username" , "Your username is exists");
            logger.warn("Registration failed");
            return false;
        }
        if (!password.equals(confirmPassword)){
            model.addAttribute("password_dif" , "Your new password and confirm password do not match");
            logger.warn("Registration failed");
            return false;
        }
        OTP otp = new OTP();
        otp.setUsername(username);
        Password passwords = new Password();
        passwords.setUsername(username);
        passwords.setCurrentPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setRole("user");
        user.setFirstLogin("true");
        user.setCreationDate();
//        user.setPasswords(passwords);
//        user.setOtp(otp);
        otpRepository.save(otp);
        passwordRepository.save(passwords);
        userRepository.save(user);
        return true;
    }

    public boolean changePassword(String username , String currentPassword , String newPassword , String confirmPassword , Model model){
        User user = getUser(username);
        if (isUser(username)) {
            if (!passwordRepository.findByUsername(username).get().getCurrentPassword().equals(currentPassword)) {
                model.addAttribute("currentPassword_wrong", "Your password has been used before");
                logger.warn("Username: {} change password failed (wrong current password)", username);
                return false;
            }
            if (!isPasswordMeetAuthentication(username, newPassword, confirmPassword, model)) {
                return false;
            }
        }
        Password passwords = passwordRepository.findByUsername(username).get();
        passwords.setPassword(newPassword);
        user.setFirstLogin("false");
        passwordRepository.save(passwords);
        userRepository.save(user);
        logger.info("Username: {} changed password" , username);
        model.addAttribute("success", "Password changed successfully");
        return true;
    }

    public boolean resetPassword(String username , String otp , String newPassword, String confirmPassword , Model model){
        User user = getUser(username);
        OTP userOTP = otpRepository.findByUsername(username).get();
        if (!isOtpCorrect(username , otp , model)){
            logger.warn("Username: {} otp incorrect ", username);
            return false;
        }
        if (!isPasswordMeetAuthentication(username , newPassword , confirmPassword , model)){
            return false;
        }
        else {
            Password passwords = passwordRepository.findByUsername(username).get();
            passwords.setPassword(newPassword);
            userOTP.setOtp("");
            userOTP.setOtpExpiry("");
            otpRepository.save(userOTP);
            passwordRepository.save(passwords);
            userRepository.save(user);
            logger.info("Username: {} reset password", username);
            model.addAttribute("success", "Reset password successfully");
            return true;
        }
    }

    private boolean isPasswordMeetAuthentication(String username , String newPassword , String confirmPassword , Model model){
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("password_dif" , "Your new password and confirm password do not match");
            logger.warn("Username: {} reset password failed (wrong confirm password)" , username);
            return false;
        }
        if (!passwordAuthenticate(newPassword)) {
            model.addAttribute("not_strong_password" , "Your password is not strong enough");
            logger.warn("Username: {} reset password failed (new password doesn't meet authentication)" , username);
            return false;
        }
        if (isOldPassword(username , newPassword)){
            model.addAttribute("password_used" , "Your password has been used before");
            logger.warn("Username: {} reset password failed (new password is the same as used password)" , username);
            return false;
        }
        return true;
    }

    private boolean isOtpCorrect(String username , String otp , Model model){
        OTP userOTP = otpRepository.findByUsername(username).get();
        if (!userOTP.getOtp().equals(otp)){
            model.addAttribute("otp_error" , "Your otp is wrong");
            logger.warn("Username: {} wrong otp" , username);
            return false;
        }
        if (Long.parseLong(userOTP.getOtpExpiry()) < System.currentTimeMillis()){
            model.addAttribute("otp_expiry" , "Your otp has expired");
            logger.warn("Username: {} otp expiry" , username);
        }
        return true;
    }

    public boolean forgetPassword(String username , Model model){
        if (!isUsernameExist(username)) {
            model.addAttribute("wrong_username" , "Your username is not exists");
            return false;
        }
        if (userRepository.findByUsername(username).get().isLocked()){
            model.addAttribute("username_locked" , "Your username has been locked.");
            return false;
        }
        generateOTP(username);
        return true;
    }

    private void generateOTP(String username){
        String tmp = UUID.randomUUID().toString();
        OTP otp = otpRepository.findByUsername(username).get();
        otp.setOtp(tmp);
        long otpExpiry = System.currentTimeMillis() + 300 * 1000;
        otp.setOtpExpiry(Long.toString(otpExpiry));
        otpRepository.save(otp);
        System.out.println("Your OTP is: " + tmp);
    }

    public boolean sendOTP(String username , Model model){
        if (!isUsernameExist(username)){
            model.addAttribute("error_username" , "Your username is not exists");
            return false;
        }
        User user = getUser(username);
        if (!user.isLocked()){
            model.addAttribute("wrong_user" , "Your account is not be locked");
            return false;
        }
        generateOTP(username);
        logger.info("Username: {} generate OTP" , username);
        return true;
    }

    public boolean unlockAccount(String username , String otp , Model model){
        OTP userOTP = otpRepository.findByUsername(username).get();
        if (!isOtpCorrect(username , otp , model)){
            return false;
        }
        unlock(username);
        clearOTP(userOTP);
        logger.info("Username: {} account unlocked" , username);
        model.addAttribute("success" , "Unlocked account");
        return true;
    }

    private boolean passwordAuthenticate(String password){
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*[0-9].*") && password.matches(".*[a-z].*") && password.matches(".*[!@#$%^&*()].*");
    }

    private void lockAccount(String username){
        User user = getUser(username);
        user.setLocked(true);
        userRepository.save(user);
    }

    private void unlock(String username){
        User user = getUser(username);
        user.setLocked(false);
        userRepository.save(user);
    }

    private void clearOTP(OTP userOTP){
        userOTP.setOtp("");
        userOTP.setOtpExpiry("");
        otpRepository.save(userOTP);
    }

    private boolean isUsernameExist(String username){
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isFirstLogin(String username){
        return userRepository.findByUsername(username).get().getFirstLogin().equals("true");
    }

    public void addInfo(HttpSession session , Model model , String email , String phoneNumber){
        User user = getUser((String)session.getAttribute("username"));
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
        model.addAttribute("success" , "Add info successful");
        logger.info("Username: {} registered" , user.getUsername());
    }

    public boolean isUser(String username){
        if (userRepository.findByUsername(username).get().getRole().equals("admin")){
            return false;
        }
        return true;
    }

    public List<User> listAll(){
        return userRepository.findAll();
    }

    public User getUser(String username){
        return userRepository.findByUsername(username).get();
    }

    public User getUserByID(int id){
        return userRepository.findById(id).get();
    }

    public Password getPasswordByID(int id){
        return passwordRepository.findById(id).get();
    }

    public OTP getOtpByID(int id){
        return otpRepository.findById(id).get();
    }

    private boolean isOldPassword(String username , String password){
        Password passwords = passwordRepository.findByUsername(username).get();
        if (password.equals(passwords.getOldPassword1())){
            return true;
        }
        if (password.equals(passwords.getOldPassword2())){
            return true;
        }
        if (password.equals(passwords.getOldPassword3())){
            return true;
        }
        return false;
    }
}
