# Netflix-Style Streaming Backend

Complete streaming backend with content catalog, recommendations, user profiles, watch history, search, and ratings.

## Architecture

```
                    ┌──────────────┐
                    │  API Gateway │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼─────┐ ┌───▼────┐ ┌────▼─────┐
        │  Content  │ │ User & │ │ Search   │
        │  Service  │ │ Profile│ │ Service  │
        └─────┬─────┘ └───┬────┘ └────┬─────┘
              │            │           │
        ┌─────▼────────────▼───────────▼─────┐
        │           PostgreSQL               │
        └────────────────────────────────────┘
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.netflix</groupId>
    <artifactId>netflix-backend</artifactId>
    <version>1.0.0</version>
    <name>netflix-backend</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml

```yaml
server:
  port: 8080
spring:
  application:
    name: netflix-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/netflix_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  cache:
    type: simple
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss

app:
  jwt:
    secret: ${JWT_SECRET:netflix-jwt-secret-change-in-production}
    expiration: 86400000
```

## NetflixApplication.java

```java
package com.netflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NetflixApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetflixApplication.class, args);
    }
}
```

---

## Content Catalog

### Content.java
```java
package com.netflix.content.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @ElementCollection
    @CollectionTable(name = "content_genres", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "genre")
    private List<String> genres = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "content_cast", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "cast_member")
    private List<String> cast = new ArrayList<>();

    @Column(name = "director")
    private String director;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "season_count")
    private Integer seasonCount;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "maturity_rating")
    private String maturityRating;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "backdrop_url")
    private String backdropUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "is_featured")
    private boolean featured;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getSeasonCount() { return seasonCount; }
    public void setSeasonCount(Integer seasonCount) { this.seasonCount = seasonCount; }
    public Integer getEpisodeCount() { return episodeCount; }
    public void setEpisodeCount(Integer episodeCount) { this.episodeCount = episodeCount; }
    public String getMaturityRating() { return maturityRating; }
    public void setMaturityRating(String maturityRating) { this.maturityRating = maturityRating; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### ContentRepository.java
```java
package com.netflix.content.repository;

import com.netflix.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByContentType(String contentType);
    List<Content> findByReleaseYear(Integer year);
    List<Content> findByFeaturedTrue();

    @Query("SELECT c FROM Content c WHERE :genre MEMBER OF c.genres")
    List<Content> findByGenre(@Param("genre") String genre);

    @Query("SELECT c FROM Content c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Content> search(@Param("query") String query);

    @Query("SELECT c FROM Content c ORDER BY c.averageRating DESC")
    List<Content> findTopRated();

    @Query("SELECT c FROM Content c WHERE c.releaseYear >= :year ORDER BY c.releaseYear DESC")
    List<Content> findRecent(@Param("year") Integer year);

    List<Content> findByMaturityRating(String maturityRating);
}
```

### ContentService.java
```java
package com.netflix.content.service;

import com.netflix.content.entity.Content;
import com.netflix.content.repository.ContentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Transactional
    public Content createContent(Content content) {
        return contentRepository.save(content);
    }

    @Cacheable(value = "content", key = "#id")
    @Transactional(readOnly = true)
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Content> getAllContent() {
        return contentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Content> getByType(String contentType) {
        return contentRepository.findByContentType(contentType);
    }

    @Transactional(readOnly = true)
    public List<Content> getByGenre(String genre) {
        return contentRepository.findByGenre(genre);
    }

    @Transactional(readOnly = true)
    public List<Content> getFeatured() {
        return contentRepository.findByFeaturedTrue();
    }

    @Transactional(readOnly = true)
    public List<Content> getTopRated() {
        return contentRepository.findTopRated();
    }

    @Transactional(readOnly = true)
    public List<Content> getRecentReleases() {
        return contentRepository.findRecent(2023);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public Content updateContent(Long id, Content details) {
        Content content = getContentById(id);
        content.setTitle(details.getTitle());
        content.setDescription(details.getDescription());
        content.setContentType(details.getContentType());
        content.setGenres(details.getGenres());
        content.setCast(details.getCast());
        content.setDirector(details.getDirector());
        content.setReleaseYear(details.getReleaseYear());
        content.setDurationMinutes(details.getDurationMinutes());
        content.setSeasonCount(details.getSeasonCount());
        content.setEpisodeCount(details.getEpisodeCount());
        content.setMaturityRating(details.getMaturityRating());
        content.setPosterUrl(details.getPosterUrl());
        content.setBackdropUrl(details.getBackdropUrl());
        content.setVideoUrl(details.getVideoUrl());
        content.setFeatured(details.isFeatured());
        return contentRepository.save(content);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public void deleteContent(Long id) {
        contentRepository.deleteById(id);
    }
}
```

### ContentController.java
```java
package com.netflix.content.controller;

import com.netflix.content.entity.Content;
import com.netflix.content.service.ContentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody Content content) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createContent(content));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Content> getContent(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContentById(id));
    }

    @GetMapping
    public ResponseEntity<List<Content>> getAllContent(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String genre) {
        if (type != null) return ResponseEntity.ok(contentService.getByType(type));
        if (genre != null) return ResponseEntity.ok(contentService.getByGenre(genre));
        return ResponseEntity.ok(contentService.getAllContent());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Content>> getFeatured() {
        return ResponseEntity.ok(contentService.getFeatured());
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Content>> getTopRated() {
        return ResponseEntity.ok(contentService.getTopRated());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Content>> getRecent() {
        return ResponseEntity.ok(contentService.getRecentReleases());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(@PathVariable Long id, @RequestBody Content content) {
        return ResponseEntity.ok(contentService.updateContent(id, content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## User Profiles

### Account.java
```java
package com.netflix.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column
    private String plan;

    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Profile> profiles = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Profile> getProfiles() { return profiles; }
    public void setProfiles(List<Profile> profiles) { this.profiles = profiles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### Profile.java
```java
package com.netflix.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String avatarUrl;

    @Column(name = "is_kid")
    private boolean kid;

    @Column(name = "language")
    private String language = "en";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public boolean isKid() { return kid; }
    public void setKid(boolean kid) { this.kid = kid; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### AccountRepository.java
```java
package com.netflix.user.repository;

import com.netflix.user.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### ProfileRepository.java
```java
package com.netflix.user.repository;

import com.netflix.user.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByAccountId(Long accountId);
}
```

### AccountService.java
```java
package com.netflix.user.service;

import com.netflix.user.entity.Account;
import com.netflix.user.entity.Profile;
import com.netflix.user.repository.AccountRepository;
import com.netflix.user.repository.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,
                          ProfileRepository profileRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Account createAccount(Account account) {
        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new RuntimeException("Email already registered: " + account.getEmail());
        }
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        if (account.getPlan() == null) account.setPlan("BASIC");
        Account saved = accountRepository.save(account);
        Profile defaultProfile = new Profile();
        defaultProfile.setName(saved.getFullName() != null ? saved.getFullName() : "Default");
        defaultProfile.setAccount(saved);
        profileRepository.save(defaultProfile);
        return saved;
    }

    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    @Transactional
    public Profile createProfile(Long accountId, Profile profile) {
        Account account = getAccountById(accountId);
        profile.setAccount(account);
        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<Profile> getProfiles(Long accountId) {
        return profileRepository.findByAccountId(accountId);
    }

    @Transactional
    public void deleteProfile(Long profileId) {
        profileRepository.deleteById(profileId);
    }
}
```

### AccountController.java
```java
package com.netflix.user.controller;

import com.netflix.user.entity.Account;
import com.netflix.user.entity.Profile;
import com.netflix.user.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Account> register(@RequestBody Account account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping("/{accountId}/profiles")
    public ResponseEntity<Profile> createProfile(
            @PathVariable Long accountId, @RequestBody Profile profile) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(accountService.createProfile(accountId, profile));
    }

    @GetMapping("/{accountId}/profiles")
    public ResponseEntity<List<Profile>> getProfiles(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getProfiles(accountId));
    }

    @DeleteMapping("/profiles/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long profileId) {
        accountService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Watch History & Continue Watching

### WatchHistory.java
```java
package com.netflix.watch.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history")
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "progress_seconds")
    private Integer progressSeconds = 0;

    @Column(name = "duration_seconds")
    private Integer durationSeconds = 0;

    @Column(name = "completed")
    private boolean completed = false;

    @Column(name = "watched_at", nullable = false)
    private LocalDateTime watchedAt;

    @PrePersist
    protected void onCreate() {
        if (watchedAt == null) watchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Integer getProgressSeconds() { return progressSeconds; }
    public void setProgressSeconds(Integer progressSeconds) { this.progressSeconds = progressSeconds; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getWatchedAt() { return watchedAt; }
    public void setWatchedAt(LocalDateTime watchedAt) { this.watchedAt = watchedAt; }
}
```

### WatchHistoryRepository.java
```java
package com.netflix.watch.repository;

import com.netflix.watch.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    List<WatchHistory> findByProfileIdOrderByWatchedAtDesc(Long profileId);

    @Query("SELECT w FROM WatchHistory w WHERE w.profileId = :profileId AND w.completed = false AND w.progressSeconds > 0 ORDER BY w.watchedAt DESC")
    List<WatchHistory> findContinueWatching(@Param("profileId") Long profileId);

    Optional<WatchHistory> findByProfileIdAndContentId(Long profileId, Long contentId);

    @Query("SELECT w.contentId, COUNT(w) as cnt FROM WatchHistory w GROUP BY w.contentId ORDER BY cnt DESC")
    List<Object[]> findMostWatchedContentIds();

    long countByProfileId(Long profileId);
}
```

### WatchHistoryService.java
```java
package com.netflix.watch.service;

import com.netflix.watch.entity.WatchHistory;
import com.netflix.watch.repository.WatchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;

    public WatchHistoryService(WatchHistoryRepository watchHistoryRepository) {
        this.watchHistoryRepository = watchHistoryRepository;
    }

    @Transactional
    public WatchHistory recordWatch(WatchHistory watch) {
        watch.setWatchedAt(LocalDateTime.now());
        return watchHistoryRepository.save(watch);
    }

    @Transactional
    public WatchHistory updateProgress(Long profileId, Long contentId, int progressSeconds, int durationSeconds) {
        WatchHistory watch = watchHistoryRepository
            .findByProfileIdAndContentId(profileId, contentId)
            .orElseGet(() -> {
                WatchHistory w = new WatchHistory();
                w.setProfileId(profileId);
                w.setContentId(contentId);
                return w;
            });
        watch.setProgressSeconds(progressSeconds);
        watch.setDurationSeconds(durationSeconds);
        watch.setCompleted(progressSeconds >= durationSeconds && durationSeconds > 0);
        watch.setWatchedAt(LocalDateTime.now());
        return watchHistoryRepository.save(watch);
    }

    @Transactional(readOnly = true)
    public List<WatchHistory> getHistory(Long profileId) {
        return watchHistoryRepository.findByProfileIdOrderByWatchedAtDesc(profileId);
    }

    @Transactional(readOnly = true)
    public List<WatchHistory> getContinueWatching(Long profileId) {
        return watchHistoryRepository.findContinueWatching(profileId);
    }

    @Transactional(readOnly = true)
    public long getWatchCount(Long profileId) {
        return watchHistoryRepository.countByProfileId(profileId);
    }
}
```

### WatchHistoryController.java
```java
package com.netflix.watch.controller;

import com.netflix.watch.entity.WatchHistory;
import com.netflix.watch.service.WatchHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watch")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    public WatchHistoryController(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }

    @PostMapping
    public ResponseEntity<WatchHistory> recordWatch(@RequestBody WatchHistory watch) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchHistoryService.recordWatch(watch));
    }

    @PutMapping("/progress")
    public ResponseEntity<WatchHistory> updateProgress(
            @RequestParam Long profileId,
            @RequestParam Long contentId,
            @RequestParam int progressSeconds,
            @RequestParam int durationSeconds) {
        return ResponseEntity.ok(watchHistoryService.updateProgress(
            profileId, contentId, progressSeconds, durationSeconds));
    }

    @GetMapping("/history/{profileId}")
    public ResponseEntity<List<WatchHistory>> getHistory(@PathVariable Long profileId) {
        return ResponseEntity.ok(watchHistoryService.getHistory(profileId));
    }

    @GetMapping("/continue/{profileId}")
    public ResponseEntity<List<WatchHistory>> getContinueWatching(@PathVariable Long profileId) {
        return ResponseEntity.ok(watchHistoryService.getContinueWatching(profileId));
    }
}
```

---

## Search Engine

### SearchIndex.java
```java
package com.netflix.search.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_index")
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column
    private String genres;

    @Column
    private String cast;

    @Column
    private String director;

    @Column
    private String contentType;

    @Column(name = "search_vector", columnDefinition = "TEXT")
    private String searchVector;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getSearchVector() { return searchVector; }
    public void setSearchVector(String searchVector) { this.searchVector = searchVector; }
}
```

### SearchIndexRepository.java
```java
package com.netflix.search.repository;

import com.netflix.search.entity.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {
    @Query(value = "SELECT * FROM search_index WHERE to_tsvector('english', title || ' ' || COALESCE(description, '') || ' ' || COALESCE(genres, '') || ' ' || COALESCE(cast, '') || ' ' || COALESCE(director, '')) @@ plainto_tsquery('english', :query) ORDER BY ts_rank(to_tsvector('english', title || ' ' || COALESCE(description, '') || ' ' || COALESCE(genres, '') || ' ' || COALESCE(cast, '') || ' ' || COALESCE(director, '')), plainto_tsquery('english', :query)) DESC", nativeQuery = true)
    List<SearchIndex> fullTextSearch(@Param("query") String query);

    @Query("SELECT s FROM SearchIndex s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.genres) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.cast) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SearchIndex> likeSearch(@Param("query") String query);

    void deleteByContentId(Long contentId);
}
```

### SearchService.java
```java
package com.netflix.search.service;

import com.netflix.content.entity.Content;
import com.netflix.content.repository.ContentRepository;
import com.netflix.search.entity.SearchIndex;
import com.netflix.search.repository.SearchIndexRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final ContentRepository contentRepository;

    public SearchService(SearchIndexRepository searchIndexRepository,
                         ContentRepository contentRepository) {
        this.searchIndexRepository = searchIndexRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public void indexContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));
        SearchIndex index = new SearchIndex();
        index.setContentId(content.getId());
        index.setTitle(content.getTitle());
        index.setDescription(content.getDescription());
        index.setGenres(String.join(", ", content.getGenres()));
        index.setCast(String.join(", ", content.getCast()));
        index.setDirector(content.getDirector());
        index.setContentType(content.getContentType());
        index.setSearchVector(buildSearchVector(content));
        searchIndexRepository.save(index);
    }

    private String buildSearchVector(Content content) {
        StringBuilder sb = new StringBuilder();
        sb.append(content.getTitle()).append(" ");
        sb.append(content.getDescription()).append(" ");
        sb.append(String.join(" ", content.getGenres())).append(" ");
        sb.append(String.join(" ", content.getCast())).append(" ");
        sb.append(content.getDirector()).append(" ");
        sb.append(content.getContentType());
        return sb.toString().toLowerCase();
    }

    @Transactional(readOnly = true)
    public List<Content> search(String query) {
        List<SearchIndex> results = searchIndexRepository.likeSearch(query);
        return results.stream()
            .map(idx -> contentRepository.findById(idx.getContentId()).orElse(null))
            .filter(c -> c != null)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchIndex> searchWithSnippets(String query) {
        return searchIndexRepository.likeSearch(query);
    }
}
```

### SearchController.java
```java
package com.netflix.search.controller;

import com.netflix.content.entity.Content;
import com.netflix.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<Content>> search(@RequestParam String q) {
        return ResponseEntity.ok(searchService.search(q));
    }

    @PostMapping("/index/{contentId}")
    public ResponseEntity<Void> indexContent(@PathVariable Long contentId) {
        searchService.indexContent(contentId);
        return ResponseEntity.ok().build();
    }
}
```

---

## Ratings & Reviews

### Rating.java
```java
package com.netflix.rating.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "content_id"})
})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String review;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### RatingRepository.java
```java
package com.netflix.rating.repository;

import com.netflix.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByContentId(Long contentId);
    List<Rating> findByProfileId(Long profileId);
    Optional<Rating> findByProfileIdAndContentId(Long profileId, Long contentId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.contentId = :contentId")
    Double getAverageRating(@Param("contentId") Long contentId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.contentId = :contentId")
    Integer getRatingCount(@Param("contentId") Long contentId);
}
```

### RatingService.java
```java
package com.netflix.rating.service;

import com.netflix.content.entity.Content;
import com.netflix.content.repository.ContentRepository;
import com.netflix.rating.entity.Rating;
import com.netflix.rating.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository;

    public RatingService(RatingRepository ratingRepository, ContentRepository contentRepository) {
        this.ratingRepository = ratingRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public Rating rateContent(Long profileId, Long contentId, Integer ratingValue, String review) {
        Rating rating = ratingRepository.findByProfileIdAndContentId(profileId, contentId)
            .orElse(new Rating());
        rating.setProfileId(profileId);
        rating.setContentId(contentId);
        rating.setRating(ratingValue);
        rating.setReview(review);
        Rating saved = ratingRepository.save(rating);

        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));
        Double avg = ratingRepository.getAverageRating(contentId);
        Integer count = ratingRepository.getRatingCount(contentId);
        content.setAverageRating(avg != null ? avg : 0.0);
        content.setRatingCount(count != null ? count : 0);
        contentRepository.save(content);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingsForContent(Long contentId) {
        return ratingRepository.findByContentId(contentId);
    }

    @Transactional(readOnly = true)
    public List<Rating> getRatingsByProfile(Long profileId) {
        return ratingRepository.findByProfileId(profileId);
    }

    @Transactional(readOnly = true)
    public Rating getUserRating(Long profileId, Long contentId) {
        return ratingRepository.findByProfileIdAndContentId(profileId, contentId)
            .orElse(null);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        ratingRepository.deleteById(ratingId);
    }
}
```

### RatingController.java
```java
package com.netflix.rating.controller;

import com.netflix.rating.entity.Rating;
import com.netflix.rating.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<Rating> rateContent(@RequestBody Map<String, Object> body) {
        Long profileId = Long.valueOf(body.get("profileId").toString());
        Long contentId = Long.valueOf(body.get("contentId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String review = (String) body.getOrDefault("review", null);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ratingService.rateContent(profileId, contentId, rating, review));
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<List<Rating>> getContentRatings(@PathVariable Long contentId) {
        return ResponseEntity.ok(ratingService.getRatingsForContent(contentId));
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<Rating>> getProfileRatings(@PathVariable Long profileId) {
        return ResponseEntity.ok(ratingService.getRatingsByProfile(profileId));
    }

    @GetMapping("/check")
    public ResponseEntity<Rating> getUserRating(
            @RequestParam Long profileId, @RequestParam Long contentId) {
        Rating r = ratingService.getUserRating(profileId, contentId);
        if (r == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Recommendation Engine

### RecommendationService.java
```java
package com.netflix.recommendation.service;

import com.netflix.content.entity.Content;
import com.netflix.content.repository.ContentRepository;
import com.netflix.rating.entity.Rating;
import com.netflix.rating.repository.RatingRepository;
import com.netflix.watch.entity.WatchHistory;
import com.netflix.watch.repository.WatchHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ContentRepository contentRepository;
    private final RatingRepository ratingRepository;
    private final WatchHistoryRepository watchHistoryRepository;

    public RecommendationService(ContentRepository contentRepository,
                                  RatingRepository ratingRepository,
                                  WatchHistoryRepository watchHistoryRepository) {
        this.contentRepository = contentRepository;
        this.ratingRepository = ratingRepository;
        this.watchHistoryRepository = watchHistoryRepository;
    }

    public List<Content> getRecommendationsForProfile(Long profileId, int limit) {
        List<Rating> myRatings = ratingRepository.findByProfileId(profileId);
        Set<Long> watchedContentIds = watchHistoryRepository
            .findByProfileIdOrderByWatchedAtDesc(profileId)
            .stream()
            .map(WatchHistory::getContentId)
            .collect(Collectors.toSet());

        Set<String> preferredGenres = new HashSet<>();
        Set<Long> highlyRatedContentIds = new HashSet<>();
        for (Rating r : myRatings) {
            if (r.getRating() >= 4) {
                highlyRatedContentIds.add(r.getContentId());
                contentRepository.findById(r.getContentId())
                    .ifPresent(c -> preferredGenres.addAll(c.getGenres()));
            }
        }

        List<Content> candidates = new ArrayList<>();
        if (!preferredGenres.isEmpty()) {
            for (String genre : preferredGenres) {
                candidates.addAll(contentRepository.findByGenre(genre));
            }
        } else {
            candidates.addAll(contentRepository.findAll());
        }

        candidates.removeIf(c -> watchedContentIds.contains(c.getId()));
        candidates.removeIf(c -> highlyRatedContentIds.contains(c.getId()));
        candidates = candidates.stream()
            .distinct()
            .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
            .limit(limit)
            .collect(Collectors.toList());

        if (candidates.size() < limit) {
            List<Content> topRated = contentRepository.findTopRated();
            for (Content c : topRated) {
                if (!watchedContentIds.contains(c.getId()) && !candidates.contains(c)) {
                    candidates.add(c);
                    if (candidates.size() >= limit) break;
                }
            }
        }

        return candidates;
    }

    public List<Content> getSimilarContent(Long contentId, int limit) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found: " + contentId));

        Set<String> genres = new HashSet<>(content.getGenres());
        List<Content> similar = new ArrayList<>();

        for (String genre : genres) {
            for (Content c : contentRepository.findByGenre(genre)) {
                if (!c.getId().equals(contentId) && !similar.contains(c)) {
                    similar.add(c);
                }
            }
        }

        return similar.stream()
            .sorted((a, b) -> {
                long commonGenresA = a.getGenres().stream().filter(genres::contains).count();
                long commonGenresB = b.getGenres().stream().filter(genres::contains).count();
                return Long.compare(commonGenresB, commonGenresA);
            })
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<Content> getTrendingNow() {
        List<Object[]> mostWatched = watchHistoryRepository.findMostWatchedContentIds();
        return mostWatched.stream()
            .limit(20)
            .map(obj -> contentRepository.findById((Long) obj[0]).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
```

### RecommendationController.java
```java
package com.netflix.recommendation.controller;

import com.netflix.content.entity.Content;
import com.netflix.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<Content>> getRecommendations(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
            recommendationService.getRecommendationsForProfile(profileId, limit));
    }

    @GetMapping("/similar/{contentId}")
    public ResponseEntity<List<Content>> getSimilar(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarContent(contentId, limit));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Content>> getTrending() {
        return ResponseEntity.ok(recommendationService.getTrendingNow());
    }
}
```

---

## Security Configuration

### SecurityConfig.java
```java
package com.netflix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/accounts/register").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Data Initialization

### DataInitializer.java
```java
package com.netflix.config;

import com.netflix.content.entity.Content;
import com.netflix.content.repository.ContentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ContentRepository contentRepository;

    public DataInitializer(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public void run(String... args) {
        if (contentRepository.count() > 0) return;

        Content movie1 = new Content();
        movie1.setTitle("The Last Horizon");
        movie1.setDescription("A group of astronauts embarks on a perilous journey to find a new habitable planet after Earth becomes uninhabitable.");
        movie1.setContentType("MOVIE");
        movie1.setGenres(List.of("Sci-Fi", "Adventure", "Drama"));
        movie1.setCast(List.of("John Smith", "Emma Watson", "Idris Elba"));
        movie1.setDirector("Christopher Nolan");
        movie1.setReleaseYear(2024);
        movie1.setDurationMinutes(148);
        movie1.setMaturityRating("PG-13");
        movie1.setFeatured(true);

        Content movie2 = new Content();
        movie2.setTitle("Midnight in Paris");
        movie2.setDescription("A nostalgic screenwriter finds himself mysteriously transported back to 1920s Paris each night at midnight.");
        movie2.setContentType("MOVIE");
        movie2.setGenres(List.of("Romance", "Comedy", "Fantasy"));
        movie2.setCast(List.of("Owen Wilson", "Rachel McAdams", "Marion Cotillard"));
        movie2.setDirector("Woody Allen");
        movie2.setReleaseYear(2023);
        movie2.setDurationMinutes(94);
        movie2.setMaturityRating("PG-13");
        movie2.setFeatured(true);

        Content series1 = new Content();
        series1.setTitle("Digital Empires");
        series1.setDescription("In a world where tech giants rule, a group of hackers fights to keep the internet free.");
        series1.setContentType("SERIES");
        series1.setGenres(List.of("Thriller", "Drama", "Tech"));
        series1.setCast(List.of("Zendaya", "Tom Holland", "Rami Malek"));
        series1.setDirector("David Fincher");
        series1.setReleaseYear(2024);
        series1.setSeasonCount(3);
        series1.setEpisodeCount(24);
        series1.setMaturityRating("TV-MA");
        series1.setFeatured(true);

        Content series2 = new Content();
        series2.setTitle("Wild Frontiers");
        series2.setDescription("A documentary series exploring the most remote and untouched wilderness areas on Earth.");
        series2.setContentType("SERIES");
        series2.setGenres(List.of("Documentary", "Nature", "Adventure"));
        series2.setCast(List.of("David Attenborough"));
        series2.setDirector("BBC Natural History");
        series2.setReleaseYear(2023);
        series2.setSeasonCount(2);
        series2.setEpisodeCount(16);
        series2.setMaturityRating("TV-PG");
        series2.setFeatured(false);

        contentRepository.saveAll(List.of(movie1, movie2, series1, series2));
    }
}
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/content` | List all content (filter by `type`, `genre`) |
| GET | `/api/content/{id}` | Get content details |
| GET | `/api/content/featured` | Get featured content |
| GET | `/api/content/top-rated` | Get top rated content |
| GET | `/api/content/recent` | Get recent releases |
| POST | `/api/content` | Create content |
| PUT | `/api/content/{id}` | Update content |
| DELETE | `/api/content/{id}` | Delete content |
| POST | `/api/accounts/register` | Register account |
| GET | `/api/accounts/{id}` | Get account |
| POST | `/api/accounts/{id}/profiles` | Create profile |
| GET | `/api/accounts/{id}/profiles` | List profiles |
| DELETE | `/api/accounts/profiles/{id}` | Delete profile |
| POST | `/api/watch` | Record watch event |
| PUT | `/api/watch/progress` | Update watch progress |
| GET | `/api/watch/history/{profileId}` | Get watch history |
| GET | `/api/watch/continue/{profileId}` | Get continue watching |
| GET | `/api/search?q=query` | Search content |
| POST | `/api/search/index/{contentId}` | Index content for search |
| POST | `/api/ratings` | Rate content |
| GET | `/api/ratings/content/{contentId}` | Get ratings for content |
| GET | `/api/ratings/profile/{profileId}` | Get ratings by profile |
| GET | `/api/recommendations/profile/{profileId}` | Get personalized recommendations |
| GET | `/api/recommendations/similar/{contentId}` | Get similar content |
| GET | `/api/recommendations/trending` | Get trending content |

## Running the Application

```bash
# Start PostgreSQL
docker run -d --name netflix-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=netflix_db \
  -p 5432:5432 \
  postgres:15

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Create an account
curl -X POST http://localhost:8080/api/accounts/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123","fullName":"John Doe","plan":"PREMIUM"}'

# Create a profile
curl -X POST http://localhost:8080/api/accounts/1/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"John","avatarUrl":"avatar1.png","kid":false}'

# Rate content
curl -X POST http://localhost:8080/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"profileId":1,"contentId":1,"rating":5,"review":"Amazing movie!"}'

# Search
curl "http://localhost:8080/api/search?q=horizon"

# Get recommendations
curl http://localhost:8080/api/recommendations/profile/1

# Get continue watching
curl http://localhost:8080/api/watch/continue/1
```
