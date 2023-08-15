package com.example.mapper;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapUserDetailsMapper implements UserDetailsContextMapper {

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String ldapUsername = ctx.getStringAttribute("uid");
        byte[] binaryData = (byte[]) ctx.getObjectAttribute("userPassword");
        String ldapPassword = new String(binaryData, StandardCharsets.UTF_8);        
        return User.builder()
                .username(ldapUsername)
                .password(ldapPassword)
                .authorities(authorities)
                .build();
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        // Not implemented as this is not needed for authentication
    }
}