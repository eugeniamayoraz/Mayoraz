package com.tp.ong.homeController; // 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/") // Mapea las peticiones GET a la URL raíz ("/") a este método
    public String home() {
        // Este método devuelve el nombre de la vista (la página HTML).
        // Spring Boot buscará automáticamente 'src/main/resources/templates/index.html'
        return "index";
    }
}