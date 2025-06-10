//export let roomId = Math.floor(Math.random()*100000)
export let roomId = new URLSearchParams(window.location.search).get("roomId");
export let userId = new URLSearchParams(window.location.search).get("userId");
import { stompClient } from "./comp_connect.js";