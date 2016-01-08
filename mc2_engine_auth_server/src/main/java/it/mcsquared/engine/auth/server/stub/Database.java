package it.mcsquared.engine.auth.server.stub;

import it.mcsquared.engine.Mc2Engine;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Database {
	private static final Logger logger = LoggerFactory.getLogger(Database.class);

	private static Map<String, String> clients = new HashMap<>();
	private static Set<String> authCodes = new HashSet<>();
	private static Map<String, Map<String, String>> users = new HashMap<>();
	private static Map<String, Map<String, String>> refreshTokens = new HashMap<>();

	private static File dbFile;

	public static void init(Mc2Engine engine) {
		try {
			dbFile = engine.getConfigurationManager().getConfigurationFile("oauth-database.json");
			if (!dbFile.exists()) {
				dbFile.createNewFile();
				dbFile.deleteOnExit();
			}
			load();
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public static void addUser(String clientId, String username, String password) {
		Map<String, String> clientUsers;
		synchronized (users) {
			clientUsers = users.get(clientId);
			if (clientUsers == null) {
				clientUsers = new HashMap<>();
				users.put(clientId, clientUsers);
			}
		}
		clientUsers.put(username, password);
		dump();
	}

	public static void addRefreshToken(String clientId, String username, String refreshToken) {
		Map<String, String> tokens;
		synchronized (refreshTokens) {
			tokens = refreshTokens.get(clientId);
			if (tokens == null) {
				tokens = new HashMap<>();
				refreshTokens.put(clientId, tokens);
			}
		}
		tokens.put(username, refreshToken);
		dump();
	}

	public static boolean authenticate(String clientId, String username, String password) {
		Map<String, String> clientUsers = users.get(clientId);
		if (clientUsers == null || clientUsers.isEmpty() || !clientUsers.containsKey(username)) {
			return false;
		}
		String pwd = clientUsers.get(username);
		if (pwd == null) {
			return false;
		}
		return password.equals(pwd);
	}

	public static void addClient(String clientId, String secret) {
		clients.put(clientId, secret);
		dump();
	}

	public static boolean isValidClient(String clientId) {
		return clients.containsKey(clientId);
	}

	public static boolean authenticateClient(String clientId, String secret) {
		return clients.get(clientId).equals(secret);
	}

	public static void addAuthCode(String authCode) {
		authCodes.add(authCode);
		dump();
	}

	public static boolean isValidAuthCode(String authCode) {
		return authCodes.contains(authCode);
	}

	private static void dump() {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("clients", clients);
			data.put("authCodes", authCodes);
			data.put("users", users);
			data.put("refreshTokens", refreshTokens);

			Gson gson = new Gson();
			String json = gson.toJson(data);
			FileUtils.write(dbFile, json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void load() {
		try {
			Gson gson = new Gson();
			String json = FileUtils.readFileToString(dbFile);

			Map<String, Object> data = gson.fromJson(json, Map.class);
			if (data != null) {
				Map<String, String> csclients = (Map<String, String>) data.get("clients");
				HashSet<String> acs = new HashSet<String>((Collection<? extends String>) data.get("authCodes"));
				Map<String, Map<String, String>> us = (Map<String, Map<String, String>>) data.get("users");
				Map<String, Map<String, String>> rts = (Map<String, Map<String, String>>) data.get("refreshTokens");
				if (csclients != null) {
					clients = csclients;
				}
				if (acs != null) {
					authCodes = acs;
				}
				if (us != null) {
					users = us;
				}
				if (rts != null) {
					refreshTokens = rts;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void clearAll() {
		try {
			Gson gson = new Gson();
			String json = FileUtils.readFileToString(dbFile);

			Map<String, Object> data = gson.fromJson(json, Map.class);
			if (data != null) {
				clients = (Map<String, String>) data.get("clients");
				authCodes = new HashSet<String>((Collection<? extends String>) data.get("authCodes"));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isValidRefreshToken(String clientId, String username, String refreshToken) {
		Map<String, String> tokens = refreshTokens.get(clientId);
		if (tokens == null || tokens.isEmpty() || !tokens.containsKey(username)) {
			return false;
		}
		String token = tokens.get(username);
		if (token == null) {
			return false;
		}
		return refreshToken.equals(token);
	}

	public static void revokeRefreshToken(String clientId, String username) {
		refreshTokens.get(clientId).remove(username);
	}
}