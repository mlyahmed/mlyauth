$("#modalTitle").text("Création de devis");

getSGIWSUri = function(){
    return "http://localhost:8889/primainsure/B/E/A/R/E/R/auto/proposal";
};

onSGIWSResponse = function(returnVal){
    success.style.display = "block";
    $("#errorText").html("");
    $("#success").html("Devis : " + returnVal.handle.proposalIdText);
    waitingSpinner.style.display = "none";
};


$('document').ready(loadSGIWSNewPropoalForm);