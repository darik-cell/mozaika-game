package com.ssau.mozaikagame.controller;

import com.ssau.mozaikagame.entity.Puzzle;
import com.ssau.mozaikagame.repository.PuzzleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

  private final PuzzleRepository puzzleRepository;

  @GetMapping("/login")
  public String loginPage() {
    return "login"; // будет отображена /WEB-INF/views/login.jsp
  }

  @GetMapping("/register")
  public String registrationPage() {
    return "registration"; // отображается /WEB-INF/views/registration.jsp
  }

  @GetMapping("/home")
    public String homePage(Model model) {
        List<Puzzle> puzzles = puzzleRepository.findAll();
        model.addAttribute("puzzles", puzzles);
        return "home"; // страница /WEB-INF/views/home.jsp
    }

  @GetMapping("/logout")
  public String logoutPage(HttpServletRequest request) {
    request.getSession().invalidate();
    return "redirect:/login";
  }
}
