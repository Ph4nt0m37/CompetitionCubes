export let userId = Math.floor(Math.random()*100000);

const searchButton = document.getElementById("search-button");
const searchText = document.getElementById("searching-text");
let searchInt = null;

//client stuff
const stompClient = new StompJs.Client({
    brokerURL: `wss://${window.location.host}/user-connect`
});

stompClient.onConnect = (frame)=>{
    console.log("connected: "+ frame);
    stompClient.subscribe('/room/matches', (matchJSON) => {
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
            window.location.replace(`${window.location.origin}/competition?roomId=${roomId}&userId=${userId}`);
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



searchButton.addEventListener("click",()=>{
    if (searchButton.textContent==="Search for match") {
        searchButton.textContent = "Cancel Search";
        startMatchSearch();
    }else {
        searchButton.textContent = "Search for match";
        cancelMatchSearch();
    }

});

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