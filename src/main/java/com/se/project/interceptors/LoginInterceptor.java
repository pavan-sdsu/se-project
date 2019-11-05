package com.se.project.interceptors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Map<String, String > map = new HashMap<>();
		map.put("email", "pavan1645@gmail.com");
		map.put("password", "pass");

		String encoded="";
		Algorithm algorithm = Algorithm.HMAC256("secret");
		try {
			encoded = JWT.create()
					.withIssuer("auth0")
					.withClaim("data", new ObjectMapper().writeValueAsString(map))
					.sign(algorithm);

		} catch (JWTCreationException exception){
			System.out.println("Invalid Signing configuration / Couldn't convert Claims.");
		}

		Map<String, String> headers = Collections
				.list(request.getHeaderNames())
				.stream()
				.collect(Collectors.toMap(h -> h, request::getHeader));

		String token = headers.get("authorization").split(" ")[1];

		DecodedJWT decoded = null;
		try {
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer("auth0")
					.build();
			decoded = verifier.verify(token);
		} catch (JWTVerificationException exception){
			System.out.println("Invalid signature/claims");
		}


		Claim data = decoded.getClaim("data");
		Map<String, Object> res = new ObjectMapper().readValue(data.asString(), HashMap.class);

		System.out.println(res);

		return true;

	}


}