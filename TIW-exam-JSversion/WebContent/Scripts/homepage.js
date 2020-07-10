(function () {

  $(document).ready(function() {

      var personalMessage = new PersonalMessage(sessionStorage.getItem("username"));
      var currentAccountsList = new CurrentAccountsList();
      var transfersList = new TransfersList();
      var newTransferForm = new NewTransferForm();
      var contacts = new Contacts();

      personalMessage.show();
      currentAccountsList.show();

      ////////////////////////////////////////////////////////////////

      function PersonalMessage(_username){
        this.username = _username;

        this.show = function() {
          $("#personalMessage").html(this.username);
        }

      }

      function CurrentAccountsList() {
        this.show = function() {
            var iduser = sessionStorage.getItem('iduser');

            $("#generalError").hide(300);

            $.ajax({
                type: "GET",
                url: "/TIW-exam-JSversion/GetCurrentAccountsList",
                data: $(iduser).serialize(),
                success: function(result, status, xhr) {
                    console.log("SUCCESS GetCurrentAccountsList");
                    console.log(result);

                    var caTable = $("#CATable");

                    for(let i = 0; i < result.length; i++) {
                        var caId = result[i].idcurrentAccount;
                        var newRow = $("<tr value='" + caId + "'></tr>");
                        var data1 = $("<td></td>");
                        var data2 = $("<td></td>");
                        var caCode = result[i].CAcode;
                        var caTotal = result[i].total;
                        $(data1).html(caCode);
                        $(data2).html(caTotal);
                        $(data1).appendTo(newRow);
                        $(data2).appendTo(newRow);
                        $(newRow).appendTo(caTable);
                    }

                    $(caTable).show(300);

                    var registrator = new CARegistrator();
                    registrator.registerButtons();

                    contacts.retrieveContacts();

                    currentAccountsList.autoclick();

                    /*
                    if(_response.getResponseHeader("Content-Type") === "application/json") {
                        var caList = JSON.parse(_response.responseText);
                        var caTable = $("#CATable");

                        for(let i = 0; i < caList.length; i++) {
                            var caId = caList[i].idcurrentAccount;
                            var newRow = $("<tr value='" + caId + "'></tr>");
                            var data1 = $("<td></td>");
                            var data2 = $("<td></td>");
                            var caCode = caList[i].CAcode;
                            var caTotal = caList[i].CAtotal;
                            $(data1).html(caCode);
                            $(data2).appendTo(caTotal);
                            $(data1).appendTo(newRow);
                            $(data2).appendTo(newRow);
                            $(newRow).appendTo(caTable);
                        }

                        $(caTable).show(300);

                        var registrator = new CARegistrator();
                        registrator.registerButtons();

                    } else {
                      console.error("GetCurrentAccountsList returned no JSON type");
                    }*/
                },
                error: function(xhr, status, error) {
                    console.error("ERROR GetCurrentAccountsList returned an error");
                    $("#generalError").html(xhr.responseText);
                    $("#generalError").show(300);
                }
            });
        }

        this.update = function(_idCA) {
            console.log("updating ca list");

            $.ajax({
                type: "GET",
                url: "/TIW-exam-JSversion/GetCurrentAccount",
                data: {idCA: _idCA},
                success: function(result, status, xhr){
                    console.log("SUCCESS GetCurrentAccount");

                    $("#CATable tr[value='" + result.idcurrentAccount +"'] td:eq(1)").html(result.total);
                },
                error: function(xhr, status, error) {
                    console.error("ERROR GetCurrentAccount:: " + xhr.responseText);
                }
            });
        }

        this.autoclick = function() {
            console.log("trying to autoclick");
            console.log($("#CATable tr:eq(1)"));
            $("#CATable tr:eq(1)").click();
        }
      }

      function TransfersList() {
        this.show = function(_idCA) {

            console.log("Sending request to GetAllTransfers");
            console.log("Id to send: " + _idCA);

            $("#transfersTable").hide(300);
            $("#transfersTable").html("");

            this.highlightCARow(_idCA);

            $.ajax({
                type: "GET",
                url: "/TIW-exam-JSversion/GetAllTransfers",
                data: {
                  idCA: _idCA
                },
                success: function(result, status, xhr) {

                    console.log("SUCCESS GetAllTransfers");

                    $("#generalError").hide(300);

                    var transfers = result;

                    console.log(transfers);

                    if(transfers.length == 0) {
                        $("#transfersTable").hide();
                        $("#noTransfersMessage").show(300);
                    } else {
                        $("#noTransfersMessage").hide();
                        var transfersTable = $("#transfersTable");

                        for(let j = 0; j < transfers.length; j++) {
                            var newRow = $("<tr></tr>");
                            var tdDate = $("<td></td>");
                            var tdAmount = $("<td></td>");
                            var tdReason = $("<td></td>");
                            var tdDeal = $("<td></td>");

                            $(tdDate).html(transfers[j].date);
                            $(tdReason).html(transfers[j].reason);

                            var cacode = $("#CATable tr.highlight td:eq(0)").html();

                            if(transfers[j].CApayer == cacode) {
                                $(tdDeal).html(transfers[j].CApayee);
                                $(tdAmount).html("- " + transfers[j].amount);
                                $(newRow).attr("class", "outoperation");
                            } else {
                                $(tdDeal).html(transfers[j].CApayer);
                                $(tdAmount).html("+ " + transfers[j].amount);
                                $(newRow).attr("class", "inoperation");
                            }

                            $(tdDate).appendTo(newRow);
                            $(tdAmount).appendTo(newRow);
                            $(tdReason).appendTo(newRow);
                            $(tdDeal).appendTo(newRow);

                            $(newRow).appendTo(transfersTable);
                        }

                        $("#transfersTable").show(300);

                      }


                        newTransferForm.update(_idCA);
                    /*
                    if(_req.getResponseHeader("Content-Type") === "application/json"){

                      var transfers = JSON.parse(_req.responseText);

                      if(transfers.length == 0) {
                          $("#transfersTable").hide();
                          $("#noTransfersMessage").show(300);

                      } else {
                          $("#noTransfersMessage").hide();
                          var transfersTable = $("#transfersTable");

                          for(let j = 0; j < transfers.length; j++) {
                              var newRow = $("<tr></tr>");
                              var tdDate = $("<td></td>");
                              var tdAmount = $("<td></td>");
                              var tdReason = $("<td></td>");
                              var tdRole = $("<td></td>");

                              $(tdDate).html(transfers[i].date);
                              $(tdAmount).html(transfers[i].amount);
                              $(tdReason).html(transfers[i].reason);

                              if(transfers[i].CApayer == _idCA) {
                                  $(tdRole).html("payer");
                              } else {
                                  $(tdRole).html("payee");
                              }

                              $(tdData).appendTo(newRow);
                              $(tdAmount).appendTo(newRow);
                              $(tdReason).appendTo(newRow);
                              $(tdRole).appendTo(newRow);

                              $(newRow).appendTo(transfersTable);
                          }

                          $("#transfersTable").show(300);
                      }

                      newTransferForm.update(_idCA);

                    } else {
                        console.log("ERROR GetAllTransfers did not return a JSON type");
                    }*/
                },
                error: function(xhr, status, error) {
                    console.error("ERROR GetAllTransfers" + xhr.responseText);
                    $("#generalError").html(xhr.responseText);
                }
            });
        }

        this.highlightCARow = function(_idCA) {
            console.log("highlighting row with id = " + _idCA);
            $("#CATable tr").attr("class", "");
            $("#CATable tr[value='" + _idCA + "']").attr("class", "highlight");
        }
      }

      function NewTransferForm() {
          this.submitRegister = undefined;

          this.contactTracker = undefined;

          this.update = function(_idCA) {
              console.log("Updating transfer form");
              $("input[name=CApayer]").attr("value", _idCA);
              $("#newTransferForm").show(300);

              //this section should be invoked only in the very first call to the update
              if(this.submitRegister == undefined) {
                  this.submitRegister = new ButtonRegister();
                  this.submitRegister.registerButton();
              }

              if(this.contactTracker == undefined) {
                  this.contactTracker = new ContactsRegister();
                  this.contactTracker.registerContactsPrevision();
              }

          }

      }

      function Contacts() {
          this.contacts = new Array();

          this.getContacts = function(){
              return contacts;
          }

          this.retrieveContacts = function(){

              $.ajax({
                  type: "GET",
                  url: "/TIW-exam-JSversion/GetAllContacts",
                  success: function(result, status, xhr) {
                      console.log("SUCCESS GetAllContacts");

                      var allContacts = result;
                      for(let i = 0; i < allContacts.length; i++) {
                          var newContact = new Contact(allContacts[i].usercode, allContacts[i].CAcode);
                          contacts.contacts.push(newContact);
                      }

                      /*
                      if(_req.getResponseHeader("Content-Type") == "application/json") {

                          this.contacts = new Array();

                          var allContacts = JSON.parse(_req.responseText);
                          for(let i = 0; i < allContacts.length; i++) {
                              var newContact = new Contact(allContacts[i].usercode, allContacts[i].CAcode);
                              this.contacts.push(newContact);
                          }

                      } else {
                          console.error("ERROR GetAllContacts:: no JSON response");
                      }*/
                  },
                  error: function(xhr, status, error) {
                      console.error("ERROR GetAllContacts:: error in the request");
                  }
              });

          }

          this.addContact = function(_CAcode) {

              $.ajax({
                  type: "POST",
                  url: "/TIW-exam-JSversion/NewContact",
                  data: {
                      CAcode: _CAcode
                  },
                  success: function(result, status, xhr) {
                      console.log("SUCCESS NewContact:: " + status);
                      var newContact = new Contact(result.usercode, result.CAcode);
                      contacts.retrieveContacts();
                      console.log("pushed contacts?");
                      console.log(contacts.contacts);
                  },
                  error: function(xhr, status, error) {
                      console.error("ERROR NewContact:: " + xhr.responseText);
                  }
              });
          }
      }

      function ContactsRegister() {
          this.registerContactsPrevision = function() {
            $("input[name=userCodePayee]").bind("keyup", function() {
                console.log("key pressed on userCodePayee");
                console.log(contacts);
                let indexFound = checkPresenceOnContact();
                if(indexFound !== -1) {
                    console.log("Checked substring in input! ");

                    let usercodeContact = contacts.contacts[indexFound].usercode;
                    let cacodeContact = contacts.contacts[indexFound].CAcode;

                    $("input[name=userCodePayee]").attr("value", usercodeContact);
                    $("input[name=CApayee]").attr("value", cacodeContact);
                }
            });
          }
      }

      //returns index of the contact insert so far
      function checkPresenceOnContact() {
          var inputSoFar = $("input[name=userCodePayee]").attr("value");

          for(let i = 0; i < contacts.contacts.length; i++) {

              let thisUsercode = contacts.contacts[i].usercode;
              console.log(thisUsercode);
              console.log(thisUsercode.substr(0, inputSoFar.length));
              console.log(inputSoFar == thisUsercode.substr(0, inputSoFar.length));
              if(inputSoFar == thisUsercode.substr(0, inputSoFar.length)) {
                console.log(i);
                return i;
              }
          }

          return -1;
      }

      function Contact(_usercodeContact, _CAcodeContact) {
        this.usercode = _usercodeContact;
        this.CAcode = _CAcodeContact;
      }

      function CARegistrator() {
        this.registerButtons = function() {
            console.log("Registering buttons on current accounts tables");
            $("#CATable tr").click(function() {
                var idCA = $(this).attr("value");
                console.log("Clicked row with value = " + idCA);
                if(idCA != undefined) { //click on the headers row
                  transfersList.show(idCA);
                }
            });
        }
      }

      function ButtonRegister(){
          this.registerButton = function() {

              console.log("Registering form's button listener...");

              $("#newTransferButton").click(function(event) {
                  event.preventDefault();

                  console.log("Clicked new transfer button");

                  var form = $("#newTransferForm")[0];

                  if(form.checkValidity()){

                      console.log("Sending NewTransfer form");
                      console.log($(form).serialize());
                      var serialized = $(form).serialize();

                      $.ajax({
                          type: "POST",
                          url: "/TIW-exam-JSversion/NewTransfer",
                          data: serialized,
                          success: function(result, status, xhr) {

                                console.log("SUCCESS NewTransfer");
                                $("#transferError").hide(300);
                                form.reset();

                                var ca = result;

                                var currentContacts = contacts.getContacts();

                                if(!isCACodeInContacts(currentContacts, ca.CAcode)){
                                    if(confirm("Do you want to save this contact?")){
                                        console.log("saving " + ca.CAcode);
                                        contacts.addContact(ca.CAcode);
                                    }
                                }

                                transfersList.show(ca.idcurrentAccount);

                                //currentAccountsList.show();

                                //TODO: check cacode and confirm
                                var cacode = $("#CATable tr.highlight td:eq(0)").html();
                                currentAccountsList.update(cacode);

                                /*
                                if(_req.getResponseHeader("Content-Type") == "application/json") {

                                    var ca = JSON.parse(_req.responseText);

                                    var currentContacts = contacts.getContacts();

                                    if(!isCACodeInContacts(currentContacts, ca.CACode)){
                                        if(confirm("Do you want to save this contact?")){
                                            contacts.addContact(ca.CAcode);
                                        }
                                    }

                                    transfersList.show(ca.idcurrentAccount);

                                } else {
                                    console.error("NewTransfer did not return JSON data");
                                }*/

                          },
                          error:  function(xhr, status, error) {

                              console.log("ERROR NewTransfer::" + xhr.responseText);
                              $("#transferError").html(xhr.responseText);
                              $("#transferError").show(300);

                          }
                      });

                  } else {
                      form.reportValidity();
                  }
              })
          }
      }

  }); //end of ready

  function isCACodeInContacts(_contactsArray, _CACode){
      for(let i = 0; i < _contactsArray.length; i++) {
          if(_contactsArray[i].CACode == _CACode) {
              return true;
          }
      }
      return false;
  }

})();
