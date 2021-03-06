package com.mrakks.onlinegalerija.controller;

import com.mrakks.onlinegalerija.model.User;
import com.mrakks.onlinegalerija.repository.PostRepository;
import com.mrakks.onlinegalerija.repository.UserRepository;
import com.mrakks.onlinegalerija.security.UserPrincipal;
import com.mrakks.onlinegalerija.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

@Controller
public class HomeController {

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    //landing page
    @GetMapping("/")
    public String index() {
        return "index";
    }

    //home page
    @GetMapping("/home")
    public String home(Model model, Authentication auth) {

        // User currently logged in
        if (auth != null) {
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            User user = userPrincipal.getUser();
//                String username = user.getUsername();
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", new User());
        }

        model.addAttribute("listPosts", postService.getAllPosts());

        return "home";
    }

    // profile
    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {

        if (auth != null) {
            //Logged in user
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            User user = userPrincipal.getUser();
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", new User());
        }

        return "profile";
    }


    //login
    @GetMapping("/login")
    public String login() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }

        return "login";
    }

    //register
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/newUser")
    public String newUser(@RequestParam("username") String username,
                          @RequestParam("password") String password) {
        User user = new User(username, passwordEncoder.encode(password), "USER", "ADD_POST,EDIT_POST");
        userRepository.save(user);
        return "redirect:/login";
    }

    //change username
    @PostMapping("username/{id}")
    public String changeUsername(@PathVariable("id") Long id, @RequestParam("username") String username,
                                 HttpServletRequest request) throws ServletException {
        User user = userRepository.findById(id).get();
        user.setUsername(username);

        userRepository.save(user);
        request.logout();
        return "redirect:/profile";
    }

    //change username
    @PostMapping("password/{id}")
    public String changePassword(@PathVariable("id") Long id,
                                 @RequestParam("oldPassword") String oldPwd,
                                 @RequestParam("newPassword") String newPwd,
                                 @RequestParam("repeatedPassword") String pwdRepeat,
                                 HttpServletRequest request) throws ServletException {
        User user = userRepository.findById(id).get();
        String encodedDbPwd = user.getPassword();
        if (passwordEncoder.matches(oldPwd, encodedDbPwd)) {
            if (newPwd.equals(pwdRepeat)) {
                user.setPassword(passwordEncoder.encode(newPwd));
                userRepository.save(user);
            }
        }

        request.logout();
        return "redirect:/profile";
    }
}
