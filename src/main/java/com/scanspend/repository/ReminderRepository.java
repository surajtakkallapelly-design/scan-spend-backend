package com.scanspend.repository;

import com.scanspend.model.Reminder;
import com.scanspend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUser(User user);
    List<Reminder> findByNotifiedFalseAndDueDateBetween(LocalDate from, LocalDate to);
}
