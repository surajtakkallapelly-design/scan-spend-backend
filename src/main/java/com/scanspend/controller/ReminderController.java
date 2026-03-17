package com.scanspend.controller;

import com.scanspend.dto.ReminderDto;
import com.scanspend.service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ResponseEntity<List<ReminderDto>> list() {
        return ResponseEntity.ok(reminderService.listForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<ReminderDto> create(@RequestBody ReminderDto dto) {
        return ResponseEntity.ok(reminderService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reminderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
