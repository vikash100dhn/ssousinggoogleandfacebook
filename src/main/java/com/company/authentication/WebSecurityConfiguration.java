package com.company.authentication;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

@SpringBootApplication
@EnableOAuth2Client
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	private OAuth2ClientContextFilter oauth2ClientContextFilter;
	@Override
	 protected void configure(HttpSecurity http) throws Exception {
	     http
	             .csrf()
	                 .disable()
	             .antMatcher("/**")
	                 .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
	                 .addFilterAfter(oauth2ClientContextFilter, SecurityContextPersistenceFilter.class)
	             .authorizeRequests()
	             .antMatchers("/chooseLogin.html")
	                 .permitAll()
	             .anyRequest()
	                 .authenticated()
	             .and()
	                 .formLogin()
	                     .loginPage("/chooseLogin.html");
	 }
	
	@Bean
	@ConfigurationProperties("security.oauth2.google.client")
	public AuthorizationCodeResourceDetails google() {
	    return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("security.oauth2.google.resource")
	public ResourceServerProperties googleResource() {
	    return new ResourceServerProperties();
	}
	
	@Bean
	@ConfigurationProperties("security.oauth2.facebook.client")
	public AuthorizationCodeResourceDetails facebook() {
	    return new AuthorizationCodeResourceDetails();
	}
	 
	@Bean
	@ConfigurationProperties("security.oauth2.facebook.resource")
	public ResourceServerProperties facebookResource() {
	    return new ResourceServerProperties();
	}

	private Filter ssoGoogleFilter() {
	    OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/google");
	    OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
	    googleFilter.setRestTemplate(googleTemplate);
	    googleFilter.setTokenServices(new UserInfoTokenServices(googleResource().getUserInfoUri(), google().getClientId()));
	    return googleFilter;
	}
	private Filter ssoFacebookFilter() {
	    OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/facebook");
	    OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);
	    facebookFilter.setRestTemplate(facebookTemplate);
	    facebookFilter.setTokenServices(new UserInfoTokenServices(facebookResource().getUserInfoUri(), facebook().getClientId()));
	    return facebookFilter;
	}

	private Filter ssoFilter() {
	     List<Filter> filters = new ArrayList<>();
	     filters.add(ssoGoogleFilter());
	     filters.add(ssoFacebookFilter());
	 
	     CompositeFilter filter = new CompositeFilter();
	     filter.setFilters(filters);
	     return filter;
	}
}
