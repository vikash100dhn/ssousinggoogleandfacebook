package com.company.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
public class HomeController {


	@Autowired
	GoogleAuthenticationRepository googleRepository;

	@Autowired
	FacebookAuthenticationRepository facebookRepository;


	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHelloWorld(Principal principal, OAuth2Authentication authentication) {
		
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

		System.out.println(authentication.toString());
		LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getUserAuthentication().getDetails();

		/*for (Map.Entry<String, String> entry : details.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("Key:"+key +"Value:"+value);
		} 
*/
		//if login is via google
		if(details.get("id").length() == 21)
		{
			GoogleUser googleUser = new GoogleUser();
			googleUser.setFirstname(details.get("given_name"));
			googleUser.setDisplay_name(details.get("name"));
			googleUser.setGoogle_id(details.get("id"));
			googleUser.setGoogle_img_url(details.get("picture"));
			googleUser.setGoogle_pic_link(details.get("link"));
			googleUser.setLastname(details.get("family_name"));
			googleUser.setUsername(details.get("email"));
			googleUser.setCreated(currentTimestamp);
			googleUser.setModified(currentTimestamp);

			googleRepository.save(googleUser);
		}
		//if login is via Facebook
		else
		{
			//System.out.println("pic"+ data.get("picture"));
	
			FacebookUser facebookUser = new FacebookUser();
			facebookUser.setDisplay_name(details.get("name"));
			facebookUser.setFacebook_id(details.get("id"));
			facebookUser.setUsername(details.get("email"));
			facebookUser.setFirstname(details.get("first_name"));
			facebookUser.setLastname(details.get("last_name"));
			
			facebookUser.setCreated(currentTimestamp);
			facebookUser.setModified(currentTimestamp);
			
			Object picture = details.get("picture");
			//System.out.println(picture.toString()+" "+picture.getClass());
			
			ObjectMapper mapper = new ObjectMapper();
			Map<String, HashMap<String,String>> map = mapper.convertValue(picture, Map.class);
			//System.out.println(map);
			Map<String, String> data = new HashMap<>();
			for(Entry<String, HashMap<String,String>> entry: map.entrySet())
			{
				data= entry.getValue();
			}
			facebookUser.setFacebook_img_url(data.get("url"));
			facebookUser.setFacebook_pic_link(data.get("url"));
			facebookRepository.save(facebookUser);
			//System.out.println("Picutre details: "+details.get("picture"));
		}
		return "Login Successful.";
	}

	public static StringBuffer getOutputJson(String address, String method, String request) throws IOException {
		//StreamLambdaHandler.logger.log("getOutputJson address: " + address);
		URL url = new URL(address);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/json");
		OutputStream outputStream = null;
		if (method.equals("POST")) {
			outputStream = connection.getOutputStream();
			outputStream.write(request.getBytes());
			outputStream.flush();
		}
		//	StreamLambdaHandler.logger.log("outputStream: " + outputStream);
		if (connection.getResponseCode() != 200 && connection.getResponseCode() != 201) {
			//	StreamLambdaHandler.logger.log(ServiceCallConstants.ERR_LOGGER + connection.getResponseCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String inputLine;
		StringBuffer outputJson = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			outputJson.append(inputLine);
		}
		connection.disconnect();
		if (method.equals("POST")) {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		br.close();
		return outputJson;
	}
	
}
