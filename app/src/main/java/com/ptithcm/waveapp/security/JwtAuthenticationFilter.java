package com.ptithcm.waveapp.security;

import lombok.RequiredArgsConstructor;

/**
 * Filter này đã được vô hiệu hóa vì Android không hỗ trợ Servlet Filter / Spring Security.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
}
