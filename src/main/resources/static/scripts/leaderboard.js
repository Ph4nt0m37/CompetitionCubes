const leaderboardDiv = document.getElementById("leaderboard-div");
const userEntryTemplate = document.getElementById("user-entry-template");

const threeCubeButton = document.getElementById("3x3-button");
threeCubeButton.style.backgroundColor="#3df188";

const eloButton = document.getElementById("elo-button");
eloButton.style.backgroundColor="#3df188";

const singleButton = document.getElementById("single-button");
const averageButton = document.getElementById("avg-button");

const optionKeyText = document.getElementsByClassName("leaderboard-key elo-text")[0];

const eventMap = {
    "3x3":"THREE_BY_THREE"
};

const sortingMethodMap = {
    ELO: 1,
    SINGLE: 2,
    AVERAGE: 3
}

let selectedEvent = threeCubeButton.getAttribute("event");

let eloSortedUsers = null;
let currentSortingMethod = sortingMethodMap.ELO;
sortByElo(selectedEvent);

eloButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.ELO) {
        clearOptionsColors();
        eloButton.style.backgroundColor="#3df188";
        sortByElo(selectedEvent);
        currentSortingMethod = sortingMethodMap.ELO;
    }
});

singleButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.SINGLE) {
        clearOptionsColors();
        singleButton.style.backgroundColor="#3df188";
        sortBySingle(selectedEvent);
        currentSortingMethod = sortingMethodMap.SINGLE;
    }
});

averageButton.addEventListener("click",()=>{
    if (currentSortingMethod!=sortingMethodMap.AVERAGE) {
        clearOptionsColors();
        averageButton.style.backgroundColor="#3df188";
        sortByAverage(selectedEvent);
        currentSortingMethod = sortingMethodMap.AVERAGE;
    }
});

function clearOptionsColors() {
    eloButton.style.backgroundColor="#f7f7f7";
    singleButton.style.backgroundColor="#f7f7f7";
    averageButton.style.backgroundColor="#f7f7f7";
}

function removeLeaderboardEntries() {
    const children = leaderboardDiv.children;
    for (let i=children.length-1;i>=2;i--) {
        leaderboardDiv.removeChild(children[i]);
    }
}



function sortByElo(selectedEvent) {
    fetch(`/api/get-sorted-users-by-elo/${selectedEvent}`).then((response)=> {
        return response.json();
        }).then(function(data) {
            eloSortedUsers=data;
            removeLeaderboardEntries();
            let lastRank = 1;
            for(let i=0;i<(eloSortedUsers.length>100 ? 100 : eloSortedUsers.length);i++) {
                let userEntry = userEntryTemplate.cloneNode(true);
                let userRank = i+1;
                let userElo = Math.floor(eloSortedUsers[i]['stat']);
                userEntry.removeAttribute("id");
                optionKeyText.textContent = 'ELO';
                if (i>0 && userElo===Math.floor(eloSortedUsers[i-1]['stat'])) {
                    userRank=String(lastRank);
                }else{
                    lastRank=userRank;
                }
                //editing rank
                userEntry.children[0].textContent=String(userRank);
                //editing username
                userEntry.children[1].innerHTML=`<a href=/user/${eloSortedUsers[i]['userId']}>${eloSortedUsers[i]['username']}</a>`;
                //editing elo
                userEntry.children[2].textContent=String(userElo);
                //adding userEntry
                leaderboardDiv.appendChild(userEntry);
            }
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
}

function sortBySingle(selectedEvent) {
    fetch(`/api/get-sorted-users-by-single/${selectedEvent}`).then((response)=> {
        return response.json();
        }).then(function(data) {
            let singleSortedUsers=data;
            removeLeaderboardEntries();
            let lastRank = 1;
            for(let i=0;i<(singleSortedUsers.length>100 ? 100 : singleSortedUsers.length);i++) {
                let userSingle = singleSortedUsers[i]['stat'];
                optionKeyText.textContent = 'Single';
                if (userSingle>-1) {
                    let userEntry = userEntryTemplate.cloneNode(true);
                    userEntry.removeAttribute("id");
                    let userRank = i+1;
                    if (i>0 && userSingle===singleSortedUsers[i-1]['stat']) {
                        userRank=String(lastRank);
                    }else{
                        lastRank=userRank;
                    }
                    //editing rank
                    userEntry.children[0].textContent=String(userRank);
                    //editing username
                    userEntry.children[1].innerHTML=`<a href=/user/${singleSortedUsers[i]['userId']}>${singleSortedUsers[i]['username']}</a>`;
                    //editing elo
                    userEntry.children[2].textContent=String(userSingle);
                    //adding userEntry
                    leaderboardDiv.appendChild(userEntry);
                }
            }
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
}

function sortByAverage(selectedEvent) {
    fetch(`/api/get-sorted-users-by-average/${selectedEvent}`).then((response)=> {
        return response.json();
        }).then(function(data) {
            let averageSortedUsers=data;
            removeLeaderboardEntries();
            let lastRank = 1;
            for(let i=0;i<(averageSortedUsers.length>100 ? 100 : averageSortedUsers.length);i++) {
                let userAverage = averageSortedUsers[i]['stat'];
                optionKeyText.textContent = 'Average';
                if (userAverage>-1) {
                    let userEntry = userEntryTemplate.cloneNode(true);
                    userEntry.removeAttribute("id");
                    let userRank = i+1;
                    userEntry.removeAttribute("id");
                    if (i>0 && userAverage===averageSortedUsers[i-1]['stat']) {
                        userRank=String(lastRank);
                    }else{
                        lastRank=userRank;
                    }
                    //editing rank
                    userEntry.children[0].textContent=String(userRank);
                    //editing username
                    userEntry.children[1].innerHTML=`<a href=/user/${averageSortedUsers[i]['userId']}>${averageSortedUsers[i]['username']}</a>`;
                    //editing elo
                    userEntry.children[2].textContent=String(userAverage);
                    //adding userEntry
                    leaderboardDiv.appendChild(userEntry);
                }
            }
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
}