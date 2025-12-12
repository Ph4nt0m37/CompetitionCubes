fetch("/api/get-invalid-times").then((promise)=>{
    return promise.json();
}).then((solves)=>{
    const dashboardDiv = document.getElementById("dashboard-div");
    const loadingText = document.getElementById("loading-text");
    if (solves.length>0) {
        loadingText.style.display="none";
        const template = document.querySelector(".template");
        for (let i=0;i<solves.length;i++) {
            let type = "Average";
            if (solves[i]['scramble']) {
                type="Single"
            }
            let solve = template.cloneNode(true);
            solve.querySelector(".solve-user").textContent = solves[i]['username'];
            solve.querySelector(".solve-time").textContent = `${type}: ${solves[i]['timeString']}`;
            solve.querySelector(".wca-pb").textContent = `WCA Single: ${solves[i]['wcasingle']} | WCA Average: ${solves[i]['wcaaverage']}`;
            solve.classList.remove("template");

            solve.querySelector(".dnf-button").addEventListener("click",()=>{
                dnfSingle(solves[i]['userId'], solves[i]['event'], solves[i]['timeDouble']);
            });

            dashboardDiv.appendChild(solve);
        }
    }else {
        loadingText.textContent="No invalid solves found! Yay!";
    }
});

function dnfSingle(userId, event, time) {
    fetch("/api/dnf-single", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    });
}

function dnfAverage(userId, event, time) {
    fetch("/api/dnf-average", {
        method: "POST",
        body: JSON.stringify({
            userId: userId,
            event: event,
            time: time
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    });
}