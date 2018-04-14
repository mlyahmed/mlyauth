loadSGIWSPriceForm = function () {
    $('#ws-form').jsonForm({
        "schema" : {
            "effectiveDate" : {
                "title" : "Date début des garanties",
                "type" : "string",
                "default": new Date().toJSON().slice(0,10).replace(/-/g,'/')
            },

            "contractor": {
                "type": "object",
                "properties": {
                    "birthdate": {
                        "title" : "Date De Naissance",
                        "type": "string",
                        "default": "1980/12/31"
                    },
                    "job": {
                        "title" : "Catégorie Socio Professionnelle",
                        "type": "string",
                        "default": "1"
                    },
                    "mobilePhone": {
                        "title" : "Téléphone mobile",
                        "type": "string",
                        "default" : "0601020304"
                    },
                    "email": {
                        "title" : "Email",
                        "type": "string",
                        "default" : "francois.dupont@free.fr"
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
                            "default" : "true"
                        },
                        "isInsuredMoreThanAYear" : {
                            "title" : "Durée du contrat actuel",
                            "type" : "string",
                            "default" : "true"
                        },
                        "firstCirculationDate": {
                            "title" : "Date de 1ere mise en circulation",
                            "type": "string",
                            "default": "2009/09/14"
                        },
                        "parkingInseeCode": {
                            "title" : "Code INSEE Stationnement",
                            "type": "string",
                            "default": "92026"
                        },
                        "parkingType": {
                            "title" : "Type de stationnement",
                            "type": "string",
                            "default": "C"
                        },
                        "use": {
                            "title" : "Usage",
                            "type": "string",
                            "default": "PU"
                        },
                        "overRisk": {
                            "title" : "Surrisque",
                            "type": "string",
                            "default": "0"
                        },
                        "drivers" : {
                            "type" : "array",
                            "items" : {
                                "type" : "object",
                                "properties" :{

                                    "title" : {
                                        "title" : "Civilité",
                                        "type" : "string",
                                        "default" : "0"
                                    },

                                    "firstName" : {
                                        "title" : "Nom",
                                        "type" : "string",
                                        "default" : "Pierre"
                                    },

                                    "name" : {
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
                                        "default": 0.5
                                    },


                                    "bonusMalus12" : {
                                        "title" : "Nombre d'année CRM 0.50",
                                        "type" : "string",
                                        "default" : 1
                                    },

                                    "isMain" : {
                                        "title" : "Conducteur principal ? ",
                                        "type" : "string",
                                        "default" : "true"
                                    }
                                }
                            }
                        },
                        "car" : {
                            "type" : "object",
                            "properties" : {

                                "power": {
                                    "title" : "Puissance fiscale",
                                    "type": "string",
                                    "default" : "21"
                                },

                                "energy": {
                                    "title" : "Energie",
                                    "type": "string",
                                    "default" : "ES"
                                },

                                "body": {
                                    "title" : "Carrosserie",
                                    "type": "string",
                                    "default" : "BER"
                                },

                                "group": {
                                    "title" : "Groupe",
                                    "type": "string",
                                    "default" : "33"
                                },

                                "klass": {
                                    "title" : "Classe",
                                    "type": "string",
                                    "default" : "E"
                                },

                                "id": {
                                    "title" : "ID SRA",
                                    "type": "string",
                                    "default" : "JA14001"
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
                                "items": [ "contractor" ]
                            },
                            {
                                "type" : "fieldset",
                                "htmlClass": "columns pushRight pushLeft",
                                "title" : "Informations Contrat",
                                "items" : [
                                    "effectiveDate"
                                ]
                            }
                        ]
                    },
                    {
                        "type": "fieldset",
                        "notitle": "true",
                        "htmlClass": "columns",
                        "items": [
                            {
                                "type": "fieldset",
                                "htmlClass": "columns",
                                "title": "Informations Véhicule",
                                "items": [
                                    "insuredObjects[0].firstCirculationDate",
                                    "insuredObjects[0].car",
                                    "insuredObjects[0].overRisk",
                                    "insuredObjects[0].use", "insuredObjects[0].parkingInseeCode", "insuredObjects[0].parkingType" ]
                            },
                            {
                                "type": "array",
                                "htmlClass": "columns",
                                "items":
                                    {
                                        "type" : "array",
                                        "items" : [{
                                            "title" : "Informations Conducteur",
                                            "type" : "fieldset",
                                            "key" : "insuredObjects[0].drivers[]"
                                        }]
                                    }
                            }
                        ]
                    }
                ]
            },
            {
                "type": "submit",
                "title": "Submit"
            }
        ],
        onSubmit: function (errors, values)
        {
            $("#success").html("");
            clearResponseModal();
            $('#responseModal').modal();
            $("success").html("");
            submitForm(errors, values);
        }
    });

    $(".icon-plus-sign").html("<strong>+</strong>");
    $(".icon-minus-sign").html("<strong>-</strong>");
    $("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-deletelast').css("display", "none");
    $("#jsonform-0-elt-counter-1").find('> span > a._jsonform-array-addmore').css("display", "none");

};
