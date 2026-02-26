package dev.pakn.competitioncubes;

import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CommandManager {
    private static Logger logger = LoggerFactory.getLogger(CommandManager.class);
    @PostMapping("/api/set-maintenance")
    public ResponseEntity<?> setMaintenance(@AuthenticationPrincipal User user, @RequestBody PostRequestClass.MaintenanceRequest req) {
        if (user.getPermissionLevel().canAddMaintenanceTime()) {
            ServerInfo.setMaintenanceTime(req);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    
}
