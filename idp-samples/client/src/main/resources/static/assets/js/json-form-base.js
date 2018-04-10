function getBasicAuthorization() {
    return 'Basic ' + btoa('gestF:gestF');
}

$.postJson2Json = function(url, data, callback, errorInfo, authorization) {
    return jQuery.ajax({
        type: 'POST',
        url: url,
        data: data,
        dataType : 'json',   //you may use jsonp for cross origin request
        crossDomain:true,
        headers: {
            'Content-Type':'application/json',
            'Authorization' : authorization
        },
        success: callback,
        error : errorInfo
    });
};




callWS = function(errors, values){
    refreshAccess(function(token){
        var auth = 'Bearer '+token.serialized;
        $.postJson2Json(getSGIWSUri(), JSON.stringify(values), onSGIWSResponse, onSGIWSError, auth);
    }, function(error){
        onSGIWSError(error);
    });
}

onSGIWSError = function(error){
    $("#success").css("display", "none");
	$("#errors_table").html("");
	console.info(error);
	if(error.status == 403){
        $("#errorText").html("Vous n'avez pas le droit d'accès à cette resource.");
        errorBlock.style.display = "block";
    } else if(error.responseJSON){
    	//TODO make a distinction between error messages that are displayed in web services, and the ones displayed in endorsement tunnel
        $.each(error.responseJSON, function(i, err) {
            var $tr = $('<tr>').append(
                $('<td>').html("<b> " + err.code+ "</b></br> " + err.message + "<hr>")   
            ).appendTo('#errors_table');
            console.log($tr.wrap('<p>').html());
        });
        errorBlock.style.display = "block";
    }else if(error.status != 200){
        $("#errorText").html(error.responseText || "Error");
        errorBlock.style.display = "block";
    }else{
        onSGIWSResponse();
    }
    waitingSpinner.style.display = "none";
};

function clearResponseModal(){	
	$("#errorText").html("");
	$("#errors_table").html("");
	waitingSpinner.style.display = "block"; 
}
