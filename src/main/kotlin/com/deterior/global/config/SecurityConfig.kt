package com.deterior.global.config

import com.deterior.global.filter.JwtAuthenticationFilter
import com.deterior.global.service.JwtProvider
import com.deterior.global.util.JwtUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@EnableWebSecurity
@Configuration
class SecurityConfig @Autowired constructor(
    private val jwtProvider: JwtProvider,
    private val jwtUtils: JwtUtils
) {
    val permitAllList: MutableList<String> = mutableListOf(
        "/api/member/sign-in",
        "/api/member/sign-up",
        "/api/member/reset-password",
        "/api/mail/**",
        "/test/**",
        "error",
        "/env"
    )

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }         //bearer를 사용하기 위함
            .csrf { it.disable() }              //토큰을 사용하므로 필요없음
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests{ it
                .requestMatchers(*permitAllList.toTypedArray()).permitAll()
                .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthenticationFilter(jwtProvider, jwtUtils), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    //@Order(Ordered.HIGHEST_PRECEDENCE)
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { it
                .requestMatchers("/test/member/user").hasRole("USER")
                .requestMatchers("/test/index").permitAll()
            }
            .build()
        ;
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun authenticationManager(
        authenticationConfiguration: AuthenticationConfiguration
    ): AuthenticationManager = authenticationConfiguration.authenticationManager
}