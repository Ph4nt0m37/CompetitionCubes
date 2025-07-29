let userId = 0;

fetch(`/api/get-user-data`).then((response)=> {
    return response.json();
    }).then(function(data) {
        userId=data.userId;
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
    });

const leaderboardButton = document.getElementById("leaderboard-button");

leaderboardButton.addEventListener("click",()=>{
    window.location.href=`/rankings`;
});


const profileButton = document.getElementById("profile-button");
const profileDropdown = document.getElementById("profile-dropdown")
const profileDropdownContent = document.getElementById("profile-dropdown-content");

const profileDropdownLink = document.getElementById("profile-dropdown-link");

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