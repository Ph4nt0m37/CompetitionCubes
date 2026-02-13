package dev.pakn.competitioncubes;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DonorController {
    //idk maybe implement later
    @PostMapping("/api/add-donor")
    public ResponseEntity<?> addDonor(@RequestParam String data) {
        JSONObject dataJson = new JSONObject(data);
        //System.out.println("received!");
        //System.out.println(dataJson);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
