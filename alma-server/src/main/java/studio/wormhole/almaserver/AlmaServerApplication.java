package studio.wormhole.almaserver;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import studio.wormhole.almaserver.filters.RequestLogFilter;

@SpringBootApplication
public class AlmaServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(AlmaServerApplication.class, args);
  }

  @Bean
  public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
  webServerFactoryCustomizer() {
    return factory -> factory.setContextPath("/v1");
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://app.alma.camp","http://10.31.20.200:8000","http://localhost:8000");
      }
    };
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer customJackson() {
    return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
  }

  @Bean
  public FilterRegistrationBean requestFilter() {
    RequestLogFilter loggingFilter = new RequestLogFilter();
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(loggingFilter);
    registration.addUrlPatterns("/*");
    registration.setName("logFilter");
    registration.setOrder(1);
    return registration;
  }

}
