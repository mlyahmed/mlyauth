loadSGIWSNewPropoalForm = function () {
    $('#ws-form').jsonForm({
        "schema" : {
            "contractor": {
                "type": "object",
                "properties": {

                    "externalReference": {
                        "title" : "Identifiant",
                        "type": "string",
                        default : Math.random().toString(36).toUpperCase().substring(2, 12) //diff is length
                    },

                    "externalRole": {
                        "title" : "Typologie du client",
                        "type": "string",
                        default : "PROSPECT"
                    },

                    "title": {
                        "title" : "Civilité",
                        "type": "string",
                        default : "0"
                    },

                    "surname": {
                        "title" : "Nom",
                        "type": "string",
                        default : "Durant"
                    },

                    "birthName": {
                        "title" : "Nom de Naissance",
                        "type": "string",
                        default : "Dubois"
                    },

                    "firstName": {
                        "title" : "Prénom",
                        "type": "string",
                        default : "Pierre"
                    },

                    "address": {
                        "title" : "Adresse1",
                        "type": "string",
                        default : "153 Rue des courcelles"
                    },

                    "addressExtra": {
                        "title" : "Adresse2",
                        "type": "string",
                        default : "Batiment A"
                    },

                    "addressExtra2": {
                        "title" : "Adresse3",
                        "type": "string",
                        default : "Escalier 2 - Etage 3"
                    },

                    "addressExtra3": {
                        "title" : "Adresse4",
                        "type": "string"
                    },

                    "zipCode": {
                        "title" : "Code Postal",
                        "type": "string",
                        default : "93100"
                    },

                    "city": {
                        "title" : "Ville",
                        "type": "string",
                        default : "MONTREUIL"
                    },

                    "country": {
                        "title" : "Pays",
                        "type": "string",
                        default : "FR"
                    },

                    "job": {
                        "title" : "Catégorie Socio Professionnelle",
                        "type": "string",
                        default : "1"
                    },

                    "birthdate": {
                        "title" : "Date de Naissance",
                        "type": "string",
                        default : "1980/12/31"
                    },

                    "mobilePhone": {
                        "title" : "Téléphone mobile",
                        "type": "string",
                        default : "0601020304"
                    },

                    "email": {
                        "title" : "Email",
                        "type": "string",
                        default : "francois.dupont@free.fr"
                    }
                }
            },

            "inForcePolicy" : {
                "type" : "object",
                "properties" : {

                    "dueDate" : {
                        "title" : "Jour et mois de la date d'échéance principale",
                        "type" : "string",
                        default : "04/12"
                    },

                    "subscriptionDate" : {
                        "title" : "Date de souscription du contrat actuel",
                        "type" : "string",
                        default : "2017/04/12"
                    },

                    "companyName" : {
                        "title" : "Compagnie précédente",
                        "type" : "string",
                        default : "MACIF"
                    },

                    "policyId" : {
                        "title" : "N Contrat Compagnie Précédente",
                        "type" : "string",
                        default : "XXXXXXXXX"
                    }
                }
            },

            "insuredObjects" : {
                "type" : "array",
                "items" : {
                    "type" : "object",
                    "properties" : {

                        "isCurrentlyInsured" : {
                            "title" : "Véhicule actuellement assuré",
                            "type" : "string",
                            default : "false"
                        },

                        "isInsuredMoreThanAYear" : {
                            "title" : "Durée du contrat actuel",
                            "type" : "string",
                            default : "false"
                        },

                        "formulaId" : {
                            "title" : "Formule choisie",
                            "type" : "string",
                            default : "BA_TIERS"
                        },

                        "plateNumber": {
                            "title" : "N d'immatriculation",
                            "type": "string",
                            default : "501 - BWS - 83"
                        },

                        "firstCirculationDate": {
                            "title" : "Date de 1ère mise en circulation",
                            "type": "string",
                            default : "1994/08/30"
                        },

                        "parkingZipCode": {
                            "title" : "Code Postal de Stationnement",
                            "type": "string",
                            default : "92400"
                        },

                        "parkingInseeCode": {
                            "title" : "Code INSEE Stationnement",
                            "type": "string",
                            default : "92026"
                        },


                        "parkingCity": {
                            "title" : "Ville de stationnement",
                            "type": "string",
                            default : "Courbevoie"
                        },

                        "parkingType": {
                            "title" : "Type de stationnement",
                            "type": "string",
                            default : "C"
                        },

                        "use": {
                            "title" : "Usage du véhicule",
                            "type": "string",
                            default : "PU"
                        },

                        "overRisk": {
                            "title" : "Surrisque",
                            "type": "string",
                            default: "0"
                        },


                        "car" : {
                            "type" : "object",
                            "properties" : {

                                "brandId": {
                                    "title" : "Marque du véhicule",
                                    "type": "string",
                                    default : "ME"
                                },

                                "modelId": {
                                    "title" : "Modèle du véhicule",
                                    "type": "string",
                                    default : "42"
                                },

                                "power": {
                                    "title" : "Puissance fiscale",
                                    "type": "string",
                                    default : "14"
                                },

                                "energy": {
                                    "title" : "Energie",
                                    "type": "string",
                                    default : "GO"
                                },

                                "gearbox": {
                                    "title" : "Boîte de vitesse",
                                    "type": "string",
                                    default : "A"
                                },

                                "body": {
                                    "title" : "Carrosserie",
                                    "type": "string",
                                    default : "4X4"
                                },

                                "version": {
                                    "title" : "Version",
                                    "type": "string",
                                    default : "G350 TD BREAK LONG"
                                },

                                "group": {
                                    "title" : "Groupe",
                                    "type": "string",
                                    default : "31"
                                },

                                "klass": {
                                    "title" : "Classe",
                                    "type": "string",
                                    default : "T"
                                },

                                "doorCount": {
                                    "title" : "Nombre de portes",
                                    "type": "string",
                                    default : "0"
                                },

                                "marketingDate": {
                                    "title" : "Date de commercialisation",
                                    "type": "string",
                                    default : "1991/01/01"
                                },

                                "id": {
                                    "title" : "ID SRA",
                                    "type": "string",
                                    default : "ME42015"
                                }
                            }
                        },

                        "drivers" : {
                            "type" : "array",
                            "items" : {
                                "type" : "object",
                                "properties" : {

                                    "isMain" : {
                                        "title" : "Conducteur principal",
                                        "type" : "string",
                                        default : "true"
                                    },

                                    "title" : {
                                        "title" : "Civilité",
                                        "type" : "string",
                                        default : "0"
                                    },

                                    "firstName" : {
                                        "title" : "Prénom",
                                        "type" : "string",
                                        default : "Pierre"
                                    },

                                    "name" : {
                                        "title" : "Nom",
                                        "type" : "string",
                                        default: "Durant"
                                    },

                                    "birthdate" : {
                                        "title" : "Date de Naissance",
                                        "type" : "string",
                                        default : "1980/12/31"
                                    },

                                    "licenceIssueDate" : {
                                        "title" : "Date de permis B",
                                        "type" : "string",
                                        default : "2000/05/05"
                                    },

                                    "bonusMalus" : {
                                        "title" : "CRM",
                                        "type" : "string",
                                        default : "0.7"
                                    },

                                    "bonusMalus12" : {
                                        "title" : "Nombre d'année CRM 0.50",
                                        "type" : "string",
                                        default : "1"
                                    }
                                }
                            }
                        }
                    }
                }
            },

            "payment" : {
                "type" : "object",
                "properties" : {
                    "bankDetails" : {
                        "type" : "object",
                        "properties" : {

                            "accountOwner" : {
                                "title" : "Titulaire du Compte",
                                "type" : "string",
                                default : "M. Alain Dubois"
                            },

                            "agencyName" : {
                                "title" : "Nom de la banque Domiciliation",
                                "type" : "string",
                                default : "BOURSORAMA BANQUE"
                            },

                            "agencyAddressNumber" : {
                                "title" : "Numéro de rue de la banque de domiciliation",
                                "type" : "string",
                                default : "18"
                            },

                            "agencyAddress" : {
                                "title" : "Nom de la rue de la banque de domiciliation",
                                "type" : "string",
                                default : "Quai du point du jour"
                            },

                            "agencyZipCode" : {
                                "title" : "Code postal de la Banque de domiciliation",
                                "type" : "string",
                                default : "92659"
                            },

                            "agencyCity" : {
                                "title" : "Ville de la banque de domiciliation",
                                "type" : "string",
                                default : "BOULOGNE BILLANCOURT"
                            },

                            "agencyCountry" : {
                                "title" : "Pays de la banque de domiciliation",
                                "type" : "string",
                                default : "FR"
                            },

                            "agencyAddressExtra" : {
                                "title" : "Complément",
                                "type" : "string",
                                default : "TSA 60201"
                            },

                            "bankData" : {
                                "title" : "Code IBAN",
                                "type" : "string",
                                default : "FR1420041010050500013M02606"
                            },

                            "BIC" : {
                                "title" : "Code BIC",
                                "type" : "string",
                                default : "BOUSFRPPXXX"        }

                        }
                    }
                }
            },

            "productId" : {
                "title" : "Produit",
                "type" : "string",
                default : "BRS_AUTO1"
            },

            "effectiveDate" : {
                "title" : "Date de début des garanties",
                "type" : "string",
                "default" : new Date().toJSON().slice(0,10).replace(/-/g,'/')
            },

            "periodicity" : {
                "title" : "Périodicité",
                "type" : "string",
                default : "M"
            },

            "suggestedFormula" : {
                "title" : "Formule préconisée",
                "type" : "string",
                default : "BA_TIERS"
            }
        }
        ,
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
                                "title": "Informations Souscripteur",
                                "expandable": false,
                                "items": [ "contractor" ]
                            }
                        ]
                    },
                    {
                        "type": "fieldset",
                        "notitle": "true",
                        "htmlClass": "columns pushRight",
                        "items": [
                            {
                                "type": "fieldset",
                                "title": "Informations Contrat",
                                "expandable": false,
                                "items": [ 	"insuredObjects[0].isCurrentlyInsured", "insuredObjects[0].isInsuredMoreThanAYear",
                                    "inForcePolicy",
                                    "insuredObjects[0].formulaId", "effectiveDate", "periodicity", "suggestedFormula" ]
                            },
                            {
                                "type": "fieldset",
                                "title": "Modalités De Prélèvement",
                                "expandable": false,
                                "items": [ "payment" ]
                            }
                        ]
                    },
                    {
                        "type": "fieldset",
                        "notitle": "true",
                        "htmlClass": "columns pushRight",
                        "items": [
                            {
                                "type": "fieldset",
                                "title": "Informations Véhicule",
                                "expandable": false,
                                "items": [ 	"insuredObjects[0].plateNumber", "insuredObjects[0].car.brandId", "insuredObjects[0].car.modelId", "insuredObjects[0].firstCirculationDate",
                                    "insuredObjects[0].car.power", "insuredObjects[0].car.energy", "insuredObjects[0].car.gearbox", "insuredObjects[0].car.body", "insuredObjects[0].car.version",
                                    "insuredObjects[0].car.group", "insuredObjects[0].car.klass", "insuredObjects[0].car.doorCount", "insuredObjects[0].car.marketingDate",
                                    "insuredObjects[0].overRisk", "insuredObjects[0].car.id", "insuredObjects[0].use", "insuredObjects[0].parkingInseeCode", "insuredObjects[0].parkingZipCode",
                                    "insuredObjects[0].parkingCity", "insuredObjects[0].parkingType" ]
                            },
                            {
                                "type" : "fieldset",
                                "notitle" : "true",
                                "items" : [ "productId" ]
                            }
                        ]
                    },
                    {
                        "type": "array",
                        "notitle": "true",
                        "htmlClass": "columns",
                        "items":
                            {
                                "type" : "array",
                                "items" : [{
                                    "expandable" : false,
                                    "title" : "Conducteur",
                                    "type" : "fieldset",
                                    "key" : "insuredObjects[].drivers[0]"
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
        onSubmit: function (errors, values){
            clearResponseModal_();
            $('#responseModal').modal();
            submitForm(errors, values);
        }
    });


    $(".icon-plus-sign").html("<strong>+</strong>");
    $(".icon-minus-sign").html("<strong>-</strong>");
    $("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-deletelast').css("display", "none");
    $("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-addmore').css("display", "none");
};

function clearResponseModal_(){
    $("#success").html("");
    clearResponseModal();
}

