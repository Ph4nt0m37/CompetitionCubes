import { user } from "./profile.js";
const viewInfoButton = document.getElementById("view-info-button");
const actionsPopup = document.getElementById("background-overlay");
const body = document.getElementsByTagName("body")[0];

const infoPopup = document.getElementById("info-popup")
const warningsText = document.getElementById("warnings-text");
const bansText = document.getElementById("bans-text");

const closeInfoX = document.getElementById("close-info-x");
closeInfoX.addEventListener("click",()=>{
    actionsPopup.style.display="none";
    infoPopup.style.display = "none";
    body.style.overflowY="visible";
});

viewInfoButton.addEventListener("click",()=>{
    actionsPopup.style.display="grid";
    infoPopup.style.display = "flex";
    warningsText.textContent = `Warnings: ${user['strikes']}`;
    bansText.textContent = `Bans: ${user['bans']}`;
    body.style.overflowY="hidden";
});

actionsPopup.addEventListener("click",(event)=>{
    if (event.target===event.currentTarget) {
        actionsPopup.style.display="none";
        infoPopup.style.display = "none";
        body.style.overflowY="visible";
    }
});

document.addEventListener("keydown",(event)=>{
    if (event.key=="Escape") {
        actionsPopup.style.display="none";
        infoPopup.style.display = "none";
        body.style.overflowY="visible";
    }
});