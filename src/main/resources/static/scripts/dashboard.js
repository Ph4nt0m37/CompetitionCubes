const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

const reportedSolvesButton = document.getElementById("reported-solves-button");
const reportedUsersButton = document.getElementById("reported-users-button");

const reportingMethods = {
    SOLVES: 1,
    USERS: 2
};

let currReportMethod = reportingMethods.SOLVES;
reportedSolvesButton.style.backgroundColor="#d0d0d0";


fetch("/api/get-invalid-times").then((promise)=>{
    return promise.json();
}).then((solves)=>{
    const dashboardDiv = document.getElementById("dashboard-div");
    const loadingText = document.getElementById("loading-text");
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

            solve.querySelector(".ok-button").addEventListener("click",()=>{
                solve.remove();
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
                }else {
                    createNotification(`Something went wrong with this action. Please DM a developer to resolve it.`);
                }
            });

            dashboardDiv.appendChild(solve);
        }
    }else {
        loadingText.textContent="No invalid solves found! Yay!";
    }
});

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
}