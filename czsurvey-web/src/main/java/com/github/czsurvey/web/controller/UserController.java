package com.github.czsurvey.web.controller;

import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.entity.User;
import com.github.czsurvey.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {
        return userRepository.findByPhoneOrEmail(username).orElse(null);
    }

    @GetMapping("/userinfo")
    public User getUserinfo() {
        Long userId = LoginUser.me().getId();
        return userRepository.findById(userId).orElse(null);
    }
}
