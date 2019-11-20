var date = new Date();
var dd = date.getDate();
var mm = date.getMonth() + 1;
var yyyy = date.getFullYear();
if (dd < 10)
	dd = '0' + dd;
if (mm < 10)
	mm = '0' + mm;
var today = yyyy + '-' + mm + '-' + dd;
//var today = "2019-08-12";
console.log(today);
reservations(today);
var name = window.localStorage.getItem('name');
var role = window.localStorage.getItem('role');
$("#role").append("Welcome, " + name);
$("#regDate").attr('autocomplete', 'off');
var list = document.getElementById('card-id');

$("#getReg-btn").click(function () {
	var day = $("#regDate").val();
	console.log(day);
	reservations(day);

});
function reservations(resdate) {
	$("#nobookingalert").hide();
	$(".card").html("");
	today = resdate;
	$.ajax({
		url: 'https://se532.herokuapp.com/getAllReservations',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'date': today
		}),
		success: function (data, status) {
			var records = "";
			if (data.success === 0) {
				console.log(data.success);
				console.log(data.data);
				$("#nobookingalert").show();
			}

			else {
				$.each(data.data, function (index, element) {
					records += "<div class='card-header' id='" + element.rid + index + "' data-toggle='collapse' data-target='#collapse" + element.rid + "' aria-expanded='false' aria-controls='collapse" + element.rid + "'>Reservation ID :" + element.rid + "</div>";
					records += "<div id='collapse" + element.rid + "' class='collapse border' aria-labelledby='" + element.rid + index + "' data-parent='#accordionExample'><div class='card-body'> First Name: " + element.firstName + " <br> " + "Reservation start date : " + element.startDate + "<br>";
					records += "Reservation End Date : " + element.endDate + "<br>";
					records += "Total ammount : $" + element.totalAmount + "<br>";
					records += "Ammount Paid : $" + element.amountPaid + "<br>";
					records += "Reservation Type : " + element.reservationType + "<br>";
					if (element.checkinTime !== null) records += "Check In : " + moment(element.checkinTime).format("dddd, MMMM Do YYYY, h:mm:ss a") + "<br>";
					if (element.checkoutTime !== null) records += "Check Out : " + moment(element.checkoutTime).format("dddd, MMMM Do YYYY, h:mm:ss a") + "<br>";
					
					records += "Number of rooms : " + element.noRooms + "<br />"

					if (element.roomNo === null) records += "Room Number(s) : Not Allocated yet" + "<br>";
					else records += "Room Number(s) : " + element.roomNo + "<br>";

					if (element.checkinTime === null) 
						records += "<input type=\"button\" class =\"btn btn-primary\"  onclick=\"checkInModal(" + element.rid + ")\" value =\"Check in\">";
					else 
						records += "<input type=\"button\" class =\"btn btn-primary\"  onclick=\"checkInModal(" + element.rid + ")\" value =\"Check in\" disabled>";

					if (element.checkoutTime === null)
						records += "<input type=\"button\" class =\"btn btn-primary\" value =\"Check Out\" onclick=\"checkOutModal(" + element.rid + ")\">";
					else 
						records += "<input type=\"button\" class =\"btn btn-primary\" value =\"Check Out\" onclick=\"checkOutModal(" + element.rid + ")\" disabled>";

						records += "<input type=\"button\" class =\"btn btn-primary\" value =\"Modify Booking\">";

					if (!element.roomNo) 
						records += "<input type=\"button\" class =\"btn btn-primary\" value =\"Allocate Room\" onclick=\"openModal('#allocateRoomModal', " + element.noRooms + ", " + element.rid + ")\">";
					
					records += "<input type=\"button\" class =\"btn btn-primary\" value =\"Send Reminder\">";

					if (element.comments !== "Penalty charged" && element.amountPaid == 0 )
						records += "<input type='button' class ='btn btn-primary' value ='Charge Penalty' onclick='chargePenaltyModal('" + element.reservationType + "', " + element.rid + ",'" + element.ccNo + "')'>";
					else	
						records += "<input type='button' class ='btn btn-primary' value ='Charge Penalty' onclick='chargePenaltyModal('" + element.reservationType + "', " + element.rid + ",'" + element.ccNo + "')' disabled>";

						records += "</div></div>";
				});
				$(".card").append(records);
			}
		}
	})

}
function checkInModal(rid) {
	console.log(rid);
	$.ajax({
		url: 'https://se532.herokuapp.com/checkInUser',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'rid': rid
		}),

		success: function (data, status) {
			if (data.sucess == 0) return console.log('Error', data.message);
			$("#checkInModal").modal('show');
			reservations(today);
		}


	})
}

$(document).ready(() => {
	$('#allocateRoomModal').on('shown.bs.modal', function (e) {
		$.ajax({
			url: 'https://se532.herokuapp.com/getAvailableRooms',
			method: 'POST',
			contentType: 'application/json',
			dataType: 'json',
			data: JSON.stringify({
				"date": new Date().toJSON().substr(0, 10)
			}),
			success: function (data, status) {
				if (data.success == 0) return console.log('Error', data.message);
				let availRooms = data.data;
				let html = "";
				availRooms.forEach(room => {
					html += "<div class='form-check form-check-inline'><input class='form-check-input' type='checkbox' name='rooms' id='" + room + "' value='" + room + "'><label class='form-check-label border' for='" + room + "'>" + room + "</label></div>"
				});

				$("#roomAllocateForm").append(html);
			}
		})
	})
})

function openModal(target, noRooms, rid) {
	$(target).modal('show');
	$("#roomAllocateForm").attr("data-room-count", noRooms);
	$("#roomAllocateForm").attr("data-rid", rid);
}

$("#roomAllocateForm").on("submit", (e) => {
	e.preventDefault();
	let data = $("#roomAllocateForm").serializeArray();
	let rooms = [];
	data.forEach(d => rooms.push(d.value));

	const maxRooms = $("#roomAllocateForm").attr("data-room-count");
	const rid = $("#roomAllocateForm").attr("data-rid");

	if (rooms.length != maxRooms) {
		$("#allocateRoomModal .alert-danger").text("You can allocate only " + maxRooms + " rooms for this reservation");
		$("#allocateRoomModal .alert-danger").removeClass("d-none");
		return;
	} else $("#allocateRoomModal .alert-danger").addClass("d-none");

	$.ajax({
		url: 'https://se532.herokuapp.com/allocateRoom',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			"rid": Number(rid),
			"roomNo": rooms.toString()
		}),
		success: function (data, status) {
			if (data.sucess == 0) return console.log('Error', data.message);
			$("#allocateRoomModal .alert-success").removeClass("d-none");
			reservations(today);
		}
	})
})

function checkOutModal(rid) {
	console.log(rid);
	$.ajax({
		url: 'https://se532.herokuapp.com/checkOutUser',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'rid': rid
		}),

		success: function (data, status) {
			if (data.sucess == 0) return console.log('Error', data.message);
			$("#checkoutModal").modal('show');
			console.log(today);
			reservations(today);
		}


	})

}

function chargePenaltyModal(resType, rid, ccNu) {
	$("#chargePenaltyModal .alert-success").addClass("d-none");
	console.log("hello");
	console.log(resType, rid, ccNu);
	var CCEncoded;
	CCEncoded = "XXXX XXXX XXXX " + ccNu.substr(ccNu.length - 4);
	if (resType === 'incentive' || resType === 'conventional') {
		$("#chargePenaltyModal").modal('show');
		$("#ccData").html("Credit Card Number : " + CCEncoded);
		$("#ccData").append("<br>Penalty of First Day will be charged for no show");
	}
	else {
		$("#ccData").empty();
		$("#ccData").html("No Refund Will be provided");
		$("#chargePenaltyModal").modal('show');	
	}

	$("#chargePenalty").click(function(){
		$.ajax({
			url: 'https://se532.herokuapp.com/chargePenalty',
			method: 'POST',
			contentType: 'application/json',
			dataType: 'json',
			data: JSON.stringify({
				'rid': rid
			}),
	
			success: function (data, status) {
				if (data.success == 0) return console.log('Error', data.message);
				$("#chargePenaltyModal .alert-success").removeClass("d-none");
				$(".card").html("");
				reservations(today);
			}
	
	
		})
	
	});
}


