let myInput = document.getElementById("newPassword");
let confirmPassword = document.getElementById("confirmPassword");
let letter = document.getElementById("letter");
let capital = document.getElementById("capital");
let number = document.getElementById("number");
let length = document.getElementById("length");
let special = document.getElementById("special");

let numbers = /[0-9]/g;
let upperCaseLetters = /[A-Z]/g;
let lowerCaseLetters = /[a-z]/g;
let specialLetters = /[!@#$%^&*<>]/;

myInput.onkeyup = function() {
  if(myInput.value.match(lowerCaseLetters)) {
    letter.classList.remove("invalid");
    letter.classList.add("valid");
  } else {
    letter.classList.remove("valid");
    letter.classList.add("invalid");
  }
  if(myInput.value.match(upperCaseLetters)) {
    capital.classList.remove("invalid");
    capital.classList.add("valid");
  } else {
    capital.classList.remove("valid");
    capital.classList.add("invalid");
  }
  if(myInput.value.match(numbers)) {
    number.classList.remove("invalid");
    number.classList.add("valid");
  } else {
    number.classList.remove("valid");
    number.classList.add("invalid");
  }
  if(myInput.value.length >= 8) {
    length.classList.remove("invalid");
    length.classList.add("valid");
  } else {
    length.classList.remove("valid");
    length.classList.add("invalid");
  }
  if(myInput.value.match(specialLetters)) {
    special.classList.remove("invalid");
    special.classList.add("valid");
  } else {
    special.classList.remove("valid");
    special.classList.add("invalid");
  }

  if (myInput.value.match(lowerCaseLetters) && myInput.value.match(upperCaseLetters) && myInput.value.match(numbers) && myInput.value.match(specialLetters) && myInput.value.length >= 8) {
    document.getElementById("message").style.display = "none";
  }
  else {
    document.getElementById("message").style.display = "block";
  }
}

confirmPassword.onkeyup = function(){
  if (confirmPassword.value === myInput.value){
      document.getElementById("error").style.display = "none";
  }
  else {
    document.getElementById("error").style.display = "block";
  }
}

window.addEventListener( "pageshow", function ( event ) {
  var historyTraversal = event.persisted ||
      ( typeof window.performance != "undefined" &&
          window.performance.navigation.type === 2 );
  if ( historyTraversal ) {
    // Handle page restore.
    window.location.reload();
  }
});