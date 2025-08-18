let userId = 0;

const leaderboardButton = document.getElementById("leaderboard-button");

leaderboardButton.addEventListener("click",()=>{
    window.location.href=`/rankings`;
});

const signInButton = document.getElementById("sign-in-button");

const profileButton = document.getElementById("profile-button");
const profileDropdown = document.getElementById("profile-dropdown")
const profileDropdownContent = document.getElementById("profile-dropdown-content");

const profileDropdownLink = document.getElementById("profile-dropdown-link");
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
    window.location.href=`/user/${userId}`;
});

signOutDropdownLink.addEventListener("click",()=>{
    fetch('/wca-auth/sign-out', {
        method: 'DELETE'
    }).then(() => {
        location.reload();
    });
});

signInButton.addEventListener("click",()=>{
    window.location.replace(`${window.location.origin}/wca-auth`);
});

fetch(`/api/get-user-data`).then((response)=> {
    return response.json();
    }).then(function(data) {
        userId=data.userId;
        if (userId>0) {
            signInButton.style.display="none";
            profileButton.style.display="block";
        }else {
            profileButton.style.display="none";
            signInButton.style.display="block";
        }
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
        profileButton.style.display="none";
        signInButton.style.display="block";
    });