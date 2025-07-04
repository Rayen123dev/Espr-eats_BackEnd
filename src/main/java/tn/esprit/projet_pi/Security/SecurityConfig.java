package tn.esprit.projet_pi.Security;

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


    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          OAuth2AuthenticationSuccessHandler successHandler, CustomOAuth2UserService customOAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/verify-email").permitAll() // ✅ Autoriser vérification email
                        .requestMatchers("/api/auth/**", "/api/auth/user_del/**", "/api/reclamations/**", "/api/abonnement/**", "/api/application/**", "/api/offer/**").permitAll()
                        .requestMatchers("/api/menus/**").permitAll()
                        .requestMatchers("/api/regimes/**").hasRole("Staff")
                        .requestMatchers("/api/plats/**").permitAll()
                        .requestMatchers("/api/consultations/**").permitAll()
                        .requestMatchers("/api/profil/**").authenticated()
                        .requestMatchers("/api/historique-profil/**").permitAll()
                        .requestMatchers("/api/recommandations/**").permitAll()
                        .requestMatchers("/api/enums/**").permitAll()
                        .requestMatchers("/api/image-analysis/**").permitAll()
                        .requestMatchers("produit/**").permitAll()
                        .requestMatchers("historique/**").permitAll()
                        .requestMatchers("/api/abonnement/**").permitAll()
                        .requestMatchers("/api/discount/**").permitAll()
                        .requestMatchers("/api/transaction/**").permitAll()
                        .requestMatchers("/post/**").permitAll()
                        .requestMatchers("/reaction/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/forum-uploads/**").permitAll()
                        .requestMatchers("/predict").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()));

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:4200");
        configuration.addAllowedOrigin("http://172.20.10.2:4200");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}