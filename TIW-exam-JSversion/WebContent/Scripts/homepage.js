(function () {

  $(document).ready(function() {

    if(sessionStorage.getItem("iduser") == null) {
        window.location.href = "/TIW-exam-JSversion/"; //does not resend well user
    }

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
                ifModified: true,
                success: function(result, status, xhr) {
                    console.log(result);
                    console.log(status);
                    console.log(xhr.status);
                    if(result == undefined) { //304 by server
                        return;
                    }
                    console.log("SUCCESS GetCurrentAccountsList");
                    console.log(result);

                    if(result.length == 0) {
                        $("#generalError").html("You do not own any current account, contact an admin to register a new current account");
                        $("#generalError").show(300);
                        return;
                    } else {
                        $("#generalError").hide();
                    }

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

                    currentAccountsList.autoclick(1);

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
            console.log(_idCA);

            $.ajax({
                type: "GET",
                url: "/TIW-exam-JSversion/GetCurrentAccount",
                data: {
                    idCA: _idCA
                },
                success: function(result, status, xhr){
                    console.log("SUCCESS GetCurrentAccount");
                    console.log(result);

                    $("#CATable tr[value='" + result.idcurrentAccount +"'] td:eq(1)").html(result.total);

                },
                error: function(xhr, status, error) {
                    console.error("ERROR GetCurrentAccount:: " + xhr.responseText);
                }
            });
        }

        this.autoclick = function(_numRowToClick) {
            console.log("trying to autoclick");
            console.log($("#CATable tr:eq("+ _numRowToClick + ")"));
            //$("#CATable tr:eq(1)").click();
            $("#CATable tr:eq("+ _numRowToClick + ")").click();
        }
      }

      function TransfersList() {
        this.show = function(_idCA) {

            console.log("Sending request to GetAllTransfers");
            console.log("Id to send: " + _idCA);

            $("#transfersTable").hide(300);
            //match only from children with index greater than 0. This means that
            //only the header row will never be deleted
            $("#transfersTable tr:gt(0)").remove();
            //$("#transfersTable").html("");

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
                        var cacode = $("#CATable tr.highlight td:eq(0)").html();

                        for(let j = 0; j < transfers.length; j++) {
                            var newRow = $("<tr></tr>");
                            var tdDate = $("<td></td>");
                            var tdAmount = $("<td></td>");
                            var tdReason = $("<td></td>");
                            var tdDeal = $("<td></td>");

                            $(tdDate).html(transfers[j].date);
                            $(tdReason).html(transfers[j].reason);


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

                      contacts.contacts = new Array();

                      var allContacts = result;
                      for(let i = 0; i < allContacts.length; i++) {
                          var newContact = new Contact(allContacts[i].usercode, allContacts[i].CAcode);
                          contacts.contacts.push(newContact);
                      }

                      console.log("Finished success getAllContacts");
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
                      $("#ContactList").hide(300);
                  },
                  error: function(xhr, status, error) {
                      console.error("ERROR NewContact:: " + xhr.responseText);
                  }
              });
          }
      }

      /*function ContactsRegister() {
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
      }*/

      function ContactsRegister(){
          this.registerContactsPrevision = function () {
              $("input[name=userCodePayee]").bind("keyup", function () {

                  let compatibleContacts = checkContactList();

                  var contactList = $("#ContactList");


                  if(compatibleContacts.length != 0){

                	  console.log("This correspondencies were found /n" + compatibleContacts);

                	  console.log("Populating Contact List");

                	  $("#ContactList tr:gt(1)").remove();

                      for(let i = 0; i < compatibleContacts.length; i++){
                          var usercode = compatibleContacts[i].usercode;
                          var newRow = $("<tr value='" + usercode + "'></tr>");
                          var data1 = $("<td></td>");
                          var data2 = $("<td></td>");
                          var CACode = compatibleContacts[i].CAcode;
                          $(data1).html(usercode);
                          $(data2).html(CACode);
                          $(data1).appendTo(newRow);
                          $(data2).appendTo(newRow);
                          $(newRow).appendTo(contactList);
                      }

                      $("#ContactList tr").click(function () {
                    	  $("#ContactList tr:gt(1)").attr("class", "");
                    	  $(this).attr("class", "highlight");
                    	  $("input[name=userCodePayee]").attr("value", $(this).children('td:eq(0)').text());
                          $("input[name=CApayee]").attr("value", $(this).children('td:eq(1)').text());
                      })

                      contactList.show(300);

                      $("#ContactList tr:eq(2)").click();

                  } else {
                	  console.log("No compatible contacts were found!");
                      contactList.hide(300);

                  }
              })
          }
      }

      function checkContactList() {
          var inputSoFar = $("input[name=userCodePayee]").attr("value");
          var compatibleContacts = new Array();
          console.log("Checking presence on contacts");

          if(inputSoFar.length < 1){
              return compatibleContacts;
          }

          for(let i = 0; i < contacts.contacts.length; i++){

              let thisUsercode = contacts.contacts[i].usercode;
              if(inputSoFar == thisUsercode.substr(0, inputSoFar.length)){
                  compatibleContacts.push(contacts.contacts[i]);
              }
          }

          return compatibleContacts;
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

                                var currentContacts = contacts.getContacts().contacts;

                                if(!isCACodeInContacts(currentContacts, ca.CAcode)){
                                    if(confirm("Do you want to save this contact?")){
                                        console.log("saving " + ca.CAcode);
                                        contacts.addContact(ca.CAcode);
                                    }
                                }

                                var caid = $("#CATable tr.highlight").attr("value");
                                console.log("Highlighted id was: ");
                                console.log(caid);

                                transfersList.show(caid);

                                //currentAccountsList.show();

                                //TODO: check cacode and confirm
                                console.log("Terminating newTransfer: updating currentAccountsList with caid : ");
                                console.log(caid);
                                currentAccountsList.update(caid);


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
      console.log("checking contacts: ");
      console.log(_contactsArray);
      console.log(_CACode);
      for(let i = 0; i < _contactsArray.length; i++) {
          if(_contactsArray[i].CAcode == _CACode) {
              return true;
          }
      }
      console.log("FALSE");
      console.log(_contactsArray);
      console.log(_CACode);
      return false;
  }

})();
