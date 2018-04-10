loadSGIWSForm = function () {
	$('#ws-form').jsonForm({
		"schema" : {	
			"productId" : {
				"title" : "Produit",
				"type" : "string",				
				"default": "BRS_AUTO1"
			},
			
			"effectiveDate" : {
				"title" : "Date de début des garanties",
				"type": "string",
				"default": new Date().toJSON().slice(0,10).replace(/-/g,'/')
			},

			"inForcePolicy": {
				"type": "object",
				"properties": {
					"subscriptionDate": {
						"title" : "Date de souscription",
						"type": "string",
						"default": "2017/05/20"
					}
				}
			},

			"contractor": {
				"type": "object",
				"properties": {
					"birthdate": {
						"title" : "Date De Naissance",
						"type": "string",
						"default": "1980/12/31"
					},
					"email": {
						"title" : "Email",
						"type": "string",
						"default": "test@mail.com"
					},
					"mobilePhone": {
						"title" : "Téléphone mobile",
						"type": "string",
						"default": "0601020304"
					}
				}
			},

			"insuredObjects" : {
				"type" : "array",
				"items" : {
					"type" : "object",
					"properties" : {
                        "firstCirculationDate": {
                            "title" : "Date de mise en circulation",
                            "type": "string",
                            "default" : "2009/09/14"
                        },
                        "overRisk": {
                            "title" : "Sur-risque du véhicule",
                            "type": "string",
                            "default": "0"
                        },

                        "use": {
                            "title" : "Usage du véhicule",
                            "type": "string",
                            "default" : "PU"
                        },

            			"isCurrentlyInsured": {
            				"title" : "Véhicule actuellement assuré",
            				"type": "string",
            				"default": "true"
            			},

            			"isInsuredMoreThanAYear": {
            				"title" : "Durée du contrat actuel",
            				"type": "string",
            				"default": "true"
            			},
            			"parkingInseeCode": {
							"title" : "Code INSEE Stationnement",
							"type": "string",
							default: "92026"
						},
						"parkingType": {
							"title" : "Type de stationnement",
							"type": "string",
							default: "C"
						},

						"drivers" : {
							"type" : "array",
							"items" : {
								"type" : "object",
								"properties" : {

									"isMain" : {
                                        "title" : "Conducteur principal",
                                        "type" : "string",
                                        "default" : "true"
                                    },

                                    "title" : {
                                        "title" : "Civilité",
                                        "type" : "string",
                                        "default" : "0"
                                    },

                                    "name" : {
                                        "title" : "Nom",
                                        "type" : "string",
                                        "default" : "Pierre"
                                    },

                                    "firstName" : {
                                        "title" : "Prénom",
                                        "type" : "string",
                                        "default": "Durant"
                                    },

                                    "birthdate" : {
                                        "title" : "Date de Naissance",
                                        "type" : "string",
                                        "default" : "1980/09/14"
                                    },

                                    "licenceIssueDate" : {
                                        "title" : "Date de permis B",
                                        "type" : "string",
                                        "default" : "2000/09/14"
                                    },

                                    "bonusMalus" : {
                                        "title" : "CRM",
                                        "type" : "string",
                                        "default" : "0.5"
                                    },

                                    "bonusMalus12" : {
                                        "title" : "CRM 0.50",
                                        "type" : "string",
                                        "default" : "1"
                                    }
                                }
							}
						},

						"car" : {
							"type" : "object",
							"properties" : {
                                "group": {
                                    "title" : "Groupe du véhicule",
                                    "type": "string",
                                    "default" : "6"
                                },

                                "klass": {
                                    "title" : "Classe du véhicule",
                                    "type": "string",
                                    "default" : "C"
                                }
							}
						}
					}
				}
			}
		},
		"form": [
			{				
				"type": "fieldset",
				"notitle": "true",                
				"htmlClass": "formContainer",
				"items": [
					{
						"type": "fieldset",
						"notitle": "true",                
						"htmlClass": "columns",
						"items": [
							{
							  "type": "fieldset",              
							  "htmlClass": "columns",
							  "title": "Informations Souscripteur",
							  "items": [ "contractor" ,{
								  "type": "fieldset",
								  "title": "Informations Contrat",
								  "items": [ "insuredObjects[0].isCurrentlyInsured",
											"insuredObjects[0].isInsuredMoreThanAYear",
											"inForcePolicy.subscriptionDate","effectiveDate", "productId"
											]
								}]
							},							
							{
								
								"type": "fieldset",                
								"htmlClass": "columns",
								"title": "Informations Véhicule",
									  "items": [ "insuredObjects[0].firstCirculationDate",
												"insuredObjects[0].car",
												"insuredObjects[0].overRisk",
												"insuredObjects[0].use",
												"insuredObjects[0].parkingInseeCode",
												"insuredObjects[0].parkingType"
												]
							}
						]
					},					
					{
						"type": "array",
                        "htmlClass": "columns driversColumn",
                        "items": 
                        {	
                        	"type" : "array",
                        	"items" : [{
                        		"title" : "Conducteur",
                        		"type" : "fieldset",
                        		"key" : "insuredObjects[0].drivers[]"
                        	}]
                        }
					}
				]
			},
			{
			  "type": "submit",
			  "title": "Submit"
			}
		],
        "params" : {
            "fieldHtmlClass" : "input-medium"
        },
		onSubmit: function (errors, values) 
		{ 
            clearResponseModal();
            $('#responseModal').modal();
            $.postJSON(getSGIWSUri(), JSON.stringify(values), onSGIWSResponse, onSGIWSError);            
		}
	});
	
	$.postJSON = function(url, data, callback, errorInfo) {
		return jQuery.ajax({
			'type': 'POST',
			'url': url,
			'contentType': 'application/json',
			'data': data,
			'dataType': 'json',
			'success': callback,
			'error' : errorInfo
		});	
	};
	
	$(".icon-plus-sign").html("<strong>+</strong>"); 
	$(".icon-minus-sign").html("<strong>-</strong>");
	$("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-deletelast').css("display", "none");
	$("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-addmore').css("display", "none");


};
