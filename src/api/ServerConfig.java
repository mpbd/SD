package api;

public class ServerConfig
{
	private String apiKey;
	private String apuSecret;
	private String token;
	private String tokenSecret;

	public ServerConfig(String apiKey, String apuSecret, String token, String tokenSecret) {
		super();
		this.apiKey = apiKey;
		this.apuSecret = apuSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getApuSecret() {
		return apuSecret;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}
	
}
