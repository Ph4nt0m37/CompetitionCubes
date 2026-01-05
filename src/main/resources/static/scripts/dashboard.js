const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

const reportedSolvesButton = document.getElementById("reported-solves-button");
const reportedUsersButton = document.getElementById("reported-users-button");

const reportingMethods = {
    SOLVES: 1,
    USERS: 2
};

let currReportMethod = reportingMethods.SOLVES;
reportedSolvesButton.style.backgroundColor="#d0d0d0";;

const reportTypeText = document.getElementById("report-type-text");
const loadingText = document.getElementById("loading-text");

const actionsPopup = document.getElementById("background-overlay");

const banPopup = document.getElementById("ban-confirm-popup");
banPopup.style.display="none";

const yearTimeInput = document.getElementById("ban-time-year");
const monthTimeInput = document.getElementById("ban-time-month");
const dayTimeInput = document.getElementById("ban-time-day");
const hourTimeInput = document.getElementById("ban-time-hour");
const minuteTimeInput = document.getElementById("ban-time-mins");

const banConfirmButton = document.getElementById("ban-confirm");
banConfirmButton.addEventListener("click",()=>{
    actionsPopup.style.display="none";
    banPopup.style.display="none";
    fetch("/api/ban-user", {
        method: "POST",
        body: JSON.stringify({
            userId: 71,
            //TODO: check valid input
            duration: (yearTimeInput.value * 31557600000)+(monthTimeInput.value * 2629800000)+(dayTimeInput.value * 86400000)+(hourTimeInput.value * 3600000)+(minuteTimeInput.value * 60000)
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    });
});

const banDenyButton = document.getElementById("forfeit-deny");
banDenyButton.addEventListener("click",()=>{
    banPopup.style.display="none";
});

actionsPopup.addEventListener("click",(event)=>{
    if (event.target===event.currentTarget && banPopup.style.display==="none") {
        if (banPopup.style.display==="none") {
            actionsPopup.style.display="none";
        }
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        banPopup.style.display="none";
    }
});

document.addEventListener("keydown",(event)=>{
    if (event.key=="Escape" && banPopup.style.display==="none") {
        if (banPopup.style.display==="none") {
            actionsPopup.style.display="none";
        }
        //resetting and hiding the ban popup
        banReasonDropdown.children[0].selected = "selected";
        banPopup.style.display="none";
    }
});

const banReasonDropdown = document.getElementById("ban-reason-dropdown");

const banPopup = document.getElementById("ban-popup")
const banUserButtons = document.getElementById("ban-user-button");
banUserButtons.addEventListener("click",()=>{
    if (banPopup.style.display==="none")
        banPopup.style.display="flex";
});

const clsoeBanButton = document.getElementById("close-ban-button");
clsoeBanButton.addEventListener("click",()=>{
    //resetting and hiding the ban popup
    banReasonDropdown.children[0].selected = "selected";
    banPopup.style.display="none";
});

const closeMenuButton = document.getElementById("close-menu-button");
closeMenuButton.addEventListener("click",()=>{
    if (banPopup.style.display==="none") {
        actionsPopup.style.display="none";
        banPopup.style.display="none";
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
    reportTypeText.textContent = "Reported Solves";
    fetch("/api/get-invalid-times").then((promise)=>{
        return promise.json();
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

                dashboardDivContent.appendChild(solve);
            }
        }else {
            loadingText.textContent="No invalid solves found! Yay!";
        }
    });
}

function getReportedUsers() {
    clearReports();
    reportTypeText.textContent = "Reported Users";
    fetch("/api/get-reported-users").then((promise)=>{
        return promise.json();
    }).then((users)=>{
        const dashboardDivContent = document.getElementById("div-content");
        if (users.length>0) {
            loadingText.style.display="none";
            const template = document.querySelector(".user-entry.template");
            for (let i=0;i<users.length;i++) {
                let user = template.cloneNode(true);
                const username = users[i]['username'];
                const reason = users[i]['reason'];
                user.querySelector(".solve-user-link").textContent = username;
                user.querySelector(".solve-user-link").href = `/user/${users[i]['userId']}`;
                user.querySelector(".report-reason").textContent = `Reason: ${String(reason).charAt(0).toUpperCase() + String(reason).slice(1)}`;
                user.classList.remove("template");

                user.querySelector(".ok-button").addEventListener("click",async ()=>{
                    let success = await okReport(users[i]['userId'], username, reason);
                    if (success) {
                        createNotification(`Successfully resolved ${username}'s report`);
                        user.remove();
                        getReportedUsers();
                    }else {
                        createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
                    }
                });

                dashboardDivContent.appendChild(user);
            }
        }else {
            loadingText.textContent="No reported users found! Yay!";
        }
    });
}

async function okReport(userId, username, reason) {
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
        return resp.json();
    }).then((success)=>{
        return success;
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
        return resp.json();
    }).then((success)=>{
        return success;
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
        return resp.json();
    }).then((success)=>{
        return success;
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
        return resp.json();
    }).then((success)=>{
        if (success) {
            fetch()
        }
        return success;
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
        return resp.json();
    }).then((success)=>{
        return success;
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