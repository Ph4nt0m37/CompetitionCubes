import { user } from "./profile.js";

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

const warnButton = document.getElementById("warn-button");
const warnPopup = document.getElementById("warn-popup")

const banButton = document.getElementById("ban-button");
const unbanButton = document.getElementById("unban-button");

const unbanConfirmPopup = document.getElementById("unban-confirm-popup");
unbanConfirmPopup.style.display="none";

const unbanConfirmButton = document.getElementById("unban-confirm");
unbanConfirmButton.addEventListener("click",()=>{
    fetch(`/api/unban-user?id=${user['userId']}`,{
        method: "POST",
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(resp=>{
        if (resp.ok) {
            createNotification(`Successfully unbanned ${user['username']}`);
            actionsPopup.style.display="none";
            unbanConfirmPopup.style.display="none";
        }else {
            createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
        }
    });
});

warnButton.addEventListener("click",async ()=>{
    actionsPopup.style.display="grid";
    warnPopup.style.display="flex";
});

const unbanDenyButton = document.getElementById("unban-deny");
unbanDenyButton.addEventListener("click",()=>{
    unbanConfirmPopup.style.display="none";
});

unbanButton.addEventListener("click",()=>{
    unbanConfirmPopup.style.display="flex";
});

const banPopup = document.getElementById("ban-popup");

banButton.addEventListener("click",()=>{
    actionsPopup.style.display="grid";
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
const permaWarnCheckbox = document.getElementById("perma-warn-check");

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

const warnConfirmPopup = document.getElementById("warn-confirm-popup");
warnConfirmPopup.style.display="none";

const otherWarnReasonDiv = document.getElementById("other-warn-reason-div");

const warnReasonDropdown = document.getElementById("warn-reason-dropdown");
warnReasonDropdown.addEventListener("change",(event)=>{
    if (event.target.value==="other") {
        otherWarnReasonDiv.style.display = "flex";
    }else {
        otherWarnReasonDiv.style.display = "none";
    }
});

const otherWarnReason = document.getElementById("other-warn-reason");
const warnUserText = document.getElementById("warn-user-text");

const warnConfirmButton = document.getElementById("warn-confirm");
warnConfirmButton.addEventListener("click", async ()=>{
    if (warnReasonDropdown.value==="default") {
        createNotification("Please select a warn reason.");
        return;
    }

    actionsPopup.style.display="none";
    warnPopup.style.display="none";
    warnConfirmPopup.style.display="none";

    const duration = permaWarnCheckbox.checked ? -1 : 2629800000;
    let reason = warnReasonDropdown.value;
    if (reason==="other") {
        reason = otherWarnReason.value;
        if (reason==="") reason = "other (not specified)"
    }

    if (reason==="username") {
        await fetch(`/api/rename-user-random?id=${user['userId']}`, {
            method: "POST",
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(resp=>{
            if (!resp.ok) {
                createNotification(`Something went wrong renaming this user. Please try renaming manually or contact a developer.`);
            }
        }).catch(error=>{
                createNotification(`Something went wrong renaming this user. Please try renaming manually or contact a developer.`);
        });
    }

    await fetch("/api/warn-user", {
        method: "POST",
        body: JSON.stringify({
            userId: user['userId'],
            duration: duration,
            reason: reason
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(resp=>{
        if (resp.ok) {
            createNotification(`Successfully warned ${user['username']} for "${reason}"`);
            actionsPopup.style.display="none";
            warnPopup.style.display="none"
            warnConfirmPopup.style.display="none";

            warnReasonDropdown.children[0].selected = "selected";
            otherWarnReason.value = "";
            otherWarnReasonDiv.style.display = "none";
        }else {
            createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
        }
    });

    warnReasonDropdown.children[0].selected = "selected";
    otherWarnReason.value = "";
    permaWarnCheckbox.checked = false;
    otherWarnReasonDiv.style.display = "none";

    resetBanTimeInputs();
});

const closeWarnX = document.getElementById("close-warn-x");
closeWarnX.addEventListener("click",()=>{
    //resetting and hiding the ban popup
    warnReasonDropdown.children[0].selected = "selected";
    otherWarnReason.value = "";
    otherWarnReasonDiv.style.display = "none";
    warnPopup.style.display="none";
});

const warnNowButton = document.getElementById("confirm-warn-button");
warnNowButton.addEventListener("click",()=>{
    warnConfirmPopup.style.display="flex";
});

const warnDenyButton = document.getElementById("warn-deny");
warnDenyButton.addEventListener("click",()=>{
    warnConfirmPopup.style.display="none";
});

const banConfirmButton = document.getElementById("ban-confirm");
banConfirmButton.addEventListener("click",()=>{
    if (banReasonDropdown.value==="default") {
        createNotification("Please select a ban reason.");
        banConfirmPopup.style.display="none";
        return;
    }

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
        if (resp.ok) {
            createNotification(`Successfully banned ${user['username']} for "${reason}"`);
            actionsPopup.style.display="none";
            banPopup.style.display="none"
            banConfirmPopup.style.display="none";

            banReasonDropdown.children[0].selected = "selected";
            otherBanReason.value = "";
            otherReasonDiv.style.display = "none";

            resetBanTimeInputs();
        }else {
            createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
        }
    });
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
    if (event.target===event.currentTarget) {
        if (banConfirmPopup.style.display==="none" && warnConfirmPopup.style.display==="none") {
            actionsPopup.style.display="none";
            banPopup.style.display="none";
            warnPopup.style.display="none";
        }
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        otherBanReason.value = "";
        otherReasonDiv.style.display = "none";
        resetBanTimeInputs();
    }
});

document.addEventListener("keydown",(event)=>{
    if (event.key=="Escape") {
        if (banConfirmPopup.style.display==="none" && warnConfirmPopup.style.display==="none") {
            actionsPopup.style.display="none";
            banPopup.style.display="none";
            warnPopup.style.display="none";
        }
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        otherBanReason.value = "";
        otherReasonDiv.style.display = "none";
        warnReasonDropdown.children[0].selected = "selected";
        otherWarnReason.value = "";
        otherWarnReasonDiv.style.display = "none";
        resetBanTimeInputs();
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

const viewInfoButton = document.getElementById("view-info-button");
viewInfoButton.addEventListener("click",()=>{
    const userBan = user['userBan'];
    if (userBan) {
        banButton.style.display="none";
        warnButton.style.display="none";
        unbanButton.style.display="block";
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