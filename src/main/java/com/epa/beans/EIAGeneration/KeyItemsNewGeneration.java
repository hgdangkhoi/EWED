package com.epa.beans.EIAGeneration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The composite key for the table of the advanced system Generation consists 
 * of the following elements
 * 
 */
@Embeddable
public class KeyItemsNewGeneration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6956150923327017509L;

	@Column(name = "fuelMover")
	String fuelMover;

	@Column(name = "emmCode")
	String emmCode;
	
	@Column(name = "genYear")
	String genYear;

	public String getFuelMover() {
		return fuelMover;
	}

	public void setFuelMover(String fuelMover) {
		this.fuelMover = fuelMover;
	}

	public String getEmmCode() {
		return emmCode;
	}

	public void setEmmCode(String emmCode) {
		this.emmCode = emmCode;
	}

	public String getGenYear() {
		return genYear;
	}

	public void setGenYear(String genYear) {
		this.genYear = genYear;
	}

	@Override
	public String toString() {
		return "KeyItemsNewGeneration [fuelMover=" + fuelMover + ", emmCode="
				+ emmCode + ", genYear=" + genYear + "]";
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		KeyItemsNewGeneration key = (KeyItemsNewGeneration) obj;
		return (this.getFuelMover().equals(key.getFuelMover()) &&
				this.getEmmCode().equals(key.getEmmCode()) &&
				this.getGenYear().equals(key.getGenYear())) ;
	}
	
	
}
