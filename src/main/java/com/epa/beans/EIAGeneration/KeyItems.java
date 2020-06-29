package com.epa.beans.EIAGeneration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The composite key for the table Generation consists 
 * of the following elements
 *
 */
@Embeddable
public class KeyItems implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3324231350230064568L;

	@Column(name = "plantCode")
	int plantCode;
	
	@Column(name = "genYear")
	int genYear;
	
	@Column(name = "genMonth")
	int genMonth;
	
	public KeyItems() {
		// TODO Auto-generated constructor stub
	}
	
	public int getPlantCode() {
		return plantCode;
	}

	public void setPlantCode(int plantCode) {
		this.plantCode = plantCode;
	}
	
	public int getGenYear() {
		return genYear;
	}

	public void setGenYear(int genYear) {
		this.genYear = genYear;
	}

	public int getGenMonth() {
		return genMonth;
	}

	public void setGenMonth(int genMonth) {
		this.genMonth = genMonth;
	}

	@Override
	public String toString() {
		return "KeyItems [plantCode=" + plantCode + ", genYear=" + genYear + ", genMonth=" + genMonth + "]";
	}

	
	
	
	
}
