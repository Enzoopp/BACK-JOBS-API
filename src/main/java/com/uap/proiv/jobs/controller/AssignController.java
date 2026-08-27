package com.uap.proiv.jobs.controller;

import com.uap.proiv.jobs.dto.AssignRequest;
import com.uap.proiv.jobs.dto.UserJobAssigned;
import com.uap.proiv.jobs.service.UserJobAssignedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AssignController {

    private final UserJobAssignedService userJobAssignedService;

    @Autowired
    public AssignController(UserJobAssignedService userJobAssignedService) {
        this.userJobAssignedService = userJobAssignedService;
    }

    @PostMapping("/assign")
    public ResponseEntity<Object> assign(@RequestBody  @Valid AssignRequest request) {
        try {
            List<UserJobAssigned> list = userJobAssignedService.assign();
            Map<String, Object> response = new HashMap<>();
            response.put("Client", request.getClientName());
            response.put("Request_Number", request.getRequestNumber());
            response.put("Assign", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
