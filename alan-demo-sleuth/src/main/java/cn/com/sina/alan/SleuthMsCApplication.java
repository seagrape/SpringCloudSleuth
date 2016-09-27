package cn.com.sina.alan;


import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
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
public class SleuthMsCApplication {
	
	private static final Logger log = LoggerFactory.getLogger(SleuthMsCApplication.class);
	
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
	
	@Autowired
	private Random random;
	
	@RequestMapping(value = "/ms-c", method = RequestMethod.GET)
	public String MsCCtr() throws InterruptedException {
		
		Span span = this.tracer.createSpan("http:customTraceEndpoint",new AlwaysSampler());
		
		int millis = 3000;
		Thread.sleep(millis);
		
		this.tracer.addTag("ms-c-random-sleep-millis", String.valueOf(millis));
		this.tracer.close(span);
		
		log.info("ms-c");
		return "ms-c";
	}

    public static void main(String[] args) {
    	String[] args2 = new String[]{"--server.port=8083","--spring.application.name=msc"};
        SpringApplication.run(SleuthMsCApplication.class, args2);
    }
}
