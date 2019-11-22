package com.epa.beans.SummaryData;

import org.springframework.stereotype.Component;

@Component
public class FacilityDataSummary {
	private String plantType;
	private String fuelType;
	private String coolingSystemType;
	private String waterType;
	private String waterSource;
	private String waterSourceName;
	private String WaterConsumptionSummary;
	private String EmissionSummary;
	private String GenerationSummary;
	private String WaterWithdrawalSummary;
	
	public FacilityDataSummary() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FacilityDataSummary(String waterConsumptionSummary,
			String emissionSummary, String generationSummary,
			String waterWithdrawalSummary) {
		super();
		WaterConsumptionSummary = waterConsumptionSummary;
		EmissionSummary = emissionSummary;
		GenerationSummary = generationSummary;
		WaterWithdrawalSummary = waterWithdrawalSummary;
	}

	public FacilityDataSummary(String plantType, String fuelType,
			String coolingSystemType, String waterType, String waterSource,
			String waterSourceName, String waterConsumptionSummary,
			String emissionSummary, String generationSummary,
			String waterWithdrawalSummary) {
		super();
		this.plantType = plantType;
		this.fuelType = fuelType;
		this.coolingSystemType = coolingSystemType;
		this.waterType = waterType;
		this.waterSource = waterSource;
		this.waterSourceName = waterSourceName;
		WaterConsumptionSummary = waterConsumptionSummary;
		EmissionSummary = emissionSummary;
		GenerationSummary = generationSummary;
		WaterWithdrawalSummary = waterWithdrawalSummary;
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

	public String getWaterConsumptionSummary() {
		return WaterConsumptionSummary;
	}

	public void setWaterConsumptionSummary(String waterConsumptionSummary) {
		WaterConsumptionSummary = waterConsumptionSummary;
	}

	public String getEmissionSummary() {
		return EmissionSummary;
	}

	public void setEmissionSummary(String emissionSummary) {
		EmissionSummary = emissionSummary;
	}

	public String getGenerationSummary() {
		return GenerationSummary;
	}

	public void setGenerationSummary(String generationSummary) {
		GenerationSummary = generationSummary;
	}

	public String getWaterWithdrawalSummary() {
		return WaterWithdrawalSummary;
	}

	public void setWaterWithdrawalSummary(String waterWithdrawalSummary) {
		WaterWithdrawalSummary = waterWithdrawalSummary;
	}

	@Override
	public String toString() {
		return "MonthlyDataSummary [plantType=" + plantType + ", fuelType="
				+ fuelType + ", coolingSystemType=" + coolingSystemType
				+ ", waterType=" + waterType + ", waterSource=" + waterSource
				+ ", waterSourceName=" + waterSourceName
				+ ", WaterConsumptionSummary=" + WaterConsumptionSummary
				+ ", EmissionSummary=" + EmissionSummary
				+ ", GenerationSummary=" + GenerationSummary
				+ ", WaterWithdrawalSummary=" + WaterWithdrawalSummary + "]";
	}
	
	
}
