var date = new Date();
var dd = date.getDate();
var mm = date.getMonth() + 1;
var yyyy = date.getFullYear();
if (dd < 10)
dd = '0' + dd;
if (mm < 10)
mm = '0' + mm;
var today = yyyy + '-' + mm + '-' + dd;
var name = window.localStorage.getItem('name');
var role = window.localStorage.getItem('role');
$("#role").append("Welcome, " + name);
// var today = "2019-08-12";
console.log(today);
reservations(today);
var list = document.getElementById('card-id');
$("#getReg-btn").click(function () {
	var day = $("#endDate").val();
	console.log(day);
	reservations(day);
});
function reservations(endDate) {
	$("#nobookings").empty();
	$("#card-id").empty();
	$.ajax({
		url: 'https://se532.herokuapp.com/getAllReservations',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'date': endDate
		}),
		success: function (data, status) {
			var records = "";
			if (data.success === 0) {
				console.log(data.success);
				console.log(data.data);
				$("#nobookings").append("No Bookings found for today");
			}
			
			else {
				$.each(data.data, function (index, element) {
					//if (element.endDate === today) {
					records += "<div class='card-header border' id='" + element.rid + index + "' data-toggle='collapse' data-target='#collapse" + element.rid + "' aria-expanded='false' aria-controls='collapse" + element.rid + "'>Reservation ID :" + element.rid + "</div>";
					records += "<div id=\"collapse" + element.rid + "\" class=\"collapse\" aria-labelledby=\"" + element.rid + index + "\" data-parent=\"#accordionExample\"><div class=\"card-body\"> First Name: " + element.firstName + " <br> " + "Reservation start date : " + element.startDate + "<br>";
					records += "Reservation End Date : " + element.endDate + "<br>";
					records += "Total ammount : $" + element.totalAmount + "<br>";
					records += "Ammount Paid : $" + element.amountPaid + "<br>";
					records += "Reservation Type : " + element.reservationType + "<br>";
					if (element.checkinTime !== null) records += "Check In : " + moment(element.checkinTime).format("dddd, MMMM Do YYYY, h:mm:ss a") + "<br>";
					if (element.checkoutTime !== null) records += "Check Out : " + moment(element.checkoutTime).format("dddd, MMMM Do YYYY, h:mm:ss a") + "<br>";
					
					if (element.roomNo === null)
					records += "Room Number : Not Allocated yet" + "<br>";
					else
					records += "Room Number : " + element.roomNo + "<br>";
					
					records += "<input type=\"button\" class =\"btn btn-primary\" onclick=\"generatebill(" + element.rid + ")\" value =\"Generate Bill \" data-toggle=\"modal\"data-target=\"#generateModal\">";
					records += "<input type=\"button\" class =\"btn btn-primary\" onclick=\"paybill(" + element.rid + ")\" value =\"Pay Bill\">";
					records += "</div></div>";
					//}
				});
				$(".card").append(records);
			}
		}
	})
	
}

function paybill(rid) {
	console.log(rid);
	
	$.ajax({
		url: 'https://se532.herokuapp.com/payBill',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'rId': rid
		}),
		
		
		success: function (data, status) {
			alert(data.data);
			console.log(today);
			while (list.firstChild) {
				list.removeChild(list.firstChild);
			}
			reservations(today);
		}
		
		
	})
}

var html = $("#generateModal .modal-body").html();
function generatebill(rid) {
	console.log(rid);
	$.ajax({
		url: 'https://se532.herokuapp.com/generateBill',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'rid': rid
		}),
		
		success: function (data, status) {
			$("#generateModal .modal-body").html(html);
			$("#resid").append (data.data.rId);
			$("#uid").append (data.data.userId);
			$("#fname").append (data.data.firstName);
			$("#lname").append (data.data.lastName);
			$("#add1").append (data.data.addressLine1);
			$("#email").append (data.data.email);
			$("#add2").append (data.data.addressLine2);
			$("#ph").append (data.data.phoneNumber);
			$("#city").append (data.data.city);
			$("#state").append (data.data.state);
			$("#country").append (data.data.country);
			$("#zip").append (data.data.zip);
			$("#btime").append (moment(data.data.bookingTime).format("dddd, MMMM Do YYYY, h:mm:ss a"));
			$("#sdate").append (data.data.startDate);
			$("#cintime").append (moment(data.data.checkinTime).format("dddd, MMMM Do YYYY, h:mm:ss a"));
			$("#edate").append (data.data.endDate);
			$("#couttime").append (moment(data.data.checkoutTime).format("dddd, MMMM Do YYYY, h:mm:ss a"));
			$("#nrooms").append (data.data.noRooms);
			$("#rno").append (data.data.roomNo);
			$("#rtype").append (data.data.reservationType);
			$("#amt").append (data.data.amountPaid);
			$("#tamt").append (data.data.totalAmount);
			$("#issdt").append (moment().format("dddd, MMMM Do YYYY, h:mm:ss a"));
			reservations(today);
		}
		
		
	})
}