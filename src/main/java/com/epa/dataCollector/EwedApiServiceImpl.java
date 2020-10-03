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
import java.util.TreeMap;
import java.util.stream.*;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epa.beans.EWEDMonthlyData;
import com.epa.beans.Facility.Facility;
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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

@Service
public class EwedApiServiceImpl implements EwedApiService {

	// Object mapper is used to convert objects to jsons
	@Autowired
	ObjectMapper mapper = new ObjectMapper();
	//public Session session;
	NumberFormat formatter = new DecimalFormat("########");

	/*
	 * /getFacility API
	 */
	@Override
	@Transactional(readOnly = true)
	public String getFacility(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		// query facility		
		List<Facility> facList = listOfFacilitiesWithinFilter(session, filterField, filterValue);
		List<String> plantCodes = new ArrayList<String>();
		for (Facility fac : facList) {
			plantCodes.add(fac.getPgmSysId());
		}

		// query monthly data
		List<EWEDMonthlyData> monthlyDataList = queryMonthlyData(session, null, plantCodes, minYear, minMonth, maxYear, maxMonth);
		
		// query the data summary of the facility
		List<FacilityDataSummary> facilityDataSummaryList = new ArrayList<FacilityDataSummary>();
		facilityDataSummaryList = queryFacilityDataSummary(session, null, filterField, filterValue, minYear, minMonth, maxYear, maxMonth);

		LinkedHashMap<Object, Object> facilityReturnMap = new LinkedHashMap<Object, Object>();
		facilityReturnMap.put("Facility", facList);
		facilityReturnMap.put("MonthlyData", monthlyDataList);
		facilityReturnMap.put("FacilityDataSummary", facilityDataSummaryList);

		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(facilityReturnMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}

	/*
	 * /getFacility API for advanced system
	 */
	@Override
	@Transactional(readOnly = true)
	public String getFutureFacility(String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// query facility
		List<Facility> facList = listOfFacilitiesWithinFilter(session, filterField, filterValue);
		List<String> plantCodes = new ArrayList<String>();
		for (Facility fac : facList) {
			plantCodes.add(fac.getPgmSysId());
		}

		// query monthly data
		List<EWEDMonthlyData> monthlyDataList = queryMonthlyData(session, caseModel, plantCodes, minYear, minMonth, maxYear, maxMonth);

		// query the data summary of the facility
		List<FacilityDataSummary> facilityDataSummaryList = new ArrayList<FacilityDataSummary>();
		facilityDataSummaryList = queryFacilityDataSummary(session, caseModel, filterField, filterValue, minYear, minMonth, maxYear, maxMonth);

		LinkedHashMap<Object, Object> facilityReturnMap = new LinkedHashMap<Object, Object>();
		facilityReturnMap.put("Facility", facList);
		facilityReturnMap.put("MonthlyData", monthlyDataList);
		facilityReturnMap.put("FacilityDataSummary", facilityDataSummaryList);

		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(facilityReturnMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}

	/*
	 * method to query the data summary for a facility
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system 
	 */

	//@Transactional(readOnly = true)
	public List<FacilityDataSummary> queryFacilityDataSummary(Session session, String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<FacilityDataSummary> monthlyDataSummaryList = new ArrayList<FacilityDataSummary>();

		if (caseModel == null) {
			gewQueryBuilder.append(
					"WITH total AS ( SELECT LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as WaterConsumptionSummary, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as EmissionSummary, LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as GenerationSummary, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as WaterWithdrawalSummary from facGenEmWaterView_new where plantCode")
					.append(" = \'").append(filterValue).append("\' AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))))").append(",")
					.append("recent AS (SELECT TOP 1 plantCode, plantType, fuelType, coolingSystemType, waterType, waterSource, waterSourceName from facGenEmWaterView_new WHERE plantCode")
					.append(" = \'").append(filterValue).append("\' AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" ORDER BY genYear, genMonth desc ) ")
					.append("SELECT recent.plantType, recent.fuelType, recent.coolingSystemType, recent.waterType, recent.waterSource, recent.waterSourceName, total.WaterConsumptionSummary, total.EmissionSummary, total.GenerationSummary, total.WaterWithdrawalSummary from recent, total");
		} else {
			gewQueryBuilder.append(
					"WITH total AS ( SELECT LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as WaterConsumptionSummary, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as EmissionSummary, LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as GenerationSummary, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as WaterWithdrawalSummary from p_facGenEmWaterView_")
					.append(caseModel).append(" where plantCode").append(" = \'").append(filterValue).append("\' AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))))").append(",")
					.append("recent AS (SELECT TOP 1 plantCode, plantType, fuelType, coolingSystemType, waterType, waterSource, waterSourceName from p_facGenEmWaterView_").append(caseModel)
					.append(" WHERE plantCode").append(" = \'").append(filterValue).append("\' AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" ORDER BY genYear, genMonth desc ) ")
					.append("SELECT recent.plantType, recent.fuelType, recent.coolingSystemType, recent.waterType, recent.waterSource, recent.waterSourceName, total.WaterConsumptionSummary, total.EmissionSummary, total.GenerationSummary, total.WaterWithdrawalSummary from recent, total");
		}
		try {
			//session.beginTransaction();
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


	/*
	 * /getFacilityData API
	 */
	@Override
	@Transactional(readOnly = true)
	public String getFacilityData(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// get the facilities
		List<FacilityWithSummaryData> facList = queryFacilityWithFuelType(session, null, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		// get totalSummary
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, null, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		// Stores the complete structure
		Map<String, Object> completeGenEmWaterOutput = new HashMap<String, Object>();
		completeGenEmWaterOutput.put("Summary", totalSummaryList);
		completeGenEmWaterOutput.put("All Facilities", facList);
		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeGenEmWaterOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}
	
	/*
	 * /getFacilityData API for advanced system
	 */
	@Override
	@Transactional(readOnly = true)
	public String getFutureFacilityData(String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes,
			String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// get the facilities
		List<FacilityWithSummaryData> facList = queryFacilityWithFuelType(session, caseModel, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		// get totalSummary
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, caseModel, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		
		// Stores the complete structure
		Map<String, Object> completeGenEmWaterOutput = new HashMap<String, Object>();
		completeGenEmWaterOutput.put("Summary", totalSummaryList);
		completeGenEmWaterOutput.put("All Facilities", facList);
		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeGenEmWaterOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}
	
	/*
	 * /getMonthWiseSummary API 
	 */
	@Override
	@Transactional(readOnly = true)
	public String getMonthWiseSummary(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// get monthWise summary
		List<MonthWiseSummary> monthWiseSummaryList = new ArrayList<MonthWiseSummary>();
		monthWiseSummaryList = queryMonthWiseSummary(session, null, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		List<String> hucCodes = new ArrayList<String>();
		List<WaterAvailability> waterAvailabilityList = new ArrayList<WaterAvailability>();

		// if filter by state, huc8code or huc8name, then add waterAvailable
		if (filterField.equals("stateName") || filterField.equals("huc8Code")) {
			hucCodes = getAllHUCCodes(session, filterField, filterValue, minYear, minMonth, maxYear, maxMonth);
			waterAvailabilityList = getWaterAvailabilityDataList(session, null, hucCodes, minYear, minMonth, maxYear, maxMonth);
		} else if (filterField.equals("HUC8Name")) { // convert HUC8Name into
														// HUC8Code
			hucCodes = getHUCCodesFromName(session, filterField, filterValue);
			waterAvailabilityList = getWaterAvailabilityDataList(session, null, hucCodes, minYear, minMonth, maxYear, maxMonth);
		}

		// Stores the month wise summary of all facilities in the given range of year
		HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>> facMonthWiseData = new HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>>();
		
		// structure will look like {"2016": {1: {}, 2:{}, 3{},...}, "2017": {1: {}, 2:{}, 3{},...}, etc
		for (MonthWiseSummary monthWise : monthWiseSummaryList) {
			HashMap<Integer, DefaultOutputJson_custom> genEmWaterPerMonth = new HashMap<Integer, DefaultOutputJson_custom>();
			DefaultOutputJson_custom customObj = new DefaultOutputJson_custom();
			customObj.setGeneration(monthWise.getGeneration());
			customObj.setEmission(monthWise.getEmission());
			customObj.setWaterConsumption(monthWise.getWaterConsumption());
			customObj.setWaterWithdrawal(monthWise.getWaterWithdrawal());
			customObj.setWaterAvailability("0");
			if (facMonthWiseData.containsKey(monthWise.getGenYear())) {
				facMonthWiseData.get(monthWise.getGenYear()).put(monthWise.getGenMonth(), customObj);
			} else {
				genEmWaterPerMonth.put(monthWise.getGenMonth(), customObj);
				facMonthWiseData.put(monthWise.getGenYear(), genEmWaterPerMonth);
			}	
		}
		// add water available, if applicable
		System.out.println("list: " + waterAvailabilityList);
		if (filterField.equals("stateName") || filterField.equals("huc8Code") || filterField.equals("HUC8Name")) {
			for (WaterAvailability waData : waterAvailabilityList) {
				if (facMonthWiseData.get(waData.getYear()) != null && facMonthWiseData.get(waData.getYear()).get(waData.getMonth()) != null) {
					facMonthWiseData.get(waData.getYear()).get(waData.getMonth()).setWaterAvailability(waData.getWaterAvailable());
				}
			}
		}
		// Stores the complete structure
		Map<String, Object> completeGenEmWaterOutput = new HashMap<String, Object>();
		completeGenEmWaterOutput.put("MonthWiseSummary", facMonthWiseData);
		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeGenEmWaterOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}
	
	/*
	 * /getMonthWiseSummary API for advanced system
	 */
	@Override
	@Transactional(readOnly = true)
	public String getFutureMonthWiseSummary(String caseModel, String climateModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes,
			String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// get monthWise summary
		List<MonthWiseSummary> monthWiseSummaryList = new ArrayList<MonthWiseSummary>();
		monthWiseSummaryList = queryMonthWiseSummary(session, caseModel, filterField, filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		List<String> hucCodes = new ArrayList<String>();
		List<WaterAvailability> waterAvailabilityList = new ArrayList<WaterAvailability>();

		// if filter by state, huc8code or huc8name, then add waterAvailable
		if (filterField.equals("stateName") || filterField.equals("huc8Code")) {
			hucCodes = getAllHUCCodes(session, filterField, filterValue, minYear, minMonth, maxYear, maxMonth);
			waterAvailabilityList = getWaterAvailabilityDataList(session, climateModel, hucCodes, minYear, minMonth, maxYear, maxMonth);
		} else if (filterField.equals("HUC8Name")) { // convert HUC8Name into HUC8Code
			hucCodes = getHUCCodesFromName(session, filterField, filterValue);
			waterAvailabilityList = getWaterAvailabilityDataList(session, climateModel, hucCodes, minYear, minMonth, maxYear, maxMonth);
		}

		// Stores the month wise summary of all facilities in the given range of
		// year
		HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>> facMonthWiseData = new HashMap<Integer, HashMap<Integer, DefaultOutputJson_custom>>();
		
		// structure will look like {"2016": {1: {}, 2:{}, 3{},...}, "2017": {1: {}, 2:{}, 3{},...}, etc
		for (MonthWiseSummary monthWise : monthWiseSummaryList) {
			HashMap<Integer, DefaultOutputJson_custom> genEmWaterPerMonth = new HashMap<Integer, DefaultOutputJson_custom>();
			DefaultOutputJson_custom customObj = new DefaultOutputJson_custom();
			customObj.setGeneration(monthWise.getGeneration());
			customObj.setEmission(monthWise.getEmission());
			customObj.setWaterConsumption(monthWise.getWaterConsumption());
			customObj.setWaterWithdrawal(monthWise.getWaterWithdrawal());
			customObj.setWaterAvailability("0");
			if (facMonthWiseData.containsKey(monthWise.getGenYear())) {
				facMonthWiseData.get(monthWise.getGenYear()).put(monthWise.getGenMonth(), customObj);
			} else {
				genEmWaterPerMonth.put(monthWise.getGenMonth(), customObj);
				facMonthWiseData.put(monthWise.getGenYear(), genEmWaterPerMonth);
			}	
		}
		// add water available, if applicable
		if (filterField.equals("stateName") || filterField.equals("huc8Code") || filterField.equals("HUC8Name")) {
			for (WaterAvailability waData : waterAvailabilityList) {
				facMonthWiseData.get(waData.getYear()).get(waData.getMonth()).setWaterAvailability(waData.getWaterAvailable());
			}
		}
		// Stores the complete structure
		Map<String, Object> completeGenEmWaterOutput = new HashMap<String, Object>();
		completeGenEmWaterOutput.put("MonthWiseSummary", facMonthWiseData);
		
		// Convert Map to JSON
		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeGenEmWaterOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return "{\"Result\": \"Data not found\"}";
	}

	/*
	 * method to query the total sum 
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system 
	 */
	//@Transactional(readOnly = true)
	public List<TotalSummary> queryTotalSummary(Session session, String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType,
			String[] fuelTypeList) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();

		if (fuelTypeList[0].equals("all")) {
			if (caseModel == null) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView ");
			} else {
				gewQueryBuilder.append(
						"SELECT LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as TotalGeneration, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as TotalEmission, LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as TotalWaterConsumption, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as TotalWaterWithdrawal ")
						.append("from p_facGenEmWaterView_").append(caseModel);
			}
			if (filterValue.equals("")) {
				gewQueryBuilder
						.append(" where ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ");
			} else {
				gewQueryBuilder.append(" where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ");
			}
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			if (caseModel == null) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.TotalSummary (TRIM(str(ISNULL(SUM(generation), 0), 17)) as TotalGeneration, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as TotalEmission, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as TotalWaterConsumption, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as TotalWaterWithdrawal) ")
						.append("from GenEmWaterView g ");
			} else {
				gewQueryBuilder.append(
						"SELECT LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as TotalGeneration, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as TotalEmission, LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as TotalWaterConsumption, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as TotalWaterWithdrawal ")
						.append("from p_facGenEmWaterView_").append(caseModel);
			}
			if (filterValue.equals("")) {
				gewQueryBuilder.append(" where ").append("fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ");
			} else {
				gewQueryBuilder.append(" where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString)
						.append(") AND ")
						.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ");
			}
		}

		try {
			//session.beginTransaction();
			//session = sessionManager.createNewSessionAndTransaction();
			Query query = null;
			if (caseModel == null) {
				query = session.createQuery(gewQueryBuilder.toString());
			} else {
				query = session.createSQLQuery(gewQueryBuilder.toString());
				query.setResultTransformer(Transformers.aliasToBean(TotalSummary.class));
			}

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);

			List<TotalSummary> temp_totalSummaryList = query.list();
			totalSummaryList.addAll(temp_totalSummaryList);
			
		} catch (HibernateException e) {
			e.printStackTrace();
		} 
		return totalSummaryList;
	}

	/*
	 * Method to query the monthWiseSummary
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system  
	 */
	//@Transactional(readOnly = true)
	public List<MonthWiseSummary> queryMonthWiseSummary(Session session, String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType,
			String[] fuelTypeList) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder gewQueryBuilder = new StringBuilder();
		List<MonthWiseSummary> monthWiseSummaryList = new ArrayList<MonthWiseSummary>();

		if (fuelTypeList[0].equals("all")) {
			if (caseModel == null) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.MonthWiseSummary (g.genYear, g.genMonth, TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as waterWithdrawal) ")
						.append("from GenEmWaterView");
			} else {
				gewQueryBuilder.append(
						"SELECT g.genYear, g.genMonth, LTRIM(RTRIM(str(ISNULL(SUM(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(SUM(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption, LTRIM(RTRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17))) as waterWithdrawal ")
						.append("from p_facGenEmWaterView_").append(caseModel);
			}
			gewQueryBuilder.append(" g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("group by genYear, genMonth ").append("order by genYear, genMonth");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			if (caseModel == null) {
				gewQueryBuilder.append(
						"SELECT new com.epa.beans.SummaryData.MonthWiseSummary (g.genYear, g.genMonth, TRIM(str(ISNULL(SUM(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(SUM(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption, TRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17)) as waterWithdrawal) ")
						.append("from GenEmWaterView");
			} else {
				gewQueryBuilder.append(
						"SELECT g.genYear, g.genMonth, LTRIM(RTRIM(str(ISNULL(SUM(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(SUM(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption, LTRIM(RTRIM(str(ISNULL(SUM(g.waterWithdrawal), 0), 17))) as waterWithdrawal ")
						.append("from p_facGenEmWaterView_").append(caseModel);
			}
			gewQueryBuilder.append(" g where ( ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString)
					.append(") AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("group by genYear, genMonth ").append("order by genYear, genMonth");
		}

		try {
			//session.beginTransaction();
			Query query = null;
			if (caseModel == null) {
				query = session.createQuery(gewQueryBuilder.toString());
			} else {				
				query = session.createSQLQuery(gewQueryBuilder.toString());
				query.setResultTransformer(Transformers.aliasToBean(MonthWiseSummary.class));
			}

			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);

			List<MonthWiseSummary> temp_monthWiseList = query.list();
			monthWiseSummaryList.addAll(temp_monthWiseList);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return monthWiseSummaryList;
	}

	@Override
	@Transactional(readOnly = true)
	public String getAllFacilities(String filterField, String filterValue) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		try {
			return mapper.writeValueAsString(listOfFacilitiesWithinFilter(session, filterField, filterValue));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		

		return "{\"Result\": \"Data not found\"}";

	}

	/*
	 * Method to filter the facilities using the fuelType
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system  
	 */
	//@Transactional(readOnly = true)
	public List<FacilityWithSummaryData> queryFacilityWithFuelType(Session session, String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelType,
			String[] fuelTypeList) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		List<FacilityWithSummaryData> facList = new ArrayList<FacilityWithSummaryData>();

		if (fuelTypeList[0].equals("all")) {
			if (caseModel == null) {
				facilityQuery.append(
						"SELECT new com.epa.beans.SummaryData.FacilityWithSummaryData (g.plantCode as pgmSysId, g.primaryName, TRIM(str(g.naicsCode)) as naicsCode, TRIM(str(g.registryId, 17)) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, TRIM(str(g.latitude, 17, 3)) as latitude, TRIM(str(g.longitude, 17, 3)) as longitude, TRIM(str(g.GEOID)) as geoid, g.CountyState1, g.CountyState2, TRIM(str(g.HUC8Code)) as HUC8Code, g.HUC8Name, TRIM(str(g.HUC8Acres)) as HUC8Acres, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as WaterConsumptionSummary, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as EmissionSummary, TRIM(str(ISNULL(SUM(generation), 0), 17)) as GenerationSummary, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as WaterWithdrawalSummary ) ")
						.append("from GenEmWaterView g ");
			} else {
				facilityQuery.append(
						"SELECT LTRIM(RTRIM(STR(g.plantCode))) as plantCode, g.primaryName, LTRIM(RTRIM(str(g.naicsCode))) as naicsCode, LTRIM(RTRIM(str(g.registryId, 17))) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, LTRIM(RTRIM(str(g.latitude, 17, 3))) as latitude, LTRIM(RTRIM(str(g.longitude, 17, 3))) as longitude, LTRIM(RTRIM(str(g.GEOID))) as GEOID, g.CountyState1, g.CountyState2, LTRIM(RTRIM(str(g.HUC8Code))) as HUC8Code, g.HUC8Name, LTRIM(RTRIM(str(g.HUC8Acres))) as HUC8Acres, LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as WaterConsumptionSummary, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as EmissionSummary, LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as GenerationSummary, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as WaterWithdrawalSummary ")
						.append("from p_facGenEmWaterView_").append(caseModel).append(" g ");
			}
			facilityQuery.append(" where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append("GROUP BY g.plantCode, primaryName, naicsCode, registryId, facAddr, cityName, stateName, postalCode, latitude, longitude, GEOID, CountyState1, CountyState2, HUC8Code, HUC8Name, HUC8Acres")
					.append(" ORDER BY primaryName");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			if (caseModel == null) {
				facilityQuery.append(
						"SELECT new com.epa.beans.SummaryData.FacilityWithSummaryData (g.plantCode as pgmSysId, g.primaryName, TRIM(str(g.naicsCode)) as naicsCode, TRIM(str(g.registryId, 17)) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, TRIM(str(g.latitude, 17, 3)) as latitude, TRIM(str(g.longitude, 17, 3)) as longitude, TRIM(str(g.GEOID)) as geoid, g.CountyState1, g.CountyState2, TRIM(str(g.HUC8Code)) as HUC8Code, g.HUC8Name, TRIM(str(g.HUC8Acres)) as HUC8Acres, TRIM(str(ISNULL(SUM(waterConsumption), 0), 17)) as WaterConsumptionSummary, TRIM(str(ISNULL(SUM(emissions), 0), 17)) as EmissionSummary, TRIM(str(ISNULL(SUM(generation), 0), 17)) as GenerationSummary, TRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17)) as WaterWithdrawalSummary ) ")
						.append("from GenEmWaterView g ");
			} else {
				facilityQuery.append(
						"SELECT LTRIM(RTRIM(STR(g.plantCode))) as plantCode, g.primaryName, LTRIM(RTRIM(str(g.naicsCode))) as naicsCode, LTRIM(RTRIM(str(g.registryId, 17))) as registryId, g.facAddr, g.cityName, g.stateName, g.postalCode, LTRIM(RTRIM(str(g.latitude, 17, 3))) as latitude, LTRIM(RTRIM(str(g.longitude, 17, 3))) as longitude, LTRIM(RTRIM(str(g.GEOID))) as GEOID, g.CountyState1, g.CountyState2, LTRIM(RTRIM(str(g.HUC8Code))) as HUC8Code, g.HUC8Name, LTRIM(RTRIM(str(g.HUC8Acres))) as HUC8Acres, LTRIM(RTRIM(str(ISNULL(SUM(waterConsumption), 0), 17))) as WaterConsumptionSummary, LTRIM(RTRIM(str(ISNULL(SUM(emissions), 0), 17))) as EmissionSummary, LTRIM(RTRIM(str(ISNULL(SUM(generation), 0), 17))) as GenerationSummary, LTRIM(RTRIM(str(ISNULL(SUM(waterWithdrawal), 0), 17))) as WaterWithdrawalSummary ")
						.append("from p_facGenEmWaterView_").append(caseModel).append(" g ");
			}
			facilityQuery.append(" where ").append(filterField).append(" = \'").append(filterValue).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append("GROUP BY g.plantCode, primaryName, naicsCode, registryId, facAddr, cityName, stateName, postalCode, latitude, longitude, GEOID, CountyState1, CountyState2, HUC8Code, HUC8Name, HUC8Acres")
					.append(" ORDER BY primaryName");
		}

		try {
			//session.beginTransaction();
			Query query = null;
			if (caseModel == null) {
				query = session.createQuery(facilityQuery.toString());
			} else {
				query = session.createSQLQuery(facilityQuery.toString());
				query.setResultTransformer(Transformers.aliasToBean(FacilityWithSummaryData.class));
			}
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

	/*
	 * filter the facilities using filterField with value
	 * Example: query by stateName = "California"
	 */
	//@Transactional(readOnly = true)
	public List<Facility> listOfFacilitiesWithinFilter(Session session, String filterField, String filterValue) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		Query query = null;
		//session.beginTransaction();
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

	/*
	 * Method to query the monthly data
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system
	 * */
	//@Transactional(readOnly = true)
	public List<EWEDMonthlyData> queryMonthlyData(Session session, String caseModel, List<String> plantCodes, int minYear, int minMonth, int maxYear, int maxMonth) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();

		List<EWEDMonthlyData> monthlyDataList = new ArrayList<EWEDMonthlyData>();
		StringBuilder gewQueryBuilder = new StringBuilder();
		if (caseModel == null) {
			gewQueryBuilder.append(
					"SELECT new com.epa.beans.EWEDMonthlyData (g.genYear, g.genMonth, g.plantType, g.fuelType, g.coolingSystemType, g.waterType, g.waterSource, g.waterSourceName, TRIM(str(ISNULL(g.generation, 0), 17)) as generation, TRIM(str(ISNULL(g.emissions, 0), 17)) as emissions, TRIM(str(ISNULL(g.waterWithdrawal, 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(g.waterConsumption, 0), 17)) as waterConsumption) ")
					.append("from GenEmWaterView g where (g.plantCode in (:ids) and ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("order by plantCode, genYear, genMonth");
		} else {
			gewQueryBuilder.append(
					"SELECT g.genYear as year, g.genMonth as month, g.plantType, g.fuelType, g.coolingSystemType, g.waterType, g.waterSource, g.waterSourceName, LTRIM(RTRIM(str(ISNULL(g.generation, 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(g.emissions, 0), 17))) as emissions, LTRIM(RTRIM(str(ISNULL(g.waterWithdrawal, 0), 17))) as waterWithdrawal, LTRIM(RTRIM(str(ISNULL(g.waterConsumption, 0), 17))) as waterConsumption ")
					.append("from p_facGenEmWaterView_").append(caseModel).append(" g where (g.plantCode in (:ids) and ")
					.append("((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))) ")
					.append("order by plantCode, genYear, genMonth");
		}
		try {
			//session.beginTransaction();
			Query query = null;
			if (caseModel == null) {
				query = session.createQuery(gewQueryBuilder.toString());
			} else {
				query = session.createSQLQuery(gewQueryBuilder.toString());
				query.setResultTransformer(Transformers.aliasToBean(EWEDMonthlyData.class));
			}
			query.setParameterList("ids", plantCodes);
			query.setParameter("minYear", minYear);
			query.setParameter("maxYear", maxYear);
			query.setParameter("minMonth", minMonth);
			query.setParameter("maxMonth", maxMonth);
			System.out.println(query);
			List<EWEDMonthlyData> temp = query.list();
			monthlyDataList.addAll(temp);

		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return monthlyDataList;

	}

	/*
	 * DEPRECATED - NEVER USED 
	 */
	@Deprecated
	public List<GenEmWaterView> queryGEWView(List<String> plantCodes, int minYear, int minMonth, int maxYear, int maxMonth) {

		Session session = HibernateUtil.getSessionFactory().openSession();

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

	/*
	 * /defaultView API
	 * */
	@Transactional(readOnly = true)
	public String defaultGEWData(String filterName, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// query the total summary
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, null, filterName, "", minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		
		Map<String, Object> completeData = new HashMap<String, Object>();
		StringBuilder queryBuilder = new StringBuilder();
		if (fuelTypeList[0].equals("all")) {
			queryBuilder.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterName).append(" as string)").append(" as filterName,").append(
					" TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) ")
					.append(" from com.epa.views.GenEmWaterView g")
					.append(" where ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))")
					.append(" Group by ").append(filterName)
					.append(" ORDER BY filterName");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			queryBuilder.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterName).append(" as string) as filterName,").append(
					" TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) ")
					.append(" from com.epa.views.GenEmWaterView g WHERE ").append("fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterName)
					.append(" ORDER BY filterName");
		}

		try {			
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
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "{\"Result\": \"Data not found\"}";
	}
	
	/*
	 * /defaultView API for advanced system
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system
	 * */
	@Transactional(readOnly = true)
	public String futureDefaultGEWData(String caseModel, String filterName, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// get the total summary
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, caseModel, filterName, "", minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		Map<String, Object> completeData = new HashMap<String, Object>();
		StringBuilder queryBuilder = new StringBuilder();
		if (fuelTypeList[0].equals("all")) {
			queryBuilder.append("SELECT g.").append(filterName).append(" as filterName,").append(
					" LTRIM(RTRIM(str(ISNULL(sum(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(sum(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17))) as waterWithdrawal, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption ")
					.append("from p_facGenEmWaterView_").append(caseModel).append(" g ")
					.append(" where ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear)))")
					.append(" Group by ").append(filterName)
					.append(" ORDER BY filterName");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			queryBuilder.append("SELECT g.").append(filterName).append(" as filterName,").append(
					" LTRIM(RTRIM(str(ISNULL(sum(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(sum(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17))) as waterWithdrawal, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption ")
					.append("from p_facGenEmWaterView_").append(caseModel).append(" g ")
					.append(" WHERE ").append("fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterName)
					.append(" ORDER BY filterName");
		}

		try {
			Query query = session.createSQLQuery(queryBuilder.toString());
			query.setResultTransformer(Transformers.aliasToBean(DefaultOutputJson.class));
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
			session.getTransaction().commit();
			return mapper.writeValueAsString(completeData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "{\"Result\": \"Data not found\"}";

	}

	/* 
	 * DEPRECATED - NEVER USED
	 */
	@Deprecated
	public String returnWaterAvailabilityFromHUCs(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		List<String> hucCodes = new ArrayList<String>();
		Session session = HibernateUtil.getSessionFactory().openSession();
		hucCodes = getAllHUCCodes(session, filterField, filterValue, minYear, minMonth, maxYear, maxMonth);
		if (hucCodes.size() == 0) {
			return "{\"Result\": \"Data not found\"}";
		}

		List<WaterAvailability> waterAvailibilityList = new ArrayList<WaterAvailability>();
		double totalWaterAvailable = 0;

		waterAvailibilityList = getWaterAvailabilityDataList(session, null, hucCodes, minYear, minMonth, maxYear, maxMonth);

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

	/*
	 * Method to get the HUC8Code from HUC8Name
	 * */
	//@Transactional(readOnly = true)
	public List<String> getHUCCodesFromName(Session session, String filterField, String filterValue) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder hucQuery = new StringBuilder();
		List<HucCodeToName> huc8CodeList = new ArrayList<HucCodeToName>();
		hucQuery.append("SELECT new com.epa.beans.Huc.HucCodeToName (g.HUC8Name, STR(g.HUC8Code)) ").append("from GenEmWaterView g where ").append(filterField).append(" like \'%").append(filterValue)
				.append("%\'")
				.append("GROUP BY HUC8Name, HUC8Code");

		//session.beginTransaction();
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

	/*
	 * method to get all HUC8Codes from filterField by filterValue
	 * Example: get all HUC8Codes from stateName = 'California'
	 * */
	//@Transactional(readOnly = true)
	public List<String> getAllHUCCodes(Session session, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth) {
		// allowed filterField - state and Huc
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		StringBuilder facilityQuery = new StringBuilder();
		Query query = null;
		//session.beginTransaction();
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

	/*
	 * method to query water availability from the HUC lists 
	 * if @param climateModel is null, it is used for historical system
	 * if @param climateModel is nonnull, it is used for advanced system
	 */
	//@Transactional(readOnly = true)
	public List<WaterAvailability> getWaterAvailabilityDataList(Session session, String climateModel, List<String> hucCodes, int minYear, int minMonth, int maxYear, int maxMonth) {
		//session = HibernateUtil.getSessionFactory().getCurrentSession();
		List<WaterAvailability> waterAvailibilityList = new ArrayList<WaterAvailability>();
		//session.beginTransaction();

		if (hucCodes.size() == 1) {
			StringBuilder queryBuilder = new StringBuilder();
			if (climateModel == null) {
				queryBuilder.append("SELECT new com.epa.beans.WaterUsage.WaterAvailability (year, month, TRIM(STR(SUM(waterAvailable))) as waterAvailable)").append(" from WaterAvailability");
			} else if (climateModel.equalsIgnoreCase("Avg45") || climateModel.equalsIgnoreCase("Avg85")) {
				queryBuilder.append("SELECT year, month, LTRIM(RTRIM(STR(SUM(waterAvailable)))) as waterAvailable").append(" from climate.waterAvailability")
						.append(climateModel.substring(0, 1).toUpperCase() + climateModel.substring(1)); // capitalize first letter
			} else {
				queryBuilder.append("SELECT year, month, LTRIM(RTRIM(STR(SUM(waterAvailable)))) as waterAvailable").append(" from climate.[").append(climateModel).append("]");
			}
			queryBuilder.append(" where HUCCode = \'").append(hucCodes.get(0).trim()).append("\'").append(" and").append(" ((( year = :minYear and month >= :minMonth) OR")
					.append(" (year > :minYear))").append(" and ((year = :maxYear and month <= :maxMonth) or (year < :maxYear)))").append(" group by year, month");

			Query query = null;
			if (climateModel == null) {
				query = session.createQuery(queryBuilder.toString());
			} else {
				query = session.createSQLQuery(queryBuilder.toString());
				query.setResultTransformer(Transformers.aliasToBean(WaterAvailability.class));
			}
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
				if (climateModel == null) {
					queryBuilder.append("SELECT new com.epa.beans.WaterUsage.WaterAvailability (year, month, TRIM(STR(SUM(waterAvailable))) as waterAvailable)").append(" from WaterAvailability");
				} else if (climateModel.equalsIgnoreCase("Avg45") || climateModel.equalsIgnoreCase("Avg85")) {
					queryBuilder.append("SELECT year, month, LTRIM(RTRIM(STR(SUM(waterAvailable)))) as waterAvailable").append(" from climate.waterAvailability")
							.append(climateModel.substring(0, 1).toUpperCase() + climateModel.substring(1)); // capitalize first letter
				} else {
					queryBuilder.append("SELECT year, month, LTRIM(RTRIM(STR(SUM(waterAvailable)))) as waterAvailable").append(" from climate.[").append(climateModel).append("]");
				}
				queryBuilder.append(" where (HUCCode in (:ids) and").append(" ((( year = :minYear and month >= :minMonth) OR").append(" (year > :minYear))")
						.append(" and ((year = :maxYear and month <= :maxMonth) or (year < :maxYear))))").append(" group by year, month");

				Query query = null;
				if (climateModel == null) {
					query = session.createQuery(queryBuilder.toString());
				} else {
					query = session.createSQLQuery(queryBuilder.toString());
					query.setResultTransformer(Transformers.aliasToBean(WaterAvailability.class));
				}

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
	
	/*
	 * getSummaryWithin API
	 * */
	@Transactional(readOnly = true)
	public String getSummaryWithin(String filterField1, String filterValue1, String filterField2, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, null, filterField1, filterValue1, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		StringBuilder viewQuery = new StringBuilder();

		if (fuelTypeList[0].equals("all")) {
			viewQuery.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterField2).append(
					" as string) as filterName, TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) from com.epa.views.GenEmWaterView g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2)
					.append(" ORDER BY filterName");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			viewQuery.append("SELECT new com.epa.views.DefaultOutputJson(cast(g.").append(filterField2).append(
					" as string) as filterName, TRIM(str(ISNULL(sum(g.generation), 0), 17)) as generation, TRIM(str(ISNULL(sum(g.emissions), 0), 17)) as emission, TRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17)) as waterWithdrawal, TRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17)) as waterConsumption) from com.epa.views.GenEmWaterView g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2)
					.append(" ORDER BY filterName");
		}
		HashMap<String, Object> returnData = new HashMap<String, Object>();

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
		}

		try {
			//mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
			session.getTransaction().commit();
			return mapper.writeValueAsString(returnData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "{\"Result\": \"Data not found\"}";
	}
	
	/*
	 * getSummaryWithin API for advanced system
	 * if @param caseModel is null, it is used for historical system
	 * if @param caseModel is nonnull, it is used for advanced system
	 * */
	@Transactional(readOnly = true)
	public String getFutureSummaryWithin(String caseModel, String filterField1, String filterValue1, String filterField2, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList) {
		//Session session = HibernateUtil.getSessionFactory().openSession();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		List<TotalSummary> totalSummaryList = new ArrayList<TotalSummary>();
		totalSummaryList = queryTotalSummary(session, caseModel, filterField1, filterValue1, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);

		StringBuilder viewQuery = new StringBuilder();

		if (fuelTypeList[0].equals("all")) {
			viewQuery.append("SELECT g.").append(filterField2).append(" as filterName,") 
					.append(" LTRIM(RTRIM(str(ISNULL(sum(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(sum(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17))) as waterWithdrawal, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption ")
					.append("from p_facGenEmWaterView_").append(caseModel).append(" g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2)
					.append(" ORDER BY filterName");
		} else {
			String fuelTypeListString = Stream.of(fuelTypeList).collect(Collectors.joining("','", "'", "'"));
			viewQuery.append("SELECT g.").append(filterField2).append(" as filterName,") 
					.append(" LTRIM(RTRIM(str(ISNULL(sum(g.generation), 0), 17))) as generation, LTRIM(RTRIM(str(ISNULL(sum(g.emissions), 0), 17))) as emission, LTRIM(RTRIM(str(ISNULL(sum(g.waterWithdrawal), 0), 17))) as waterWithdrawal, LTRIM(RTRIM(str(ISNULL(SUM(g.waterConsumption), 0), 17))) as waterConsumption ")
					.append("from p_facGenEmWaterView_").append(caseModel).append(" g where ")
					.append(filterField1).append(" = \'").append(filterValue1).append("\'").append(" AND fuelType").append(" in (").append(fuelTypeListString).append(") AND ")
					.append(" ((( genYear = :minYear and genMonth >= :minMonth) OR (genYear > :minYear)) and ((genYear = :maxYear and genMonth <= :maxMonth) or (genYear < :maxYear))) ")
					.append(" GROUP BY ").append(filterField2)
					.append(" ORDER BY filterName");
		}
		HashMap<String, Object> returnData = new HashMap<String, Object>();

		try {
			//session = sessionManager.createNewSessionAndTransaction();
			Query query = session.createSQLQuery(viewQuery.toString());
			query.setResultTransformer(Transformers.aliasToBean(DefaultOutputJson.class));
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
		} 

		try {
			session.getTransaction().commit();
			return mapper.writeValueAsString(returnData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			session.close();
		}
		return "{\"Result\": \"Data not found\"}";
	}

	/*
	 * DEPRECATED - NO LONGER USED
	 * */
	@Deprecated
	public List<Top4> getTop4Records(String filterField1, String filterValue1, String filterField2, String totalOf, int minYear, int minMonth, int maxYear, int maxMonth) {

		Session session = HibernateUtil.getSessionFactory().openSession();
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

	/*
	 * DEPRECATED - NO LONGER USED
	 */
	@Deprecated
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
