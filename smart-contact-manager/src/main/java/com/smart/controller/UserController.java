package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import ch.qos.logback.core.status.Status;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
//		System.out.println("UserName : "+userName);
		
		//get the user using username
		User user = userRepository.getUserByUserName(userName);
		
//		System.out.println("USER : "+user);
		
		model.addAttribute("user", user);

	}
	
	//Dashboard home handler
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
	//handler for open add form
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session
			) {

		try {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			//processing and uploading file
			if (file.isEmpty()) { 
				System.out.println("File is empty !!");
				contact.setImage("contact.png");
				
			} else {
				//msg
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/image").getFile();
				
				//create image path 
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			
			System.out.println("Contact :"+contact);
			System.out.println("Added to database");
			
			//message success
			session.setAttribute("message", new Message("Your contact is added !","success"));
		}
		catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			
			//message error
			session.setAttribute("message", new Message("Something went wrong !! try again","danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page = 5[n]
	//current page =0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		

		//ContactPer page = 5
		//CurrentPage-page
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "Show User Contacts");
		
		return "normal/show_contacts";
	}

	//showing perticular contact details
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") int cId,Model model, Principal principal) {
		
	    Optional<Contact> contactOptional = contactRepository.findById(cId);
	    Contact contact = contactOptional.get();
	    
	    //check security
	    String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		System.out.println("CID : "+cId);
		
		return "normal/contact_detail";
	}
	
	//delete contact handler 
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid, Model model, HttpSession session, Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		/*contact.setUser(null);//this code not working
		this.contactRepository.delete(contact);//this code not working
		*/
		User user = userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	
	//Open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		
		Contact contact = contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		
		model.addAttribute("title", "Update Contact");
		
		return "normal/update_contact";
	}

	//process update form handler
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
		
		
		try {
			
			//old contact details
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			
			//image code
			if(!file.isEmpty()) {	
				//rewrite file code here
				//delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldContact.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				
				//create image path 
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			
			session.setAttribute("message", new Message("Your contact is updated !","success"));
	
			User user = userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile page");
		return "normal/profile";
	}
	
	//Open settings handler change-password
	@GetMapping("/settings")
	public String openSettings(Model model) {
		
		model.addAttribute("title", "Settings page");
		return "normal/settings";
	}
	
	//change password handler 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Model model, Principal principal,HttpSession session) {
		
		System.out.println("OLDP : "+oldPassword);
		System.out.println("NEWP : "+newPassword);
		
		//get current login user
		User currentUser = userRepository.getUserByUserName(principal.getName());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your password is successfully changed.","success"));
		}
		else {
			session.setAttribute("message", new Message("Please enter currect old password.","danger"));
			return "redirect:/user/settings";
			
		}
		model.addAttribute("title", "Settings page");
		
		return "redirect:/user/index";
	}
	
	
	//Creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
		
		int amount = Integer.parseInt(data.get("amount").toString());
		
//		System.out.println("Amount is :"+amount);
//		System.out.println("Hey order function is executed");
			
		RazorpayClient client = new RazorpayClient("rzp_test_2BbqUiv4QYzXu6", "5pIJZIQdWYLoBxTr552v1NiR");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amount*100);
		ob.put("currency", "INR");
		ob.put("receipt", "TXN_202324");
		
		//creating new order
		Order order = client.orders.create(ob);
		
		//if you want you can save this to your database.....
//		System.out.println("Order is :"+order);
		
		//save the order in the database
		MyOrder myOrder = new MyOrder();
		
		User user = userRepository.getUserByUserName(principal.getName());
		
		int a = order.get("amount");
		myOrder.setAmount(a/100);//convert paise to rupees
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("Created");
		myOrder.setUser(user);
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		 
		this.myOrderRepository.save(myOrder);
		
		System.out.println("DATA : "+data);
		
		Map<String, String> map = new HashMap<>();
		map.put("msg", "updated");
		
		return ResponseEntity.ok(map);
	}
	
	
}
