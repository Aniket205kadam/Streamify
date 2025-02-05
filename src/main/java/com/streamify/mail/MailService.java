package com.streamify.mail;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    // send confirmation mail
    void sendMail(MailConfirmationRequest request) throws MessagingException;

    // send mail
    void sendMail(MailRequest request) throws MessagingException;
}
