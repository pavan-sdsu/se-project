package com.se.project.models;

import javax.persistence.*;

@Entity
public class Staff {


	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	private User userId;

	@Id
	@GeneratedValue
	@Column(name = "password")
	private String password;

	public User getUserId() {
		return userId;
	}

	public void setUserId(User userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
