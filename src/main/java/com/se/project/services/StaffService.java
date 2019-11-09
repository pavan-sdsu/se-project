package com.se.project.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.project.models.Response;
import com.se.project.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class StaffService {

	@PersistenceContext
	EntityManager entityManager;

	@GetMapping("/")
	public String index() { return "Hello World"; }

	@PostMapping("/login")
	public Response login(@RequestBody HashMap creds) throws NoSuchAlgorithmException {
		Response res = new Response();

		String email = (String) creds.get("email");
		String pass = (String) creds.get("password");

		/* Creating password hash */
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(StandardCharsets.UTF_8.encode(pass));
		pass = String.format("%032x", new BigInteger(1, md5.digest()));
		/* Creating password hash */

		String[] cols = {"u.userId", "u.firstName", "u.lastName", "u.email", "u.phoneNumber", "s.role"};

		List result = entityManager.createNativeQuery("select " + colsToString(cols) + " from user u inner join staff s on u.userId = s.userId where u.email='" + email + "' and s.password = '" + pass + "'").getResultList();

		Map<String, String> data = new HashMap<>();

		if (result.size() == 0) {
			res.setSuccess(0);
			data.put("message", "Invalid credentials.");
			res.setData(data);
			return res;
		}

		Object[] o = (Object[]) result.get(0);
		for (int i = 0; i < o.length; i++) data.put(cols[i].split("\\.")[1], String.valueOf(o[i]));

		/* Creating JWT */
		Algorithm algorithm = Algorithm.HMAC256("secret");

		String encoded="";
		try {
			encoded = JWT.create()
					.withIssuer("auth0")
					.withClaim("data", new ObjectMapper().writeValueAsString(data))
					.sign(algorithm);
		} catch (Exception exception){
			res.setData("Error creating token");
			return res;
		}
		/* Creating JWT */

		data.put("message", "Logged in successfully.");
		data.put("token", encoded);

		res.setSuccess(1);
		res.setData(data);
		return res;
	}

	/* Utility methods */
	private String colsToString(String[] cols) {
		return Arrays.toString(cols).replaceAll("\\[|\\]", "");
	}
}
