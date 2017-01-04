package io.pivotal.fe.demo;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

// http://stackoverflow.com/questions/28703847/how-do-you-set-a-resource-id-for-a-token

/*
 
curl -v -u myclient:myclientsecret -X POST http://localhost:9999/uaa/oauth/token -H "Accept: application/json" -d "password=password&username=user&grant_type=password&scope=read&client_secret=myclientsecret&client_id=myclient"

curl myclient:myclientsecret@localhost:9999/uaa/oauth/token -d grant_type=client_credentials

curl myclient:myclientsecret@localhost:9999/uaa/oauth/token -d grant_type=password -d username=user -d password=password

curl -G http://localhost:9000/ -H "Authorization: Bearer a63ebb74-8c6d-4d98-8496-114efde69e2e"

curl -G http://resource.local2.pcfdev.io/test -H "Authorization: Bearer a63ebb74-8c6d-4d98-8496-114efde69e2e"

curl -H "Authorization: Bearer e2c8ecb2-d896-4c1c-a870-bc5f4cbd073c" localhost:9999/uaa/user
 * 
 */
@EnableDiscoveryClient
@SpringBootApplication
@RestController
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class UiOauth2Application {

	@Autowired
	private ResourceServerProperties sso;

	 private TokenExtractor tokenExtractor = new BearerTokenExtractor();

	 	@Bean
		public ResourceServerTokenServices myUserInfoTokenServices() {
			return new CustomUserInfoTokenServices(sso.getUserInfoUri(), sso.getClientId());
		}
	

	    @Bean
	    public GlobalMethodSecurityConfiguration globalMethodSecurityConfiguration() {
	        return new GlobalMethodSecurityConfiguration() {
	            @Override
	            protected MethodSecurityExpressionHandler createExpressionHandler() {
	                return new OAuth2MethodSecurityExpressionHandler();
	            }
	        };
	    }
	    
	    @Bean
	    public ResourceServerConfigurer resourceServerConfigurerAdapter() {
	        return new ResourceServerConfigurerAdapter() {
	            @Override
	            public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
	                resources.resourceId("resource");
	            }

	            @Override
	            public void configure(HttpSecurity http) throws Exception {
	                http.addFilterAfter(new OncePerRequestFilter() {
	                    @Override
	                    protected void doFilterInternal(HttpServletRequest request,
	                                                    HttpServletResponse response, FilterChain filterChain)
	                        throws ServletException, IOException {
	                        // We don't want to allow access to a resource with no token so clear
	                        // the security context in case it is actually an OAuth2Authentication
	                        if (tokenExtractor.extract(request) == null) {
	                            SecurityContextHolder.clearContext();
	                        }
	                        filterChain.doFilter(request, response);
	                    }
	                }, AbstractPreAuthenticatedProcessingFilter.class);
	                http.csrf().disable();
	                http.authorizeRequests().anyRequest().authenticated();
	            }
	        };
	    }

	@Value("${spring.application.name}")
	String iam;

	public static void main(String[] args) {
		SpringApplication.run(UiOauth2Application.class, args);
	}
	
	@RequestMapping("/howAreYou")
	@PreAuthorize("#oauth2.hasScope('resource.read')")
	String howAreYou()
	{
		return iam + " is doing fine!";
	}

	@RequestMapping("/")
	@PreAuthorize("#oauth2.hasScope('resource.read')")
	public Message home() {
		return new Message("Hello World");
	}

	@RequestMapping("/test")
	public Message test() {
		return new Message("Hello World");
	}

	@RequestMapping("/test1")
	@PreAuthorize("#oauth2.hasScope('read')")
	public Message test1() {
		return new Message("Hello World");
	}

}

class Message {
	private String id = UUID.randomUUID().toString();
	private String content;

	Message() {
	}

	public Message(String content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public String getContent() {
		return content;
	}
}

