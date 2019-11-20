
$(document).ready(function () {
    var name = window.localStorage.getItem('name');
var role = window.localStorage.getItem('role');
$("#role").append("Welcome: " + name + "  Role: " + role);
    var date = new Date();
    var dd = date.getDate();
    var mm = date.getMonth() + 1;
    var yyyy = date.getFullYear();
    if (dd < 10)
        dd = '0' + dd;
    if (mm < 10)
        mm = '0' + mm;
    //var today = yyyy + '-' + mm + '-' + dd;
    today = "2019-12-13";
    console.log(today);
    $.ajax({
        url: 'https://se532.herokuapp.com/getOccupancyReport',
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

                var occtable = "";
                $.each(data.data, function (index, element) {
                    occtable += "<tr><td>" + element.firstName + "</td><td>" + element.lastName + "</td><td>" + element.statDate + "</td><td>" + element.endDate + "</td><td>" + element.email + "</td></tr>";
                    console.log(occtable);
                    document.querySelector("tbody").innerHTML = occtable;
                });
                
            }
        }

    })


});