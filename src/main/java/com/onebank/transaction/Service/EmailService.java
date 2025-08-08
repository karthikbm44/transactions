package com.onebank.transaction.Service;

import com.onebank.transaction.Dto.EmailDetails;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmailWithAttachment(EmailDetails emailDetails) throws MessagingException;
}
