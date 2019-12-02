package com.epa.beans.EIAGeneration;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class maps to the table that holds the new predicted generation for 
 * case REF2019
 *
 */

@Entity
@Table(name = "p_genREF2019")
public class GenerationRef2019 {
	
	@Column(name = "genData")
	String genData;
	
	@Column(name = "units")
	String units;

	@EmbeddedId
	KeyItemsNewGeneration keyTimes;
	
	public KeyItemsNewGeneration getKeyTimes() {
		return keyTimes;
	}

	public void setKeyTimes(KeyItemsNewGeneration keyTimes) {
		this.keyTimes = keyTimes;
	}

	@Override
	public String toString() {
		return "GenerationRef2019 [genData=" + genData + ", units=" + units
				+ ", keyTimes=" + keyTimes + "]";
	}

	public String getGenData() {
		return genData;
	}

	public void setGenData(String genData) {
		this.genData = genData;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}
	
}
