var tokenCounter;

function resetCountDown(){
    if(tokenCounter) clearInterval(tokenCounter);
    document.getElementById("expirationCounter").innerHTML = "Refresh token...";
}

function countdown(expirationDate){
    var countDownDate = expirationDate.getTime();
    if(tokenCounter) clearInterval(tokenCounter);
    tokenCounter = setInterval(function() {
        var now = new Date().getTime();
        var distance = countDownDate - now;
        if(distance > 0){
            var days = Math.floor(distance / (1000 * 60 * 60 * 24));
            var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
            var seconds = Math.floor((distance % (1000 * 60)) / 1000);
            document.getElementById("expirationCounter").innerHTML = "Current expires in "+days+"d "+hours+"h "+minutes+"m "+seconds+"s ";
        } else {
            document.getElementById("expirationCounter").innerHTML = "Token expired";
        }
    }, 0);
    return tokenCounter;
}

function toDate(str){
    var year = str.substring(0, 4);
    var month = str.substring(4, 6);
    var day = str.substring(6, 8);
    var hour = str.substring(8, 10);
    var minute = str.substring(10, 12);
    var second = str.substring(12, 14);
    return new Date(year, month-1, day, hour, minute, second);
}

function refreshAccess(onseccuss, onerror) {
    resetCountDown();

    // PREPARE FORM DATA
    var token = {
        "delegator" : $("#delegator").val()
    }


    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/token/refreshAccess",
        data: JSON.stringify(token),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            countdown(toDate(data.expiryTime));
            document.getElementById("tokenCost").innerHTML = "Took "+ data.elapsed + "ms";
            console.log("SUCCESS : ", data);
            if(onseccuss) onseccuss(data);
        },
        error: function (e) {
            document.getElementById("expirationCounter").innerHTML = "Erreur !";
            console.log("ERROR : ", e);
            if(onerror) onerror(e);
        }
    });
}