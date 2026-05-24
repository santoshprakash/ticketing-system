package com.ticketing.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.system.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityExceptionResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityExceptionResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                request.getRequestURI(),
                MDC.get("correlationId"),
                List.of()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
