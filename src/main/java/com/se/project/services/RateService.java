package com.se.project.services;

import com.se.project.models.Response;
import com.sun.istack.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.time.temporal.ChronoUnit.*;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
public class RateService {
	@PersistenceContext
	private EntityManager entityManager;


	@PostMapping("/getRate")
	public Response getRate(@RequestBody HashMap body) {
		Response response = new Response();

		int month = (int) body.get("month");
		int year = (int) body.get("year");

		YearMonth yearMonth = YearMonth.of( year, month );
		LocalDate firstOfMonth = yearMonth.atDay( 1 );
		LocalDate lastOfMonth = yearMonth.atEndOfMonth();

		String sql = "SELECT date, baseRate FROM rate WHERE date BETWEEN ?1 AND ?2 AND status='active'";

		List li = entityManager.createNativeQuery(sql)
				.setParameter(1, firstOfMonth)
				.setParameter(2, lastOfMonth)
				.getResultList();

		if(li.size() == 0) {
			response.setSuccess(0);
			response.setData("No rates found");
			return response;
		}

		List data = new ArrayList();

		for (int i = 0; i < li.size(); i++) {
			Object[] temp = (Object[]) li.get(i);
			HashMap<String, String> ans = new HashMap<String, String>();
			ans.put("date", String.valueOf(temp[0]));
			ans.put("rate", String.valueOf(temp[1]));
			data.add(ans);
		}

		response.setSuccess(1);
		response.setData(data);

		return response;
	}


	@Transactional
	@PostMapping("/setBaseRate")
	public Response setBaseRate(@RequestBody HashMap body) {
		Response res = new Response();

		String startDate = "'" + body.get("fromDate") + "'";
		String endDate = "'" + body.get("toDate") + "'";

		String sql = "SELECT date FROM rate WHERE date BETWEEN " + startDate + " AND " + endDate+ " AND status = 'active'";
		List exisitingVals = entityManager.createNativeQuery(sql).getResultList();
		if (exisitingVals.size() > 0) {
			res.setData("Rates already set for following dates " + exisitingVals.toString());
			return res;
		}

		String dateStr = getListOfRates(body, null);
		int insertRes = entityManager.createNativeQuery("INSERT INTO rate(date, baseRate, createdBy, createdTime) VALUES " + dateStr)
				.executeUpdate();

		if (insertRes == 0) {
			res.setSuccess(0);
			res.setData("No entries inserted");
			return res;
		}

		res.setSuccess(1);
		res.setData("Inserted values successfully");

		return res;
	}


	@Transactional
	@PostMapping("/updateBaseRate")
	public Response updateBaseRate(@RequestBody HashMap body) {
		/*TODO: Check if values updated and no. of dates are same*/
		Response res = new Response();

		LocalDate[] localDate = parseFromToDates(body);
		LocalDate fromDate = localDate[0];
		LocalDate toDate = localDate[1];

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		int updateRes = entityManager.createNativeQuery("UPDATE rate SET status='inactive', endTime='" + timestamp + "' WHERE date BETWEEN ?1 AND ?2")
				.setParameter(1, fromDate)
				.setParameter(2, toDate)
				.executeUpdate();

		if (updateRes == 0) {
			res.setSuccess(0);
			res.setData("No entries inserted");
			return res;
		}

		String dateStr = getListOfRates(body, timestamp);
		System.out.println(dateStr);
		int insertRes = entityManager.createNativeQuery("INSERT INTO rate(date, baseRate, createdBy, createdTime) VALUES " + dateStr)
				.executeUpdate();

		if (insertRes == 0) {
			res.setSuccess(0);
			res.setData("No entries inserted");
			return res;
		}

		res.setSuccess(1);
		res.setData("Inserted values successfully");

		return res;
	}


	@Transactional
	@PostMapping("/calcAmount")
	public Response calcAmount(@RequestBody HashMap body) {
		Response res = new Response();
		int noRooms = (int) body.get("noRooms");
		LocalDate[] localDate = parseFromToDatesWOAdd(body);
		LocalDate fromDate = localDate[0];
		LocalDate toDate = localDate[1];
		LocalDate currDate = LocalDate.now();
		long daysBetween = DAYS.between(currDate, fromDate);
		long daysOfStay = DAYS.between(fromDate, toDate);
		double nintyDaysAdv = 0;
		double sixtyDaysAdv = 0;
		double conventional = 0;

		String sql = "SELECT date, baserate from rate where date BETWEEN ?1 AND ?2 AND status='active'";
		List li = entityManager.createNativeQuery(sql)
				.setParameter(1, fromDate)
				.setParameter(2, toDate)
				.getResultList();

		if(li.size() == 0) {
			res.setSuccess(0);
			res.setData("No rates found");
			return res;
		}
		// form a hashmap and of all the dates from the start to the end with the baseRate.
		double totalForOneRoom = 0;
		HashMap<String, Double> dateWiseBaseRates = new HashMap<String, Double>();
		for (int i = 0; i < li.size(); i++) {
			Object[] temp = (Object[]) li.get(i);
			System.out.println(String.valueOf(temp[0]));
			System.out.println(Double.valueOf(temp[1].toString()));
			dateWiseBaseRates.put(String.valueOf(temp[0]),Double.valueOf(temp[1].toString()));
			totalForOneRoom = totalForOneRoom + Double.valueOf(temp[1].toString());
		}
		boolean giveIncDisc = false;
		if (daysBetween < 30) {
			// Update the hashmap with the daily incentive discount
			int cumulativeOccupancy = 0;
			Iterator dateWiseBaseRatesItr = dateWiseBaseRates.entrySet().iterator();
			while (dateWiseBaseRatesItr.hasNext()) {
				Map.Entry mapElement = (Map.Entry)dateWiseBaseRatesItr.next();
				cumulativeOccupancy = cumulativeOccupancy + getOccupancy(mapElement.getKey().toString());
			}
			if((cumulativeOccupancy / daysOfStay) <= (0.60 * 45)) {
				giveIncDisc = true;
			}
		}
		//Multiply with the Number of rooms
		double totalForAllRooms = totalForOneRoom * noRooms;

		if (daysBetween >= 90){
			//set all the three
			nintyDaysAdv = 0.75 * totalForAllRooms;
			sixtyDaysAdv = 0.85 * totalForAllRooms;
			conventional = totalForAllRooms;
		} else if (daysBetween < 90 && daysBetween >= 60){
			sixtyDaysAdv = 0.85 * totalForAllRooms;
			conventional = totalForAllRooms;
		}else{
			if(daysBetween < 30 && giveIncDisc) {
				conventional = 0.80 * totalForAllRooms;
			}else{
				conventional = totalForAllRooms;
			}
		}
		HashMap hm = new HashMap();
		hm.put("nintyDaysAdv",nintyDaysAdv);
		hm.put("sixtyDaysAdv",sixtyDaysAdv);
		hm.put("conventional",conventional);
		res.setData(hm);
		res.setSuccess(1);
		return res;
	}

	private int getOccupancy(String date){
		int[] dateIntArray = Arrays.stream(((String) date).split("-")).mapToInt(Integer::parseInt).toArray();
		LocalDate formattedDate = LocalDate.of(dateIntArray[0], dateIntArray[1], dateIntArray[2]);
		String sql = "Select count(noRooms) from reservations where startDate <= ?1 AND endDate > ?2 AND status='active'";
		List li = entityManager.createNativeQuery(sql)
				.setParameter(1, formattedDate)
				.setParameter(2, formattedDate)
				.getResultList();
		return Integer.parseInt(li.get(0).toString());
	}

	private String getListOfRates(HashMap body, @Nullable Timestamp timestamp) {
		LocalDate[] localDate = parseFromToDates(body);
		LocalDate fromDate = localDate[0];
		LocalDate toDate = localDate[1];
		int rate = (int) body.get("rate");
		StringBuffer sb = new StringBuffer("");
		if(timestamp == null) timestamp = new Timestamp(System.currentTimeMillis());
		while (fromDate.isBefore(toDate)) {
			sb.append("('" + fromDate.toString() +  "', '" + rate + "', '1', '" + timestamp + "'),");
			fromDate = fromDate.plusDays(1);
		}
		return sb.substring(0, sb.length() - 1);
	}

	private LocalDate[] parseFromToDates(HashMap body) {
		LocalDate[] localDate = new LocalDate[2];
		int[] from = Arrays.stream(((String) body.get("fromDate")).split("-")).mapToInt(Integer::parseInt).toArray();
		int[] to = Arrays.stream(((String) body.get("toDate")).split("-")).mapToInt(Integer::parseInt).toArray();
		LocalDate fromDate = LocalDate.of(from[0], from[1], from[2]);
		LocalDate toDate = LocalDate.of(to[0], to[1], to[2]);
		toDate = toDate.plusDays(1);
		localDate[0] = fromDate;
		localDate[1] = toDate;
		return localDate;
	}

	private LocalDate[] parseFromToDatesWOAdd(HashMap body) {
		LocalDate[] localDate = new LocalDate[2];
		int[] from = Arrays.stream(((String) body.get("fromDate")).split("-")).mapToInt(Integer::parseInt).toArray();
		int[] to = Arrays.stream(((String) body.get("toDate")).split("-")).mapToInt(Integer::parseInt).toArray();
		LocalDate fromDate = LocalDate.of(from[0], from[1], from[2]);
		LocalDate toDate = LocalDate.of(to[0], to[1], to[2]);
		localDate[0] = fromDate;
		localDate[1] = toDate;
		return localDate;
	}

}