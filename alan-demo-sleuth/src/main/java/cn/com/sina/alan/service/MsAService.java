package cn.com.sina.alan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MsAService {


	@Autowired
	private RestTemplate restTemplate;
	
//	@Async
	public String toMsB(){
		
		String s = this.restTemplate.getForObject("http://localhost:8082/ms-b", String.class);
		
		System.out.println("--- GateWayService.toMsB");
		
		return s;
		
	}
	
//	@Async
	public String toMsC(){
		
		String s = this.restTemplate.getForObject("http://localhost:8083/ms-c", String.class);
		
		System.out.println("--- GateWayService.toMsC");
		
		return s;
		
	}

}
