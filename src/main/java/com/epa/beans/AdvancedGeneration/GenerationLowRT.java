package com.epa.beans.AdvancedGeneration;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.epa.beans.EIAGeneration.KeyItemsNewGeneration;

/**
 * This class maps to the table that holds the new predicted generation for 
 * case LOWRT
 *
 */

@Entity
@Table(name = "p_genLOWRT")
public class GenerationLowRT {
	
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
		return "GenerationLowRT [genData=" + genData + ", units=" + units + ", keyTimes=" + keyTimes + "]";
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
