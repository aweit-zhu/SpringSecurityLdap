package com.example.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LdapAuthoritiesFilter extends OncePerRequestFilter {

	@Autowired
	LdapTemplate ldapTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()
				&& authentication instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
			if (usernamePasswordAuthenticationToken.getPrincipal() instanceof User) {
				User ldapUserDetails = (User) usernamePasswordAuthenticationToken.getPrincipal();
				UsernamePasswordAuthenticationToken updatedAuthentication = new UsernamePasswordAuthenticationToken(
						ldapUserDetails, ldapUserDetails.getPassword(),
						retrieveLdapAuthorities(ldapUserDetails.getUsername()));
				updatedAuthentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
			}
		}

		// Proceed with the filter chain
		filterChain.doFilter(request, response);
	}

	private Collection<? extends GrantedAuthority> retrieveLdapAuthorities(String username) {

		// Define the LDAP search filter to retrieve the authorities for the user
		String filter = "(memberUid=cn=" + username + ",ou=users,dc=example,dc=com)";

		// Set the search controls to limit the attributes returned
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setReturningAttributes(new String[] { "cn" });

		// Perform the LDAP search and retrieve the authorities
		Set<GrantedAuthority> authorities = new HashSet<>();
		ldapTemplate.search("ou=groups,dc=example,dc=com", filter, searchControls, (ContextMapper<Void>) ctx -> {
			Attributes attributes = ((DirContextOperations) ctx).getAttributes();
			try {
				javax.naming.directory.Attribute memberOfAttribute = attributes.get("cn");
				if (memberOfAttribute != null) {
					for (int i = 0; i < memberOfAttribute.size(); i++) {
						String authority = (String) memberOfAttribute.get(i);
						authorities.add(new SimpleGrantedAuthority("ROLE_" + authority.toUpperCase()));
					}
				}
			} catch (NamingException e) {
				e.printStackTrace();
			}
			return null;
		});
		return authorities;
	}
}