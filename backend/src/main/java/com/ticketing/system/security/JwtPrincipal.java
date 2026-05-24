package com.ticketing.system.security;

import java.util.Set;

public record JwtPrincipal(String subject, Set<String> roles) {
}
