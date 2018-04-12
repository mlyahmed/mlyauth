$("#modalTitle").text("Tarification");

getSGIWSUri = function(){
    return "http://localhost:8889/primainsure/B/E/A/R/E/R/auto/price";
};

onSGIWSResponse = function(returnVal){
    success.style.display = "block";
    $("#errorText").html("");
    generateFormulaDivs(returnVal);
    waitingSpinner.style.display = "none";
};

generateFormulaDivs = function(returnVal) {
    $("#success").html("");
    for(var i = 0 ; i < returnVal.formulas.length ; i++) {
        var formulaDiv = document.createElement("div");
        formulaDiv.setAttribute("id", "formulaDiv"+i);
        formulaDiv.setAttribute("class", "expandable");
        formulaDiv.innerHTML = "<p onclick=expandFormuleClick("+i+") class=expandable><legend>Formule : " + returnVal.formulas[i].code + "</legend></p>";

        document.getElementById('success').appendChild(formulaDiv);

        var cotisationsDiv = document.createElement("p");
        cotisationsDiv.setAttribute("class","cotisationsDiv");
        cotisationsDiv.innerHTML = "Contribution attentat : " + returnVal.formulas[i].attackContributionAmount + "&euro;";
        formulaDiv.appendChild(cotisationsDiv);

        var paymentScheduleDiv = document.createElement("p");
        paymentScheduleDiv.setAttribute("class","paymentScheduleDiv");
        formulaDiv.appendChild(paymentScheduleDiv);

        generateData(returnVal, i);
    }

    var suggestedFormulaDiv = document.createElement("div");
    suggestedFormulaDiv.setAttribute("id", "suggestedFormulaDiv");
    suggestedFormulaDiv.innerHTML = "</br><legend>Formule Préconisée : " + returnVal.suggestedFormula + "</legend>";

    document.getElementById('success').appendChild(suggestedFormulaDiv);

};

generateData = function(returnVal, formulaNum) {
    for(var i = 0 ; i < returnVal.formulas[formulaNum].cotisationTypes.length ; i++) {

        var tableElement = document.createElement("div");
        tableElement.setAttribute("class","tables"+i);
        $('#formulaDiv'+formulaNum).find('.cotisationsDiv').append(tableElement);

        var coverBeans = returnVal.formulas[formulaNum].cotisationTypes[i].coverBeans;
        var cotisationTypeCode = returnVal.formulas[formulaNum].cotisationTypes[i].code;
        var generatedTables = generateTables(coverBeans, cotisationTypeCode, i, formulaNum);

        $('#formulaDiv'+formulaNum).find('.tables' + i).html(generatedTables);
        generatePriceForm(returnVal.formulas[formulaNum].cotisationTypes[i], formulaNum,i);
    }

    var paymentScheduleTitle = document.createElement("legend");
    paymentScheduleTitle.innerHTML = "Liste des échéances";
    var paymentScheduleTableElement = document.createElement("div");
    paymentScheduleTableElement.setAttribute("class","paymentScheduleTable");
    $('#formulaDiv'+formulaNum).find('.cotisationsDiv').append(paymentScheduleTitle);
    paymentScheduleTableElement.innerHTML = generatePaymentScheduleTable(returnVal, formulaNum);
    $('#formulaDiv'+formulaNum).find('.cotisationsDiv').append(paymentScheduleTableElement);

};


generatePaymentScheduleTable = function(returnVal, formulaNum) {
    var tableHtml = "<table style=margin-left:20px>" +
        "<thead>"+
        "<tr>"+
        "<th bgcolor=blue style=color:white >Date de l'échéance</th>"+
        "<th bgcolor=blue style=color:white >Montant de l'échéance</th>"+
        "</tr>" +
        "</thead>" +
        "<tbody>"+generatePaymentScheduleTableRows(returnVal.formulas[formulaNum])+"</tbody>" +
        "</table>";
    return tableHtml;
};

generatePaymentScheduleTableRows = function(formula) {
    var paymentScheduleTableRows = "";
    if(formula.scheduleCalendarItems) {
        for (var i = 0; i < formula.scheduleCalendarItems.length; i++) {
            paymentScheduleTableRows += "<tr>";
            paymentScheduleTableRows += "<td>" + formula.scheduleCalendarItems[i].mmddregul + "</td>";
            paymentScheduleTableRows += "<td>" + formula.scheduleCalendarItems[i].percent + "&euro;</td>";
            paymentScheduleTableRows += "</tr>";
        }
    }
    return paymentScheduleTableRows;
};

generateTables = function(coverBeans, cotisationId, cotisationNum, formulaNum) {
    var tableRows = "";
    var franchiseTableRows = "";

    for(var i = 0 ; i < coverBeans.length ; i++) {
        tableRows += generateTableRow(coverBeans[i]);
        franchiseTableRows+="<div onclick = franchiseTableColapse("+formulaNum+","+cotisationNum+ ","+i+") class=expandable><legend_secondary>Les franchises de la garantie "+coverBeans[i].id+"</legend_secondary></div>" +
            "<div>"+
            "<table style=display:none;margin-left:20px id=franchiseTable"+i+">" +
            "<thead>"+
            "<tr>"+
            "<th bgcolor=blue style=color:white >Code</th>"+
            "<th bgcolor=blue style=color:white >Montant</th>"+
            "<th bgcolor=blue style=color:white >Texte</th>"+
            "</tr>" +
            "</thead>" +
            "<tbody>"+generateFranchiseTableRow(coverBeans[i])+"</tbody>" +
            "</table>"+
            "</div>";
    }

    var table =
        "<div style=margin-top:10px font-size:18px onclick=onResponseTableExpandClick("+cotisationNum+","+formulaNum+") class=expandable><legend>Cotisation : "+cotisationId+"</legend></div>"+
        "<div id=tableNum"+cotisationNum+" style=display:none;margin-left:20px>"+
        "<table>"+
        "<thead>"+
        " <tr>"+
        "<th colspan=17 align=center>Tableau des garanties</th>"+
        "</tr>"+
        "<tr>"+
        "<th>Code garantie</th>"+
        "<th>Libellé garantie</th>"+
        "<th bgcolor=yellow>" + (cotisationId === "03" ? "Tarif par km HT" : "Montant Tarif Annuel HT")+ "</th>"+
        "<th bgcolor=yellow>" + (cotisationId === "03" ? "Tarif par km Taxe" : "Montant Tarif annuel Taxe") + "</th>"+
        "<th bgcolor=yellow>" + (cotisationId === "03" ? "Tarif par km TTC" : "Montant Tarif Annuel TTC") + "</th>"+
        "<th bgcolor=green style=color:white >" + (cotisationId === "03" ? "Tarif forfait journalier HT" : "Montant cotisation exercice HT") + "</th>"+
        "<th bgcolor=green style=color:white >" + (cotisationId === "03" ? "Tarif forfait journalier Taxe" : "Montant cotisation exercice Taxe") + "</th>"+
        "<th bgcolor=green style=color:white >" + (cotisationId === "03" ? "Tarif forfait journalier TTC" : "Montant cotisation exercice TTC") + "</th>"+
        "<th bgcolor=red style=color:white >Code Seuil*</th>"+
        "<th bgcolor=red style=color:white >Texte Seuil*</th>"+
        "<th bgcolor=red style=color:white >Montant Seuil*</th>"+
        "<th bgcolor=pink>Code Plafond*</th>"+
        "<th bgcolor=pink>Texte Plafond*</th>"+
        "<th bgcolor=pink>Montant Plafond*</th>"+
        "</tr>"+
        "</thead>"+
        "<tbody>"+tableRows+"</tbody>"+
        "</table>"+
        "<br/>";
    table+=franchiseTableRows;
    table+="</div>";
    return table;
};

generateFranchiseTableRow = function(coverBean) {
    var franchiseTableRow = "";
    if(coverBean.franchiseBeans){
        for (var i = 0; i < coverBean.franchiseBeans.length; i++) {
            if(coverBean.franchiseBeans[i].franchiseType !== 'FLOOR' && coverBean.franchiseBeans[i].franchiseType !== 'CEILING'){
                franchiseTableRow += "<tr>";
                franchiseTableRow += "<td>" + coverBean.franchiseBeans[i].code+"</td>";
                franchiseTableRow += "<td>" + coverBean.franchiseBeans[i].amount+"&euro;</td>";
                franchiseTableRow += "<td>" + coverBean.franchiseBeans[i].text+"</td>";
                franchiseTableRow += "</tr>";
            }
        }
    }
    return franchiseTableRow;
};

generateTableRow = function(coverBean) {
    var tableRow = "";
    tableRow+="<tr>";
    tableRow += "<td>" + coverBean.id + "</td>";
    tableRow += "<td>" + coverBean.name + "</td>";

    if (coverBean.contributionType == "03") {
        tableRow += "<td>" + coverBean.kilometerPrice.ht.toFixed(4) + "&euro;</td>";
        tableRow += "<td>" + coverBean.kilometerPrice.tax.toFixed(4) + "&euro;</td>";
        tableRow += "<td>" + coverBean.kilometerPrice.ttc.toFixed(4) + "&euro;</td>";
        tableRow += "<td>" + coverBean.dailyPrice.ht.toFixed(4) + "&euro;</td>";
        tableRow += "<td>" + coverBean.dailyPrice.tax.toFixed(4) + "&euro;</td>";
        tableRow += "<td>" + coverBean.dailyPrice.ttc.toFixed(4) + "&euro;</td>";
    } else {
        tableRow += "<td>" + coverBean.annualPrice.ht.toFixed(2) + "&euro;</td>";
        tableRow += "<td>" + coverBean.annualPrice.tax.toFixed(2) + "&euro;</td>";
        tableRow += "<td>" + coverBean.annualPrice.ttc.toFixed(2) + "&euro;</td>";
        tableRow += "<td>" + coverBean.exercicePrice.ht.toFixed(2) + "&euro;</td>";
        tableRow += "<td>" + coverBean.exercicePrice.tax.toFixed(2) + "&euro;</td>";
        tableRow += "<td>" + coverBean.exercicePrice.ttc.toFixed(2) + "&euro;</td>";
    }

    //Lower limit
    var floorFranchiseBean = getFranchiseBeanByType(coverBean, "FLOOR");
    tableRow += "<td>" + floorFranchiseBean.code + "</td>";
    tableRow += "<td>" + floorFranchiseBean.text +"</td>";
    tableRow += "<td>" + floorFranchiseBean.amount + "&euro;</td>";
    //Higher limit
    var ceilingFranchiseBean = getFranchiseBeanByType(coverBean, "CEILING");
    tableRow += "<td>" + ceilingFranchiseBean.code + "</td>";
    tableRow += "<td>" + ceilingFranchiseBean.text +"</td>";
    tableRow += "<td>" + ceilingFranchiseBean.amount + "&euro;</td>";

    tableRow+="</tr>";
    return tableRow;
};

function getFranchiseBeanByType(coverBean, franchiseTypeName){
    var cFBArray = [];
    if(coverBean){
        if(coverBean.franchiseBeans && franchiseTypeName){
            cFBArray = coverBean.franchiseBeans.filter(franchiseBean => franchiseBean === franchiseTypeName);
        }
    }
    return (cFBArray.length > 0) ? cFBArray[0] : {code: "", text: "", amount: 0 };
}

function generatePriceForm(response,formulaNum,cotisationNum) {
    var priceForm = {};
    priceForm.annualPrice = response.annualPrice;
    var decimalPoints = response.code === "03" ? 4 : 2;
    priceForm.annualPrice.ht = priceForm.annualPrice.ht.toFixed(decimalPoints);
    priceForm.annualPrice.tax = priceForm.annualPrice.tax.toFixed(decimalPoints);
    priceForm.annualPrice.ttc = priceForm.annualPrice.ttc.toFixed(decimalPoints);

    priceForm.exercicePrice = response.exercicePrice;
    priceForm.exercicePrice.ht = priceForm.exercicePrice.ht.toFixed(decimalPoints);
    priceForm.exercicePrice.tax = priceForm.exercicePrice.tax.toFixed(decimalPoints);
    priceForm.exercicePrice.ttc = priceForm.exercicePrice.ttc.toFixed(decimalPoints);

    $("#formulaDiv"+formulaNum).find('#tableNum'+ cotisationNum).jsonForm({
        'schema' : {
            "attackContributionAmount" : {
                "title" : "Contribution attentat",
                "type" : "integer"
            },
            "annualPrice" : {
                "title" : (response.code === "03" ? "PAR KM" : "ANNUEL"),
                "type" : "object",
                "properties" : {
                    "ht" : {
                        "title" : (response.code === "03" ? "Tarif par km HT" : "Montant Tarif Annuel HT"),
                        "type" : "integer"
                    },
                    "tax" : {
                        "title" : (response.code === "03" ? "Tarif par km Taxe" : "Montant Tarif annuel Taxe"),
                        "type" : "integer"
                    },
                    "ttc" : {
                        "title" : (response.code === "03" ? "Tarif par km TTC" : "Montant Tarif Annuel TTC"),
                        "type" : "integer"
                    }
                }
            },
            "exercicePrice" : {
                "title" : (response.code === "03" ? "PAR KM":"ANNUEL"),
                "type" : "object",
                "properties" : {
                    "ht" : {
                        "title" : (response.code === "03" ? "Tarif forfait journalier HT" : "Montant cotisation exercice HT"),
                        "type" : "integer"
                    },
                    "tax" : {
                        "title" : (response.code === "03" ? "Tarif forfait journalier Taxe" : "Montant cotisation exercice Taxe"),
                        "type" : "integer"
                    },
                    "ttc" : {
                        "title" : (response.code === "03" ? "Tarif forfait journalier TTC" : "Montant cotisation exercice &nbsp TTC"),
                        "type" : "integer"
                    }
                }
            }
        },
        "value" : priceForm,
        "form" :[
            {
                "type" : "fieldset",
                "htmlClass" : "formContainer",
                "items": [
                    {
                        "type": "fieldset",
                        "htmlClass" : "columnsWithMargin",
                        "title": (response.code === "03" ? "PAR KM" : "ANNUEL"),
                        "expandable": false,
                        "items": [
                            {
                                "type" : "label",
                                "key" : "annualPrice.ht"
                            },
                            {
                                "type" : "label",
                                "key" : "annualPrice.tax"
                            },
                            {
                                "type" : "label",
                                "key" : "annualPrice.ttc"
                            }
                        ]
                    },
                    {
                        "type": "fieldset",
                        "htmlClass" : "columns",
                        "title": (response.code === "03" ? "FORFAIT JOURNALIER" : "EXERCISE"),
                        "expandable": false,
                        "items": [
                            {
                                "type" : "label",
                                "key" : "exercicePrice.ht"
                            },
                            {
                                "type" : "label",
                                "key" : "exercicePrice.tax"
                            },
                            {
                                "type" : "label",
                                "key" : "exercicePrice.ttc"
                            }
                        ]
                    }
                ]
            }
        ]
    });
};

onResponseTableExpandClick = function (cotisationNum, formulaNum) {
    $("#formulaDiv"+formulaNum).find('#tableNum'+cotisationNum).slideToggle(200);
};

franchiseTableColapse = function(formulaNum, cotisationNum, coverNum) {
    $("#formulaDiv"+formulaNum).find('#tableNum'+cotisationNum).find('#franchiseTable'+coverNum).slideToggle(200);
};

expandFormuleClick = function (formulaNum) {
    $("#formulaDiv"+formulaNum).find(".cotisationsDiv").slideToggle(200);
};

$('document').ready(loadSGIWSPriceForm);