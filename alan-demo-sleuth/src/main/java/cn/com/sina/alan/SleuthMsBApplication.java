package cn.com.sina.alan;


import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cn.com.sina.alan.service.MsAService;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@RestController
public class SleuthMsBApplication {
	
	private static final Logger log = LoggerFactory.getLogger(SleuthMsBApplication.class);
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Autowired
	MsAService wshService;

	@Autowired
	private Tracer tracer;
	@Autowired
	private SpanAccessor accessor;
	
	@RequestMapping(value = "/ms-b", method = RequestMethod.GET)
	public Callable<String> MsBCtr() {
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				
				int millis = 1000;
				
				Thread.sleep(millis);
				
				SleuthMsBApplication.this.tracer.addTag("ms-b-callable-sleep-millis", String.valueOf(millis));
				Span currentSpan = SleuthMsBApplication.this.accessor.getCurrentSpan();
				
				log.info("MsBCtr-" + currentSpan);
				return "MsBCtr-" + currentSpan;
			}
			
			@Override 
			public String toString() {
		        return "MSBSPAN";
		    }
		};
	}

    public static void main(String[] args) {
    	String[] args2 = new String[]{"--server.port=8082","--spring.application.name=msb"};
        SpringApplication.run(SleuthMsBApplication.class, args2);
    }
}
