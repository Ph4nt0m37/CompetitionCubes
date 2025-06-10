package dev.pakn.competitioncubes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LinkMappings {
    @RequestMapping("/competition")
    public String compPage() {
        return "comp.html";
    }

    @RequestMapping("/")
    public String mainPage() {
        return "main.html";
    }
}
