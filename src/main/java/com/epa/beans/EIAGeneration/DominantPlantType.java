package com.epa.beans.EIAGeneration;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
 * Table dominantPlantType to pull date and get the dominant plant for each facility each year 
 * */
@Entity
@Table(name = "dominantPlantType")
public class DominantPlantType implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 878310634545025282L;

	@EmbeddedId
	CompositeKeyForDominantType compKey;
	
	@Column(name="dominantType")
	String dominantType;
	
	@Column(name="derived")
	int derived;
	
	public DominantPlantType() {
		
	}

	public DominantPlantType(CompositeKeyForDominantType compKey, String dominantType, int derived) {
		super();
		this.compKey = compKey;
		this.dominantType = dominantType;
		this.derived = derived;
	}

	public String getDominantType() {
		return dominantType;
	}

	public void setDominantType(String dominantType) {
		this.dominantType = dominantType;
	}

	public int getDerived() {
		return derived;
	}

	public void setDerived(int derived) {
		this.derived = derived;
	}

	@Override
	public String toString() {
		return "DominantPlantType [ dominantType=" + dominantType + "]";
	}
	
}