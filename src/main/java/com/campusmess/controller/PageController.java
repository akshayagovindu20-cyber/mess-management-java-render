package com.campusmess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/register", "/registration"})
    public String registrationPage() {
        return "redirect:/register.html";
    }

    @GetMapping({"/login", "/signin"})
    public String loginPage() {
        return "redirect:/login.html";
    }
}
