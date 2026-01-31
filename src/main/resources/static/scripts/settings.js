const settingsDiv = document.getElementById("settings-div");
const loadingText = document.getElementById("loading-text");

const saveButton = document.getElementById("save-button");

//settings selectors
const usernameBox = document.getElementById("username-input");
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
    const settings = user['userSettings'];
    loadingText.classList.add("hidden");
    settingsDiv.classList.remove("hidden");
    inspectionAudioToggle.checked = settings['inspectionAudio'];
    matchSoundsToggle.checked = settings['matchSounds'];
    return;
});

fetch(`/api/get-user-settings/${userId}`).then((resp)=>{
    if (resp.ok)
        return resp.json();
    createNotification("Something went wrong when loading this page. Please try again later.","#c53838");
}).then(settings=>{
    console.log(settings);

    
});

saveButton.addEventListener("click",()=>{
    fetch("/api/update-user-settings",{
        method: "POST",
        body: JSON.stringify({
            inspectionAudio: inspectionAudioToggle.checked,
            matchSoundsToggle: matchSoundsToggle.checked
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(resp=>{
        if (!resp.ok) {
            createNotification("Successfully saved settings.","#22eb51")
        }else {
            createNotification("Something went wrong when trying to save these settings. Please try again later.","#c53838");
        }
    })
});

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

function createNotification(text, color) {
    const notif = notificationTemplate.cloneNode(true);
    notif.styles.backgroundColor=color;
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