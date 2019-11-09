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
		Algorithm algorithm = Algorithm.HMAC256("secret");

		Map<String, String> headers = Collections
				.list(request.getHeaderNames())
				.stream()
				.collect(Collectors.toMap(h -> h, request::getHeader));

		String auth = headers.get("authorization");
		if (auth == null) {
			response.sendError(532, "No token found");
			return false;
		}

		String token = auth.split(" ")[1];

		DecodedJWT decoded = null;
		JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();

		try {
			decoded = verifier.verify(token);
		} catch (JWTVerificationException exception){
			response.sendError(532, "Invalid login token");
			return false;
		}

		return true;

	}


}