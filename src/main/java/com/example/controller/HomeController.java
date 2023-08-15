package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/error-403")
	public String accessDenied() {
	    return "error/error-403";
	}
}
