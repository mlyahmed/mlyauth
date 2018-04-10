$.postJson2Text = function(url, data, callback, errorInfo) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': data,
        'dataType': 'json',
        'async': false,
        'headers': {
            "Authorization": "Basic " + btoa("gestF:gestF")
        },
        'success': callback,
        'error' : errorInfo
    });
};

$.postJson2Json = function(url, data, callback, errorInfo) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        crossDomain: false,
        crossOrigin: false,
        'contentType': 'application/json',
        'data': data,
        'dataType': 'json',
        'async': true,
        'beforeSend': function (xhr){
            xhr.setRequestHeader('Authorization', 'Basic ' + btoa('gestF:gestF'));
        },
        'success': callback,
        'error' : errorInfo
    });
};




callWS = function(errors, values){
    $.postJson2Json(getSGIWSUri(), JSON.stringify(values), onSGIWSResponse, onSGIWSError);
}

onSGIWSError = function(error){
    $("#success").css("display", "none");
	$("#errors_table").html("");

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
