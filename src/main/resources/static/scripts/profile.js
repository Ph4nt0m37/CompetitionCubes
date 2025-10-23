const usernameText = document.getElementById("username-text");
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
    "BRONZE":"bronze-rank",
    "SILVER":"silver-rank",
    "GOLD":"gold-rank",
    "DIAMOND":"diamond-rank",
    "EMERALD":"emerald-rank",
    "CHAMPION":"champ-rank",
}

let userId = pathname[pathname.length-1];

//if I switch back to buttons i'll use this
/*const button3x3 = document.getElementById("3x3-button");
button3x3.style.backgroundColor="#3df188";

let selectedEvent = threeCubeButton.getAttribute("event");*/


fetch(`/api/get-user-data-by-id/${userId}`).then((response)=> {
    return response.json();
    }).then(function(data) {
        let user = data;
        console.log(data);
        usernameText.textContent=user.username;
        let badgesDiv = document.getElementById("badges-div");
        for (let i=0;i<user.badges.length;i++) {
            let clonedBadge = badgesDiv.children[user.badges[i]].cloneNode(true);
            clonedBadge.style.display="block";
            badgesDiv.appendChild(clonedBadge);
        }
        let rankingsDiv = document.getElementById("rankings-div");
        let eventTemplate = document.getElementById("event-entry-template");
        fetch(`/api/get-user-ranks/${userId}`).then((response)=> {
            return response.json();
        }).then(function(ranks) {
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
                document.getElementsByClassName("world-rank-text")[i+1].textContent = String(ranks[EVENTS[i]]);
                document.getElementsByClassName("rank-text")[i+1].children[0].classList.add(rankToClassName[user.ranks[EVENTS[i]]])
                const single = user.singles[EVENTS[i]];
                if (single===-1) {
                    document.getElementsByClassName("single-text")[i+1].textContent = "N/A";
                }else{
                    document.getElementsByClassName("single-text")[i+1].textContent = single;
                }
                const average = user.averages[EVENTS[i]];
                if (average===-1) {
                    document.getElementsByClassName("avg-text")[i+1].textContent = "N/A";
                }else{
                    document.getElementsByClassName("avg-text")[i+1].textContent = average;
                }
            }
        });
        updateUserStatistics(user);
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
    });

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