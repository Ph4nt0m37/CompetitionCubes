export let stompClient = null;

export let navUserId = 0;
export let navUser = null;

const header = document.getElementById("header");

let stompScript=document.createElement('script');
stompScript.src = "https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js";
document.getElementsByTagName('head')[0].appendChild(stompScript);


fetch("/header.html")
        .then(res => res.text())
        .then(html => {
        header.innerHTML = html;

        /*const leaderboardButton = document.getElementById("leaderboard-button");

        leaderboardButton.addEventListener("click",()=>{
            window.location.href=`/rankings`;
        });*/

        /*const rulesButton = document.getElementById("rules-button");

        rulesButton.addEventListener("click",()=>{
            window.location.href=`/rules`;
        });*/

        /*const bugButton = document.getElementById("bug-button");

        bugButton.addEventListener("click",()=>{
            window.open("https://forms.gle/XqEMeJ8JavkhuhVD7","_blank");
        });*/

        /*const feedbackButton = document.getElementById("feedback-button");

        feedbackButton.addEventListener("click",()=>{
            window.open("https://forms.gle/j8VnwBCfFiT52ryCA","_blank");
        });*/

        const signInButton = document.getElementById("sign-in-button");

        /*
        //sign in stuff
        signInButton.addEventListener("click",()=>{
            window.location.replace(`/wca-auth`);
        });*/

        const profileButton = document.getElementById("profile-button");
        const profileDropdown = document.getElementById("profile-dropdown")
        const profileDropdownContent = document.getElementById("profile-dropdown-content");

        const profileDropdownLink = document.getElementById("profile-dropdown-link");
        const settingsDropdownLink = document.getElementById("settings-dropdown-link");
        const tutorialDropdownLink = document.getElementById("tutorial-dropdown-link");
        const signOutDropdownLink = document.getElementById("sign-out-dropdown-link");

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
            profileDropdownContent.style.visibility="hidden";
        });

        settingsDropdownLink.addEventListener("click",()=>{
            profileDropdownContent.style.visibility="hidden";
            window.location.href=`/settings`;
        });

        signOutDropdownLink.addEventListener("click",()=>{
            fetch(`/api/waiting-list`, {
                method: "DELETE",
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            }).catch(error=>{
                //do nothing!
            });
            fetch('/wca-auth/sign-out', {
                method: 'DELETE'
            }).then(() => {
                location.reload();
            });
        });

        /*signInButton.addEventListener("click",()=>{
            window.location.replace(`${window.location.origin}/wca-auth`);
        });*/

        const searchBar = document.getElementById("search-bar");
        searchBar.addEventListener("keydown",(event)=>{
            if (event.key==="Enter") {
                window.location.href=`/search?query=${searchBar.value}`;
            }
        });

        const searchIcon = document.getElementById("search-icon");
        searchIcon.addEventListener("click",()=>{
            window.location.href=`/search?query=${searchBar.value}`;
        });

        fetch(`/api/get-user-data`).then((response)=> {
            if (response.ok)
                return response.json();
            createNotification("Something went wrong loading your data, so some things may not work as expected. Please contact a developer if this keeps happening.");
            }).then(function(data) {
                navUser = data;
                navUserId=data.userId;
                profileButton.textContent = data.username;
                profileDropdownLink.href=`/user/${navUserId}`;
                const chevronIcon = document.createElement("img");
                chevronIcon.src="/images/chevron-down.svg";
                profileButton.appendChild(chevronIcon);
                if (navUserId>0) {
                    signInButton.style.display="none";
                    profileButton.style.display="flex";
                }else {
                    profileButton.style.display="none";
                    signInButton.style.display="block";
                }

                if (data['permissionLevel']['hasAdminDashboardAccess']) {
                    document.getElementById("admin-dropdown-link").style.display="block";
                }
            }).catch(function(err) {
                //console.log('Failed to fetch!', err);
                profileButton.style.display="none";
                signInButton.style.display="block";
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
                        console.log(match);
                        fetch(`/api/waiting-list`, {
                            method: "DELETE",
                            headers: {
                                "Content-type": "application/json; charset=UTF-8"
                            }
                        }).catch(error=>{
                            //do nothing!
                        });
                        //users[0] will be the requester
                        if (navUserId===users[0]) {
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
                    }
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
                        'event': "3x3",
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
                        'event': "3x3",
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
    });