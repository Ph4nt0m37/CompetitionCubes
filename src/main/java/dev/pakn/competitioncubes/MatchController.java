package dev.pakn.competitioncubes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class MatchController {

    private static Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static SimpMessagingTemplate staticSimpMessagingTemplate;

    private static CopyOnWriteArrayList<Match> matches = new CopyOnWriteArrayList<>();

    private static ConcurrentHashMap <PrivateMatchRequest, Integer> privateMatchRequests = new ConcurrentHashMap<>();

    private HashMap<Integer, Integer> rematchRoomIds = new HashMap<>();

    private CopyOnWriteArrayList<Match> prototypeMatches = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        staticSimpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/update-match")
    public void updateMatch(MatchCommand command) {
        Match match = null;
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==command.getRoomId()) {
                match=currMatch;
            }
        }

        if (match==null) return;
        
        if (command.getCommand().equals("solveFinished")) {
            match.nextSolver();
        }

        simpMessagingTemplate.convertAndSend("/room/matches/"+command.getRoomId(),match);
    }

    public static void sendMatchData(Match match) {
        staticSimpMessagingTemplate.convertAndSend("/room/matches/"+match.getRoomId(),match);
    }

    @GetMapping("/api/get-match-info/{roomId}")
    public ResponseEntity<Match> getMatchInfo(@PathVariable int roomId) {
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==roomId) {
                return new ResponseEntity<>(currMatch,HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public static CopyOnWriteArrayList<Match> getMatches() {
        return matches;
    }

    //very sloppy code, might fix later (probably not lol)
    @MessageMapping("/private-match-request")
    @SendTo("/room/private-match-update/")
    public PrivateMatchRequest privateMatchRequest(PrivateMatchRequest privateMatchRequest) {
        try {
            //for some reason keySet().contains does not work (im assuming because its parameter is an Object type on a PrivateMatchRequest type)
            PrivateMatchRequest ogReq = getRequestById(privateMatchRequest.getRequestId());
            if (ogReq!=null) {
                privateMatchRequests.remove(ogReq);
                if (privateMatchRequest.accepted()) {
                    int userId = ogReq.getUserId();
                    User user = DBController.getUserByIDList(userId);
                    if (user.getCurrentMatch()!=null) {
                        ogReq.setPrivateRequestCode(PrivateRequestCode.IN_MATCH);
                        return ogReq;
                    }
                    int oppId = ogReq.getOppId();
                    User opp = DBController.getUserByIDList(oppId);
                    if (opp.getCurrentMatch()!=null) {
                        ogReq.setPrivateRequestCode(PrivateRequestCode.OPP_IN_MATCH);
                        return ogReq;
                    }
                    Event event = DBController.stringToEventMap.get(ogReq.getEvent());
                    MatchFinder.removeFromWaitingList(userId);
                    MatchFinder.removeFromWaitingList(oppId);
                    logger.info("private match created between "+userId+" and "+oppId);
                    PrivateMatch match = new PrivateMatch(event,new int[]{userId,oppId},(int)(Math.random()*9999999));
                    matches.add(match);
                    user.setCurrentMatch(match);
                    opp.setCurrentMatch(match);
                    ogReq.setPrivateRequestCode(PrivateRequestCode.ACCEPTED);
                    ogReq.setMatch(match);
                    return ogReq;
                }else {
                    ogReq.setPrivateRequestCode(PrivateRequestCode.REJECTED);
                    return ogReq;
                }
            }
            if (privateMatchRequest.getRequestId()==-1) {
                int userId = privateMatchRequest.getUserId();
                User user = DBController.getUserByIDList(userId);
                if (user.getCurrentMatch()!=null) {
                    privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.IN_MATCH);
                    return privateMatchRequest;
                }
                int oppId = privateMatchRequest.getOppId();
                User opp = DBController.getUserByIDList(oppId);
                if (opp.getCurrentMatch()!=null) {
                    privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.OPP_IN_MATCH);
                    return privateMatchRequest;
                }
                if (!opp.getUserSettings().acceptsChallengeRequests()) {
                    privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.NOT_ACCEPTING);
                    return privateMatchRequest;
                }
                privateMatchRequest.setRequestId((int)(Math.random()*9999999));
                privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.WAITING);
                privateMatchRequests.put(privateMatchRequest,60);
                simpMessagingTemplate.convertAndSend("/room/private-match-receiver/"+privateMatchRequest.getOppId(),privateMatchRequest);
                return privateMatchRequest;
            }else {
                privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.EXPIRED);
                return privateMatchRequest;
            }
        }catch (Exception e) {
            logger.error("Something went wrong with the private match", e);
            privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.ERROR);
            return privateMatchRequest;
        }
    }

    private PrivateMatchRequest getRequestById(int requestId) {
        for (PrivateMatchRequest privateMatchRequest:privateMatchRequests.keySet()) {
            if (privateMatchRequest.getRequestId()==requestId) {
                return privateMatchRequest;
            }
        }
        return null;
    }

    @MessageMapping("/rematch/{roomId}")
    private void rematchRequest(@DestinationVariable int roomId, RematchRequest rematchRequest) {
        System.out.println(rematchRequest);
        User user = DBController.getUserByIDList(rematchRequest.getUserId());
        simpMessagingTemplate.convertAndSend("/room/rematch/"+roomId+"/"+rematchRequest.getOppId(),user.getUsername());
        if (rematchRoomIds.keySet().contains(roomId)) {
            int userId = rematchRequest.getUserId();
            int oppId = rematchRequest.getOppId();
            User opp = DBController.getUserByIDList(rematchRequest.getOppId());
            Event event = DBController.stringToEventMap.get(rematchRequest.getEvent());
            logger.info("private match created between "+userId+" and "+oppId);
            PrivateMatch match = new PrivateMatch(event,new int[]{userId,oppId},(int)(Math.random()*9999999));
            PrivateMatchRequest pMatchRequest = new PrivateMatchRequest(-1,userId,user.getUsername(),oppId,event.getEventId(),true);
            pMatchRequest.setPrivateRequestCode(PrivateRequestCode.ACCEPTED);
            pMatchRequest.setMatch(match);
            matches.add(match);
            user.setCurrentMatch(match);
            opp.setCurrentMatch(match);
            rematchRoomIds.remove(roomId);
            simpMessagingTemplate.convertAndSend("/room/private-match-update/",pMatchRequest);
        }
        rematchRoomIds.put(roomId,30);
    }

    @Scheduled(fixedRate = 1000)
    private void decrementPrivateRequestExpiration() {
        //need to be weary of concurrentmodificationexception
        PrivateMatchRequest[] requests = privateMatchRequests.keySet().toArray(new PrivateMatchRequest[0]);
        for (int i=0;i<requests.length;i++) {
            PrivateMatchRequest req = requests[i];
            privateMatchRequests.put(req, privateMatchRequests.get(req)-1);
            if (privateMatchRequests.get(req)<0) {
                privateMatchRequests.remove(req);
            }
        }

        Integer[] rematchReqRoomIds = rematchRoomIds.keySet().toArray(new Integer[0]);
        for (int i=0;i<rematchReqRoomIds.length;i++) {
            int rematchRoom = rematchReqRoomIds[i];
            rematchRoomIds.put(rematchRoom, rematchRoomIds.get(rematchRoom)-1);
            if (rematchRoomIds.get(rematchRoom)<0) {
                rematchRoomIds.remove(rematchRoom);
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    public void findMatches() {
        try {
            for (Match match:prototypeMatches) {
                //go through prototype matches and check if users pinged back
                int userId1 = match.getUsers()[0];
                int userId2 = match.getUsers()[1];
                boolean user1Confirmed = HeartbeatHandler.getCurrentConnections().containsKey(userId1);
                boolean user2Confirmed = HeartbeatHandler.getCurrentConnections().containsKey(userId2);
                User user = DBController.getUserByIDList(userId1);
                User oppUser = DBController.getUserByIDList(userId2);
                if (user1Confirmed && user2Confirmed) {
                    logger.info("match created between "+userId1+" and "+userId2);
                    matches.add(match);
                    user.setCurrentMatch(match);
                    oppUser.setCurrentMatch(match);
                    simpMessagingTemplate.convertAndSend("/room/found-match/"+userId1,match);
                    simpMessagingTemplate.convertAndSend("/room/found-match/"+userId2,match);
                }else {
                    //this can only happen if at least one user didnt confirm. Thus, for the user that did confirm, we add them back to the waiting list
                    if (user1Confirmed) {
                        WaitlistRequest request = new WaitlistRequest(userId1, match.getEvent().getEventId());
                        MatchFinder.getWaitingList().add(request);
                    }else {
                        //Remove user for redundancy (although these should both already be null)
                        MatchFinder.removeFromWaitingList(userId1);
                        user.setCurrentMatch(null);
                    }
                    if (user2Confirmed) {
                        WaitlistRequest request = new WaitlistRequest(userId2, match.getEvent().getEventId());
                        MatchFinder.getWaitingList().add(request);
                    }
                    else {
                        //Remove user for redundancy (although these should both already be null)
                        MatchFinder.removeFromWaitingList(userId2);
                        oppUser.setCurrentMatch(null);
                    }
                }
            }
            prototypeMatches.clear();


            //cloning waitlist because we want to ignore userId and if we don't clone it we will accidentally remove userId from actual waitlist
            ArrayList<WaitlistRequest> waitList = new ArrayList<>(MatchFinder.getWaitingList());
            //O(n^2) :barf:
            //TODO: sort requests by ELO, then just check adjacent users
            for (WaitlistRequest userReq:waitList) {
                User user = DBController.getUserByIDList(userReq.getUserId());
                //ideally waitlists would be a hashmap with events and their waitlists, but there's only one event so far so i'll sort this out later
                Event event = DBController.stringToEventMap.get(userReq.getEvent());
                for (WaitlistRequest oppReq:waitList) {
                    if (oppReq.getUserId()==userReq.getUserId()) continue;
                    User oppUser = DBController.getUserByIDList(oppReq.getUserId());
                    if (oppReq.getEvent().equals(userReq.getEvent()) && Math.abs(user.getElo(event)-oppUser.getElo(event))<100 && MatchFinder.getWaitingList().contains(userReq) && MatchFinder.getWaitingList().contains(oppReq)) {
                        HeartbeatHandler.checkHeartbeat(user.getUserId(), 990);
                        HeartbeatHandler.checkHeartbeat(oppUser.getUserId(), 990);
                        MatchFinder.removeFromWaitingList(user.getUserId());
                        MatchFinder.removeFromWaitingList(oppUser.getUserId());
                        logger.info("match made. pinging "+user.getUserId()+" and "+oppUser.getUserId());
                        Match match = new Match(event,new int[]{user.getUserId(),oppUser.getUserId()},(int)(Math.random()*9999999),false);
                        prototypeMatches.add(match);
                    }
                }
            }
        }catch (Exception e) {
            logger.error("find matches error!", e);
        }
    }
}
