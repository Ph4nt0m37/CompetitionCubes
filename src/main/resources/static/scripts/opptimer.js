import { timerStates } from "./timer.js";

let oppTimer = null;
let timerState = null;

let timerInterval = null;

export function setTimerState(ts) {
    oppTimer = document.getElementById("opp-timer");
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

export function setTime(time) {
    oppTimer.textContent=time;
}