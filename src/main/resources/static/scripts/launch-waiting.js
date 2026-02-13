const launchEpoch = 1774728000000; //March 28th, 2026 3 PM EST
//1774728000000

const timeText = document.getElementById("time-text");

setTimeLeft();
setInterval(setTimeLeft,1000);

function setTimeLeft() {
    const now = Date.now();

    timeText.textContent = epochToDateString(now);

    if (now>=launchEpoch) window.location.reload();
}

function epochToDateString(now) {
    const epochDiff = launchEpoch-now;
    const months = Math.floor(epochDiff/2629800000);
    const days = Math.floor(epochDiff/86400000)%31;
    const hours = Math.floor(epochDiff/3600000)%24;
    const mins = Math.floor(epochDiff/60000)%60;
    const secs = Math.floor(epochDiff/1000)%60;
    if (months<1) {
        if (days<1) {
            return `${hours}h ${mins}m ${secs}s`;
        }
        return `${days}d ${hours}h ${mins}m ${secs}s`;
    }
    return `${months}M ${days}d ${hours}h ${mins}m ${secs}s`;
}