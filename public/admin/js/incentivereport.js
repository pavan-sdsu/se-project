
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
		url: 'https://se532.herokuapp.com/incentiveReport',
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
				
				$("#tdisc").append (data.data.totalDiscount.toFixed(2)).append(" %");
				$("#avgdisc").append (data.data.averageDiscount.toFixed(2)).append(" %");
				var expinc = "";
				$.each(data.data.expectedIncome, function (index, element) {
					expinc += "<tr><td>" + element.date + "</td><td>" + element.incentiveDiscount + "</td></tr>";
					/*console.log(element.date);
					console.log(element.incentive);
					console.log(element.conventional);
					console.log(element.prepaid);
					console.log(element.sixtyDays);*/
					console.log(expinc);
					document.querySelector("tbody").innerHTML = expinc;
				});
				/* document.querySelector("tbody").innerHTML = rateTable;
			} else {
				rateTable += "No Rates Found";
				document.querySelector("tbody").innerHTML = rateTable;*/

				displayChart(data.data.expectedIncome)
			}
		}
		
	})
});

function displayChart(data) {	
	let dates = data.map(d => d.date);
	let incentiveDiscount = data.map(d => d.incentiveDiscount);
	
	var config = {
		type: 'line',
		data: {
			labels: dates,
			datasets: [{
				label: 'Income',
				backgroundColor: "#00e676",
				borderColor: "#00e676",
				data: incentiveDiscount,
				fill: false,
			}]
		},
		options: {
			responsive: true,
			title: {
				display: true,
				text: 'Incentive Discount Report'
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