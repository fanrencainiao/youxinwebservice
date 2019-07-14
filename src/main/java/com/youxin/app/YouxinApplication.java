package com.youxin.app;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.request.RequestContextListener;

import com.youxin.app.filter.AuthorizationFilterProperties;



@SpringBootApplication
@EntityScan("com.youxin.app.entity")
@ComponentScan(basePackages ="com.youxin.app")
@EnableConfigurationProperties(AuthorizationFilterProperties.class)
@ServletComponentScan
public class YouxinApplication {

	public static void main(String[] args) {
		SpringApplication.run(YouxinApplication.class, args);
	}
	
	
	@Bean
	public RequestContextListener requestContextListener(){
		    return new RequestContextListener();
		} 

}
