package com.se.project.services;

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
import java.util.HashMap;
import java.util.List;

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

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(StandardCharsets.UTF_8.encode(pass));
		pass = String.format("%032x", new BigInteger(1, md5.digest()));


		List result = entityManager.createNativeQuery("select s.* from user u inner join staff s on u.userId = s.userId where u.email=?1 and s.password = ?2")
				.setParameter(1, email)
				.setParameter(2, pass)
				.getResultList();

		if (result.size() == 0) {
			res.setSuccess(0);
			res.setData("Invalid credentials.");
		} else {
			res.setSuccess(1);
			res.setData("Logged in successfully.");
		}

		return res;
	}

}
