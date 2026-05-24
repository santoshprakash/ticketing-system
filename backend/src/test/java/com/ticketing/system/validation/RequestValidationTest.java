package com.ticketing.system.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.system.auth.dto.RegisterRequest;
import com.ticketing.system.auth.dto.ResetPasswordRequest;
import com.ticketing.system.ticket.domain.TicketPriority;
import com.ticketing.system.ticket.dto.AddTicketAttachmentRequest;
import com.ticketing.system.ticket.dto.CreateTicketRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerRequestShouldRequireStrongPasswordAndValidEmail() {
        var violations = validator.validate(new RegisterRequest("A", "not-email", "weak", null));

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fullName", "email", "password");
    }

    @Test
    void resetPasswordRequestShouldAcceptStrongPassword() {
        var violations = validator.validate(new ResetPasswordRequest("reset-token", "NewPassword123!"));

        assertThat(violations).isEmpty();
    }

    @Test
    void createTicketRequestShouldRequireMinimumTextAndPriority() {
        var violations = validator.validate(new CreateTicketRequest("Bad", "Too short", null, ""));

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "description", "priority", "category");
    }

    @Test
    void attachmentRequestShouldValidateSizeAndChecksum() {
        var violations = validator.validate(new AddTicketAttachmentRequest(
                "error.png",
                "image/png",
                0,
                "tickets/error.png",
                "not-a-checksum"
        ));

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fileSizeBytes", "checksumSha256");
    }

    @Test
    void createTicketRequestShouldAcceptValidPayload() {
        var violations = validator.validate(new CreateTicketRequest(
                "Portal login issue",
                "Customer cannot access the service portal after MFA reset.",
                TicketPriority.HIGH,
                "ACCESS"
        ));

        assertThat(violations).isEmpty();
    }
}
