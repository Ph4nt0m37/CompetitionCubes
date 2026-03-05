const settingsDiv = document.getElementById("settings-div");
const loadingText = document.getElementById("loading-text");

const saveButton = document.getElementById("save-button");

//settings selectors
const usernameBox = document.getElementById("username-input");
const changeUsernameText = document.getElementById("change-username-text");
const inspectionAudioToggle = document.getElementById("inspection-audio-toggle");
const matchSoundsToggle = document.getElementById("match-sounds-toggle");

let user = null;
let userId = null;

await fetch(`/api/get-user-data`).then(async (response)=> {
    if (response.ok)
        return response.json();
    createNotification("Something went wrong loading your data, so some things may not work as expected. Please contact a developer if this keeps happening.");
}).then(function(data) {
    user = data;
    userId=data.userId;
    usernameBox.value = user['username'];
    console.log(user);
    const userWarnings = user['userWarnings'];
    for (const warning of userWarnings) {
        if (warning['reason']==="username") {
            usernameBox.disabled = true;
            changeUsernameText.classList.remove("hidden");
            break;
        }
    }
    const settings = user['userSettings'];
    loadingText.classList.add("hidden");
    settingsDiv.classList.remove("hidden");
    inspectionAudioToggle.checked = settings['inspectionAudio'];
    matchSoundsToggle.checked = settings['matchSounds'];
    return;
});

saveButton.addEventListener("click",()=>{
    let success = true;
    let reloadTime = 2000;
    fetch("/api/save-user-settings",{
        method: "POST",
        body: JSON.stringify({
            inspectionAudio: inspectionAudioToggle.checked,
            matchSounds: matchSoundsToggle.checked
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(resp=>{
        if (!resp.ok) {
            success = false;
        }
    });
    if (usernameBox.value!==user['username']) {
        fetch(`/api/rename-user?userId=${userId}&newUsername=${usernameBox.value}`,{
            method: "POST",
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(resp=>{
            if (resp.status==403) {
                createNotification("Your username was not saved because you have an active warning for your username.","#c53838");
                reloadTime = 4000;
                return;
            }
            if (!resp.ok) {
                success = false;
            }
        });
    }

    if (success) {
        createNotification("Successfully saved settings. Reloading page...","#22eb51");
        setTimeout(()=>{location.reload()}, reloadTime);
    }else {
        createNotification("Something went wrong when trying to save these settings. Please try again later or contact a developer.","#c53838");
    }
});

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

function createNotification(text, color) {
    const notif = notificationTemplate.cloneNode(true);
    notif.style.backgroundColor=color;
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