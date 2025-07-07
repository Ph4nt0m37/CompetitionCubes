const searchButton = document.getElementById("search-button");
const searchText = document.getElementById("searching-text");
const signInButton = document.getElementById("sign-in-button");
let searchInt = null;


//sign in stuff
signInButton.addEventListener("click",()=>{
    window.location.replace(`${window.location.origin}/wca-auth`);
});