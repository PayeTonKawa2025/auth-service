package org.payetonkawa.auth.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PublicKeyController {

    private final JwtService jwtService;

    @GetMapping(value = "/public-key", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getPublicKey() {
        return jwtService.getEncodedPublicKey();
    }
}
