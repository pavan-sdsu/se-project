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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
}