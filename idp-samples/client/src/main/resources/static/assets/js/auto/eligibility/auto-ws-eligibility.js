$("#modalTitle").text("Eligibility");

getSGIWSUri = function(){
    return "http://localhost:8889/primainsure/B/E/A/R/E/R/auto/eligibility";
};

onSGIWSResponse = function(returnVal){
    success.style.display = "block";
    waitingSpinner.style.display = "none";
};

$('document').ready(loadSGIWSForm);