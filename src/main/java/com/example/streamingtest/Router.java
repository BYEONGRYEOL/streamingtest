package com.example.streamingtest;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Router {

	private TestHandler testHandler;

	public Router(TestHandler testHandler){
		this.testHandler = testHandler;
	}

	@Bean
	public RouterFunction<ServerResponse> routeTest(){
		return route()
			.GET("/abcd", request -> testHandler.test(request))
			.build();
	}

	@Bean
	public RouterFunction<ServerResponse> streamHlsRoute(){
		return route()
			.GET("/stream/{streamername}", request -> testHandler.stream(request))
			.build();
	}

}
