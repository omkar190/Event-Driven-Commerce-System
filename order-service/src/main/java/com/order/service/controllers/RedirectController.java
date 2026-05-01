package com.order.service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class RedirectController {
    @GetMapping(value = {"/"})
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://order-ui-y50d.onrender.com");
    }
}
