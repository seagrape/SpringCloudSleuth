package cn.com.sina.alan;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.DefaultSpanNamer;
import org.springframework.cloud.sleuth.SpanAccessor;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import cn.com.sina.alan.service.MsAService;
import cn.com.sina.alan.utils.ConcurrentUtils;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
@RestController
public class SleuthMsAApplication {
	
	private static final Logger log = LoggerFactory.getLogger(SleuthMsAApplication.class);
	
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
	
	@RequestMapping(value = "/ms-a", method = RequestMethod.GET)
	public String MsACtr() throws Exception{

		String result = "";
		
//		1 顺序调用
//		String sb = wshService.toMsB();
//		String sc = wshService.toMsC();
//		result = "{sb=>"+sb+",sc=>"+sc+"}";
		
//		2 并发工具调用，但不传递trans信息
//		result = ConcurrentUtils.concurrentExecuteSame(
//		        () -> wshService.toMsB(),
//		        () -> wshService.toMsC(),
//		        (r1, r2) -> "{sb=>"+r1+",sc=>"+r2+"}" // 聚合逻辑：两个结果相加
//		);
		
//		3 并发调用，传递trans信息
//		TraceKeys traceKeys = new TraceKeys();
//		SpanNamer spanNamer = new DefaultSpanNamer();
//		ExecutorService executor = Executors.newFixedThreadPool(3);
//
//		// tag::completablefuture[]
//		CompletableFuture<String> completableFuture = 
//				CompletableFuture.supplyAsync(
//				() -> wshService.toMsB(), 
//				new TraceableExecutorService(executor,this.tracer, traceKeys, spanNamer, "calculateTax-1")
//		).thenCombine(
//				CompletableFuture.supplyAsync(
//				() -> wshService.toMsC(),
//				new TraceableExecutorService(executor,this.tracer, traceKeys, spanNamer, "calculateTax-2")
//        ), (r1, r2) -> "{sb=>"+r1+",sc=>"+r2+"}");
//		
//		result = completableFuture.get();
		
//		4 并发工具调用，传递trans信息
		TraceKeys traceKeys = new TraceKeys();
		SpanNamer spanNamer = new DefaultSpanNamer();
		result = ConcurrentUtils.concurrentExecuteSame(
		        () -> wshService.toMsB(),
		        () -> wshService.toMsC(),
		        (r1, r2) -> "{sb=>"+r1+",sc=>"+r2+"}", // 聚合逻辑：两个结果相加
		        new TraceableExecutorService(ConcurrentUtils.getPool(),this.tracer, traceKeys, spanNamer, "calculateTax-1")
		);

		
		log.info(result);
		
		return result;
	}

    public static void main(String[] args) {
    	String[] args2 = new String[]{"--server.port=8081","--spring.application.name=msa"};
        SpringApplication.run(SleuthMsAApplication.class, args2);
    }
}
