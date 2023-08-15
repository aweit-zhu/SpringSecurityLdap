package com.example.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.model.LdapUser;

@Service
public class LdapUserService {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	LdapTemplate ldapTemplate;

	final Base64.Encoder encoder = Base64.getEncoder();

	public void createUser(LdapUser user) throws NoSuchAlgorithmException {

		// Create user
		Attribute objectClass = new BasicAttribute("objectClass");
		objectClass.add("top");
		objectClass.add("person");
		objectClass.add("organizationalPerson");
		objectClass.add("inetOrgPerson");

		Attributes attributes = new BasicAttributes();
		attributes.put(objectClass);
		attributes.put("cn", user.getUsername());
		attributes.put("sn", user.getUsername());
		attributes.put("userid", user.getUsername());
		attributes.put("userPassword", "{md5}" + encodePassword(user.getPassword()));

		String dnStr = "cn=" + user.getUsername() + ",ou=users,dc=example,dc=com";
		Name dn = LdapUtils.newLdapName(dnStr);
		ldapTemplate.bind(dn, null, attributes);

		// Add role list to 'cn=users,ou=groups,dc=example,dc=com'
		Attribute memberUid = new BasicAttribute("memberUid", dnStr);
		ModificationItem[] modificationItems = new ModificationItem[1];
		modificationItems[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, memberUid);

		Name groupDn = LdapUtils.newLdapName("cn=user,ou=groups,dc=example,dc=com");
		ldapTemplate.modifyAttributes(groupDn, modificationItems);

	}

	private String encodePassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		byte[] digest = md.digest();
		String base64str = encoder.encodeToString(digest);
		return base64str;
	}

}