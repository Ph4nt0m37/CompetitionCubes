import { userId } from "./competition.js";
//export let userId = Math.floor(Math.random()*100000);

const searchText = document.getElementById("searching-text");

const searchButton = document.getElementById("search-button");

let searchInt = null;

const stompClient = new StompJs.Client({
    brokerURL: `wss://${window.location.host}/user-connect`,
    connectHeaders: {
        user_id: userId
    }
});

//search button
searchButton.addEventListener("click",()=>{
    if (searchButton.textContent==="Next Match") {
        searchButton.textContent = "Cancel Search";
        startMatchSearch(stompClient);
    }else {
        searchButton.textContent = "Next Match";
        cancelMatchSearch(stompClient);
    }

});

onload = (event)=>{
    //on website loading stuff
    stompClient.onConnect = (frame)=>{
        console.log("connected: "+ frame);
        stompClient.subscribe('/room/found-match', (matchJSON) => {
            let match = JSON.parse(matchJSON.body)
            let users = match.users;
            let roomId = match.roomId;
            if (users && users.includes(Number.parseInt(userId))) {
                console.log("hellooo");
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
                console.log("redirecting...");
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
}


function startMatchSearch(stompClient) {
    //post request to add user to waiting list
    fetch(`${window.location.origin}/waiting-list`, {
        method: "POST",
        body: JSON.stringify({
            'userId': userId,
            'event':'3x3',
            //'sessionId':
        }),
        headers: {
            "Content-type": "application/json; charset=UTF-8"
        }
    }).then((result)=> {
        return result.json();
    }).then((data)=> {
        if (!data) {
            searchText.style.visibility="visible";
            searchText.style.color="#e23333";
            searchText.textContent = "You are already searching for (or are in) a match!"
            return;
        }
        searchButton.textContent = "Cancel Search";
        searchText.textContent = `Searching...`;
        searchText.style.color="#555";
        searchText.style.visibility="visible";

        stompClient.activate();

        //searching for user interval. also controls dots
        let dotCount = 1;
        searchInt = setInterval(()=>{
            //search
            if (dotCount===3) {
                stompClient.publish({
                    destination: "/app/find-match",
                    body: JSON.stringify({
                        'userId':userId,
                        'event':'3x3'
                    })
                });
            }

            //dots
            searchText.textContent = `Searching${".".repeat(dotCount)}`;
            dotCount++;
            if (!(dotCount%4)) {
                dotCount=1;
            }
        },250);
    });
}

function cancelMatchSearch(stompClient) {
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