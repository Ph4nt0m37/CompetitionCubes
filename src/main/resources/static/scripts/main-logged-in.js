export let user = null;
export let userId = null;
//export let userId = Math.floor(Math.random()*100000);

const searchButton = document.getElementById("search-button");
const searchText = document.getElementById("searching-text");

const tutorialDiv = document.getElementById("tutorial-div");
const tutorialButton = document.getElementById("tutorial-accept-button");

const searchingUsersText = document.getElementById("searching-users-text");

let searchInt = null;

onload = (event)=>{
    getWaitingUserCount()
    setInterval(getWaitingUserCount,10000);

    //on website loading stuff
    fetch(`/api/get-user-data`).then((response)=> {
        if (response.ok)
            return response.json();
        createNotification("Something went wrong loading your data, so some things may not work as expected. Please contact a developer if this keeps happening.");
        }).then(function(data) {
            user=data;
            userId=user.userId;

            //notification system
            const warnings = getCookie("warnings");
            if (warnings!="") {
                const strikes = user['strikes'];
                if (strikes>parseInt(warnings)) {
                    const userWarnings = user['userWarnings'];
                    console.log(userWarnings);
                    createNotification(`You have been warned for "${userWarnings[userWarnings.length-1]['reason']}". This warning will expire in a month, or you can dispute it on our Discord.`);   
                }
                document.cookie = `warnings=${strikes};`;
            }else {
                document.cookie = `warnings=${user['strikes']};`;
            }

            //notification system
            const bans = getCookie("bans");
            if (bans!="") {
                const bansRealNum = user['bans'];
                if (bansRealNum>parseInt(bans)) {
                    const userBan = user['userBan'];
                    if (userBan)
                        createNotification(`You have been banned for "${userBan['reason']}". Check your profile for more information.`);   
                }
                document.cookie = `bans=${bansRealNum};`;
            }else {
                document.cookie = `bans=${user['bans']};`;
            }

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
                        fetch(`/waiting-list`, {
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
                fetch(`/waiting-list`, {
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

            let tutorial_complete = false;
            //cookie example: tutorial_complete=false
            const tutorialCompleteCookie = getCookie("tutorial_complete");
            tutorial_complete = (tutorialCompleteCookie==="true");

            if (!tutorial_complete) {
                tutorialDiv.style.display="flex";
                tutorialButton.addEventListener("click",()=>{
                    document.cookie = "tutorial_complete=true;";
                    tutorialDiv.style.display="none";
                });
            }

        }).catch(function(err) {
            console.log('Failed to fetch!', err);
        });
}

function getWaitingUserCount() {
    fetch("/waiting-list/3x3").then((response)=>{
        return response.json();
    }).then((numSearching)=>{
        let userWord = "users";
        if (numSearching==1) userWord = "user";
        searchingUsersText.textContent=`${numSearching} ${userWord} searching for match...`;
    });
}

function startMatchSearch(stompClient) {
    //post request to add user to waiting list
    fetch(`/waiting-list`, {
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
        const waitlistCode = data['waitlistCode'];
        let expirationDate = data['expirationDate'];
        console.log(expirationDate)
        if (expirationDate) expirationDate = expirationDate.replace(" "," (dd/MM/yyyy), ")+" UTC-5";
        if (waitlistCode==="SUCCESS") {
            searchButton.textContent = "Cancel Search";
            searchText.textContent = `Searching...`;
            searchText.style.color="#555";
            searchText.style.display="block";

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
        }else {
            searchText.style.display="block";
            searchText.style.color="#e23333";
            if (waitlistCode==="IN_MATCH") {
                searchText.textContent = "You are already searching for (or are in) a match!"
                return;
            }else if (waitlistCode==="BANNED") {
                searchText.innerHTML = `You have been banned from competing until ${expirationDate}.<br>Appeal your ban on our <a href="https://discord.gg/NsaFuasMrV" target="_blank">Discord</a> or at this <a href="mailto:compcube@pakn.dev">email</a>.`;
                return;
            }else if (waitlistCode==="BANNED_PERMANENTLY") {
                searchText.innerHTML = `You have been permanently banned from competing.<br>Appeal your ban on our <a href="https://discord.gg/NsaFuasMrV" target="_blank">Discord</a> or at this <a href="mailto:compcube@pakn.dev">email</a>.`;
                return;
            }
        }
    });
}

function cancelMatchSearch(stompClient) {
    stompClient.deactivate();
    fetch(`/waiting-list`, {
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
    searchText.style.display="none";
    clearInterval(searchInt);
}

function getCookie(cname) {
  let name = cname + "=";
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i <ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

function createNotification(text) {
    const notif = notificationTemplate.cloneNode(true);
    notif.textContent = text;
    notif.classList.remove("template");
    notificationBox.appendChild(notif);
    setTimeout(()=>{
        notif.classList.add("fade-out");
        setTimeout(()=>{
            notif.remove();
        },1500);
    },5000);
}