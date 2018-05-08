package com.citsh;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Web {
	@GetMapping("test")
	public void Test(){
		return ;
	}

}
