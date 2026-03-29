import { timerStates } from "./timer_private.js";
import { setScramble } from "./competition_private.js";

let oppTimer = document.getElementById("opp-timer");
let oppPenaltyText = document.getElementById("penalty-text-opp");
let timerState = null;

let timerInterval = null;

export function setTimerState(ts) {
    timerState = timerStates.STOPPED;
    timerState = ts;
    if (timerState===timerStates.STOPPED) {
        oppTimer.style.color="black";
        clearInterval(timerInterval);
    }else if (timerState===timerStates.INSPECTION) {
        
        oppTimer.style.color="rgb(255, 0, 0)";
        let inspectionTime = 15;
        clearInterval(timerInterval);
        oppTimer.textContent=inspectionTime.toString();
        timerInterval = setInterval(()=> {
            inspectionTime--;
            if (inspectionTime>0) {
                oppTimer.textContent=inspectionTime.toString();
            }else if (inspectionTime<1 && inspectionTime>-2) {
                oppTimer.textContent="+2";
            }else {
                oppTimer.textContent="DNF";
                clearInterval(timerInterval);
            }
        },1000);
        
    }else if (timerState===timerStates.TIMING) {
        oppTimer.style.color="black";
        clearInterval(timerInterval);
        let startTime = Date.now();
        timerInterval = setInterval(()=> {
            let ms = Math.floor(((Date.now()-startTime)/10)%100);
            let s = Math.floor(((Date.now()-startTime)/1000)%60);
            let min = Math.floor((Date.now()-startTime)/60000);

            if (min) {
                oppTimer.textContent=`${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;
            } else {
                oppTimer.textContent=`${s.toString()}.${(ms).toString().padStart(2,0)}`;
            }
        },10);
    }
}

export function setOppTime(time) {
    oppTimer.textContent=time;
}

export function setEarlyTime(time) {
    oppTimer.textContent=time;
    setScramble("Waiting for Opponent to confirm solve...");
}

export function setPenalty(penalty) {
    oppPenaltyText.style.display="block";
    oppPenaltyText.textContent = penalty;
    if (penalty==="+2" || penalty==="+4") {
        oppPenaltyText.style.color="#d7e233";
    }else if (penalty==="DNF") {
        oppPenaltyText.style.color="#e23333";
    }
}

export function clearPenalty() {
    oppPenaltyText.style.display="none";
    oppPenaltyText.style.color="#242424";
}