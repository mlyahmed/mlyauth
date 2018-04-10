$("#modalTitle").text("Eligibility");

getSGIWSUri = function(){
    return "https://uat-sgi-policy01.prima-solutions.com/primainsure/A/P/I/auto/eligibility";
};

onSGIWSResponse = function(returnVal){
    success.style.display = "block";
    waitingSpinner.style.display = "none";
};

$('document').ready(loadSGIWSForm);