package com.epa.beans;

import org.springframework.stereotype.Component;

@Component
public class EWEDMonthlyData {
	
	private int year;
	private int month;
	private String plantType;
	private String fuelType;
	private String coolingSystemType;
	private String waterType;
	private String waterSource;
	private String waterSourceName;
	private String generation;
	private String emissions;
	private String waterWithdrawal;
	private String waterConsumption;
	
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getPlantType() {
		return plantType;
	}

	public void setPlantType(String plantType) {
		this.plantType = plantType;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}

	public String getCoolingSystemType() {
		return coolingSystemType;
	}

	public void setCoolingSystemType(String coolingSystemType) {
		this.coolingSystemType = coolingSystemType;
	}

	public String getWaterType() {
		return waterType;
	}

	public void setWaterType(String waterType) {
		this.waterType = waterType;
	}

	public String getWaterSource() {
		return waterSource;
	}

	public void setWaterSource(String waterSource) {
		this.waterSource = waterSource;
	}

	public String getWaterSourceName() {
		return waterSourceName;
	}

	public void setWaterSourceName(String waterSourceName) {
		this.waterSourceName = waterSourceName;
	}

	public String getGeneration() {
		return generation;
	}

	public void setGeneration(String generation) {
		this.generation = generation;
	}

	public String getEmissions() {
		return emissions;
	}

	public void setEmissions(String emissions) {
		this.emissions = emissions;
	}

	public String getWaterWithdrawal() {
		return waterWithdrawal;
	}

	public void setWaterWithdrawal(String waterWithdrawal) {
		this.waterWithdrawal = waterWithdrawal;
	}

	public String getWaterConsumption() {
		return waterConsumption;
	}

	public void setWaterConsumption(String waterConsumption) {
		this.waterConsumption = waterConsumption;
	}

	@Override
	public String toString() {
		return "EWEDMonthlyData [year=" + year + ", month=" + month + ", plantType=" + plantType
				+ ", coolingSystemType=" + coolingSystemType + ", waterType=" + waterType + ", waterSource="
				+ waterSource + ", waterSourceName=" + waterSourceName + ", generation=" + generation + ", emissions="
				+ emissions + ", waterWithdrawal=" + waterWithdrawal + ", waterConsumption=" + waterConsumption + "]";
	}

	public EWEDMonthlyData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EWEDMonthlyData(int year, int month, String plantType, String fuelType, String coolingSystemType, String waterType, String waterSource, String waterSourceName, String generation,
			String emissions, String waterWithdrawal, String waterConsumption) {
		super();
		this.year = year;
		this.month = month;
		this.plantType = plantType;
		this.fuelType = fuelType;
		this.coolingSystemType = coolingSystemType;
		this.waterType = waterType;
		this.waterSource = waterSource;
		this.waterSourceName = waterSourceName;
		this.generation = generation;
		this.emissions = emissions;
		this.waterWithdrawal = waterWithdrawal;
		this.waterConsumption = waterConsumption;
	}
	
}
