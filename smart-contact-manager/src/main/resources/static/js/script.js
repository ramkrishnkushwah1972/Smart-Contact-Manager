console.log("This is js file");

//js for sidebar
const toggleSidebar = () => {

	if($(".sidebar").is(":visible")) {
		
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	} else {
			
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};
/*Seach fuction for search contacts*/
 	const search =()=>{
// 		console.log("Done......");
 		let query = $("#search-input").val()
 		
 		if(query==''){
 			$(".search-result").hide();
 		}
 		else{
 			console.log(query);
 			
 			let url = `http://localhost:8080/search/${query}`;
 			
 			fetch(url)
 			.then((response) => {
 				return response.json();
 			}).then((data) => {
 				//data.....
// 				console.log(data);
 				
 				let text = `<div class='list-group'>`;
 				//traverse array and select contact name
 				data.forEach((contact) => {
 					text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'> ${contact.name}</a>`
 				});
 				
 				text +=`</div>`;
 					
 				$(".search-result").html(text);
 				$(".search-result").show();
 					
 			});
 			
 			$(".search-result").show();
 		}
 	}
//js for payment integration
 	const paymentStart = () => {
 		console.log("Payment start.........");
 		let amount = $("#payment_field").val();
 		
 		if(amount <= 0 || amount == '')
 		{
// 			alert("Amount is required please enter amount!!");
 			swal("Failed !!", "Amount is required please enter amount!!", "error");
 			return;
 		}
 		console.log(amount)
 		
// 	I will use ajax to send request to server to create order - jquery
 	$.ajax(
 		{
	 		url:'/user/create_order',
	 		data:JSON.stringify({amount:amount,info:'order_request'}),
	 		contentType:'application/json',
	 		type:'POST',
	 		dataType:'json',
	 		success:function(response){
	 			//invoked when success
	 			console.log(response);
	 			if(response.status == "created")
	 			{
	 				
	 				//open payment form
	 				let options = 
	 				{
	 						key:'rzp_test_2BbqUiv4QYzXu6',
	 						amount:response.amount,
	 						currency: "INR",
	 						name:'Smart Contact Manager',
	 						description:'Doanation',
	 						image:'data:image/jpeg;base64,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',
	 						order_id:response.id,
	 						//if payment is success then this function will execute
	 						handler:function(response){
	 							console.log(response.razorpay_payment_id);
	 							console.log(response.razorpay_order_id);
	 							console.log(response.razorpay_signature);
	 							console.log("Payment Successfull !!	");
	 							
	 							//this method for update payment_id in order table
	 							updatePaymentOnServer(
	 									response.razorpay_payment_id,
	 									response.razorpay_order_id,
	 									"paid"
	 								);
	 							
//	 							alert("Congrats : Payment Successfull !!");
//	 							swal("Good job!", "Congrats ! Payment Successfull !!", "success");
	 						},
	 						"prefill": {
	 					        "name": "",
	 					        "email": "",
	 					        "contact": ""
	 					    },
	 					    "notes": {
	 					        "address": "RK Corporate Office"
	 					    },
	 					    "theme": {
	 					        "color": "#3399cc"
	 					    }			
	 				};//end option variable
	 				
	 				//for payment initiate
	 				let rzp = new Razorpay(options);
	 				rzp.open();//open() for open form
	 				
	 				//if payment is unsuccessful
	 				rzp.on('payment.failed', function (response){
	 			        console.log(response.error.code);
	 			        console.log(response.error.description);
	 			        console.log(response.error.source);
	 			        console.log(response.error.step);
	 			        console.log(response.error.reason);
	 			        console.log(response.error.metadata.order_id);
	 			        console.log(response.error.metadata.payment_id);
//	 			        alert("Opps payment fail !!");
//	 			       swal("Failed !!", "Opps ! payment failed !!", "error");
	 				});
	 			}//end if
	 		},//end success
	 		error:function(error){
	 			//invoked when error
	 			console.log(error);
//	 			alert("Something went wrong !!");
	 			swal("", "Something went wrong !!", "error");
	 		}
	 	}
 	)//end ajax function
 };
 	
 function updatePaymentOnServer(payment_id, order_id, status)
 {
	 $.ajax({
		 		url:'/user/update_order',
				data:JSON.stringify({payment_id:payment_id, order_id:order_id, status:status}),
			 	contentType:'application/json',
			 	type:'POST',
			 	dataType:'json',
			 	success:function(response){
			 		swal("Good job!", "Congrats ! Payment Successfull !!", "success");
			 	},
			 	error:function(error){
			 		swal("Failed !!", "Your payment is successful , but we did not get on server , we will contact you as soon as possible !!", "error");
			 	},
		 	});
 }