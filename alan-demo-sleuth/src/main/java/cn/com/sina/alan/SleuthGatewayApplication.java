package cn.com.sina.alan;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cn.com.sina.alan.service.GateWayService;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@RestController
public class SleuthGatewayApplication {
	
	private static final Logger log = LoggerFactory.getLogger(SleuthGatewayApplication.class);
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Autowired
	GateWayService wshService;
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private Tracer tracer;
	@Autowired
	private SpanAccessor accessor;
	
	@RequestMapping(value = "/do/{msg}", method = RequestMethod.GET)
	public String doSomething(@PathVariable("msg") String msg) throws InterruptedException{
		
		log.info(msg);

		String s = wshService.toMsA();
		
		String result = wshService.trans(s);
		
		wshService.localBackGround();
		
		return result;
	}

    public static void main(String[] args) {
    	String[] args2 = new String[]{"--server.port=8080","--spring.application.name=gateway"};
        SpringApplication.run(SleuthGatewayApplication.class, args2);
    }
}
