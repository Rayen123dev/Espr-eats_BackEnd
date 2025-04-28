package tn.esprit.projet_pi.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.esprit.projet_pi.Log.OAuth2AuthenticationSuccessHandler;
import tn.esprit.projet_pi.Service.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2AuthenticationSuccessHandler successHandler,
                          CustomOAuth2UserService customOAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.successHandler = successHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // on désactive CSRF pour les API stateless
                .csrf(AbstractHttpConfigurer::disable)

                // configuration des accès
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/verify-email").permitAll()

                        // authentication publique
                        .requestMatchers("/api/auth/**", "/api/auth/user_del/**").permitAll()
                        .requestMatchers("/api/reclamations/**").permitAll()
                        .requestMatchers("/api/abonnement/**").permitAll()

                        .requestMatchers("/api/menus/**").permitAll()
                        .requestMatchers("/api/plats/**").permitAll()
                        .requestMatchers("/api/consultations/**").permitAll()
                        .requestMatchers("/api/historique-profil/**").permitAll()
                        .requestMatchers("/api/recommandations/**").permitAll()
                        .requestMatchers("/api/enums/**").permitAll()
                        .requestMatchers("/api/image-analysis/**").permitAll()
                        .requestMatchers("/produit/**").permitAll()
                        .requestMatchers("/historique/**").permitAll()
                        .requestMatchers("/api/discount/**").permitAll()
                        .requestMatchers("/api/transaction/**").permitAll()
                        .requestMatchers("/post/**").permitAll()
                        .requestMatchers("/reaction/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()

                        // offres d'emploi publiques
                        .requestMatchers("/api/offer/**").permitAll()

                        // candidatures : POST nécessite authentification, GET reste public
                        .requestMatchers(HttpMethod.POST, "/api/application/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/application/**").permitAll()

                        // profil et autres routes non listées
                        .requestMatchers("/api/profil/**").authenticated()
                        .anyRequest().authenticated()
                )

                // on intercale ton filtre JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Cross-Origin Resource Sharing
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))

                // OAuth2 login Google
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(successHandler)
                )

                // en cas d’accès non autorisé, on renvoie 401 plutôt que redirect
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.addAllowedOrigin("http://localhost:4200");
        cfg.addAllowedOrigin("http://192.168.1.19:4200");
        cfg.addAllowedMethod("*");
        cfg.addAllowedHeader("*");
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
