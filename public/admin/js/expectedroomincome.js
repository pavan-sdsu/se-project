
$(document).ready(function () {
	var name = window.localStorage.getItem('name');
	var role = window.localStorage.getItem('role');
	$("#role").append("Welcome, " + name);
	var date = new Date();
	var dd = date.getDate();
	var mm = date.getMonth() + 1;
	var yyyy = date.getFullYear();
	if (dd < 10)
	dd = '0' + dd;
	if (mm < 10)
	mm = '0' + mm;
	//var today = yyyy + '-' + mm + '-' + dd;
	today = "2019-12-12";
	console.log(today);
	$.ajax({
		url: 'https://se532.herokuapp.com/expRoomIncome',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify({
			'date': today
		}),
		success: function (data, status) {
			
			// console.log(data.data);
			//console.log(data.success);
			if (data.success === 1) {
				console.log(data.data.averageIncome);
				
				$("#totalinc").append ("$" + data.data.totalIncome.toFixed(2));
				$("#avginc").append ("$" + data.data.averageIncome.toFixed(2));
				var einctable = "";
				$.each(data.data.expectedIncome, function (index, element) {
					einctable += "<tr><td>" + element.date + "</td><td>" + element.expectedIncome +"</td></tr>";
					/*console.log(element.date);
					console.log(element.incentive);
					console.log(element.conventional);
					console.log(element.prepaid);
					console.log(element.sixtyDays);*/
					console.log(einctable);
					document.querySelector("tbody").innerHTML = einctable;
				});
				loadChart(data.data.expectedIncome);
				/* document.querySelector("tbody").innerHTML = rateTable;
			} else {
				rateTable += "No Rates Found";
				document.querySelector("tbody").innerHTML = rateTable;*/
			}
		}
		
	})
});

function loadChart(data) {
	let dates = data.map(d => d.date);
	let expInc = data.map(d => d.expectedIncome);
	
	var config = {
		type: 'line',
		data: {
			labels: dates,
			datasets: [{
				label: 'Income',
				backgroundColor: "#00e676",
				borderColor: "#00e676",
				data: expInc,
				fill: false,
			}]
		},
		options: {
			responsive: true,
			title: {
				display: true,
				text: 'Expected Room Income Report'
			},
			tooltips: {
				mode: 'index',
				intersect: false,
			},
			hover: {
				mode: 'nearest',
				intersect: true
			},
			scales: {
				xAxes: [{
					display: true,
					scaleLabel: {
						display: true,
						labelString: 'Date'
					}
				}],
				yAxes: [{
					display: true,
					scaleLabel: {
						display: true,
						labelString: 'Income'
					}
				}]
			}
		}
	};
	
	var ctx = document.getElementById('canvas').getContext('2d');
	window.myLine = new Chart(ctx, config);
	
}