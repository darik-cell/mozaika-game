package com.ssau.mozaikagame.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

  @GetMapping("/login")
  public String loginPage() {
    return "login"; // будет отображена /WEB-INF/views/login.jsp
  }

  @GetMapping("/register")
  public String registrationPage() {
    return "registration"; // отображается /WEB-INF/views/registration.jsp
  }

  @GetMapping("/home")
  public String homePage() {
    return "home"; // отображается /WEB-INF/views/home.jsp
  }

  @GetMapping("/logout")
  public String logoutPage(HttpServletRequest request) {
    request.getSession().invalidate();
    return "redirect:/login";
  }
}
