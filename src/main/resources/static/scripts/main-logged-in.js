export let user = null;
export let userId = null;
//export let userId = Math.floor(Math.random()*100000);

const searchButton = document.getElementById("search-button");
const searchText = document.getElementById("searching-text");
const profileButton = document.getElementById("profile-button");
const profileDropdown = document.getElementById("profile-dropdown")
const profileDropdownContent = document.getElementById("profile-dropdown-content");

//header links
const leaderboardButton = document.getElementById("leaderboard-button");

//dropdown links
const profileDropdownLink = document.getElementById("profile-dropdown-link");


let searchInt = null;

//button functions
searchButton.addEventListener("click",()=>{
    if (searchButton.textContent==="Search for match") {
        searchButton.textContent = "Cancel Search";
        startMatchSearch();
    }else {
        searchButton.textContent = "Search for match";
        cancelMatchSearch();
    }

});

profileDropdownContent.style.visibility="hidden";
profileButton.addEventListener("click",()=>{ 
    if (profileDropdownContent.style.visibility==="hidden") {
        profileDropdownContent.style.visibility="visible";
        profileButton.style.borderRadius="5px 5px 0 0";
    }else {
        profileDropdownContent.style.visibility="hidden";
        profileButton.style.borderRadius="5px";
    }
});

profileDropdownLink.addEventListener("click",()=>{
    cancelMatchSearch();
    profileDropdownContent.style.visibility="hidden";
    window.location.href=`/user/${userId}`;
});

leaderboardButton.addEventListener("click",()=>{
    cancelMatchSearch();
    window.location.href=`/rankings`;
});

onload = (event)=>{
    //on website loading stuff
    fetch(`/api/get-user-data`).then((response)=> {
        return response.json();
        }).then(function(data) {
            user=data;
            userId=user.userId;
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
}

//client stuff
const stompClient = new StompJs.Client({
    brokerURL: `wss://${window.location.host}/user-connect`
});

stompClient.onConnect = (frame)=>{
    console.log("connected: "+ frame);
    stompClient.subscribe('/room/found-match', (matchJSON) => {
        let match = JSON.parse(matchJSON.body)
        let users = match.users;
        let roomId = match.roomId;
        if (users && users.includes(userId)) {
            fetch(`${window.location.origin}/waiting-list`, {
                method: "DELETE",
                body: JSON.stringify({
                    userId: userId
                }),
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            }).catch(error=>{
                //do nothing!
            });
            sessionStorage.setItem("userId",userId);
            window.location.replace(`${window.location.origin}/competition?roomId=${roomId}`);
        }
    });
}

stompClient.onDisconnect = (frame)=>{
    console.log("disconnected: "+ frame);
    //delete request to remove user from waiting list
    fetch(`${window.location.origin}/waiting-list`, {
        method: "DELETE",
        body: JSON.stringify({
            userId: userId
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).catch(error=>{
        //do nothing!
    });

}

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};





function startMatchSearch() {


    searchText.style.visibility="visible";

    //post request to add user to waiting list
    fetch(`${window.location.origin}/waiting-list`, {
        method: "POST",
        body: JSON.stringify({
            userId: userId
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    });

    stompClient.activate();

    //searching for user interval. also controls dots
    let dotCount = 1;
    searchInt = setInterval(()=>{
        //search
        if (dotCount===3) {
            stompClient.publish({
                destination: "/app/find-match",
                body: userId
            });
        }

        //dots
        searchText.textContent = `Searching${".".repeat(dotCount)}`;
        dotCount++;
        if (!(dotCount%4)) {
            dotCount=1;
        }
    },250);
}

function cancelMatchSearch() {
    stompClient.deactivate();
    fetch(`${window.location.origin}/waiting-list`, {
        method: "DELETE",
        body: JSON.stringify({
            userId: userId
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).catch(error=>{
        //do nothing!
    });
    searchText.style.visibility="hidden";
    clearInterval(searchInt);
}