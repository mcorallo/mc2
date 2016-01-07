package it.mcsquared.engine.manager;

import it.mcsquared.engine.Mc2Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPManager {
	private String domain;
	private String server;
	private String base;
	private Integer port;

	private String systemPrincipal;
	private String systemPassword;

	private Logger logger = LoggerFactory.getLogger(LDAPManager.class);

	public LDAPManager(Mc2Engine engine) {
		this(engine, null);
	}

	public LDAPManager(Mc2Engine engine, String[] bases) {

		domain = engine.getSystemProperty("ldap.domain");
		if (domain != null) {
			systemPrincipal = engine.getSystemProperty("ldap.principal");
			systemPassword = engine.getSystemProperty("ldap.password");
			server = engine.getSystemProperty("ldap.server");
			port = Integer.parseInt(engine.getSystemProperty("ldap.server.port"));
			base = engine.getSystemProperty("ldap.base");
		} else {
			logger.error("ldap domain property not found. LDAPManager disabled.");
		}
	}

	public Map<String, Object> authenticate(String username, String password) throws NamingException {
		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL, "LDAP://" + server + ":" + port);
		properties.put(Context.SECURITY_PRINCIPAL, domain + "\\" + username);
		properties.put(Context.SECURITY_CREDENTIALS, password);

		Map<String, Object> userData = null;
		InitialDirContext context = null;
		try {
			context = new InitialDirContext(properties);// se questa riga non lancia un'eccezione allora le credenziali sono corrette
			logger.debug("Created InitialDirContext for: " + username + "@" + server + ":" + port);
			userData = new HashMap<String, Object>();
			userData.put("username", username);
		} catch (Exception e) {
			logger.error("Invalid authentication for systemUsername '" + username + "': " + e.toString());
		} finally {
			if (context != null) {
				context.close();
			}
		}
		return userData;
	}

	@SuppressWarnings("rawtypes")
	public List<Map<String, Object>> search(String username) throws NamingException {
		Properties properties = getDomainProperties();

		List<Map<String, Object>> users = new ArrayList<>();
		InitialDirContext context = null;
		try {
			logger.debug("Creating InitialDirContext for: " + properties);
			context = new InitialDirContext(properties);
			logger.debug("Created InitialDirContext for: " + systemPrincipal + "@" + server + ":" + port);

			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			String[] attrIDs = {
					"name",
					"samaccountname",
					"mail", };
			searchCtls.setReturningAttributes(attrIDs);

			String filter = "samaccountname=*" + username + "*";

			NamingEnumeration answer = context.search("OU=NPOSistemi,DC=nposistemi,DC=it", filter, searchCtls);
			Attribute attr;
			while (answer.hasMore()) {
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();
				Map<String, Object> userData = new HashMap<String, Object>();
				attr = attrs.get("samaccountname");
				if (attr == null) {
					logger.error("null samaccountname for " + attrs);
					continue;
				}
				userData.put("username", attr.get());
				attr = attrs.get("mail");
				if (attr != null) {
					userData.put("email", attr.get());
				}
				users.add(userData);
			}

			logger.debug("searching for: " + base + " -> " + filter + " = " + users.size());
			return users;

		} catch (Exception e) {
			logger.error("Invalid search for username '" + username, e);
		} finally {
			if (context != null) {
				context.close();
			}
		}
		return null;
	}

	private Properties getDomainProperties() {
		Properties properties = new Properties();

		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL, "LDAP://" + server + ":" + port);

		properties.put(Context.SECURITY_AUTHENTICATION, "simple");
		properties.put(Context.SECURITY_PRINCIPAL, "CN=NPS AD Reader,OU=_VARIE,OU=_SYSTEM,DC=nposistemi,DC=it");
		properties.put(Context.SECURITY_CREDENTIALS, "==BXZiD73Z4!KBK!2PP!");
		// properties.put(Context.SECURITY_PRINCIPAL, systemPrincipal);
		// properties.put(Context.SECURITY_CREDENTIALS, systemPassword);

		return properties;
	}

}