(function() {
    $(document).ready(function(){


      setCheckValidity();
      registerRegistrationButton();

      function registerRegistrationButton() {

          $("#registrationButton").click(function(event){
// username check -> usercode check -> form sent
              event.preventDefault();
              console.log("event prevented default");
              $("#errorForm").hide(300);

              if($("#registrationForm")[0].checkValidity()){
                  var rPass = $("input[name=repPassword]")[0];
                  if(rPass.checkValidity()){
                      checkUsername();
                  } else {
                    $("#errorForm").html("The two passwords must be the same");
                    $("#errorForm").show(300);
                  }
              } else {
                  $("#registrationForm")[0].reportValidity();
              }
          });

      }

      function setCheckValidity() {

          $("input[name=repPassword]")[0].checkValidity = function () {
              var password = $("input[name=password]").val();
              var repPassword = $("input[name=repPassword]").val();
              console.log("returning from repPassword checkValidity");
              console.log(password === repPassword);
              return password === repPassword;
          };

          $("input[name=repPassword]").bind("keyup", function() {
            var password = $("input[name=password]").val();
            if($(this).val() != password) {
              $(this).css("border-bottom", "1 px solid #f00");
              $("#errorRepPassword").html("Password are not matching").css("color", "red");
              $("#errorRepPassword").show(300);
            } else {
              console.log("password are now the same");
              var oldStyle = document.querySelectorAll("input[name=username]")[0].style;
              document.querySelectorAll("input[name=repPassword]")[0].style = oldStyle;
              $("#errorRepPassword").hide(300);
              if($("#errorForm").attr("style") == "display: inline;") {
                  $("#errorForm").hide(300);
              }
            }
          });

      }

      function checkUsername() {
          console.log("Checking username ...");
          $("#errorUsername").hide(300);

          var username = $("input[name=username]").serialize();
          console.log("Passing" + username + " as username");

          $.ajax({
            type: "GET",
            url: "/TIW-exam-JSversion/CheckUsername",
            data: username,
            success: usernameIsOk,
            error: showUsernameError
          });
      }

      //parameters are these becaues it has to be these for syntax (p.568 Flanagan)
      function showUsernameError(_xmlHttpRequest, _jQueryStatusCode, _errorObjectThrown) {
          var message = _xmlHttpRequest.responseText;
          console.log("username response text: " + message);

          $("#errorUsername").html(message);
          $("#errorUsername").show(300);
      }

      function usernameIsOk(_data) {
          console.log("Username did not exist, continuing registration");
          console.log("SUCCESS:: username request");
          //checkUsercode();
          sendForm();
      }

      /*function checkUsercode() {
          console.log("Checking usercode...");
          $("#errorUsercode").hide(300);

          var usercode = $("input[name=usercode]").serialize();
          console.log("Passing " + usercode + " as usercode");

          $.ajax({
              type: "GET",
              url: "/TIW-exam-JSversion/CheckUsercode",
              data: usercode,
              success: usercodeIsOk,
              error: showUsercodeError
          });
      }*/

      function showUsercodeError(_xmlHttpRequest) {
          var message = _xmlHttpRequest.responseText;
          console.log("usercode response text: " + message);

          $("#errorUsercode").html(message);
          $("#errorUsercode").show(300);
      }

      function usercodeIsOk(_data){
          console.log("SUCCESS:: usercode request");
          sendForm();
      }

      function sendForm() {
          var registrationData = new FormData($("#registrationForm")[0]);
          console.log("registration data: ", registrationData);
          $("#registrationButton").attr("disabled", true);
          $("#errorForm").hide(300);
          var serData = $("#registrationForm").serialize();
          console.log(serData);

          $.ajax({
              type: "POST",
              url: "/TIW-exam-JSversion/Registration",
              data: serData,
              success: registrationSuccess,
              error: showRegistrationError
          });
      }

      function registrationSuccess(data) {
          $("#registrationForm")[0].reset();
          console.log("SUCCESS: ", data);
          alert("Your registration have been successful! Now access with your credentials");
          window.location.href = "/TIW-exam-JSversion/index.html";
      }

      function showRegistrationError(_response) {
          var message = _response.responseText;
          console.log("registration response text: " + message);
          $("#registrationButton").attr("disabled", false);

          $("#errorForm").html(message);
          $("#errorForm").show(300);
      }
    });


})();
