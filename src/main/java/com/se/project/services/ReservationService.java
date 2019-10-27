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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
	@PostMapping("/checkIn")
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
	@PostMapping("/checkOut")
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



}
