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

let userId = pathname[pathname.length-1];

fetch(`/api/get-user-data-by-id/${userId}`).then((response)=> {
    return response.json();
    }).then(function(data) {
        let user = data;
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
            for (let i=0;i<EVENTS.length;i++) {
                let clonedEvent = eventTemplate.cloneNode(true);
                clonedEvent.id="";
                clonedEvent.style.display="flex";
                rankingsDiv.appendChild(clonedEvent);
                //i+2 because we already have two of each ...-text for the key and template
                document.getElementsByClassName("event-text")[i+2].textContent = eventsToText[EVENTS[i]];
                document.getElementsByClassName("elo-text")[i+2].textContent = user.elos[EVENTS[i]];
                document.getElementsByClassName("rank-text")[i+2].textContent = String(ranks[EVENTS[i]]);
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
    if (matchesPlayed>0) {
        winLossText.textContent=`Win-Loss %: ${(matchesWon/(matchesPlayed))*100}%`;
    }else {
        winLossText.textContent=`Win-Loss %: N/A`;
    }
}