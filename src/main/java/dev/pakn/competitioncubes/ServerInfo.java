package dev.pakn.competitioncubes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.pakn.competitioncubes.PostRequestClass.MaintenanceRequest;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Component
@RestController
public class ServerInfo {

    private static Logger logger = LoggerFactory.getLogger(CompetitioncubesApplication.class);

	//public static final long launchEpoch = 0;
    public static final long launchEpoch = 1774724400000l;

    public static final long badgeAwardDelay = launchEpoch+259200000l;

	private static MaintenanceRequest maintenanceTime = null; //not storing this in database because it's uneeded complexity. sure, it'll reset once I reboot the server but that should be fine

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final File serverInfoFile = new File("C:\\Users\\Phant\\Programs\\CompetitionCubes\\src\\main\\resources\\server_info.json");

    private static final String overridePassword = "0v3rR1de";

    @PostConstruct
    public void init() {
        try {
            JSONObject maintenanceJSONArr = new JSONObject(Files.readString(serverInfoFile.toPath())).getJSONObject("maintenance");
            maintenanceTime = new MaintenanceRequest(maintenanceJSONArr.getLong("time"), maintenanceJSONArr.getString("reason"));
        } catch (Exception e) {
            logger.error("Something went wrong reading the server_info file.",e);
        }
    }
    
    public static boolean hasLaunched() {
		long currentEpoch = System.currentTimeMillis();
		return currentEpoch >= ServerInfo.launchEpoch;
	}

    public static boolean canAwardBadges() {
		long currentEpoch = System.currentTimeMillis();
		return currentEpoch >= ServerInfo.badgeAwardDelay;
	}

	public static MaintenanceRequest getMaintenanceTime() {
		return maintenanceTime;
	}

    public static boolean isUnderMaintenance() {
		return maintenanceTime.getTime()<0 || maintenanceTime.getTime()>(System.currentTimeMillis()/1000l);
	}

	public static void setMaintenanceTime(MaintenanceRequest maintenanceTimeReq) {
        //really ugly but works. Can't change the name of the long or else jackson will serialize it weirdly
		ServerInfo.maintenanceTime = maintenanceTimeReq;
        long time = maintenanceTimeReq.getTime();
        String reason = maintenanceTimeReq.getReason();
        JSONObject maintenanceJsonObject = new JSONObject().put("time", time).put("reason", reason);

        //ugly but works
        try {
            JSONObject serverInfoJson = new JSONObject(Files.readString(serverInfoFile.toPath()));
            try (FileWriter serverInfoFileWriter = new FileWriter(serverInfoFile)) {
                serverInfoJson.put("maintenance", maintenanceJsonObject);
                serverInfoFileWriter.write(serverInfoJson.toString());
                sendMaintenance();
            } catch (IOException e) {
                logger.error("Something went wrong updating the maintenance time.", e);
            }
        } catch (JSONException | IOException e) {
            logger.error("Something went wrong updating the maintenance time.", e);
        }
	}

    @SendTo("/room/under-maintenance")
    public static boolean sendMaintenance() {
        return ServerInfo.isUnderMaintenance();
    }

    @GetMapping("/api/public/check-override-password")
    public ResponseEntity<Boolean> checkOverridePassword(@RequestParam String pass, HttpServletResponse response) {
        if (pass.equals(overridePassword)) {
            Cookie overrideCookie = new Cookie("launch_override", "true");
            overrideCookie.setSecure(true);
            overrideCookie.setHttpOnly(true);
            overrideCookie.setMaxAge(86400);
            overrideCookie.setPath("/");
            response.addCookie(overrideCookie);
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        return new ResponseEntity<>(false,HttpStatus.FORBIDDEN);
    }
    
}
