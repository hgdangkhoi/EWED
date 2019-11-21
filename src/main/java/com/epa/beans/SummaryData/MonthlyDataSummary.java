package com.epa.beans.SummaryData;

import org.springframework.stereotype.Component;

@Component
public class MonthlyDataSummary {
	private String plantType;
	private String coolingSystemType;
	private String waterType;
	private String waterSource;
	private String waterSourceName;
	private String MonthlyWaterConsumption;
	private String MonthlyEmission;
	private String MonthlyGeneration;
	private String MonthlyWaterWithdrawal;
	public String getPlantType() {
		return plantType;
	}
	public void setPlantType(String plantType) {
		this.plantType = plantType;
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
	public String getMonthlyWaterConsumption() {
		return MonthlyWaterConsumption;
	}
	public void setMonthlyWaterConsumption(String monthlyWaterConsumption) {
		MonthlyWaterConsumption = monthlyWaterConsumption;
	}
	public String getMonthlyEmission() {
		return MonthlyEmission;
	}
	public void setMonthlyEmission(String monthlyEmission) {
		MonthlyEmission = monthlyEmission;
	}
	public String getMonthlyGeneration() {
		return MonthlyGeneration;
	}
	public void setMonthlyGeneration(String monthlyGeneration) {
		MonthlyGeneration = monthlyGeneration;
	}
	public String getMonthlyWaterWithdrawal() {
		return MonthlyWaterWithdrawal;
	}
	public void setMonthlyWaterWithdrawal(String monthlyWaterWithdrawal) {
		MonthlyWaterWithdrawal = monthlyWaterWithdrawal;
	}
	@Override
	public String toString() {
		return "MonthlyDataSummary [plantType=" + plantType
				+ ", coolingSystemType=" + coolingSystemType + ", waterType="
				+ waterType + ", waterSource=" + waterSource
				+ ", waterSourceName=" + waterSourceName
				+ ", MonthlyWaterConsumption=" + MonthlyWaterConsumption
				+ ", MonthlyEmission=" + MonthlyEmission
				+ ", MonthlyGeneration=" + MonthlyGeneration
				+ ", MonthlyWaterWithdrawal=" + MonthlyWaterWithdrawal + "]";
	}
	public MonthlyDataSummary(String plantType, String coolingSystemType,
			String waterType, String waterSource, String waterSourceName,
			String monthlyWaterConsumption, String monthlyEmission,
			String monthlyGeneration, String monthlyWaterWithdrawal) {
		super();
		this.plantType = plantType;
		this.coolingSystemType = coolingSystemType;
		this.waterType = waterType;
		this.waterSource = waterSource;
		this.waterSourceName = waterSourceName;
		MonthlyWaterConsumption = monthlyWaterConsumption;
		MonthlyEmission = monthlyEmission;
		MonthlyGeneration = monthlyGeneration;
		MonthlyWaterWithdrawal = monthlyWaterWithdrawal;
	}
	public MonthlyDataSummary() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
