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
  if (confirmPassword.value == myInput.value){
      document.getElementById("error").style.display = "none";
  }
  else {
    document.getElementById("error").style.display = "block";
  }
}

function sortTable(n) {
  var table, rows, switching, i, x, y, shouldSwitch , dir , switchCount = 0;
  table = document.getElementById("myTable");
  switching = true;
  dir = "asc";
  while (switching) {
    switching = false;
    rows = table.rows;
    for (i = 1; i < (rows.length - 1); i++) {
      shouldSwitch = false;
      x = rows[i].getElementsByTagName("td")[n];
      y = rows[i + 1].getElementsByTagName("td")[n];
      if (dir == "asc"){
        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
            shouldSwitch = true;
            break;
        }
      }
      else if (dir == "desc") {
        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
            shouldSwitch = true;
            break;
        }
      }
    }
    if (shouldSwitch) {
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
      switchCount++;
    }
    else {
        if (switchCount == 0 && dir == "asc"){
            dir = "desc";
            switching = true;
        }
    }
  }
}