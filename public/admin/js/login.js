$(".fas.fa-spinner.fa-spin").hide();
$(".alert.alert-danger").hide();
$("form").submit(function(event){
    $("#error").empty();
    event.preventDefault();
    var emailid = $("#inputEmail").val();
    var pwd = $("#inputPassword").val();
    $(".alert.alert-danger").hide();
    $(".fas.fa-spinner.fa-spin").show();
$.ajax({
        url: 'https://se532.herokuapp.com/login',
        method: 'POST',
        contentType : 'application/json',
        dataType : 'json',
        data: JSON.stringify({
            'email': emailid,
            'password': pwd
        }),
        success: function (data, status) {
        console.log(data.success);
        console.log(data.data);
        $(".fas.fa-spinner.fa-spin").hide();
        if ( data.success === 0){
            $(".alert.alert-danger").show();
        }
        else{
            console.log(data.data.role);
            console.log(data.data.firstName);
            window.localStorage.setItem('role', data.data.role);
            window.localStorage.setItem('name', data.data.firstName);
            window.location.href = "managebooking.html";
        }
    }
})
});