package com.epa.beans.SummaryData;

import org.springframework.stereotype.Component;

/**
 * 
 * Struct to store TotalSummary data
 *
 */
@Component
public class TotalSummary {
	private String TotalGeneration;
	private String TotalEmission;
	private String TotalWaterConsumption;
	private String TotalWaterWithdrawal;
	public String getTotalGeneration() {
		return TotalGeneration;
	}
	public void setTotalGeneration(String totalGeneration) {
		TotalGeneration = totalGeneration;
	}
	public String getTotalEmission() {
		return TotalEmission;
	}
	public void setTotalEmission(String totalEmission) {
		TotalEmission = totalEmission;
	}
	public String getTotalWaterConsumption() {
		return TotalWaterConsumption;
	}
	public void setTotalWaterConsumption(String totalWaterConsumption) {
		TotalWaterConsumption = totalWaterConsumption;
	}
	public String getTotalWaterWithdrawal() {
		return TotalWaterWithdrawal;
	}
	public void setTotalWaterWithdrawal(String totalWaterWithdrawal) {
		TotalWaterWithdrawal = totalWaterWithdrawal;
	}
	@Override
	public String toString() {
		return "TotalSummary [TotalGeneration=" + TotalGeneration
				+ ", TotalEmission=" + TotalEmission
				+ ", TotalWaterConsumption=" + TotalWaterConsumption
				+ ", TotalWaterWithdrawal=" + TotalWaterWithdrawal + "]";
	}
	public TotalSummary(String totalGeneration, String totalEmission,
			String totalWaterConsumption, String totalWaterWithdrawal) {
		super();
		TotalGeneration = totalGeneration;
		TotalEmission = totalEmission;
		TotalWaterConsumption = totalWaterConsumption;
		TotalWaterWithdrawal = totalWaterWithdrawal;
	}
	public TotalSummary() {
		super();
		// TODO Auto-generated constructor stub
	}
}
