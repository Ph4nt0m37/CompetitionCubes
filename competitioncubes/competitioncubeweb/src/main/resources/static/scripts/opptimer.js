import { timerStates } from "./timer.js";

const oppTimer = document.getElementById("opp-timer");
let timerState = timerStates.STOPPED;

let timerInterval = null;

export function setTimerState(ts) {
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
        let ms = 0;
        let s = 0;
        let min = 0;
        timerInterval = setInterval(()=> {
            ms+=1;
            if (ms>=100) {
                ms=0;
                s++;
            }
            if (s>=60) {
                s=0;
                min++;
            }

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