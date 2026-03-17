package com.scanspend.service;

import com.scanspend.dto.ReminderDto;
import com.scanspend.model.Reminder;
import com.scanspend.model.User;
import com.scanspend.repository.ReminderRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {
    private final ReminderRepository repository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JavaMailSender mailSender;

    public ReminderService(ReminderRepository repository, UserDetailsServiceImpl userDetailsService, JavaMailSender mailSender) {
        this.repository = repository;
        this.userDetailsService = userDetailsService;
        this.mailSender = mailSender;
    }

    public List<ReminderDto> listForCurrentUser() {
        User user = userDetailsService.getCurrentUser();
        return repository.findByUser(user).stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReminderDto create(ReminderDto dto) {
        User user = userDetailsService.getCurrentUser();
        Reminder r = new Reminder();
        r.setTitle(dto.title());
        r.setAmount(dto.amount());
        r.setDueDate(dto.dueDate());
        r.setNotified(false);
        r.setUser(user);
        return toDto(repository.save(r));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Scheduled(fixedDelay = 60_000)
    public void notifyDue() {
        LocalDate now = LocalDate.now();
        LocalDate soon = now.plusDays(1);
        List<Reminder> due = repository.findByNotifiedFalseAndDueDateBetween(now, soon);
        for (Reminder r : due) {
            r.setNotified(true);
            repository.save(r);
            trySendEmail(r);
            System.out.println("REMINDER: " + r.getTitle() + " due " + r.getDueDate() + " amount " + r.getAmount());
        }
    }

    private void trySendEmail(Reminder r) {
        try {
            if (mailSender instanceof JavaMailSenderImpl sender && (sender.getHost() == null || sender.getHost().isBlank())) {
                return; // mail not configured
            }
            User user = r.getUser();
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("EMI due soon: " + r.getTitle());
            msg.setText("Reminder: " + r.getTitle() + " is due on " + r.getDueDate() + " amount ₹" + r.getAmount());
            mailSender.send(msg);
        } catch (Exception ignored) {
            // fail-safe: just log
            System.err.println("Email reminder send failed: " + ignored.getMessage());
        }
    }

    private ReminderDto toDto(Reminder r) {
        return new ReminderDto(r.getId(), r.getTitle(), r.getAmount(), r.getDueDate(), r.isNotified());
    }
}
