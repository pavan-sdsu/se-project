$(document).ready(function () {
    var name = window.localStorage.getItem('name');
    var role = window.localStorage.getItem('role');
    $("#role").append("Welcome: " + name + "  Role: " + role);
    $("#success").hide();
    $("#updateSuccess").hide();
    refereshBaseRate();
    $("#updateBaseRates").click(function (event) {
        var arr = $("#txtDate").val().split(" ");
        baseRateMonth = arr[0];
        baseRateYear = Number(arr[1]);
        var months = [
            'January', 'February', 'March', 'April', 'May',
            'June', 'July', 'August', 'September',
            'October', 'November', 'December'
        ];
        var baseRateMonth = Number(months.indexOf(baseRateMonth) + 1);
        selectedBaseRate(baseRateMonth, baseRateYear);
    });
    $("#setRate").submit(function (event) {
        event.preventDefault();
        $("#success").hide();
        $("#success").empty();
        var startdate = $("#sdate").val();
        var enddate = $("#edate").val();
        var rate = Number($("#rate").val());
        if (startdate === "" || enddate === "") {
            $("#success").append("Please enter all required fields before submitting");
        }
        else {
            $.ajax({
                url: 'https://se532.herokuapp.com/setBaseRate',
                method: 'POST',
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify({
                    'fromDate': startdate,
                    'toDate': enddate,
                    'rate': rate
                }),
                success: function (data, status) {
                    if (data.success === 1) {
                        $("#success").append("Base Rate updated successfully");
                        $("#success").show();
                        refereshBaseRate();
                    }
                    else{
                        $("#success").append("Rate Already exists please use update rate functionality");
                        $("#success").show();
                    }
                }
            })
        }
    });
    $("#updateBaseRate").submit(function (event) {
        event.preventDefault();
        $("#updateSuccess").empty();
        var startdateUpdate = $("#sdateUpdate").val();
        var enddateUpdate = $("#edateUpdate").val();
        var rateUpdate = Number($("#rateUpdate").val());
        if (startdateUpdate === "" || enddateUpdate === "") {
            $("#updateSuccess").append("Please enter all required fields before submitting");
        }
        else {
            $.ajax({
                url: 'https://se532.herokuapp.com/updateBaseRate',
                method: 'POST',
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify({
                    'fromDate': startdateUpdate,
                    'toDate': enddateUpdate,
                    'rate': rateUpdate
                }),
                success: function (data, status) {
                    if (data.success === 1) {
                        $("#updateSuccess").append("Base Rate updated successfully");
                        refereshBaseRate();
                    }
                }
            })

        }
    });
    function refereshBaseRate() {
        var date = new Date();
        var currentMonth = (date.getMonth() + 1);
        var currentYear = date.getFullYear();
        $.ajax({
            url: 'https://se532.herokuapp.com/getRate',
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                'month': currentMonth,
                'year': currentYear
            }),
            success: function (data, status) {
                var rateTable = "";
                if (data.success === 1) {
                    $.each(data.data, function (index, element) {
                        rateTable += "<tr><td>" + element.date + "</td><td>" + element.rate + "</td></tr>";
                    });
                    document.querySelector("tbody").innerHTML = rateTable;
                } else {
                    rateTable += "No Rates Found";
                    document.querySelector("tbody").innerHTML = rateTable;
                }
            }
        })

    }
    function selectedBaseRate(baseRateMonth, baseRateYear) {
        $.ajax({
            url: 'https://se532.herokuapp.com/getRate',
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                'month': baseRateMonth,
                'year': baseRateYear
            }),
            success: function (data, status) {
                var rateTable = "";
                if (data.success === 1) {
                    $.each(data.data, function (index, element) {
                        rateTable += "<tr><td>" + element.date + "</td><td>" + element.rate + "</td></tr>";
                    });
                    document.querySelector("tbody").innerHTML = rateTable;
                } else {
                    rateTable += "No Rates Found";
                    document.querySelector("tbody").innerHTML = rateTable;
                }
            }
        })
    }
});