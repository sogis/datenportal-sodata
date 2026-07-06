package ch.so.agi.datenportal.config;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class StaticAssetCachingConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/vendor/so-web-components/**")
                .addResourceLocations("classpath:/static/vendor/so-web-components/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
        registry.addResourceHandler("/vendor/jetbrains-mono/**")
                .addResourceLocations("classpath:/static/vendor/jetbrains-mono/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
        registry.addResourceHandler("/explore/**")
                .addResourceLocations("classpath:/static/explore/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/explore-extensions/**")
                .addResourceLocations("classpath:/static/explore-extensions/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/webr/**")
                .addResourceLocations("classpath:/static/webr/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/webr-packages/**")
                .addResourceLocations("classpath:/static/webr-packages/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }
}
