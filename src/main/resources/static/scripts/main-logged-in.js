export let user = null;
export let userId = null;
//export let userId = Math.floor(Math.random()*100000);

const searchButton = document.getElementById("search-button");
const searchText = document.getElementById("searching-text");

let searchInt = null;

onload = (event)=>{
    //on website loading stuff
    fetch(`/api/get-user-data`).then((response)=> {
        return response.json();
        }).then(function(data) {
            user=data;
            userId=user.userId;

            //client stuff
            const stompClient = new StompJs.Client({
                brokerURL: `wss://${window.location.host}/user-connect`,
                connectHeaders: {
                    user_id: String(userId)
                }
            });

            //button functions
            searchButton.addEventListener("click",()=>{
                if (searchButton.textContent==="Search for match") {
                    startMatchSearch(stompClient);
                }else {
                    searchButton.textContent = "Search for match";
                    cancelMatchSearch(stompClient);
                }

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
        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
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