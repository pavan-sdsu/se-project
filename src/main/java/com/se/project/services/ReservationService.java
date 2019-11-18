package com.se.project.services;


import com.se.project.models.Response;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@RestController
public class ReservationService {

	@PersistenceContext
	private EntityManager entityManager;

	@PostMapping("/getAllReservations")
	public Response getAllReservations(@RequestBody HashMap body) {
		Response res = new Response();

		String date = (String) body.get("date");

		String[] cols = {"r.rid", "u.firstName", "r.startDate", "r.endDate", "r.noRooms", "r.roomNo", "r.amountPaid", "r.totalAmount", "r.reservationType", "r.checkinTime", "r.checkoutTime", "r.comments", "u.ccNo"};

		String query = "SELECT " + colsToString(cols) + " FROM reservations r INNER JOIN user u ON r.userId = u.userId WHERE startDate <='" + date + "' AND endDate >= '" + date + "' and status='active'";
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
	@PostMapping("/chargePenalty")
	public Response chargePenalty(@RequestBody HashMap body) {
		Response res = new Response();
		String comments = "Penalty charged";
		int rid = (int) body.get("rid");
		String[] cols = {"r.rid", "rBR.baseRate", "r.amountPaid", "r.reservationType", "r.noRooms"};
		String query = "SELECT " + colsToString(cols) + " FROM reservations r INNER JOIN resBaseRates rBR ON rBR.uid = r.uid and rBR.date = r.startDate WHERE rId ='" + rid + "' and status='active'";
		List li = entityManager.createNativeQuery(query).getResultList();

		if(li.size() == 0) {
			res.setData("No data found");
			return res;
		}
		Object[] rs = (Object[]) li.get(0);
		HashMap hm = new HashMap();
		int i = 0;
		for(String s: cols) {
			hm.put(s.split("\\.")[1], rs[i++]);
		}
		if ("conventional".equals(hm.get("reservationType")) || "incentive".equals(hm.get("reservationType"))){
		    Float paid_amt = Float.parseFloat(hm.get("baseRate").toString()) * Float.parseFloat(hm.get("noRooms").toString());
			hm.put("amountPaid",paid_amt);
		}
		final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String SQLString = "UPDATE reservations SET amountPaid = '" + hm.get("amountPaid") + "', comments = '" + comments + "', lastPaymentTime = CURRENT_TIMESTAMP, lastModifiedTime = '" + timestamp + "' WHERE rId = '" + rid + "' AND status = 'active'";
		int updateRes = entityManager.createNativeQuery(SQLString).executeUpdate();

		if (updateRes == 0) {
			res.setData("No rows updated");
			return res;
		}
		res.setSuccess(1);
		res.setData("Penalty charged successfully");
		return res;
	}

    @Transactional
    @PostMapping("/generateBill")
    public Response generateBill(@RequestBody HashMap body) {
        Response res = new Response();
        int rid = (int) body.get("rid");
        String[] cols = {"r.rId", "r.bookingTime", "r.startDate", "r.endDate", "r.noRooms", "r.roomNo", "r.checkinTime", "r.checkoutTime", "r.amountPaid", "r.totalAmount", "r.reservationType", "u.userId", "u.firstName", "u.lastName", "u.phoneNumber", "u.email", "u.addressLine1", "u.addressLine2", "u.city", "u.state", "u.country", "u.zip"};
        String query = "SELECT " + colsToString(cols) + " FROM reservations r INNER JOIN user u on r.userId = u.userId where rId ='" + rid + "' and status='active'";
        List li = entityManager.createNativeQuery(query).getResultList();

        if(li.size() == 0) {
            res.setData("No data found");
            return res;
        }
        Object[] rs = (Object[]) li.get(0);
        HashMap hm = new HashMap();
        int i = 0;
        for(String s: cols) {
            hm.put(s.split("\\.")[1], rs[i++]);
        }
        res.setSuccess(1);
        res.setData(hm);
        return res;
    }

	@Transactional
	@PostMapping("/allocateRoom")
	public Response allocateRoom(@RequestBody HashMap body) {
		Response res = new Response();

		int rid = (int) body.get("rid");
		String roomNo = "'" + body.get("roomNo") + "'";

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

		List li = entityManager.createNativeQuery("SELECT " + colsToString(cols) + " FROM reservations r INNER JOIN user u ON r.userId = u.userId WHERE r.startDate = '" + after + "' AND r.status = 'active' AND r.reservationType = 'sixtyDays'").getResultList();

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
		Response res = new Response();

		String[] roomsArr = {
			"001","002","003","004","005","006","007","008","009","010",
			"011","012","013","014","015","016","017","018","019","020",
			"021","022","023","024","025","026","027","028","029","030",
			"031","032","033","034","035","036","037","038","039","040",
			"041","042","043","044","045"
		};
		List totalRooms = new ArrayList(Arrays.asList(roomsArr));

		String date = (String) body.get("date");
		List li = entityManager.createNativeQuery("SELECT roomNo FROM reservations WHERE startDate='" + date + "' AND status = 'active'").getResultList();

		for (int i = 0; i < li.size(); i++) {
			if(li.get(i) == null) continue;
			String[] rooms = ((String) li.get(i)).split(",");
			totalRooms.removeAll(Arrays.asList(rooms));
		}


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

		String[] cols = {"rId","userId","bookingTime","startDate","endDate","roomNo","checkinTime","checkoutTime","amountPaid","totalAmount","reservationType","lastPaymentTime","lastModifiedTime","comments","status"};

		List li = entityManager.createNativeQuery("SELECT " + colsToString(cols) + " from reservations where rid = " + rid + " AND status='active'").getResultList();

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


		String updateSqlString = "INSERT INTO reservations (" + colsToString(cols) + ") VALUES (" + updatedValuesString.substring(0, updatedValuesString.length()-1) + ")";
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
	@PostMapping("/checkout")
	public Response checkout(@RequestBody HashMap body) {
		Response res = new Response();

		HashMap<String, Object> user = (HashMap) body.get("user");
		HashMap<String, Object> reservation = (HashMap) body.get("reservation");

		String[] reservationCols = new String[reservation.keySet().size() + 3];
		String[] reservationVals = new String[reservation.keySet().size() + 3];

		int i = 0;

		String[] userCols = new String[user.keySet().size() + 1];
		String[] userVals = new String[user.keySet().size() + 1];
		for (Map.Entry e: user.entrySet()) {
			userCols[i] = String.valueOf(e.getKey());
			userVals[i] = "'" + e.getValue() + "'";
			i++;
		}

		String userId = String.valueOf(entityManager.createNativeQuery("SELECT MAX(userId) + 1 FROM user").getResultList().get(0));
		userCols[i] = "userId";
		userVals[i] = userId;

		String sql = "INSERT INTO user (" + colsToString(userCols) + ") VALUES (" + colsToString(userVals) + ")";
		int updateRes = entityManager.createNativeQuery(sql).executeUpdate();
		if (updateRes == 0) {
			res.setData("User not updated");
			return res;
		}

		List uidList = entityManager.createNativeQuery("SELECT MAX(uid) + 1 FROM reservations").getResultList();
		String uid = String.valueOf(uidList.get(0));

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
		i++;

//		set uid
		reservationCols[i] = "uid";
		reservationVals[i] = uid;

		/* INSERT resBaseRates */
		LocalDate startDate = LocalDate.parse((String) reservation.get("startDate"));
		LocalDate endDate = LocalDate.parse((String) reservation.get("endDate"));

		/* Get rates for days */
//		List rates = entityManager.createNativeQuery("SELECT baseRate FROM rate WHERE date BETWEEN '" + startDate + "' AND '" + endDate + "' AND status = 'active'").getResultList();
		List rates = entityManager.createNativeQuery("SELECT baseRate FROM rate WHERE date <'" + endDate + "' AND date >= '" + startDate + "' AND status = 'active'").getResultList();
		StringBuilder resBaseRatesVals = new StringBuilder("");

//		endDate = endDate.plusDays(1);

		if(DAYS.between(startDate, endDate) != rates.size()) {
			res.setData("Base rate not found");
			return res;
		}

		int j = 0;
		while (startDate.isBefore(endDate)) {
			resBaseRatesVals.append("(" + uid + ", '" + startDate + "', '" + rates.get(j++) + "'),");
			startDate = startDate.plusDays(1);
		}
		/* Get rates for days */

		sql = "INSERT INTO resBaseRates(uid, date, baseRate) VALUES " + resBaseRatesVals.substring(0, resBaseRatesVals.length()-1);

		updateRes = entityManager.createNativeQuery(sql).executeUpdate();
		if (updateRes == 0) {
			res.setData("ResBaseRates not added");
			return res;
		}
		/* INSERT resBaseRates */


		/* INSERT reservation */
		sql = "INSERT INTO reservations (" + colsToString(reservationCols) + ") VALUES (" + colsToString(reservationVals) + ")";
		updateRes = entityManager.createNativeQuery(sql).executeUpdate();
		if (updateRes == 0) {
			res.setData("Reservation not added");
			return res;
		}
		/* INSERT reservation */


		res.setSuccess(1);
		res.setData("Reservation added successfully");

		return res;
	}


	@Transactional
	@PostMapping("/makePayment")
	public Response makePayment(@RequestBody HashMap body) {
		Response res = new Response();

		final int rId = (int) body.get("rId");
		final String ccNo = (String) body.get("ccNo");

		int updateRes = entityManager.createNativeQuery("UPDATE user u INNER JOIN reservations r ON u.userId = r.userId SET u.ccNo=" + ccNo + " WHERE r.rId = " + rId + " AND r.status = 'active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("Credit card not added to user");
			return res;
		}

		updateRes = entityManager.createNativeQuery("UPDATE reservations SET amountPaid = totalAmount WHERE rId = " + rId + " AND status='active'").executeUpdate();

		if (updateRes == 0) {
			res.setData("Reservations not updated");
			return res;
		}

		res.setSuccess(1);
		res.setData("Amount paid successfully");
		return res;
	}


	@Transactional
	@PostMapping("/getBooking")
	public Response getBooking(@RequestBody HashMap body) {
		Response res = new Response();

		int rId = (int) body.get("rId");
		String email = "'" + body.get("email") + "'";
		String dob = "'" + body.get("dob") + "'" ;

		String[] cols = {"r.rId", "r.userId", "r.bookingTime", "r.startDate", "r.endDate", "r.amountPaid", "r.totalAmount", "r.reservationType", "u.firstName", "u.lastName"};

		String sql = "SELECT " + colsToString(cols) + " FROM reservations r INNER JOIN user u ON r.userId = u.userId WHERE r.rId = " + rId + " AND u.email = " + email + " AND u.dob = " + dob + " AND r.status = 'active'";

		List li = entityManager.createNativeQuery(sql).getResultList();

		if (li.size() == 0) {
			res.setData("No reservation found!");
			return res;
		}

		Object[] values = (Object[]) li.get(0);
		HashMap hm = new HashMap();
		for (int i = 0; i < cols.length; i++) hm.put(cols[i].split("\\.")[1], values[i]);

		res.setData(hm);
		res.setSuccess(1);

		return res;
	}


	@Transactional
	@PostMapping("/expOccupancyReport")
	public Response expOccupancyReport(@RequestBody HashMap body) {
		Response res = new Response();

		LocalDate startDate = LocalDate.parse((String) body.get("date"));
		LocalDate endDate = startDate.plusDays(30);

		List li = entityManager.createNativeQuery("SELECT COUNT(*) AS count, reservationType, startDate FROM reservations WHERE startDate BETWEEN '" + startDate + "' AND '" + endDate + "' AND endDate BETWEEN '" + startDate + "' AND '" + endDate + "' AND status='active' GROUP BY reservationType, startDate ORDER BY startDate;").getResultList();

		if (li.size() == 0) {
			res.setData("No data found");
			return res;
		}

		HashMap<Object, List> organizing = new HashMap<>();
		for (int i = 0; i < li.size(); i++) {
			Object[] o = (Object[]) li.get(i);
			List al;
			final Object date = o[2];
			if (organizing.containsKey(date)) {
				al = organizing.get(date);
			} else {
				al = Arrays.asList(0, 0, 0, 0);
			}

			final String reservationType = String.valueOf(o[1]);
			final int count = ((BigInteger) o[0]).intValue();

			if (reservationType.equals("prepaid")) al.set(0, count);
			else if (reservationType.equals("sixtyDays")) al.set(1, count);
			else if (reservationType.equals("conventional")) al.set(2, count);
			else al.set(3, count);

			organizing.put(date, al);
		}


		HashMap[] arr = new HashMap[organizing.size()];
		int i = 0;
		float[] occRates = new float[organizing.size()];
		for (Map.Entry e: organizing.entrySet()) {
			HashMap hm = new HashMap();
			List<Integer> al = (List<Integer>) e.getValue();

			hm.put("date", e.getKey());
			hm.put("prepaid", al.get(0));
			hm.put("sixtyDays", al.get(1));
			hm.put("conventional", al.get(2));
			hm.put("incentive", al.get(3));

			arr[i] = hm;
			occRates[i] = ((al.get(0) + al.get(1) + al.get(2) + al.get(3)) / (float)45);

			i++;
		}

		float sum = 0;
		for (i = 0; i < occRates.length; i++) sum += occRates[i] * 100;

		HashMap data = new HashMap();
		data.put("occupancy", arr);
		data.put("averageOccupancy", (sum/occRates.length));

		res.setSuccess(1);
		res.setData(data);

		return res;
	}

    @Transactional
    @PostMapping("/expRoomIncome")
    public Response expRoomIncome(@RequestBody HashMap body) {
        Response res = new Response();

        LocalDate startDate = LocalDate.parse((String) body.get("date"));
        LocalDate endDate = startDate.plusDays(30);

//        List li = entityManager.createNativeQuery("SELECT COUNT(*) AS count, reservationType, startDate FROM reservations WHERE startDate BETWEEN '" + startDate + "' AND '" + endDate + "' AND endDate BETWEEN '" + startDate + "' AND '" + endDate + "' AND status='active' GROUP BY reservationType, startDate ORDER BY startDate;").getResultList();
        List li = entityManager.createNativeQuery("SELECT rbr.date, SUM(r.totalAmount/r.noRooms) expectedIncome FROM reservations r INNER JOIN resBaseRates rbr ON r.uid=rbr.uid WHERE rbr.date <'" + endDate + "' AND rbr.date >= '" + startDate + "'\n" +
                "AND r.status = 'active' GROUP BY rbr.date").getResultList();

        if (li.size() == 0) {
            res.setData("No data found");
            return res;
        }

        List data = new ArrayList();
        float totalIncome = 0;

        for(Object o: li) {
            Object[] rs = (Object[]) o;
            HashMap hm = new HashMap();
            hm.put("date", rs[0]);
            hm.put("expectedIncome", rs[1]);
            totalIncome = totalIncome + Float.parseFloat(rs[1].toString());
            data.add(hm);
        }

	    HashMap result = new HashMap();
        result.put("expectedIncome", data);
        result.put("totalIncome", totalIncome);
        result.put("averageIncome", totalIncome/30);
        res.setSuccess(1);
        res.setData(result);
        return res;
    }

    @Transactional
    @PostMapping("/getOccupancyReport")
    public Response getOccupancyReport(@RequestBody HashMap body) {
        Response res = new Response();
        LocalDate date = LocalDate.parse((String) body.get("date"));
        System.out.println("date==="+date);
        LocalDate prevDate = LocalDate.parse((String) body.get("date")).minusDays(1);
        System.out.println("prevDate==="+prevDate);
        String query = "SELECT u.email, u.firstName, u.lastName, r.startDate, r.endDate, (r.startDate <='" + prevDate + "') AS lastEvening, (r.endDate ='" + date + "') AS departsToday FROM reservations r INNER JOIN user u ON u.userId = r.userId WHERE r.checkinTime IS NOT NULL and r.checkoutTime IS NULL AND r.startDate <='" + date + "' AND r.endDate >= '" + date + "' AND r.status='active'";
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
            hm.put("email", rs[0]);
            hm.put("firstName", rs[1]);
            hm.put("lastName", rs[2]);
            hm.put("statDate", rs[3]);
            hm.put("endDate", rs[4]);
            hm.put("lastEvening", rs[5]);
            hm.put("departsToday", rs[6]);
            data.add(hm);
        }
        res.setSuccess(1);
        res.setData(data);
        return res;
    }

    @Transactional
	@PostMapping("/incentiveReport")
	public Response incentiveReport(@RequestBody HashMap body) {
        Response res = new Response();

        LocalDate startDate = LocalDate.parse((String) body.get("date"));
        LocalDate endDate = startDate.plusDays(30);

        List li = entityManager.createNativeQuery("SELECT  rbr.date, SUM((rbr.baseRate * r.noRooms * 0.2)) AS loss FROM `reservations` r INNER JOIN resBaseRates rbr ON rbr.uid = r.uid WHERE r.`reservationType` = 'incentive' AND rbr.date <'" + endDate + "' AND rbr.date >= '" + startDate + "' AND r.status = 'active' GROUP BY rbr.date").getResultList();

        if (li.size() == 0) {
            res.setData("No data found");
            return res;
        }

        List data = new ArrayList();
        float totalDisc = 0;

        for(Object o: li) {
            Object[] rs = (Object[]) o;
            HashMap hm = new HashMap();
            hm.put("date", rs[0]);
            hm.put("incentiveDiscount", rs[1]);
            totalDisc = totalDisc + Float.parseFloat(rs[1].toString());
            data.add(hm);
        }

        HashMap result = new HashMap();
        result.put("expectedIncome", data);
        result.put("totalDiscount", totalDisc);
        result.put("averageDiscount", totalDisc/30);
        res.setSuccess(1);
        res.setData(result);
        return res;
	}


	/* Utility methods */
	private String colsToString(String[] cols) {
		return Arrays.toString(cols).replaceAll("\\[|\\]", "");
	}

}
