import { stompClient } from "./comp_connect.js";
import { currentScramble, roomId, userId } from "./competition.js";
export const timerStates = {
    TIMING: 0,
    INSPECTION: 1,
    STOPPED: 2
};

window.onload = ()=>{
    const userTimer = document.getElementById("user-timer");
    let timerState = timerStates.STOPPED;

    let timerInterval = null;

    let canStartTimer = false;
    let spaceDown = false;
    let startSpaceDown = 0;

    document.addEventListener("keydown", e=>{
        if (e.key===" ") {
            if (timerState===timerStates.STOPPED && !spaceDown) {
                userTimer.style.color = "rgb(0,255,0)";
            }
            if (!spaceDown) startSpaceDown = Date.now().valueOf();
            spaceDown=true;
            if (timerState===timerStates.INSPECTION) {
                if (Date.now().valueOf()-startSpaceDown>499) {
                    userTimer.style.color="rgb(0,255,0)";
                    canStartTimer=true;
                }else{
                    userTimer.style.color="rgb(238, 255, 0)";
                }
            }
        }
        if (timerState===timerStates.TIMING) {
            clearInterval(timerInterval);
            userTimer.style.color="black";
            stompClient.publish({
                destination: "/app/switchTimer",
                body: JSON.stringify({
                    'roomId':roomId,
                    'state':timerStates.STOPPED,
                    'userId':userId
                })
            });
            stompClient.publish({
                destination: "/app/solveData",
                body: JSON.stringify({
                    'roomId':roomId,
                    'time':userTimer.textContent,
                    'scramble': currentScramble,
                    'userId': userId
                })
            });
        }
    });

    document.addEventListener("keyup", e=>{
        if (timerState===timerStates.TIMING) {
            timerState = timerStates.STOPPED;
            spaceDown=false;
            return;
        }
        if (e.key===" ") {
            spaceDown=false;
            if (timerState===timerStates.STOPPED && !spaceDown) {
                timerState=timerStates.INSPECTION;
                userTimer.style.color = "rgb(255,0,0)";
                stompClient.publish({
                    destination: "/app/switchTimer",
                    body: JSON.stringify({
                        'roomId':roomId,
                        'state':timerStates.INSPECTION,
                        'userId':userId
                    })
                });
                let inspectionTime = 15;
                userTimer.textContent=inspectionTime.toString();
                timerInterval = setInterval(()=> {
                    inspectionTime--;
                    if (inspectionTime>0) {
                        userTimer.textContent=inspectionTime.toString();
                    }else if (inspectionTime<1 && inspectionTime>-2) {
                        userTimer.textContent="+2";
                    }else {
                        userTimer.textContent="DNF";
                        clearInterval(timerInterval);
                    }
                },1000);
            } else if (timerState===timerStates.INSPECTION) {
                if (!canStartTimer) {
                    userTimer.style.color = "rgb(255,0,0)";
                }else {
                    clearInterval(timerInterval)
                    canStartTimer = false;
                    timerState = timerStates.TIMING;
                    userTimer.style.color = "black";
                    //sending start data
                    stompClient.publish({
                        destination: "/app/switchTimer",
                        body: JSON.stringify({
                            'roomId':roomId,
                            'state':timerStates.TIMING,
                            'userId':userId
                        })
                    });
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
                            userTimer.textContent=`${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;
                        } else {
                            userTimer.textContent=`${s.toString()}.${(ms).toString().padStart(2,0)}`;
                        }
                    },10);
                }
            }
        }
    });
}