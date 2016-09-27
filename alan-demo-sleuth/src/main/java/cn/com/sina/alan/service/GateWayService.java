package cn.com.sina.alan.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GateWayService {

	@Autowired
	private Tracer tracer;
	@Autowired
	private Random random;
	@Autowired
	private RestTemplate restTemplate;
	
	public String toMsA(){
		
		String s = this.restTemplate.getForObject("http://localhost:8081/ms-a", String.class);
		
		System.out.println("--- GateWayService.toMsA");
		
		return s;
		
	}
	
	public String trans(String s){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--- GateWayService.trans");
		
		return "Result :" + s;
		
	}
	
	@Async
	public void localBackGround() throws InterruptedException {
		int millis = this.random.nextInt(500);
		Thread.sleep(millis);
		this.tracer.addTag("background-sleep-millis", String.valueOf(millis));
		System.out.println("--- GateWayService.localBackGround");
	}	
}
