package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	//For generate random number
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	//send otp handler
	@PostMapping("/send_otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
	
		System.out.println("Email : "+email);
		
		//Generate OTP of 4 digit
		
		int otp = random.nextInt(9999);
		
		String subject = "OTP from Smart Contact Manager";
		String message = "<div style='border:1px solid #e2e2e2; padding:20px; background:yellow'>"
						+"<h1> Your OTP is : <b>"+otp+"</b></h1> </div>"
				;
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject,message, to);
		
		if (flag) {
			
			session.setAttribute("myOtp", otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
		} else {
			
			session.setAttribute("message","Please check your email id");
			return "forgot_email_form";
		}	
	}

	//verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session) {
		
		Integer myOtp = (int)session.getAttribute("myOtp");
		String email = (String)session.getAttribute("email");
		
		if (myOtp.equals(otp)) {

			User user =  this.userRepository.getUserByUserName(email);
			
			if (user==null) {
				
				//error message 
				session.setAttribute("message","User does not exits with this email !!");
				return "forgot_email_form";
				
			} else {
				//write code here
				return "password_change_form";
			}
			
//			return "password_change_form";
		
		} else {	
			session.setAttribute("message","You have entered wrong OTP !!");
			return "verify_otp";
		}
	}
	//Change password handler
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
			
			String email = (String)session.getAttribute("email");
			
			User user = userRepository.getUserByUserName(email);
			
			user.setPassword(bCryptPasswordEncoder.encode(newPassword));
			
			userRepository.save(user);
			
			
			return "redirect:/signin?change=password changed successfully..";
		}
}
