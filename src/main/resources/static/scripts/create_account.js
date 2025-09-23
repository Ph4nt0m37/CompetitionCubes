let createButton = document.getElementById("create-button");
let usernameBox = document.getElementById("username-box");

createButton.addEventListener("click",()=>{
    if (usernameBox.value) {
        fetch("/api/create-user", {
            method: "POST",
            body: JSON.stringify({
                username: usernameBox.value
            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then((response) => response.json())
        .then((json) => window.location.replace("/"));
    }else {
        console.log("textbox empty!");
    }
});