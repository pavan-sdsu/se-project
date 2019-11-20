var price=0;
function review_reservation(){
    var checkinval=localStorage.getItem("checkin");
    var checkoutval= localStorage.getItem("checkout");
    document.getElementById("checkin").placeholder=checkinval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2");
    document.getElementById("checkout").placeholder = checkoutval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2");
    document.getElementById("rooms").placeholder = localStorage.getItem("noRooms"); 
    document.getElementById("reservationType").placeholder = localStorage.getItem("reservationType"); 
    $.ajax({
        url: 'https://se532.herokuapp.com/calcAmount',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(
            {
            "fromDate": checkinval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2"),
            "toDate": checkoutval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2"),
            "noRooms": parseInt(localStorage.getItem("noRooms"))
            }
        ),
        success: function (data, status) {
            if (data.success === 1) {
                if(localStorage.getItem("reservationType")=='prepaid'){
                    price=data.data.nintyDaysAdv; 
                    document.getElementById("price").placeholder ="$"+ data.data.nintyDaysAdv; 
                }
                else if(localStorage.getItem("reservationType")=='sixtyDaysAdv'){
                    price=data.data.sixtyDaysAdv; 
                    document.getElementById("price").placeholder ="$"+  data.data.sixtyDaysAdv; 
                }
                else if(localStorage.getItem("reservationType")=='conventional'){
                    price=data.data.conventional; 
                    document.getElementById("price").placeholder = "$"+ data.data.conventional; 
                }
            }
        }
    })
}

function bookAndPay(pay){

    var checkinval=localStorage.getItem("checkin");
    var checkoutval= localStorage.getItem("checkout");
    var totalAmount=price;
    var amountPaid=0;
    if(localStorage.getItem("reservationType")=="prepaid") amountPaid=totalAmount;
    var first_name = $("#first_name").val();
    var last_name = $("#last_name").val();
    var phone_number = $("#phone_number").val();
    var email = $("#email").val();
    var birthday = $("#birthday").val();
    if(pay){
      var credit_card = $("#credit_card").val();
      var street1 = $("#street1").val();
      var street2 = $("#street2").val();
      var zip = $("#zip").val();
      var city = $("#city").val();
      var state = $("#state").val();
      var county = $("#county").val();
    }
    else{
      var credit_card = "";
      var street1 = "";
      var street2 = "";
      var zip = -1;
      var city = "";
      var state = "";
      var country = "";
    }
    
    if(!first_name.match(/^[A-Za-z]+$/)) alert("Please Enter Valid First Name");
    else
    if(!last_name.match(/^[A-Za-z]+$/)) alert("Please Enter Valid Last Name");
    else
    if(!phone_number.match(/^\+?([0-9]{5,})+$/)) alert("Please Enter Valid Phone Number");
    else
    if(!email.match(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)) alert("Please Enter Valid Email");
    else
    if(!moment(birthday, 'YYYY-MM-DD',true).isValid()) alert("Please Enter Valid Date in 'YYYY-MM-DD' format");
    else
    if(pay && !credit_card.match(/^([0-9]{13,16})+$/)) alert("Please Enter Credit Card Number");
    else
    if(pay && !street1.match(/^[A-Za-z0-9 '\.\-\#\s\,]+$/)) alert("Please Enter Valid Address1");
    else
    if(pay && street2!='' && !street1.match(/^[A-Za-z0-9 '\.\-\#\s\,]+$/) ) alert("Please Enter Valid Address2");
    else
    if(pay && !zip.match(/^[A-Za-z0-9 '\.\-\#\s\,]+$/)) alert("Please Enter Valid Zip");
    else
      if(pay && !city.match(/^[A-Za-z ]+$/)) alert("Please Enter Valid City");
    else
      if(pay && !state.match(/^[A-Za-z ]*$/)) alert("Please Enter Valid State");
    else
      if(pay && !county.match(/^[A-Za-z ]+$/)) alert("Please Enter Valid County");
    else{
      $.ajax({
          url: 'https://se532.herokuapp.com/checkout',
          method: 'POST',
          contentType: 'application/json',
          dataType: 'json',
          data: JSON.stringify({
              
                "user": {
                  "zip": zip,
                  "lastName": last_name,
                  "country": country,
                  "city": city,
                  "ccNo": credit_card,
                  "firstName": first_name,
                  "phoneNumber": phone_number,
                  "dob": birthday,
                  "addressLine1": street1,
                  "addressLine2": street2,
                  "state": state,
                  "email": email
                },
                "reservation": {
                "startDate":checkinval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2"),
                "endDate": checkoutval.replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$1-$2"),
                "reservationType": localStorage.getItem("reservationType"),
                "noRooms": parseInt(localStorage.getItem("noRooms")),
                "amountPaid": amountPaid,
                "totalAmount": totalAmount
              }
          }),
          success: function (data, status) {
              if (data.success === 1) {
                  localStorage.setItem("rid", data.data.rid); 
                  location.href='bookingCompleted.html';
              }
              else{
                alert('Error Occured During Booking Please Try Again');
              }
          }
      })
    }
}