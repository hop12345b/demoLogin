package com.example.demo.Service.Implement;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.Service.AppService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AppServiceImp implements AppService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void saveUserDetail(UserDetail userDetail){
        userDetailRepository.save(userDetail);
        Role role = roleRepository.findByUid(userDetail.getUid()).orElseThrow();
        role.setRoleName(userDetail.getRole());
        roleRepository.save(role);
    }

    @Override
    public void delete(int id){
        Role role = roleRepository.findById(id).orElseThrow();
        if (!role.getRoleName().equals("admin")){
            userRepository.deleteById(id);
        }
    }

    @Override
    public void saveUser(User user){
        userRepository.save(user);
    }

    @Override
    public void saveUser(String username){
        User user = new User();
        user.setUsername(username);
        user.setStatus("first_login");
        userRepository.save(user);
    }

    @Override
    public void saveUserDetail(int uid , String username , String email , String creationDate , Role role){
        UserDetail userDetail = new UserDetail();
        userDetail.setUid(uid);
        userDetail.setUsername(username);
        userDetail.setEmail(email);
        userDetail.setCreationDate(creationDate);
        userDetail.setRole(role.getRoleName());
        userDetailRepository.save(userDetail);
    }

    @Override
    public void saveRole(int uid , String username){
        Role role = new Role();
        role.setUid(uid);
        role.setRoleName("user");
        if (username.equals("admin") || username.equals("hop1")) role.setRoleName("admin");
        roleRepository.save(role);
    }

    @Override
    public List<UserDetail> userDetailListByPage(Pageable pageable){
        return userDetailRepository.findAll(pageable).toList();
    }

    @Override
    public List<UserDetail> userDetailList(){
        return userDetailRepository.findAll();
    }

    @Override
    public User getUser(String username){
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public UserDetail getUserDetail(int id){
        return userDetailRepository.findById(id).orElse(null);
    }

    @Override
    public Role getRole(int id){
        return roleRepository.findById(id).orElse(null);
    }

}
