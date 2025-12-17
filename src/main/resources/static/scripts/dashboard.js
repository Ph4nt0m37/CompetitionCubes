const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

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
                    success = await dnfSingle(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble']);
                }else if (type==="Average") {
                    success = await dnfAverage(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble']);
                }
                if (success) {
                    createNotification(`Successfully DNF'ed ${user}'s ${timeString}s solve`);
                    solve.remove();
                }
            });

            dashboardDiv.appendChild(solve);
        }
    }else {
        loadingText.textContent="No invalid solves found! Yay!";
    }
});

async function dnfSingle(userId, event, time) {
    return fetch("/api/dnf-single", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time
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

async function dnfAverage(userId, event, time) {
    return fetch("/api/dnf-average", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time
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
        },2500);
    },5000);
}