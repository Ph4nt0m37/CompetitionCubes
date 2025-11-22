fetch("/api/get-invalid-times").then((promise)=>{
    return promise.json();
}).then((solves)=>{
    const dashboardDiv = document.getElementById("dashboard-div");
    const loadingText = document.getElementById("loading-text");
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
        dashboardDiv.appendChild(solve);
    }
});