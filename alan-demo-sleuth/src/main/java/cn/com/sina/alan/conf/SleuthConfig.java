package cn.com.sina.alan.conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanExtractor;
import org.springframework.cloud.sleuth.SpanInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class SleuthConfig {

	// @Bean
	// public SpanExtractor<Message> messagingSpanExtractor() {
	// return null;
	// }
	//
	// @Bean
	// public SpanInjector<MessageBuilder> messagingSpanInjector() {
	// return null;
	// }

	@Bean
	@Primary
	public SpanExtractor<HttpServletRequest> httpServletRequestSpanExtractor() {
		return new CustomHttpServletRequestSpanExtractor();
	}

	@Bean
	@Primary
	public SpanInjector<HttpServletResponse> httpServletResponseSpanInjector() {
		return new CustomHttpServletResponseSpanInjector();
	}

	static class CustomHttpServletRequestSpanExtractor implements SpanExtractor<HttpServletRequest> {

		@Override
		public Span joinTrace(HttpServletRequest carrier) {
			long traceId = Span.hexToId(carrier.getHeader("correlationId"));
			long spanId = Span.hexToId(carrier.getHeader("mySpanId"));
			// extract all necessary headers
			Span.SpanBuilder builder = Span.builder().traceId(traceId).spanId(spanId);
			// build rest of the Span
			return builder.build();
		}
	}

	static class CustomHttpServletResponseSpanInjector implements SpanInjector<HttpServletResponse> {

		@Override
		public void inject(Span span, HttpServletResponse carrier) {
			carrier.addHeader("correlationId", Span.idToHex(span.getTraceId()));
			carrier.addHeader("mySpanId", Span.idToHex(span.getSpanId()));
			// inject the rest of Span values to the header
		}
	}
}
