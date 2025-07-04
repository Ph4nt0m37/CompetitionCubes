const usernameText = document.getElementById("username-text");
const eloText = document.getElementById("elo-text");

let pathname = window.location.pathname.split("/");
fetch(`/api/get-user-data-by-id/${pathname[pathname.length-1]}`).then((response)=> {
    return response.json();
    }).then(function(data) {
        let user = data;
        usernameText.textContent=user.username;
        eloText.textContent=`ELO: ${user.elo}`;
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
    });