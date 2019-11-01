package com.se.project.services;


import com.se.project.models.Response;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

@RestController
public class ReservationService {

	@PersistenceContext
	private EntityManager entityManager;

	@PostMapping("/getAllReservations")
	public Response getAllReservations(@RequestBody HashMap body) {
		Response res = new Response();

		String date = (String) body.get("date");

		String[] cols = {"r.rid", "u.firstName", "r.startDate", "r.endDate", "r.roomNo", "r.baseRate", "r.amountPaid", "r.totalAmount", "r.reservationType"};

		String query = "SELECT " + Arrays.toString(cols).replaceAll("\\[|\\]", "") + " FROM reservations r INNER JOIN user u ON r.userId = u.userId WHERE startDate <='" + date + "' AND endDate >= '" + date + "' and status='active'";
		List li = entityManager.createNativeQuery(query).getResultList();

		if(li.size() == 0) {
			res.setData("No data found");
			return res;
		}

		List data = new ArrayList();
		for(Object o: li) {
			Object[] rs = (Object[]) o;
			HashMap hm = new HashMap();
			int i = 0;
			for(String s: cols) {
				hm.put(s.split("\\.")[1], rs[i++]);
			}
			data.add(hm);
		}

		res.setSuccess(1);
		res.setData(data);

		return res;
	}

	@Transactional
	@PostMapping("/cancelReservation")
	public Response cancelReservation(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rid");
		String comment = (String) body.get("comment");

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET status = 'cancelled', comments = '" + comment + "' WHERE rId = " + rid + " AND status='active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Cancelled reservation successfully");

		return res;
	}


	@Transactional
	@PostMapping("/allocateRoom")
	public Response allocateRoom(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rid");
		int roomNo = (int) body.get("roomNo");

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET roomNo = " + roomNo + " WHERE rId = " + rid + " AND status = 'active';").executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Allocated room successfully");

		return res;
	}


	@Transactional
	@PostMapping("/checkInUser")
	public Response checkIn(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rid");
		String time = new Timestamp(System.currentTimeMillis()).toString();

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET checkinTime = '" + time + "' WHERE rId = " + rid + " AND status='active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Allocated room successfully");

		return res;
	}

	@Transactional
	@PostMapping("/checkOutUser")
	public Response checkOut(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rid");
		String time = new Timestamp(System.currentTimeMillis()).toString();

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET checkoutTime = '" + time + "' WHERE rId = " + rid + " AND status='active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Allocated room successfully");

		return res;
	}

	@Transactional
	@PostMapping("/getDefaulters")
	public Response getDefaulters(@RequestBody HashMap body) {
		/* TODO: startDate in response issue */
		Response res = new Response();

		int nDays = (int) body.get("nDays");
		LocalDate after = LocalDate.now().plusDays(nDays);

		String[] cols = {"r.rid", "r.startDate", "u.firstName", "u.lastName", "u.email", "u.phoneNumber"};

		List li = entityManager.createNativeQuery("SELECT " + Arrays.toString(cols).replaceAll("\\[|\\]", "")  + " FROM reservations r INNER JOIN user u ON r.userId = u.userId WHERE r.startDate = '" + after + "' AND r.status = 'active' AND r.reservationType = 'sixtyDays'").getResultList();

		if(li.size() == 0) {
			res.setData("No data found");
			return res;
		}

		List data = new ArrayList();
		for(Object o: li) {
			Object[] rs = (Object[]) o;
			HashMap hm = new HashMap();
			int i = 0;
			for(String s: cols) {
				hm.put(s.split("\\.")[1], rs[i++]);
			}
			data.add(hm);
		}

		res.setSuccess(1);
		res.setData(data);

		return res;
	}


	@Transactional
	@PostMapping("/getAvailableRooms")
	public Response getAvailableRooms(@RequestBody HashMap body) {
		/* TODO: Add leading zeros to roomNos */
		Response res = new Response();

		List totalRooms = new ArrayList();
		for (int i = 0; i < 45; i++) totalRooms.add(i);

		String date = (String) body.get("date");
		List li = entityManager.createNativeQuery("SELECT roomNo FROM reservations WHERE startDate='" + date + "' AND status = 'active'").getResultList();

		totalRooms.removeAll(li);

		if (totalRooms.size() == 0) {
			res.setData("No rooms available");
			return res;
		}

		res.setSuccess(1);
		res.setData(totalRooms);

		return res;
	}


	@Transactional
	@PostMapping("/payBill")
	public Response payBill(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rId");

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET amountPaid = totalAmount, lastPaymentTime = CURRENT_TIMESTAMP WHERE rId = '" + rid + "' AND status = 'active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Allocated room successfully");

		return res;
	}


	@Transactional
	@PostMapping("/changeResDate")
	public Response changeResDate(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rId");
		String fromDate = (String) body.get("fromDate");
		String toDate = (String) body.get("toDate");

		String[] cols = {"rId","userId","bookingTime","startDate","endDate","roomNo","checkinTime","checkoutTime","baseRate","amountPaid","totalAmount","reservationType","lastPaymentTime","lastModifiedTime","comments","status"};

		String colString = Arrays.toString(cols).replaceAll("\\[|\\]", "");

		List li = entityManager.createNativeQuery("SELECT " + colString + " from reservations where rid = " + rid + " AND status='active'").getResultList();

		if(li.size() == 0) {
			res.setData("No reservation found!");
			return res;
		}

		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		int updateRes = entityManager.createNativeQuery("UPDATE reservations SET status='inactive', lastModifiedTime = '" + timestamp + "' WHERE rid=" + rid + " AND status='active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("Reservation not set inactive");
			return res;
		}

		Object[] updatedValues = (Object[]) li.get(0);
		updatedValues[updatedValues.length-3] = timestamp.toString();
		updatedValues[3] = fromDate;
		updatedValues[4] = toDate;

		StringBuilder updatedValuesString = new StringBuilder("");
		for (int i = 0; i < updatedValues.length; i++) updatedValuesString.append("'" + updatedValues[i] + "',");


		String updateSqlString = "INSERT INTO reservations (" + colString + ") VALUES (" + updatedValuesString.substring(0, updatedValuesString.length()-1) + ")";
		updateRes = entityManager.createNativeQuery(updateSqlString).executeUpdate();

		if (updateRes == 0) {
			res.setData("No reservation inserted");
			return res;
		}

		res.setSuccess(1);
		res.setData("Changed date successfully");

		return res;
	}



	/* USER QUERIES */
	@Transactional
	@PostMapping("/getUserDetails")
	public Response getUserDetails(@RequestBody HashMap body) {
		Response res = new Response();

		String[] userCols = {"userId","firstName","lastName","dob","phoneNumber","email","addressLine1","addressLine2","city","state","country","zip","ccNo"};
		String userColsString = Arrays.toString(userCols).replaceAll("\\[|\\]", "");

		final String email = (String) body.get("email");

		List li = entityManager.createNativeQuery("SELECT " + userColsString + " FROM user WHERE email = '" + email + "'").getResultList();

		if (li.size() == 0) {
			int insertRes = entityManager.createNativeQuery("INSERT INTO user (email) VALUES ('" + email + "')").executeUpdate();

			if (insertRes == 0) {
				res.setData("Could not create a new user");
				return res;
			}

			li = entityManager.createNativeQuery("SELECT " + userColsString + " FROM user WHERE email = '" + email + "'").getResultList();
		}

		HashMap data = new HashMap();
		Object[] o = (Object[]) li.get(0);
		for (int i = 0; i < userCols.length; i++) {
			data.put(userCols[i], o[i]);
		}

		res.setSuccess(1);
		res.setData(data);

		return res;
	}


	@Transactional
	@PostMapping("/checkout")
	public Response checkout(@RequestBody HashMap body) {
		Response res = new Response();

		HashMap<String, Object> user = (HashMap) body.get("user");
		HashMap<String, Object> reservation = (HashMap) body.get("reservation");

		String[] reservationCols = new String[reservation.keySet().size() + 2];
		String[] reservationVals = new String[reservation.keySet().size() + 2];

		int i = 0;

		final int userId = (int) user.get("userId");
		user.remove("userId");

		StringBuilder userVals = new StringBuilder("");
		for (Map.Entry e: user.entrySet()) {
			userVals.append(e.getKey() + "='" + e.getValue() + "',");
			i++;
		}

		int updateRes = entityManager.createNativeQuery("UPDATE user SET " + userVals.substring(0, userVals.length() - 1) + " WHERE userId = " + userId).executeUpdate();
		if (updateRes == 0) {
			res.setData("User not updated");
			return res;
		}

		i = 0;
		for (Map.Entry e: reservation.entrySet()) {
			reservationCols[i] = (String) e.getKey();
			reservationVals[i] = "'" + e.getValue() + "'";
			i++;
		}

//		set user id
		reservationCols[i] = "userId";
		reservationVals[i] = "'" + userId + "'";
		i++;

//		set reservation id
		reservationCols[i] = "rId";
		reservationVals[i] = "(SELECT MAX(rId) FROM reservations r) + 1";

		String sql = "INSERT INTO reservations (" + colsToString(reservationCols) + ") VALUES (" + colsToString(reservationVals) + ")";
		System.out.println();
		updateRes = entityManager.createNativeQuery(sql).executeUpdate();
		if (updateRes == 0) {
			res.setData("Reservation not added");
			return res;
		}

		res.setSuccess(1);
		res.setData("Reservation added successfully");

		return res;
	}


	private String colsToString(String[] cols) {
		return Arrays.toString(cols).replaceAll("\\[|\\]", "");
	}


}
