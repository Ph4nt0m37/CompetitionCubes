const leaderboardDiv = document.getElementById("leaderboard-div");
const userEntryTemplate = document.getElementById("user-entry-template");

const threeCubeButton = document.getElementById("3x3-button");
threeCubeButton.style.backgroundColor="#2ecc71";

let eloSortedUsers = null;
fetch(`/api/get-sorted-users-by-elo/3x3`).then((response)=> {
        return response.json();
        }).then(function(data) {
            eloSortedUsers=data;
            leaderboardDiv.removeChild(userEntryTemplate);
            let lastRank = 1;
            for(let i=0;i<(eloSortedUsers.length>100 ? 100 : eloSortedUsers.length);i++) {
                let userEntry = userEntryTemplate.cloneNode(true);
                let userRank = i+1;
                let userElo = eloSortedUsers[i]['elo'];
                if (i>0 && userElo===eloSortedUsers[i-1]['elo']) {
                    userRank=String(lastRank);
                }else{
                    lastRank=userRank;
                }
                //editing rank
                userEntry.children[0].textContent=String(userRank);
                //editing username
                userEntry.children[1].textContent=eloSortedUsers[i]['username'];
                //editing elo
                userEntry.children[2].textContent=String(userElo);
                //adding userEntry
                leaderboardDiv.appendChild(userEntry);
            }
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });

