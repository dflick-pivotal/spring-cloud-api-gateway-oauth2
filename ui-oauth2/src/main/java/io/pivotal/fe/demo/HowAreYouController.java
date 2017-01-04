package io.pivotal.fe.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
// http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2
// https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v
// https://spring.io/guides/tutorials/spring-boot-oauth2/
// https://jmnarloch.wordpress.com/2016/07/06/spring-boot-hystrix-and-threadlocals/
public class HowAreYouController {
	
	@Value("${spring.application.name}")
	String iam;
	String remote = "resource";
	
	@Autowired
	@Qualifier("oauth2RestTemplate")
	OAuth2RestTemplate oauth2RestTemplate;

	@RequestMapping(value={"/howAreYou"})
    @HystrixCommand(fallbackMethod = "howAreYouFallback",
    commandProperties = {
    	      @HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
    	    }
    	)
	String howAreYou()
	{
		String response = oauth2RestTemplate.getForObject("http://resource/howAreYou", String.class);
		return response;
	}	

	String howAreYouFallback()
	{
		return "Can't reach "+remote+"!";
	}
}
