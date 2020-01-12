package com.epa.beans.SummaryData;

import org.springframework.stereotype.Component;

/**
 * 
 * Struct to store MonthWiseSummary data
 *
 */
@Component
public class MonthWiseSummary {
	private int genYear;
	private int genMonth;
	private String generation;
	private String emission;
	private String waterConsumption;
	private String waterWithdrawal;
	private String waterAvailability;
	public int getGenYear() {
		return genYear;
	}
	public String getWaterAvailability() {
		return waterAvailability;
	}
	public void setWaterAvailability(String waterAvailability) {
		this.waterAvailability = waterAvailability;
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
	public String getGeneration() {
		return generation;
	}
	public void setGeneration(String generation) {
		this.generation = generation;
	}
	public String getEmission() {
		return emission;
	}
	public void setEmission(String emission) {
		this.emission = emission;
	}
	public String getWaterConsumption() {
		return waterConsumption;
	}
	public void setWaterConsumption(String waterConsumption) {
		this.waterConsumption = waterConsumption;
	}
	public String getWaterWithdrawal() {
		return waterWithdrawal;
	}
	public void setWaterWithdrawal(String waterWithdrawal) {
		this.waterWithdrawal = waterWithdrawal;
	}
	public MonthWiseSummary(int genYear, int genMonth, String generation,
			String emission, String waterConsumption, String waterWithdrawal) {
		super();
		this.genYear = genYear;
		this.genMonth = genMonth;
		this.generation = generation;
		this.emission = emission;
		this.waterConsumption = waterConsumption;
		this.waterWithdrawal = waterWithdrawal;
	}
	public MonthWiseSummary(int genYear, int genMonth, String generation,
			String emission, String waterConsumption, String waterWithdrawal,
			String waterAvailability) {
		super();
		this.genYear = genYear;
		this.genMonth = genMonth;
		this.generation = generation;
		this.emission = emission;
		this.waterConsumption = waterConsumption;
		this.waterWithdrawal = waterWithdrawal;
		this.waterAvailability = waterAvailability;
	}
	@Override
	public String toString() {
		return "MonthWiseSummary [genYear=" + genYear + ", genMonth=" + genMonth
				+ ", generation=" + generation + ", emission=" + emission
				+ ", waterConsumption=" + waterConsumption
				+ ", waterWithdrawal=" + waterWithdrawal + "]";
	}
	public MonthWiseSummary() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
