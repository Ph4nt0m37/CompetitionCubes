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
        }).then((response) => {
            if (response.ok) window.location.replace("/tutorial");
            if (response.status==403) createNotification("Unfortunately, you do not have a WCA ID, so you cannot create an account. Please come back later.");
            if (response.status==500 || response.status==405) createNotification("Something went wrong creating your account. Please try again later or contact a developer.");
        });
    }else {
        createNotification("Please enter a valid username.");
    }
});

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

export function createNotification(text) {
    const notif = notificationTemplate.cloneNode(true);
    notif.textContent = text;
    notif.classList.remove("template");
    notificationBox.appendChild(notif);
    setTimeout(()=>{
        notif.classList.add("fade-out");
        setTimeout(()=>{
            notif.remove();
        },1500);
    },5000);
}