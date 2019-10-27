package com.se.project.models;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

@Embeddable
public class RateKey implements Serializable {
	@Column(name = "date")
	private LocalDate date;

	@Column(name = "createdTime")
	private Timestamp createdTime;

	protected RateKey(){}

	public RateKey(LocalDate date, Timestamp createdTime) {
		this.date = date;
		this.createdTime = createdTime;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}
}
