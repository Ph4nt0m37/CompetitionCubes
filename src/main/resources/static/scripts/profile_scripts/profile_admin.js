import { user } from "./profile.js";

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

const warnButton = document.getElementById("warn-button");
const banButton = document.getElementById("ban-button");

const banPopup = document.getElementById("ban-popup");

banButton.addEventListener("click",()=>{
    banPopup.style.display="flex";
});

const reportTypeText = document.getElementById("report-type-text");
const loadingText = document.getElementById("loading-text");

const actionsPopup = document.getElementById("background-overlay");

const banConfirmPopup = document.getElementById("ban-confirm-popup");
banConfirmPopup.style.display="none";

const yearTimeInput = document.getElementById("ban-time-year");
const monthTimeInput = document.getElementById("ban-time-month");
const dayTimeInput = document.getElementById("ban-time-day");
const hourTimeInput = document.getElementById("ban-time-hour");
const minuteTimeInput = document.getElementById("ban-time-mins");

const permaBanCheckbox = document.getElementById("perma-ban-check");

const otherReasonDiv = document.getElementById("other-reason-div");

const banReasonDropdown = document.getElementById("ban-reason-dropdown");
banReasonDropdown.addEventListener("change",(event)=>{
    if (event.target.value==="other") {
        otherReasonDiv.style.display = "flex";
    }else {
        otherReasonDiv.style.display = "none";
    }
});

const otherBanReason = document.getElementById("other-ban-reason");

const banInputs = [yearTimeInput, monthTimeInput, dayTimeInput, hourTimeInput, minuteTimeInput];

const banUserText = document.getElementById("ban-user-text");

const banConfirmButton = document.getElementById("ban-confirm");
banConfirmButton.addEventListener("click",()=>{
    if (banReasonDropdown.value==="default") {
        createNotification("Please select a ban reason.");
        banConfirmPopup.style.display="none";
        return;
    }

    actionsPopup.style.display="none";
    banPopup.style.display="none"
    banConfirmPopup.style.display="none";

    const duration = (Math.max(0,yearTimeInput.value) * 31557600000)+(Math.max(0,monthTimeInput.value) * 2629800000)+(Math.max(0,dayTimeInput.value) * 86400000)+(Math.max(0,hourTimeInput.value) * 3600000)+(Math.max(0,minuteTimeInput.value) * 60000);
    let reason = banReasonDropdown.value;
    if (reason==="other") {
        reason = otherBanReason.value;
        if (reason==="") reason = "other (not specified)"
    }

    fetch("/api/ban-user", {
        method: "POST",
        body: JSON.stringify({
            userId: user['userId'],
            duration: permaBanCheckbox.checked ? -1 : duration,
            reason: reason
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(resp=>{
        return resp.json();
    }).then(async success=>{
        if (success) {
            createNotification(`Successfully banned ${user['username']} for "${reason}"`);
        }
    });

    banReasonDropdown.children[0].selected = "selected";
    otherBanReason.value = "";
    otherReasonDiv.style.display = "none";

    resetBanTimeInputs();
});

const confirmBanButton = document.getElementById("confirm-ban-button");
confirmBanButton.addEventListener("click",()=>{
    banConfirmPopup.style.display="flex";
});

const banDenyButton = document.getElementById("ban-deny");
banDenyButton.addEventListener("click",()=>{
    banConfirmPopup.style.display="none";
});

actionsPopup.addEventListener("click",(event)=>{
    if (event.target===event.currentTarget && banConfirmPopup.style.display==="none") {
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        otherBanReason.value = "";
        otherReasonDiv.style.display = "none";
        resetBanTimeInputs();
        banPopup.style.display="none";
    }
});

document.addEventListener("keydown",(event)=>{
    if (event.key=="Escape" && banConfirmPopup.style.display==="none") {
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        otherBanReason.value = "";
        otherReasonDiv.style.display = "none";
        resetBanTimeInputs();
        banPopup.style.display="none";
    }
});

const closeBanButton = document.getElementById("close-ban-button");
closeBanButton.addEventListener("click",()=>{
    //resetting and hiding the ban popup
    banReasonDropdown.children[0].selected = "selected";
    otherBanReason.value = "";
    otherReasonDiv.style.display = "none";
    resetBanTimeInputs();
    banPopup.style.display="none";
});

permaBanCheckbox.addEventListener('change', function() {
    const checked = permaBanCheckbox.checked;
    for (const input of banInputs) {
        input.disabled = checked;
    }
});

function resetBanTimeInputs() {
    for (const input of banInputs) {
        input.disabled = false;
        input.value = "";
    }
    permaBanCheckbox.checked = false;
}

function createNotification(text) {
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