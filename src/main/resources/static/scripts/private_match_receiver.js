export let stompClient = undefined;

let navUser = undefined;
let navUserId = undefined;

export async function connectPrivateReceiver() {
    await fetch(`/api/get-user-data`).then(async (response)=> {
        if (response.ok)
            return response.json();
        createNotification("Something went wrong loading your data, so some things may not work as expected. Please contact a developer if this keeps happening.");
        }).then(async function(data) {
            navUser = data;
            navUserId=data.userId;
            return;
        }).catch(async function(err) {
            //do nothing
            return;
    });
    setTimeout(()=>{
        //private match websocket stuff
        stompClient = new StompJs.Client({
            brokerURL: `wss://${window.location.host}/header-connect`,
            connectHeaders: {
                user_id: String(navUserId),
                do_disconnect: false
            }
        });

        stompClient.activate();

        stompClient.onConnect = (frame)=>{
            console.log(frame);
            stompClient.subscribe(`/room/private-match-receiver/${navUserId}`, (reqJson) => {
                let request = JSON.parse(reqJson.body);
                console.log(request);
                createChallengeRequest(request['reqUsername'], request['requestId'])
            });
            stompClient.subscribe('/room/private-match-update/',(reqJson) => {
                let request = JSON.parse(reqJson.body);
                let match = request['match'];
                if (request['privateRequestCode']=="ACCEPTED") {
                    const users = match['users'];
                    fetch(`/api/waiting-list`, {
                        method: "DELETE",
                        headers: {
                            "Content-type": "application/json; charset=UTF-8"
                        }
                    }).catch(error=>{
                        //do nothing!
                    });
                    //users[0] will be the requester. if roomId is -1 AND the request has been accepted, that means it's a private match
                    if (request['requestId']==-1 || navUserId===users[0]) {
                        createNotification(`Your challenge request has been accepted. Redirecting...`,"#22eb51");
                    }
                    //unnecessary timeout, just figured it would look better when a user accepted to have a little delay instead of immediately redirecting
                    setTimeout(()=>{
                        sessionStorage.setItem("userId",navUserId);
                        window.location.replace(`${window.location.origin}/competition?roomId=${match['roomId']}`);
                    },1500);
                }
                if (request['privateRequestCode']=="EXPIRED") {
                    if (navUserId===request['oppId'])
                        createNotification(`This invitation has expired.`,"#c53838");
                }else if (request['privateRequestCode']=="REJECTED") {
                    if (navUserId===request['userId']) {
                        createNotification(`Your challenge request has been rejected.`,"#c53838");
                    }
                }else if (request['privateRequestCode']=="ERROR") {
                    if (navUserId===request['oppId'])
                        createNotification("Something went wrong with this private match. Please try again later or contact a developer.","#c53838");
                }else if (request['privateRequestCode']=="OPP_IN_MATCH") {
                    if (navUserId===request['userId'])
                        createNotification("You are currently in a match. Please try again once you complete your match.","#c53838");
                }else if (request['privateRequestCode']=="IN_MATCH") {
                    if (navUserId===request['userId'])
                        createNotification("This user is currently in a match and cannot accept your request. Please try again later.","#c53838");
                }else if (request['privateRequestCode']=="NOT_ACCEPTING") {
                    if (navUserId===request['userId'])
                        createNotification("This user is not accepting challenge requests right now. Please try again later.","#c53838");
                }else if (request['privateRequestCode']=="WAITING") {
                    if (navUserId===request['userId'])
                        createNotification("Successfully sent challenge request.","#22eb51");
                }
            });

            stompClient.publish({
                destination: `/app/pong/${navUserId}`,
                body: "false"
            });
        
            stompClient.subscribe('/room/ping', (data) =>{
                stompClient.publish({
                    destination: `/app/pong/${navUserId}`,
                    body: "false"
                });
            });
        
            stompClient.subscribe(`/room/ping/${navUserId}`, (data) =>{
                stompClient.publish({
                    destination: `/app/pong/${navUserId}`,
                    body: "false"
                });
            });
        }
    },200); //add a little timeout to MAKE SURE it connects
    const notificationTemplate = document.querySelector(".notification.template");
    function createNotification(text, color) {
        const notif = notificationTemplate.cloneNode(true);
        notif.style.backgroundColor=color;
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

    const notificationBox = document.getElementById("notif-div");
    function createChallengeRequest(username, requestId) {
        const challengeRequestNotifTemplate = document.querySelector(".notification.challenge-req.template");
        const challengeRequestNotif = challengeRequestNotifTemplate.cloneNode(true);
        challengeRequestNotif.classList.remove("template");
        challengeRequestNotif.querySelector(".req-notif-text").textContent = `${username} has sent you a challenge request. Do you want to accept?`;

        const acceptButton = challengeRequestNotif.querySelector(".accept-req-button");
        acceptButton.addEventListener("click",()=>{
            stompClient.publish({
                destination: "/app/private-match-request",
                body: JSON.stringify({
                    'requestId': requestId,
                    //everything after this does not matter (other than accepted)
                    'userId': navUserId,
                    'reqUsername': navUser['username'],
                    'oppId': navUserId,
                    'event': "333",
                    'accepted': true
                })
            });
            createNotification("Successfully accepted the private match. Redirecting...","#22eb51")
            challengeRequestNotif.remove();
        });

        const declineButton = challengeRequestNotif.querySelector(".decline-req-button");
        declineButton.addEventListener("click",()=>{
            stompClient.publish({
                destination: "/app/private-match-request",
                body: JSON.stringify({
                    'requestId': requestId,
                    //everything after this does not matter (other than accepted)
                    'userId': navUserId,
                    'reqUsername': navUser['username'],
                    'oppId': navUserId,
                    'event': "333",
                    'accepted': false
                })
            });
            challengeRequestNotif.remove();
        });

        notificationBox.appendChild(challengeRequestNotif);
        setTimeout(()=>{
            challengeRequestNotif.classList.add("fade-out");
            setTimeout(()=>{
                challengeRequestNotif.remove();
            },1500);
        },60000);
    }
}

export async function connectPrivateReceiverWithData(data, userId) {
    navUser = data;
    navUserId=userId;
    connectPrivateReceiver();
}