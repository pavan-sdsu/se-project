package com.se.project.models;


import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "rate")
public class Rate {

	@EmbeddedId
	private RateKey key;

	@Column(name = "baseRate")
	private int baseRate;

	@Column(name = "createdBy")
	private int createdBy;

	protected Rate() {}

	public Rate(RateKey key, int baseRate, int createdBy) {
		this.key = key;
		this.baseRate = baseRate;
		this.createdBy = createdBy;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public RateKey getKey() {
		return key;
	}

	public void setKey(RateKey key) {
		this.key = key;
	}

	public int getBaseRate() {
		return baseRate;
	}

	public void setBaseRate(int baseRate) {
		this.baseRate = baseRate;
	}



}
