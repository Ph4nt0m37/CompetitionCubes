let userId = 0;

const header = document.getElementById("header");

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

        tutorialDropdownLink.addEventListener("click",()=>{
            document.cookie = "tutorial_complete=false;";
            window.location.replace("/");
        });

        signOutDropdownLink.addEventListener("click",()=>{
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
            return response.json();
            }).then(function(data) {
                userId=data.userId;
                profileButton.textContent = data.username;
                profileDropdownLink.href=`/user/${userId}`;
                const chevronIcon = document.createElement("img");
                chevronIcon.src="/images/chevron-down.svg";
                profileButton.appendChild(chevronIcon);
                if (userId>0) {
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
    });