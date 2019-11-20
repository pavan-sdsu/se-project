var date = new Date();
var dd = date.getDate();
var mm = date.getMonth() + 1;
var yyyy = date.getFullYear();
var reservations = new Array();
var rooms = new Array();
if (dd < 10)
    dd = '0' + dd;
if (mm < 10)
    mm = '0' + mm;
var today = yyyy + '-' + mm + '-' + dd;
console.log(today);
function slectRoom(){
$('.dropdown-menu a').click(function () {
    $('#selected').text($(this).text());
    console.log($(this).text());
});

}
getAvailableRooms(function (roomdata, success) {
    var reserveroomhtml="";
    if (roomdata.success === 1) {
        $.each(roomdata.data, function (index, element) {
            rooms.push(element);
        });
    }
    else {
        console.log("No Rooms Available!!");
    }
    getAllReservations(function (reservationdata, success) {
        if (reservationdata.success === 0) {
            console.log(data.success);
        }
        else {
            $.each(reservationdata.data, function (index, element) {
                reservations.push(element.rid);
            });
            for (index in reservations) {
                //console.log(reservations[index]);
                reserveroomhtml += "<div class=\"row\">";
                reserveroomhtml += "<div class=\"col-sm-4\"> Reservation ID : " + reservations[index] + "</div>";
                reserveroomhtml += "<div class=\"col-sm-4\">";
                reserveroomhtml += "<div class=\"dropdown\">";
                reserveroomhtml += "<button class=\"btn btn-primary dropdown-toggle\" type=\"button\" id=\""+index+"\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">Select Room No</button>";
                reserveroomhtml += "<div class=\"dropdown-menu\">";
                for(index2 in rooms){
                reserveroomhtml += "<a class=\"dropdown-item\" href=\"#\">" + rooms[index2] +"</a>";
                }
                reserveroomhtml += "</div></div></div>"
                reserveroomhtml += "<div class=\"col-sm-4\">";
                reserveroomhtml += "<button type=\"button\" class=\"btn btn-primary\">Allocate Room</button>";
                reserveroomhtml += "</div>"
                reserveroomhtml += "</div>"
            }

            console.log(reserveroomhtml);
           $(".container1").append(reserveroomhtml);
            for (index in rooms) {
                console.log(rooms[index]);
            }
        }
    })
})

function getAvailableRooms(callback) {
    $.ajax({
        url: 'https://se532.herokuapp.com/getAvailableRooms',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({
            'date': today
        }),
        success: callback
    })
}
function getAllReservations(callback) {
    $.ajax({
        url: 'https://se532.herokuapp.com/getAllReservations',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({
            'date': today
        }),
        success: callback
    })
}
