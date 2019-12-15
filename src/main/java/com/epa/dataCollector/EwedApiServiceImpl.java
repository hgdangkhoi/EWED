package com.epa.dataCollector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.*;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epa.beans.EWEDMonthlyData;
import com.epa.beans.EWEDataReturn;
import com.epa.beans.Facility.Facility;
import com.epa.beans.Facility.FacilityInfo;
import com.epa.beans.Huc.HucCodeToName;
import com.epa.beans.SummaryData.MonthWiseSummary;
import com.epa.beans.SummaryData.FacilityDataSummary;
import com.epa.beans.SummaryData.FacilityWithSummaryData;
import com.epa.beans.SummaryData.TotalSummary;
import com.epa.beans.WaterUsage.WaterAvailability;
import com.epa.util.EPAConstants;
import com.epa.util.HibernateUtil;
import com.epa.views.DefaultOutputJson;
import com.epa.views.DefaultOutputJson_custom;
import com.epa.views.GenEmWaterView;
import com.epa.views.Top4;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

@Service
public class EwedApiServiceImpl implements EwedApiService {

	// Object mapper is used to convert objects to jsons
	@Autowired
	ObjectMapper mapper;
	public Session session;
	NumberFormat formatter = new DecimalFormat("########");

	@Override
	public String getFacility(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {

		// query facility
		List<Facility> facList = listOfFacilitiesWithinFilter(filterField, filterValue);
		List<String> plantCodes = new ArrayList<String>();
		for (Facility fac : facList) {
			plantCodes.add(fac.getPgmSysId());
		}

		// query monthly data
		List<EWEDMonthlyData> monthlyDataList = queryMonthlyData(plantCodes, minYear, minMonth, maxYear, maxMonth);

		List<FacilityDataSummary> facilityDataSummaryList = new ArrayList<FacilityDataSummary>();
		facilityDataSummaryList = queryFacilityDataSummary(filterField, filterValue, minYear, minMonth, maxYear, maxMonth);

		LinkedHashMap<Object, Object> facilityReturnMap = new LinkedHashMap<Object, Object>();
		facilityDataSummaryList.get(0).setEmissionSummary(facilityDataSummaryList.get(0).getEmissionSummary());
		facilityDataSummaryList.get(0).setGenerationSummary(facilityDataSummaryList.get(0).getGenerationSummary());
		facilityDataSummaryList.get(0).setWaterConsumptionSummary(facilityDataSummaryList.get(0).getWaterConsumptionSummary());
		facilityDataSummaryList.get(0).setWaterWithdrawalSummary(facilityDataSummaryList.get(0).getWaterWithdrawalSummary());
		facilityReturnMap.put("Facility", facList);
		facilityReturnMap.put("MonthlyData", monthlyDataList);
		facilityReturnMap.put("FacilityDataSummary", facilityDataSummaryList);

		// Convert Map to JSON
		try {
			return mapper.writeValueAsString(facilityReturnMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return "{\"Result\": \"Data not found\"}";
		// return "";
	}

	@Transactional(readOnly = true)
	public List<FacilityDataSummary> queryFacilityDataSummary(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<FacilityDataSummary> monthlyDataSummaryList = new ArrayList<FacilityDataSummary>();

		gewQueryBuilder.append(
				"WITH total AS ( SELECT LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as WaterConsumptionSummary, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as EmissionSummary, LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as GenerationSummary, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as WaterWithdrawalSummary from facGenEmWaterView_new where plantCode")
				.append(" = \'").append(filterValue).append("\' AND ")
				.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))))").append(",")
				.append("recent AS (SELECT TOP 1 plantCode, plantType, fuelType, coolingSystemType, waterType, waterSource, waterSourceName from facGenEmWaterView_new WHERE plantCode").append(" = \'")
				.append(filterValue).append("\' AND ")
				.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
				.append(" ORDER BY genYear, genMonth desc ) ")
				.append("SELECT recent.plantType, recent.fuelType, recent.coolingSystemType, recent.waterType, recent.waterSource, recent.waterSourceName, total.WaterConsumptionSummary, total.EmissionSummary, total.GenerationSummary, total.WaterWithdrawalSummary from recent, total");

		try {
			session.beginTransaction();
			Query query = session.createSQLQuery(gewQueryBuilder.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);

			query.setResultTransformer(Transformers.aliasToBean(FacilityDataSummary.class));
			List<FacilityDataSummary> temp_monthlyDataList = query.list();
			monthlyDataSummaryList.addAll(temp_monthlyDataList);

		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return monthlyDataSummaryList;
	}

	public String getEWEData(List<String> registryIds, int minYear, int maxYear) {
		EWEDataReturn returnData = new EWEDataReturn();

		returnData.participatingFacilities = registryIds.size();

		Query query = session.createQuery("SELECT sum(genSum) from GenerationPerRegistryIdView g" + " where g.genViewKey.registryId in (:ids) and genYear between :minYear and :maxYear");

		query.setParameterList("ids", registryIds);
		query.setParameter("minYear", minYear);
		query.setParameter("maxYear", maxYear);
		List<String> listResult = query.list();

		returnData.generation = listResult.get(0);

		query = session.createQuery("SELECT sum(emissionAmount) from EmissionsMonthly e" + " where e.emissionsMonthlyKey.registryId in (:ids) and emYear between :minYear and :maxYear");

		query.setParameterList("ids", registryIds);
		query.setParameter("minYear", minYear);
		query.setParameter("maxYear", maxYear);
		listResult.clear();
		listResult = query.list();

		returnData.emission = listResult.get(0);

		query = session.createQuery("SELECT sum(usageSum) from WaterUsagePerRegView w" + " where w.waterViewKey.registryId in (:ids) and usageYear between :minYear and :maxYear");

		query.setParameterList("ids", registryIds);
		query.setParameter("minYear", minYear);
		query.setParameter("maxYear", maxYear);
		listResult.clear();
		listResult = query.list();

		returnData.waterUsage = listResult.get(0);

		try {
			return mapper.writeValueAsString(returnData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return EPAConstants.genericErrorReturn;
	}

	@Override
	public String getFacilityData(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {

		// get the facilities
		List<FacilityWithSummaryData> facList = queryFacilityWithFuelType(filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		// get monthWise summary
		List<MonthWiseSummary> monthWiseSummaryList = new ArrayList<MonthWiseSummary>();
		monthWiseSummaryList = queryMonthWiseSummary(filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		// get totalSummary
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		List<String> hucCodes = new ArrayList<String>();
		List<WaterAvailability> waterAvailabilityList = new ArrayList<WaterAvailability>();

		// if filter by state, huc8code or huc8name, then add waterAvailable
		if (filterField.equals("stateName") || filterField.equals("huc8Code")) {
			hucCodes = getAllHUCCodes(filterField, filterValue, minYear, minMonth, maxYear, maxMonth);
			waterAvailabilityList = getWaterAvailabilityDataList(hucCodes, minYear, minMonth, maxYear, maxMonth);
		} else if (filterField.equals("HUC8Name")) { // convert HUC8Name into
														// HUC8Code
			hucCodes = getHUCCodesFromName(filterField, filterValue);
			waterAvailabilityList = getWaterAvailabilityDataList(hucCodes, minYear, minMonth, maxYear, maxMonth);
		}

		// Stores the month wise summary of all facilities in the given range of
		// year
		HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>> facMonthWiseData = new HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>>();
		HashMap<Integer, DefaultOutputJson_custom> genEmWaterPerMonth = new HashMap<Integer, DefaultOutputJson_custom>();
		for (MonthWiseSummary monthWise : monthWiseSummaryList) {
			DefaultOutputJson_custom customObj = new DefaultOutputJson_custom();

			customObj.setGeneration(monthWise.getGeneration());
			customObj.setEmission(monthWise.getEmission());
			customObj.setWaterConsumption(monthWise.getWaterConsumption());
			customObj.setWaterWithdrawal(monthWise.getWaterWithdrawal());

			if (facMonthWiseData.containsKey(monthWise.getGenYear())) {
				genEmWaterPerMonth = facMonthWiseData.get(monthWise.getGenYear());
			}

			genEmWaterPerMonth.put(monthWise.getGenMonth(), customObj);
			facMonthWiseData.put(monthWise.getGenYear(), genEmWaterPerMonth);
		}

		// add water available, if applicable
		if (filterField.equals("stateName") || filterField.equals("huc8Code") || filterField.equals("HUC8Name")) {
			for (WaterAvailability waData : waterAvailabilityList) {

				HashMap<Integer, DefaultOutputJson_custom> waterAvailable = new HashMap<Integer, DefaultOutputJson_custom>();

				if (facMonthWiseData.containsKey(waData.getYear())) {
					waterAvailable = facMonthWiseData.get(waData.getYear());
					DefaultOutputJson_custom customObj = new DefaultOutputJson_custom();
					if (waterAvailable.containsKey(waData.getMonth())) {
						customObj = waterAvailable.get(waData.getMonth());
						double tempWA = customObj.getWaterAvailability() != null ? Double.parseDouble(customObj.waterAvailability) : 0;
						double newWA = waData.getWaterAvailable() != null ? Double.parseDouble(waData.getWaterAvailable()) : 0;
						customObj.waterAvailability = (formatter.format(tempWA + newWA));
						waterAvailable.put(waData.getMonth(), customObj);
						facMonthWiseData.put(waData.getYear(), waterAvailable);
					} else { // month not present meaning there is no
								// generation, emission, etc
						double newWA = waData.getWaterAvailable() != null ? Double.parseDouble(waData.getWaterAvailable()) : 0;
						customObj.setWaterAvailability(formatter.format(newWA));
						customObj.setGeneration("0");
						customObj.setEmission("0");
						customObj.setWaterConsumption("0");
						customObj.setWaterWithdrawal("0");
						waterAvailable.put(waData.getMonth(), customObj);
						facMonthWiseData.put(waData.getYear(), waterAvailable);
					}
				} else {
					DefaultOutputJson_custom customObj = new DefaultOutputJson_custom();
					double newWA = waData.getWaterAvailable() != null ? Double.parseDouble(waData.getWaterAvailable()) : 0;
					customObj.waterAvailability = (formatter.format(newWA));
					customObj.setGeneration("0");
					customObj.setEmission("0");
					customObj.setWaterConsumption("0");
					customObj.setWaterWithdrawal("0");
					waterAvailable.put(waData.getMonth(), customObj);
					facMonthWiseData.put(waData.getYear(), waterAvailable);
				}
			}
		}
		// Stores the complete structure
		Map<String, Object> completeGenEmWaterOutput = new HashMap<String, Object>();
		completeGenEmWaterOutput.put("Summary", totalSummaryList);
		completeGenEmWaterOutput.put("MonthWiseSummary", facMonthWiseData);
		completeGenEmWaterOutput.put("All Facilities", facList);
		// Convert Map to JSON
		try {
			return mapper.writeValueAsString(completeGenEmWaterOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return "{\"Result\": \"Data not found\"}";
	}

	@Transactional(readOnly = true)
	public List<TotalSummary> queryTotalSummary(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType, String[] fuelTypeList) {		
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();

		if (fuelTypeList[0].equals("all")) {
			if (filterValue.equals("")) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView g where ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ");
			} else {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ");
			}
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			if (filterValue.equals("")) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView g where ").append("fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ");
			} else {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (")
						.append(fuelTypeListString).append(") AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ");
			}
		}

		try {
			session.beginTransaction();
			Query query = session.createQuery(gewQueryBuilder.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			// System.out.println(query);

			List<TotalSummary> temp_totalSummaryList = query.list();
			totalSummaryList.addAll(temp_totalSummaryList);			
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		return totalSummaryList;
	}

	@Transactional(readOnly = true)
	public List<MonthWiseSummary> queryMonthWiseSummary(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType, String[] fuelTypeList) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<MonthWiseSummary> monthWiseSummaryList = new ArrayList<MonthWiseSummary>();

		if (fuelTypeList[0].equals("all")) {
			gewQueryBuilder.append(
					"SELECT new com.epa.beans.SummaryData.MonthWiseSummary (g.genYear, g.genMonth, TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as emissions, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as watherWithdrawal) ")
					.append("from GenEmWaterView g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("group by genYear, genMonth ").append("order by genYear, genMonth");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			gewQueryBuilder.append(
					"SELECT new com.epa.beans.SummaryData.MonthWiseSummary (g.genYear, g.genMonth, TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as emissions, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as watherWithdrawal) ")
					.append("from GenEmWaterView g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString)
					.append(") AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("group by genYear, genMonth ").append("order by genYear, genMonth");
		}

		try {
			session.beginTransaction();
			Query query = session.createQuery(gewQueryBuilder.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			// System.out.println(query);

			List<MonthWiseSummary> temp_monthWiseList = query.list();
			monthWiseSummaryList.addAll(temp_monthWiseList);
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		return monthWiseSummaryList;
	}

	@Override
	public String getAllFacilities(String filterField, String filterValue) {

		try {
			return mapper.writeValueAsString(listOfFacilitiesWithinFilter(filterField, filterValue));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "{\"Result\": \"Data not found\"}";

	}

	@Transactional(readOnly = true)
	public List<FacilityWithSummaryData> queryFacilityWithFuelType(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType,
			String[] fuelTypeList) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		List<FacilityWithSummaryData> facList = new ArrayList<FacilityWithSummaryData>();

		if (fuelTypeList[0].equals("all")) {
			facilityQuery.append(
					"SELECT new com.epa.beans.SummaryData.FacilityWithSummaryData (g.plantCode as pgmSysId, g.primaryName, TRIM(str(g.naicsCode)) as naicsCode, TRIM(str(g.registryId, 17)) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, TRIM(str(g.latitude, 17, 3)) as latitude, TRIM(str(g.longitude, 17, 3)) as longitude, TRIM(str(g.GEOID)) as geoid, g.CountyState1, g.CountyState2, TRIM(str(g.HUC8Code)) as HUC8Code, g.HUC8Name, TRIM(str(g.HUC8Acres)) as HUC8Acres, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as WaterConsumptionSummary, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as EmissionSummary, TRIM(str(ISNULL(SUM(generation), 0), 17)) as GenerationSummary, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as WaterWithdrawalSummary ) ")
					.append("from GenEmWaterView g where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append("GROUP BY g.plantCode, primaryName, naicsCode, registryId, facAddr, cityName, stateName, postalCode, latitude, longitude, GEOID, CountyState1, CountyState2, HUC8Code, HUC8Name, HUC8Acres");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			facilityQuery.append(
					"SELECT new com.epa.beans.SummaryData.FacilityWithSummaryData (g.plantCode as pgmSysId, g.primaryName, TRIM(str(g.naicsCode)) as naicsCode, TRIM(str(g.registryId, 17)) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, TRIM(str(g.latitude, 17, 3)) as latitude, TRIM(str(g.longitude, 17, 3)) as longitude, TRIM(str(g.GEOID)) as geoid, g.CountyState1, g.CountyState2, TRIM(str(g.HUC8Code)) as HUC8Code, g.HUC8Name, TRIM(str(g.HUC8Acres)) as HUC8Acres, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as WaterConsumptionSummary, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as EmissionSummary, TRIM(str(ISNULL(SUM(generation), 0), 17)) as GenerationSummary, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as WaterWithdrawalSummary ) ")
					.append("from GenEmWaterView g where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString)
					.append(") AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append("GROUP BY g.plantCode, primaryName, naicsCode, registryId, facAddr, cityName, stateName, postalCode, latitude, longitude, GEOID, CountyState1, CountyState2, HUC8Code, HUC8Name, HUC8Acres");
		}

		try {
			session.beginTransaction();
			Query query = session.createQuery(facilityQuery.toString());
			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);

			List<FacilityWithSummaryData> temp_facList = query.list();
			facList.addAll(temp_facList);
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		return facList;
	}

	@Transactional(readOnly = true)
	public List<Facility> listOfFacilitiesWithinFilter(String filterField, String filterValue) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		Query query = null;
		session.beginTransaction();
		if (filterValue.equalsIgnoreCase("all")) {
			facilityQuery.append("from Facility");
			query = session.createQuery(facilityQuery.toString());
		} else {
			facilityQuery.append("from Facility where ").append(filterField).append(" LIKE :").append(filterField);
			query = session.createQuery(facilityQuery.toString());
			query.setParameter(filterField, filterValue);
		}
		System.out.println("Facility query = " + query);
		List<Facility> facList = query.list();
		return facList;
	}

	@Transactional(readOnly = true)
	public List<EWEDMonthlyData> queryMonthlyData(List<String> plantCodes, int minYear, int minMonth, int maxYear, int maxMonth) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();

		List<EWEDMonthlyData> monthlyDataList = new ArrayList<EWEDMonthlyData>();
		StringBuilder gewQueryBuilder = new StringBuilder();
		gewQueryBuilder.append(
				"SELECT new com.epa.beans.EWEDMonthlyData (g.genYear, g.genMonth, g.plantType, g.fuelType, g.coolingSystemType, g.waterType, g.waterSource, g.waterSourceName, TRIM(str(ISNULL(g.generation, 0), 17)) as generation, TRIM(str(ISNULL(g.emissions, 0), 17)) as emissions, TRIM(str(ISNULL(g.waterWithdrawal, 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(g.waterConsumption, 0), 17)) as waterConsumption) ")
				.append("from GenEmWaterView g where (g.plantCode in (:ids) and ")
				.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
				.append("order by plantCode, genYear, genMonth");
		try {
			session.beginTransaction();
			Query query = session.createQuery(gewQueryBuilder.toString());
	
			query.setParameterList("ids", plantCodes);
			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
	
			// System.out.println(query);
			List<EWEDMonthlyData> temp = query.list();
			monthlyDataList.addAll(temp);
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		return monthlyDataList;

	}

	public List<GenEmWaterView> queryGEWView(List<String> plantCodes, int minYear, int minMonth, int maxYear, int maxMonth) {

		session = HibernateUtil.getSessionFactory().openSession();

		List<GenEmWaterView> gewList = new ArrayList<GenEmWaterView>();

		int partitionSize = 2000;
		for (int i = 0; i < plantCodes.size(); i += partitionSize) {
			List<String> partPlantCodes = new ArrayList<String>();
			if (plantCodes.size() < i + partitionSize)
				partPlantCodes = plantCodes.subList(i, plantCodes.size());
			else
				partPlantCodes = plantCodes.subList(i, i + partitionSize);

			StringBuilder gewQueryBuilder = new StringBuilder();

			gewQueryBuilder.append(
					"SELECT new com.epa.views.GenEmWaterView (g.plantCode, g.genYear, g.genMonth, g.plantType, g.fuelType, g.coolingSystemType, g.waterType, g.waterSource, g.waterSourceName, TRIM(str(ISNULL(g.generation, 0), 17)) as generation, TRIM(str(ISNULL(g.emissions, 0), 17)) as emissions, TRIM(str(ISNULL(g.waterWithdrawal, 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(g.waterConsumption, 0), 17)) as waterConsumption) ")
					.append("from GenEmWaterView g where (g.plantCode in (:ids) and ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("order by plantCode, genYear, genMonth");

			Query query = session.createQuery(gewQueryBuilder.toString());

			query.setParameterList("ids", partPlantCodes);
			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);

			// System.out.println(query);
			List<GenEmWaterView> temp_gewList = query.list();
			gewList.addAll(temp_gewList);
		}
		// System.out.println("all gew list size = " + gewList.size());
		return gewList;
	}

	@Transactional(readOnly = true)
	public String defaultGEWData(String filterName, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(filterName, "", minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		Map<String, Object> completeData = new HashMap<String, Object>();
		StringBuilder queryBuilder = new StringBuilder();
		if (fuelTypeList[0].equals("all")) {
			queryBuilder.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterName).append(" as string)").append(" as filterName,").append(
					" TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) ")
					.append(" from com.epa.views.GenEmWaterView g")
					.append(" where ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))")
					.append(" Group by ").append(filterName);
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			queryBuilder.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterName).append(" as string) as filterName,").append(
					" TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) ")
					.append(" from com.epa.views.GenEmWaterView g WHERE ").append("fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterName);
		}

		try {
			session.beginTransaction();
			Query query = session.createQuery(queryBuilder.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);

			List<DefaultOutputJson> results = query.list();
			completeData.put("Summary", results);
			completeData.put("Total Summary", (totalSummaryList));
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		
		try {
			return mapper.writeValueAsString(completeData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{\"Result\": \"Data not found\"}";

	}

	// end-point to get water availability json output
	public String returnWaterAvailabilityFromHUCs(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		List<String> hucCodes = new ArrayList<String>();
		hucCodes = getAllHUCCodes(filterField, filterValue, minYear, minMonth, maxYear, maxMonth);
		if (hucCodes.size() == 0) {
			return "{\"Result\": \"Data not found\"}";
		}

		List<WaterAvailability> waterAvailibilityList = new ArrayList<WaterAvailability>();
		double totalWaterAvailable = 0;

		waterAvailibilityList = getWaterAvailabilityDataList(hucCodes, minYear, minMonth, maxYear, maxMonth);

		for (WaterAvailability wa : waterAvailibilityList) {
			totalWaterAvailable += wa.getWaterAvailable() != null ? Double.parseDouble(wa.getWaterAvailable().trim()) : 0;
		}
		Map<String, Object> returnWaterAvaiData = new HashMap<String, Object>();
		returnWaterAvaiData.put("Water Availability Summary", totalWaterAvailable);
		returnWaterAvaiData.put("MonthlyData", waterAvailibilityList);

		try {
			return mapper.writeValueAsString(returnWaterAvaiData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{\"Result\": \"Data not found\"}";

	}

	@Transactional(readOnly = true)
	public List<String> getHUCCodesFromName(String filterField, String filterValue) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder hucQuery = new StringBuilder();
		List<HucCodeToName> huc8CodeList = new ArrayList<HucCodeToName>();
		hucQuery.append("SELECT new com.epa.beans.Huc.HucCodeToName (g.HUC8Name, STR(g.HUC8Code)) ").append("from GenEmWaterView g where ").append(filterField).append(" like \'%").append(filterValue)
				.append("%\'");

		session.beginTransaction();
		Query query = session.createQuery(hucQuery.toString());
		System.out.println(query);

		List<HucCodeToName> temp = query.list();
		huc8CodeList.addAll(temp);
		List<String> hucCodes = new ArrayList<String>();
		for (HucCodeToName code : huc8CodeList) {
			String huc8Code = code.getHUC8Code();
			if (!hucCodes.contains(huc8Code))
				hucCodes.add(code.getHUC8Code());
		}
		return hucCodes;
	}

	@Transactional(readOnly = true)
	public List<String> getAllHUCCodes(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		// allowed filterField - state and Huc
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		Query query = null;
		session.beginTransaction();
		if (filterValue.equalsIgnoreCase("all")) {
			facilityQuery.append("from Facility");
			query = session.createQuery(facilityQuery.toString());
		} else {
			facilityQuery.append("from Facility where ").append(filterField).append(" LIKE :").append(filterField);
			query = session.createQuery(facilityQuery.toString());
			query.setParameter(filterField, filterValue);
		}
		List<Facility> facList = query.list();

		List<String> hucCodes = new ArrayList<String>();
		for (Facility fac : facList) {
			String huc = fac.getHUC8Code();
			if (!hucCodes.contains(huc))
				hucCodes.add(fac.getHUC8Code());
		}
		// System.out.println("huccodes list size = " + hucCodes.size());
		return hucCodes;
	}

	@Transactional(readOnly = true)
	public List<WaterAvailability> getWaterAvailabilityDataList(List<String> hucCodes, int minYear, int minMonth, int maxYear, int maxMonth) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		List<WaterAvailability> waterAvailibilityList = new ArrayList<WaterAvailability>();
		session.beginTransaction();
		
		if (hucCodes.size() == 1) {
			StringBuilder queryBuilder = new StringBuilder();

			queryBuilder.append("SELECT new com.epa.beans.WaterUsage.WaterAvailability (HUCCode, year, month, waterAvailable)").append("  from WaterAvailability").append(" where HUCCode = \'")
					.append(hucCodes.get(0).trim()).append("\'").append(" and").append(" ((( year = :minYear and month >= :minMonth) OR").append(" (year > :minYear))")
					.append(" and ((year = :maxYear and month <= :maxMonth) or (year < :maxYear))))").append(" order by HUCCode, year, month");
			Query query = session.createQuery(queryBuilder.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);

			System.out.println(query);
			List<WaterAvailability> temp_waList = query.list();
			waterAvailibilityList.addAll(temp_waList);
		} else {
			int partitionSize = 2000;
			for (int i = 0; i < hucCodes.size(); i += partitionSize) {
				List<String> parthucCodes = new ArrayList<String>();
				if (hucCodes.size() < i + partitionSize)
					parthucCodes = hucCodes.subList(i, hucCodes.size());
				else
					parthucCodes = hucCodes.subList(i, i + partitionSize);

				StringBuilder queryBuilder = new StringBuilder();

				queryBuilder.append("SELECT new com.epa.beans.WaterUsage.WaterAvailability (HUCCode, year, month, waterAvailable)").append("  from WaterAvailability")
						.append(" where (HUCCode in (:ids) and").append(" ((( year = :minYear and month >= :minMonth) OR").append(" (year > :minYear))")
						.append(" and ((year = :maxYear and month <= :maxMonth) or (year < :maxYear))))").append(" order by HUCCode, year, month");
				Query query = session.createQuery(queryBuilder.toString());

				query.setParameterList("ids", parthucCodes);
				query.setParameter("minYear", minYear);
				query.setParameter("maxYear", maxYear);
				query.setParameter("minMonth", minMonth);
				query.setParameter("maxMonth", maxMonth);

				System.out.println(query);
				List<WaterAvailability> temp_waList = query.list();
				waterAvailibilityList.addAll(temp_waList);
			}
		}
		return waterAvailibilityList;

	}

	@Transactional(readOnly = true)
	public String getSummaryWithin(String filterField1, String filterValue1, String filterField2, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		session = HibernateUtil.getSessionFactory().getCurrentSession();
		List<FacilityWithSummaryData> facList = queryFacilityWithFuelType(filterField1, filterValue1, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		if (facList.size() == 0) {
			return "{\"Result\": \"Data not found\"}";
		}

		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(filterField1, filterValue1, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		StringBuilder viewQuery = new StringBuilder();

		if (fuelTypeList[0].equals("all")) {
			viewQuery.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterField2).append(
					" as string) as filterName, TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) from com.epa.views.GenEmWaterView g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2);
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			viewQuery.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterField2).append(
					" as string) as filterName, TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) from com.epa.views.GenEmWaterView g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2);
		}
		Map<String, Object> returnData = new HashMap<String, Object>();

		try {
			Query query = session.createQuery(viewQuery.toString());

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);

			System.out.println(query);
			List<DefaultOutputJson> results = query.list();
			// System.out.println("output list summarry = " + results.size());

			returnData.put("Total Summary", totalSummaryList);
			returnData.put("Summary", results);
		} catch (HibernateException e) {
			e.printStackTrace();
		} /*
			 * finally { if (session.isOpen()) { session.close(); } }
			 */

		try {
			return mapper.writeValueAsString(returnData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{\"Result\": \"Data not found\"}";
	}

	public List<Top4> getTop4Records(String filterField1, String filterValue1, String filterField2, String totalOf, int minYear, int minMonth, int maxYear, int maxMonth) {

		session = HibernateUtil.getSessionFactory().openSession();
		StringBuilder genQuery = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		genQuery.append("WITH orderByPlantType AS (select plantType, sum(").append(totalOf).append(") as total, ROW_NUMBER() OVER (ORDER BY sum(").append(totalOf).append(") desc) AS RowNumber ")
				.append("from dbo.facGenEmWaterView ").append(" where (");
		whereClause.append(" (( genYear = :minYear and genMonth >= :minMonth) OR").append(" (genYear > :minYear and genYear < :maxYear)")
				.append(" OR (genYear = :maxYear and genMonth <= :maxMonth))) group by plantType)").append("(SELECT plantType, total, RowNumber as ranking FROM orderByPlantType where RowNumber <5) ")
				.append("union (SELECT 'allOthers', Sum (total) as total, 5 as ranking FROM orderByPlantType WHERE RowNumber >4)");

		if (!filterField1.equals("")) {
			genQuery.append(filterField1).append(" LIKE :value and").append(whereClause);
		} else {
			genQuery.append(whereClause);
		}

		Query query = session.createSQLQuery(genQuery.toString());
		if (!filterField1.equals("")) {
			query.setParameter("value", filterValue1);
		}
		query.setParameter("minYear", minYear);
		query.setParameter("maxYear", maxYear);
		query.setParameter("minMonth", minMonth);
		query.setParameter("maxMonth", maxMonth);

		List<Object[]> results = (List<Object[]>) query.list();
		System.out.println("output list size = " + results.size());

		List<Top4> listOfTop4 = new ArrayList<Top4>();
		for (Object[] arr : results) {
			Top4 obj = new Top4();
			obj.setPlantType(String.valueOf((arr[0])));
			if (arr[1] instanceof Number) {
				obj.setTotal(formatter.format((Double) arr[1]));
			} else {
				obj.setTotal("0");
			}
			if (arr[2] instanceof Number) {
				obj.setRank(((BigInteger) arr[2]).intValue());
			} else {
				obj.setRank(0);
			}
			listOfTop4.add(obj);
		}
		return listOfTop4;
	}

	@Override
	public String processWaterAvailabilityFile(String fileName, int startYear, int endYear) {

		BufferedReader reader;
		FileWriter outputfile;
		String inputFileName = "C:\\Users\\epa\\Documents\\historic_monthly\\" + fileName + ".TXT";

		try {
			reader = new BufferedReader(new FileReader(inputFileName));
			outputfile = new FileWriter("out.csv");
			CSVWriter writer = new CSVWriter(outputfile);

			String line = reader.readLine();
			String[] header = line.split("\\s*,\\s*");
			writer.writeNext(header);

			line = reader.readLine();

			while (line != null) {
				String[] data = line.split("\\s*,\\s*");
				int year = Integer.parseInt(data[1]);
				if ((year >= startYear) && (year <= endYear)) {
					writer.writeNext(data);
				}
				// read next line
				line = reader.readLine();
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "{\"Result\": \"Done\"}";
	}

	@Override
	public String getFacility(String filterField, String filterValue, String matchLevel, int minYear, int maxYear) {
		// TODO Auto-generated method stub
		return null;
	}
}
