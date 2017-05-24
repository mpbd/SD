package rest.twitter;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Exemplo de acesso ao servico Twitter.
 * <p>
 * O URL base para programadores esta disponivel em: <br>
 * https://dev.twitter.com/
 * <p>
 * A API REST do sistema esta disponivel em: <br>
 * https://dev.twitter.com/rest/public
 * <p>
 * Para poder aceder ao servico Twitter, deve criar uma app em:
 * https://apps.twitter.com/ onde obtera a apiKey e a apiSecret a usar na
 * criacao do objecto OAuthService. Deve use a opcao: OAuth 2 authorization
 * without a callback URL
 * <p>
 * Este exemplo usa a biblioteca OAuth Scribe, disponivel em:
 * https://github.com/scribejava/scribejava A pagina tem informacao da
 * dependencia Maven que deves adicionar ao teu projeto.
 * <p>
 * e a biblioteca json-simple, disponivel em:
 * http://code.google.com/p/json-simple/ A dependencia Maven esta disponivel em:
 * https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple
 * <p>
 * e a biblioteca apache commons codec, disponivel em:
 * http://commons.apache.org/proper/commons-codec/
 */
public abstract class Twitter
{
	public static void main(String... args) {
		try {
			String name = "novalincs";
			if (args.length > 0)
				name = args[0];

			// Substituir pela API key atribuida
			final String apiKey = "vPcWKiqk3TSzF1CHWA3m1wWHh";
			// Substituir pelo API secret atribuido
			final String apiSecret = "Zpn8RdaRdu0m4cFO3AiNMXHI9nMI6QK1PFMJag1auQql09PDkH";

			final OAuth10aService service = new ServiceBuilder().apiKey(apiKey).apiSecret(apiSecret)
					.build(TwitterApi.instance());
			final Scanner in = new Scanner(System.in);

			final OAuth1RequestToken requestToken = service.getRequestToken();

			// Obtain the Authorization URL
			System.out.println("A obter o Authorization URL...");
			final String authorizationUrl = service.getAuthorizationUrl(requestToken);
			System.out.println("Necessario dar permissao neste URL:");
			System.out.println(authorizationUrl);
			System.out.println("e copiar o codigo obtido para aqui:");
			System.out.print(">>");
			final String code = in.nextLine();

			// Trade the Request Token and Verifier for the Access Token
			System.out.println("A obter o Access Token!");
			final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, code);

			// Ready to execute operations
			System.out.println("Agora vamos aceder aos followers dum utilizador...");
			OAuthRequest followersReq = new OAuthRequest(Verb.GET,
					"https://api.twitter.com/1.1/followers/list.json?count=100&screen_name="
							+ URLEncoder.encode(name, "UTF-8"));
			service.signRequest(accessToken, followersReq);
			final Response followersRes = service.execute(followersReq);
			System.err.println("REST code:" + followersRes.getCode());
			if (followersRes.getCode() != 200)
				return;
			// System.err.println("REST reply:" + followersRes.getBody());

			JSONParser parser = new JSONParser();
			JSONObject res = (JSONObject) parser.parse(followersRes.getBody());

			JSONArray users = (JSONArray) res.get("users");
			int count = 0;
			for (Object user : users) {
				System.out.println("" + (++count) + " > " + ((JSONObject) user).get("name"));
			}

		} catch (InterruptedException | ExecutionException | IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
