$("#modalTitle").text("Eligibility");

getSGIWSUri = function(){
    return "http://localhost:8889/primainsure/A/P/I/auto/eligibility";
};

onSGIWSResponse = function(returnVal){
    success.style.display = "block";
    waitingSpinner.style.display = "none";
};

$('document').ready(loadSGIWSForm);