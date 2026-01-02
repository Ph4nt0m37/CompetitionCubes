const usernameText = document.getElementById("username-text");
const wcaLink = document.getElementById("wca-link");
const eloText = document.getElementById("elo-text");

let pathname = window.location.pathname.split("/");

const EVENTS = [
    "TWO_BY_TWO",
    "THREE_BY_THREE",
    "FOUR_BY_FOUR",
    "FIVE_BY_FIVE",
    "SIX_BY_SIX",
    "SEVEN_BY_SEVEN",
    "CLOCK",
    "SQUARE_ONE",
    "SKEWB",
    "PYRAMINX",
    "MEGAMINX",
    "THREE_OH",
    "THREE_BLD"
]
const eventsToText = {
    "TWO_BY_TWO":"2x2",
    "THREE_BY_THREE":"3x3",
    "FOUR_BY_FOUR":"4x4",
    "FIVE_BY_FIVE":"5x5",
    "SIX_BY_SIX":"6x6",
    "SEVEN_BY_SEVEN":"7x7",
    "CLOCK":"Clock",
    "SQUARE_ONE":"Square-1",
    "SKEWB":"Skewb",
    "PYRAMINX":"Pyraminx",
    "MEGAMINX":"Megaminx",
    "THREE_OH":"3x3 OH",
    "THREE_BLD":"3x3 BLD"
}

const rankToClassName = {
    "BRONZE1":"bronze-rank",
    "BRONZE2":"bronze-rank",
    "BRONZE3":"bronze-rank",
    "SILVER1":"silver-rank",
    "SILVER2":"silver-rank",
    "SILVER3":"silver-rank",
    "GOLD1":"gold-rank",
    "GOLD2":"gold-rank",
    "GOLD3":"gold-rank",
    "DIAMOND1":"diamond-rank",
    "DIAMOND2":"diamond-rank",
    "DIAMOND3":"diamond-rank",
    "EMERALD1":"emerald-rank",
    "EMERALD2":"emerald-rank",
    "EMERALD3":"emerald-rank",
    "CHAMPION":"champ-rank",
}

const rankEnumToRankText = {
    "BRONZE1":"Bronze I",
    "BRONZE2":"Bronze II",
    "BRONZE3":"Bronze III",
    "SILVER1":"Silver I",
    "SILVER2":"Silver II",
    "SILVER3":"Silver III",
    "GOLD1":"Gold I",
    "GOLD2":"Gold II",
    "GOLD3":"Gold III",
    "DIAMOND1":"Diamond I",
    "DIAMOND2":"Diamond II",
    "DIAMOND3":"Diamond III",
    "EMERALD1":"Emerald I",
    "EMERALD2":"Emerald II",
    "EMERALD3":"Emerald III",
    "CHAMPION":"Champion",
}

let userId = pathname[pathname.length-1];

//if I switch back to event buttons i'll use this
/*const button3x3 = document.getElementById("3x3-button");
button3x3.style.backgroundColor="#3df188";

let selectedEvent = threeCubeButton.getAttribute("event");*/

const eloButton = document.getElementById("elo-button");
eloButton.style.backgroundColor="#3df188";

const singleButton = document.getElementById("single-button");
const averageButton = document.getElementById("avg-button");

const rankList = {
    elo: -1,
    single: -1,
    average: -1
}

const sortingMethodMap = {
    ELO: 1,
    SINGLE: 2,
    AVERAGE: 3
}

const eloRankText = document.querySelector(".rankings-key.elo-text");
const singleRankText = document.querySelector(".rankings-key.single-text");
const avgRankText = document.querySelector(".rankings-key.avg-text");

eloRankText.classList.add("rank-highlight");

let currentSortingMethod = sortingMethodMap.ELO;

fetch(`/api/get-user-data-by-id/${userId}`).then((response)=> {
    return response.json();
    }).then(function(data) {
        let user = data;
        console.log(data);
        usernameText.textContent=user.username;
        wcaLink.href=`https://www.worldcubeassociation.org/persons/${user["wcaId"]}`;
        wcaLink.target="_blank";
        let badgesDiv = document.getElementById("badges-div");
        for (let i=0;i<user.badges.length;i++) {
            let clonedBadge = badgesDiv.children[user.badges[i]].cloneNode(true);
            clonedBadge.style.display="block";
            badgesDiv.appendChild(clonedBadge);
        }

        fetch(`/api/get-user-elo-ranks/${userId}`).then((response)=> {
            return response.json();
        }).then(function(worldRanks) {
            rankList.elo = worldRanks;
            let rankingsDiv = document.getElementById("rankings-div");
            let eventTemplate = document.getElementById("event-entry-template");
            // v this loop is for all of the events
            //for (let i=0;i<EVENTS.length;i++) {
            for (let i=1;i<=1;i++) {
                let clonedEvent = eventTemplate.cloneNode(true);
                clonedEvent.id="";
                clonedEvent.style.display="flex";
                rankingsDiv.appendChild(clonedEvent);
                //i+2 because we already have two of each ...-text for the key and template
                //CHANGE ALL OF THE i+1 TO i+2 WHEN GOING BACK TO THE TOP IF STATEMENT
                document.getElementsByClassName("event-text")[i+1].textContent = eventsToText[EVENTS[i]];
                document.getElementsByClassName("elo-text")[i+1].textContent = user.elos[EVENTS[i]];
                document.getElementsByClassName("rank-text")[i+1].children[0].textContent = rankEnumToRankText[user.ranks[EVENTS[i]]];
                document.getElementsByClassName("rank-text")[i+1].children[0].classList.add(rankToClassName[user.ranks[EVENTS[i]]])
                const single = user.singles[EVENTS[i]];
                if (single===-1) {
                    document.getElementsByClassName("single-text")[i+1].textContent = "N/A";
                }else{
                    document.getElementsByClassName("single-text")[i+1].textContent = single.toFixed(2);
                }
                const average = user.averages[EVENTS[i]];
                if (average===-1) {
                    document.getElementsByClassName("avg-text")[i+1].textContent = "N/A";
                }else{
                    document.getElementsByClassName("avg-text")[i+1].textContent = average.toFixed(2);
                }
            }
            sortByRankList(rankList.elo);
        });

        fetch(`/api/get-user-single-ranks/${userId}`).then((response)=> {
            return response.json();
        }).then(function(worldRanks) {
            rankList.single = worldRanks;
        });

        fetch(`/api/get-user-average-ranks/${userId}`).then((response)=> {
            return response.json();
        }).then(function(worldRanks) {
            rankList.average = worldRanks;
        });
        updateUserStatistics(user);
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
    });

function sortByRankList(rankList) {
    //for (let i=0;i<EVENTS.length;i++) {
    for (let i=1;i<=1;i++) {
        document.getElementsByClassName("world-rank-text")[i+1].textContent = String(rankList[EVENTS[i]]);
    }
}

eloButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.ELO) {
        clearOptionsColors();
        eloButton.style.backgroundColor="#3df188";
        eloRankText.classList.add("rank-highlight");
        sortByRankList(rankList.elo);
        currentSortingMethod = sortingMethodMap.ELO;
    }
});

singleButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.SINGLE) {
        clearOptionsColors();
        singleButton.style.backgroundColor="#3df188";
        singleRankText.classList.add("rank-highlight");
        sortByRankList(rankList.single);
        currentSortingMethod = sortingMethodMap.SINGLE;
    }
});

averageButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.AVERAGE) {
        clearOptionsColors();
        averageButton.style.backgroundColor="#3df188";
        avgRankText.classList.add("rank-highlight");
        sortByRankList(rankList.average);
        currentSortingMethod = sortingMethodMap.AVERAGE;
    }
});

function clearOptionsColors() {
    eloButton.style.backgroundColor="#f7f7f7";
    singleButton.style.backgroundColor="#f7f7f7";
    averageButton.style.backgroundColor="#f7f7f7";
    eloRankText.classList.remove("rank-highlight");
    singleRankText.classList.remove("rank-highlight");
    avgRankText.classList.remove("rank-highlight");
}

function updateUserStatistics(user) {
    const matchesPlayedText = document.getElementById("matches-played-text");
    const matchesWonText = document.getElementById("matches-won-text");
    const matchesLostText = document.getElementById("matches-lost-text");
    const winLossText = document.getElementById("win-loss-text");

    const matchesWon = user.matchesWon;
    const matchesLost = user.matchesLost;
    const matchesPlayed = matchesWon+matchesLost;

    matchesPlayedText.textContent=`Matches Played: ${matchesPlayed}`;
    matchesWonText.textContent=`Matches Won: ${matchesWon}`;
    matchesLostText.textContent=`Matches Lost: ${matchesLost}`;
    const winLoss = (matchesWon/(matchesPlayed))*100;
    if (matchesPlayed>0) {
        winLossText.textContent=`Win-Loss %: ${Math.round((winLoss+Number.EPSILON) * 100) / 100}%`;
    }else {
        winLossText.textContent=`Win-Loss %: N/A`;
    }
}

const reportReasonDropdown = document.getElementById("report-reason-dropdown");

const reportPopup = document.getElementById("report-popup")

const closeReportButton = document.getElementById("close-report-button");
closeReportButton.addEventListener("click",()=>{
    actionsPopup.style.display="none";
    body.style.overflowY="visible";
    //resetting and hiding the report popup
    reportReasonDropdown.children[0].selected = "selected";
});

const flagButton = document.getElementById("report-flag");
const actionsPopup = document.getElementById("background-overlay");
const body = document.getElementsByTagName("body")[0];
flagButton.addEventListener("click",()=>{
    actionsPopup.style.display="grid";
    body.style.overflowY="hidden";
});

actionsPopup.addEventListener("click",(event)=>{
    if (event.target===event.currentTarget) {
        actionsPopup.style.display="none";
        body.style.overflowY="visible";
        //resetting and hiding the report popup
        reportReasonDropdown.children[0].selected = "selected";
    }
});

document.addEventListener("keydown",(event)=>{
    if (event.key=="Escape") {
        actionsPopup.style.display="none";
        body.style.overflowY="visible";
        //resetting and hiding the report popup
        reportReasonDropdown.children[0].selected = "selected";
    }
});