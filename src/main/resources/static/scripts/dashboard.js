const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

const reportedSolvesButton = document.getElementById("reported-solves-button");
const reportedUsersButton = document.getElementById("reported-users-button");

const reportingMethods = {
    SOLVES: 1,
    USERS: 2
};

class UserReport {
    #userId;
    #username;
    #reason;
    constructor(userId, username, reason) {
        this.#userId=userId;
        this.#username=username;
        this.#reason=reason;
    }

    get userId() {
        return this.#userId;
    }

    get username() {
        return this.#username;
    }

    get reason() {
        return this.#reason;
    }
}

let currUserReport = null;
let currAction = null;

let currReportMethod = reportingMethods.SOLVES;
reportedSolvesButton.style.backgroundColor="#d0d0d0";;

const reportTypeText = document.getElementById("report-type-text");
const loadingText = document.getElementById("loading-text");

const actionsPopup = document.getElementById("background-overlay");

const banConfirmPopup = document.getElementById("ban-confirm-popup");
banConfirmPopup.style.display="none";

const warnConfirmPopup = document.getElementById("warn-confirm-popup");
warnConfirmPopup.style.display="none";

const yearTimeInput = document.getElementById("ban-time-year");
const monthTimeInput = document.getElementById("ban-time-month");
const dayTimeInput = document.getElementById("ban-time-day");
const hourTimeInput = document.getElementById("ban-time-hour");
const minuteTimeInput = document.getElementById("ban-time-mins");

const permaBanCheckbox = document.getElementById("perma-ban-check");
const permaWarnCheckbox = document.getElementById("perma-warn-check");

const otherBanReasonDiv = document.getElementById("other-ban-reason-div");

const banReasonDropdown = document.getElementById("ban-reason-dropdown");
banReasonDropdown.addEventListener("change",(event)=>{
    if (event.target.value==="other") {
        otherBanReasonDiv.style.display = "flex";
    }else {
        otherBanReasonDiv.style.display = "none";
    }
});

const otherWarnReasonDiv = document.getElementById("other-warn-reason-div");

const warnReasonDropdown = document.getElementById("warn-reason-dropdown");
warnReasonDropdown.addEventListener("change",(event)=>{
    if (event.target.value==="other") {
        otherWarnReasonDiv.style.display = "flex";
    }else {
        otherWarnReasonDiv.style.display = "none";
    }
});

const otherBanReason = document.getElementById("other-ban-reason");
const otherWarnReason = document.getElementById("other-warn-reason");

const banInputs = [yearTimeInput, monthTimeInput, dayTimeInput, hourTimeInput, minuteTimeInput];

const banUserText = document.getElementById("ban-user-text");
const warnUserText = document.getElementById("warn-user-text");

const banConfirmButton = document.getElementById("ban-confirm");
banConfirmButton.addEventListener("click", async ()=>{
    if (banReasonDropdown.value==="default") {
        createNotification("Please select a ban reason");
        return;
    }

    actionsPopup.style.display="none";
    banPopup.style.display="none";
    banConfirmPopup.style.display="none";

    const duration = (Math.max(0,yearTimeInput.value) * 31557600000)+(Math.max(0,monthTimeInput.value) * 2629800000)+(Math.max(0,dayTimeInput.value) * 86400000)+(Math.max(0,hourTimeInput.value) * 3600000)+(Math.max(0,minuteTimeInput.value) * 60000);
    let reason = banReasonDropdown.value;
    if (reason==="other") {
        reason = otherBanReason.value;
        if (reason==="") reason = "other (not specified)"
    }

    if (reason==="username") {
        await fetch(`/api/rename-user-random?id=${currUserReport.userId}`, {
            method: "POST",
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(resp=>{
            if (!resp.ok) {
                createNotification(`Something went wrong renaming this user. Please try renaming manually or contact a developer.`);
            }
        });
    }

    await fetch("/api/ban-user", {
        method: "POST",
        body: JSON.stringify({
            userId: currUserReport.userId,
            duration: permaBanCheckbox.checked ? -1 : duration,
            reason: reason
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(async resp=>{
        if (resp.ok) {
            createNotification(`Successfully banned ${currUserReport.username} for "${reason}"`);
            currAction.remove();
            await removeReport(currUserReport.userId, currUserReport.username, reason);
            if (currReportMethod==reportingMethods.SOLVES) getInvalidTimes();
            if (currReportMethod==reportingMethods.USERS) getReportedUsers();
        }else {
            createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
        }
    });

    banReasonDropdown.children[0].selected = "selected";
    otherBanReason.value = "";
    otherBanReasonDiv.style.display = "none";

    resetBanTimeInputs();
});

const warnConfirmButton = document.getElementById("warn-confirm");
warnConfirmButton.addEventListener("click", async ()=>{
    if (warnReasonDropdown.value==="default") {
        createNotification("Please select a ban reason");
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
        await fetch(`/api/rename-user-random?id=${currUserReport.userId}`, {
            method: "POST",
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        }).then(resp=>{
            if (!resp.ok) {
                createNotification(`Something went wrong renaming this user. Please try renaming manually or contact a developer.`);
            }
        });
    }

    await fetch("/api/warn-user", {
        method: "POST",
        body: JSON.stringify({
            userId: currUserReport.userId,
            duration: duration,
            reason: reason
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then(async resp=>{
        if (resp.ok) {
            createNotification(`Successfully warned ${currUserReport.username} for "${reason}"`);
            currAction.remove();
            await removeReport(currUserReport.userId, currUserReport.username, reason);
            if (currReportMethod==reportingMethods.SOLVES) getInvalidTimes();
            if (currReportMethod==reportingMethods.USERS) getReportedUsers();
        }else {
            createNotification(`Something went wrong reporting this user. Please contact a developer.`);
        }
    });

    warnReasonDropdown.children[0].selected = "selected";
    otherWarnReason.value = "";
    permaWarnCheckbox.checked = false;
    otherWarnReasonDiv.style.display = "none";

    resetBanTimeInputs();
});

const banButton = document.getElementById("confirm-ban-button");
banButton.addEventListener("click",()=>{
    banConfirmPopup.style.display="flex";
});

const banDenyButton = document.getElementById("ban-deny");
banDenyButton.addEventListener("click",()=>{
    banConfirmPopup.style.display="none";
});

const warnButton = document.getElementById("confirm-warn-button");
warnButton.addEventListener("click",()=>{
    warnConfirmPopup.style.display="flex";
});

const warnDenyButton = document.getElementById("warn-deny");
warnDenyButton.addEventListener("click",()=>{
    warnConfirmPopup.style.display="none";
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
        otherBanReasonDiv.style.display = "none";
        warnReasonDropdown.children[0].selected = "selected";
        otherWarnReason.value = "";
        otherWarnReasonDiv.style.display = "none";
        resetBanTimeInputs();
        banPopup.style.display="none";
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
        otherBanReasonDiv.style.display = "none";
        warnReasonDropdown.children[0].selected = "selected";
        otherWarnReason.value = "";
        otherWarnReasonDiv.style.display = "none";
        resetBanTimeInputs();
        banPopup.style.display="none";
    }
});

const banPopup = document.getElementById("ban-popup")
const warnPopup = document.getElementById("warn-popup")

const closeBanButton = document.getElementById("close-ban-button");
closeBanButton.addEventListener("click",()=>{
    //resetting and hiding the ban popup
    banReasonDropdown.children[0].selected = "selected";
    otherBanReason.value = "";
    otherBanReasonDiv.style.display = "none";
    resetBanTimeInputs();
    actionsPopup.style.display="none";
    banPopup.style.display="none";
});

const closeWarnButton = document.getElementById("close-warn-button");
closeWarnButton.addEventListener("click",()=>{
    //resetting and hiding the ban popup
    warnReasonDropdown.children[0].selected = "selected";
    otherWarnReason.value = "";
    otherWarnReasonDiv.style.display = "none";
    resetBanTimeInputs();
    actionsPopup.style.display="none";
    warnPopup.style.display="none";
});

permaBanCheckbox.addEventListener('change', function() {
    const checked = permaBanCheckbox.checked;
    for (const input of banInputs) {
        input.disabled = checked;
    }
});

getInvalidTimes()

reportedSolvesButton.addEventListener("click",()=>{
    clearAsideButtons();
    reportedSolvesButton.style.backgroundColor="#d0d0d0";
    getInvalidTimes();
});

reportedUsersButton.addEventListener("click",()=>{
    clearAsideButtons();
    reportedUsersButton.style.backgroundColor="#d0d0d0";
    getReportedUsers();
});

function getInvalidTimes() {
    clearReports();
    currReportMethod=reportingMethods.SOLVES;
    reportTypeText.textContent = "Reported Solves";
    fetch("/api/get-invalid-times").then((promise)=>{
        if (promise.ok) return promise.json();
        createNotification("Something went wrong loading this data. Please try again later or contact a developer.");
    }).then((solves)=>{
        const dashboardDivContent = document.getElementById("div-content");
        if (solves.length>0) {
            loadingText.style.display="none";
            const template = document.querySelector(".solve-entry.template");
            for (let i=0;i<solves.length;i++) {
                let type = "Average";
                if (solves[i]['scramble']) {
                    type="Single"
                }
                let solve = template.cloneNode(true);
                const user = solves[i]['username'];
                const timeString = solves[i]['timeString'];
                solve.querySelector(".solve-user-link").textContent = user;
                solve.querySelector(".solve-user-link").title = user;
                solve.querySelector(".solve-user-link").href = `/user/${solves[i]['userId']}`;
                solve.querySelector(".solve-time").textContent = `${type}: ${timeString}`;
                solve.querySelector(".wca-pb").textContent = `WCA Single: ${solves[i]['wcasingle']} | WCA Average: ${solves[i]['wcaaverage']}`;
                solve.classList.remove("template");

                solve.querySelector(".ok-button").addEventListener("click",async ()=>{
                    solve.remove();
                    if (type==="Single") {
                        success = await okSingle(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble'],solves[i]['scramble']);
                    }else if (type==="Average") {
                        success = await okAverage(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble']);
                    }
                    if (success) {
                        createNotification(`Successfully OK'ed ${user}'s ${timeString}s solve`);
                        solve.remove();
                        getInvalidTimes();
                    }else {
                        createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
                    }
                });

                solve.querySelector(".dnf-button").addEventListener("click", async ()=>{
                    let success = false;
                    if (type==="Single") {
                        success = await dnfSingle(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble'],solves[i]['scramble']);
                    }else if (type==="Average") {
                        success = await dnfAverage(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble']);
                    }
                    if (success) {
                        createNotification(`Successfully DNF'ed ${user}'s ${timeString}s solve`);
                        solve.remove();
                        getInvalidTimes();
                    }else {
                        createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
                    }
                });

                solve.querySelector(".ban-button").addEventListener("click", ()=>{
                    openBanPopupUsername(user);
                });

                dashboardDivContent.appendChild(solve);
            }
        }else {
            loadingText.textContent="No invalid solves found! Yay!";
        }
    });
}

function getReportedUsers() {
    clearReports();
    currReportMethod=reportingMethods.USERS;
    reportTypeText.textContent = "Reported Users";
    fetch("/api/get-reported-users").then((promise)=>{
        if (promise.ok) return promise.json();
        createNotification("Something went wrong loading this data. Please try again later or contact a developer.");
    }).then((users)=>{
        const dashboardDivContent = document.getElementById("div-content");
        if (users.length>0) {
            loadingText.style.display="none";
            const template = document.querySelector(".user-entry.template");
            for (let i=0;i<users.length;i++) {
                let user = template.cloneNode(true);
                const username = users[i]['username'];
                const reason = users[i]['reason'];
                const info = users[i]['info'];
                console.log(users[i]);
                user.querySelector(".solve-user-link").textContent = username;
                user.querySelector(".solve-user-link").title = username;
                user.querySelector(".solve-user-link").href = `/user/${users[i]['userId']}`;
                user.querySelector(".report-reason").textContent = `Reason: ${String(reason).charAt(0).toUpperCase() + String(reason).slice(1)}`;
                if (reason==="time-wasting") {
                    const time = parseInt(info);
                    const timeInfo = user.querySelector(".report-info");
                    timeInfo.textContent=`Time: ${time}s`;
                    timeInfo.classList.remove("template");
                }
                user.classList.remove("template");

                user.querySelector(".ok-button").addEventListener("click",async ()=>{
                    let success = await removeReport(users[i]['userId'], username, reason);
                    if (success) {
                        createNotification(`Successfully resolved ${username}'s report`);
                        user.remove();
                        getReportedUsers();
                    }else {
                        createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
                    }
                });

                user.querySelector(".warn-button").addEventListener("click",async ()=>{
                    document.querySelector(`#warn-reason-dropdown > option[value=${String(reason).toLowerCase()}]`).selected = "selected";
                    currUserReport = new UserReport(users[i]['userId'], username, reason);
                    currAction = user;
                    openWarnPopup(currUserReport);
                });

                user.querySelector(".ban-button").addEventListener("click", ()=>{
                    document.querySelector(`#ban-reason-dropdown > option[value=${String(reason).toLowerCase()}]`).selected = "selected";
                    currUserReport = new UserReport(users[i]['userId'], username, reason);
                    currAction = user;
                    openBanPopup(currUserReport);
                });

                dashboardDivContent.appendChild(user);
            }
        }else {
            loadingText.textContent="No reported users found! Yay!";
        }
    });
}

async function removeReport(userId, username, reason) {
    return fetch("/api/remove-user-report",{
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            username: username,
            reason: reason
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((resp)=>{
        return resp.ok;
    });
}

async function okSingle(userId, event, time, scramble) {
    return fetch("/api/ok-single", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time,
            scramble: scramble
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((resp)=>{
        return resp.ok;
    });
}

async function okAverage(userId, event, time) {
    return fetch("/api/ok-average", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time,
            scramble: null
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((resp)=>{
        return resp.ok;
    });
}

async function dnfSingle(userId, event, time, scramble) {
    return fetch("/api/dnf-single", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time,
            scramble: scramble
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((resp)=>{
        return resp.ok;
    });
}

async function dnfAverage(userId, event, time) {
    return fetch("/api/dnf-average", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time,
            scramble: null
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((resp)=>{
        return resp.ok;
    });
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

function clearAsideButtons() {
    reportedSolvesButton.style.backgroundColor="#f5f5f5";
    reportedUsersButton.style.backgroundColor="#f5f5f5";
    clearReports();
}

function clearReports() {
    loadingText.textContent="Loading...";
    loadingText.style.display = "block";
    const reportsList = document.querySelectorAll(".entry:not(.template)");
    for (let i=reportsList.length-1;i>=0;i--) {
        reportsList[i].remove();
    }
}

function openBanPopup(userReport) {
    actionsPopup.style.display="grid";
    banPopup.style.display="flex";
    banUserText.textContent = `Ban ${userReport.username}`;
}

function openBanPopupUsername(username) {
    actionsPopup.style.display="grid";
    banPopup.style.display="flex";
    banUserText.textContent = `Ban ${username}`;
}

function openWarnPopup(userReport) {
    actionsPopup.style.display="grid";
    warnPopup.style.display="flex";
    warnUserText.textContent = `Ban ${userReport.username}`;
}

function clamp(min, max, x) {
    return Math.max(min, Math.min(x,max));
}

function resetBanTimeInputs() {
    for (const input of banInputs) {
        input.disabled = false;
        input.value = "";
    }
    permaBanCheckbox.checked = false;
}