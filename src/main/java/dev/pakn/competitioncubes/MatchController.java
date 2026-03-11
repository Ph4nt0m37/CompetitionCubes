package dev.pakn.competitioncubes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@RestController
public class MatchController {

    private static Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private static SimpMessagingTemplate staticSimpMessagingTemplate;

    private static ArrayList<Match> matches = new ArrayList<>();

    private static HashMap<PrivateMatchRequest, Integer> privateMatchRequests = new HashMap<>();

    @PostConstruct
    public void init() {
        staticSimpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/find-match")
    @SendTo("/room/found-match")
    public Match findMatch(WaitlistRequest waitlistRequest) {
        try {
            //cloning waitlist because we want to ignore userId and if we don't clone it we will accidentally remove userId from actual waitlist
            ArrayList<WaitlistRequest> waitList = (ArrayList<WaitlistRequest>) MatchFinder.getWaitingList().clone();
            for (int i=0;i<waitList.size();i++) {
                if (waitList.get(i).getUserId()==waitlistRequest.getUserId()) waitList.remove(i);
            }
            User user = DBController.getUsers().get(waitlistRequest.getUserId());
            for (WaitlistRequest oppReq:waitList) {
                User oppUser = DBController.getUsers().get(oppReq.getUserId());
                int oppId = oppReq.getUserId();
                Event event = DBController.stringToEventMap.get(waitlistRequest.getEvent());
                if (oppReq.getEvent().equals(waitlistRequest.getEvent()) && Math.abs(user.getElo(event)-oppUser.getElo(event))<100) {
                    //fix
                    MatchFinder.removeFromWaitingList(user.getUserId());
                    MatchFinder.removeFromWaitingList(oppId);
                    logger.info("match found between "+user.getUserId()+" and "+oppId);
                    Match match = new Match(event,new int[]{user.getUserId(),oppId},(int)(Math.random()*9999999),false);
                    matches.add(match);
                    user.setCurrentMatch(match);
                    oppUser.setCurrentMatch(match);
                    return match;
                }
            }
            return new Match();
        }catch (Exception e) {
            e.printStackTrace();
            return new Match();
        }
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
    public Match getMatchInfo(@PathVariable int roomId) {
        for (Match currMatch:matches) {
            if (currMatch.getRoomId()==roomId) {
                return currMatch;
            }
        }
        return null;
    }

    public static ArrayList<Match> getMatches() {
        return matches;
    }

    //very sloppy code, might fix later (probably not lol)
    @MessageMapping("/private-match-request")
    @SendTo("/room/private-match-update/")
    public PrivateMatchRequest privateMatchRequest(PrivateMatchRequest privateMatchRequest) {
        try {
            //for some reason keySet().contains does not work (im assuming because its parameter is an Object type on a PrivateMatchRequest type)
            for (PrivateMatchRequest pMatchReq:privateMatchRequests.keySet()) {
                logger.info(String.valueOf(pMatchReq.equals(privateMatchRequest)));
                if (pMatchReq.equals(privateMatchRequest)) {
                    privateMatchRequests.remove(pMatchReq);
                    if (privateMatchRequest.accepted()) {
                        int userId = pMatchReq.getUserId();
                        User user = DBController.getUserByIDList(userId);
                        if (user.getCurrentMatch()!=null) {
                            privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.IN_MATCH);
                            return privateMatchRequest;
                        }
                        int oppId = pMatchReq.getOppId();
                        User opp = DBController.getUserByIDList(oppId);
                        if (opp.getCurrentMatch()!=null) {
                            privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.OPP_IN_MATCH);
                            return privateMatchRequest;
                        }
                        Event event = DBController.stringToEventMap.get(pMatchReq.getEvent());
                        MatchFinder.removeFromWaitingList(userId);
                        MatchFinder.removeFromWaitingList(oppId);
                        logger.info("private match created between "+userId+" and "+oppId);
                        PrivateMatch match = new PrivateMatch(event,new int[]{userId,oppId},(int)(Math.random()*9999999));
                        matches.add(match);
                        user.setCurrentMatch(match);
                        opp.setCurrentMatch(match);
                        privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.ACCEPTED);
                        privateMatchRequest.setMatch(match);
                        return privateMatchRequest;   
                    }else {
                        privateMatchRequest.setPrivateRequestCode(PrivateRequestCode.REJECTED);
                        return privateMatchRequest;
                    }
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
    }
}
