package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.exception.CustomAccessDeniedHandler;
import com.example.filter.LdapAuthoritiesFilter;
import com.example.mapper.LdapUserDetailsMapper;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	LdapContextSource ldapContextSource;

	@Autowired
	LdapTemplate ldapTemplate;
	
	@Autowired
	LdapAuthoritiesFilter ldapAuthoriesFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(ldapAuthenticationProvider());
	}

	@Bean
	public AuthenticationProvider ldapAuthenticationProvider() {
		LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(ldapBindAuthenticator(),
				ldapAuthoritiesPopulator());
		ldapAuthenticationProvider.setUserDetailsContextMapper(new LdapUserDetailsMapper());
		return ldapAuthenticationProvider;
	}

	@Bean
	public LdapAuthenticator ldapBindAuthenticator() {
		BindAuthenticator bindAuthenticator = new BindAuthenticator(ldapContextSource);
		bindAuthenticator.setUserSearch(ldapUserSearch());
		return bindAuthenticator;
	}

	@Bean
	public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
		DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(ldapContextSource,
				"ou=groups,dc=example,dc=com");
		authoritiesPopulator.setGroupSearchFilter("memberUid={0}");
		return authoritiesPopulator;
	}

	@Bean
	public LdapUserSearch ldapUserSearch() {
		return new FilterBasedLdapUserSearch("ou=users,dc=example,dc=com", "(uid={0})", ldapContextSource);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.addFilterBefore(ldapAuthoriesFilter,BasicAuthenticationFilter.class).authorizeRequests()
		  .antMatchers("/admin/**").hasAnyRole("ADMIN")	
		  .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
		  .anyRequest().authenticated()
		.and()
		  .csrf().ignoringAntMatchers("/admin/user")
		.and()
		  .formLogin()
		.and()
		  .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
		.and()
		  .logout();
	}
	
	@Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	

}