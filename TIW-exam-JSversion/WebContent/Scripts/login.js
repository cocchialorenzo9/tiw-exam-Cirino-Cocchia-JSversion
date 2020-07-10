(function(){
    $(document).ready(function(){

      registerLoginButton();

      function registerLoginButton() {

          console.log("registering login button...");

          $("#loginButton").click(function(event) {

              event.preventDefault();

              var form = $("#loginForm")[0];
              console.log("login button clicked");

              if(form.checkValidity()){
                  var serialized = $("#loginForm").serialize();
                  $("#loginError").hide(300);
                  console.log("sending to the server...");
                  console.log(serialized);
                  $.ajax({
                      type: "POST",
                      url: "/TIW-exam-JSversion/Login",
                      data: serialized,
                      success: loginSuccess,
                      error: loginError
                  });
              } else {
                  form.reportValidity();
              }

          });
        }

      function loginSuccess(result, status, xhr) {
          console.log("login successful...");
          console.log(result);

          console.log(xhr.getResponseHeader("Content-Type"));

          sessionStorage.setItem('iduser', result.iduser);
          sessionStorage.setItem('username', result.username);
          console.log("Set on session storage: id=" + sessionStorage.getItem("iduser") +
        + " username=" + sessionStorage.getItem("username"));
          window.location.href = "Pages/homepage.html";

          /* //this code were made in order to control wat was sent by user
          var response_type = xhr.getResponseHeader("Content-Type");
          if(response_type === "application/json"){

              var user = JSON.parse(xhr.responseText);

              sessionStorage.setItem('iduser', user.iduser);
              sessionStorage.setItem('username', user.username);
              window.location.href = "Pages/homepage.html";

          } else {
              console.error("fatal error, server response is not in JSON format");
          }
          */

      }

      function loginError(xhr, status, error) {
          console.error("ERROR login:: " + xhr.responseText);
          $("#loginError").html(xhr.responseText);
          $("#loginError").show(300);
      }

  });

})();
