package io.pivotal.fe.demo;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@EnableCircuitBreaker
//@EnableHystrix
@SpringBootApplication
@EnableOAuth2Sso
//@EnableOAuth2Client
@EnableZuulProxy
@RestController
// http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2
// https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v
// https://spring.io/guides/tutorials/spring-boot-oauth2/
// https://jmnarloch.wordpress.com/2016/07/06/spring-boot-hystrix-and-threadlocals/
public class UiOauth2Application extends WebSecurityConfigurerAdapter {

    @Value("${security.oauth2.client.clientId}")
    private String clientId;
	@Value("${security.oauth2.client.clientSecret}")
	private String clientSecret;
    @Value("${security.oauth2.client.userAuthorizationUri}")
    private String authorizeUrl;
    @Value("${security.oauth2.client.accessTokenUri}")
    private String tokenUrl;

	@RequestMapping({ "/user", "/me" })
	public Map<String, String> user(Principal principal) {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("name", principal.getName());
		return map;
	}
/*
		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.authorizeRequests()
					.antMatchers("/hystrix.stream","/index.html", "/home.html", "/").permitAll()
					.anyRequest().authenticated()
					.and()
				.csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
			// @formatter:on
		}
*/
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.antMatcher("/**").authorizeRequests().antMatchers("/hystrix.stream","/index.html", "/home.html","/", "/login**", "/webjars/**").permitAll().anyRequest()
					.authenticated().and().exceptionHandling()
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
					.logoutSuccessUrl("/").permitAll().and().csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
			// @formatter:on
		}

	public static void main(String[] args) {
		SpringApplication.run(UiOauth2Application.class, args);
	}

    @Bean
    protected OAuth2ProtectedResourceDetails resource() {

        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();

        List scopes = new ArrayList<String>(2);
        scopes.add("write");
        scopes.add("read");
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId(clientId);
        resource.setClientSecret(clientSecret);
        resource.setGrantType("password");
        resource.setScope(scopes);

        return resource;
    }
    
	@Bean
	@LoadBalanced
	public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
	        OAuth2ProtectedResourceDetails details) {
	    return new OAuth2RestTemplate(resource(), oauth2ClientContext);
	}

}
