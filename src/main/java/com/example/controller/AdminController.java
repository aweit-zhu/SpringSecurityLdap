package com.example.controller;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.LdapUser;
import com.example.service.LdapUserService;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	LdapUserService ldapUserService;
    
	@GetMapping("")
	public String admin() {
		return "admin is here";
	}
	
    @PostMapping("/user")
    public void createUser(@RequestBody LdapUser user) throws NoSuchAlgorithmException {
        ldapUserService.createUser(user);
    }
}
