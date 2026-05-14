# Project 4: Social Media Backend

**Concepts:** Follow/Unfollow, News Feed, File Upload, Notifications, JPA Specifications, Complex Queries

## Project Structure

```
src/main/java/com/example/social/
├── SocialApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── FileUploadConfig.java
├── controller/
│   ├── UserController.java
│   ├── PostController.java
│   ├── CommentController.java
│   ├── LikeController.java
│   ├── FollowController.java
│   ├── FeedController.java
│   ├── NotificationController.java
│   └── SearchController.java
├── service/
│   ├── UserService.java
│   ├── PostService.java
│   ├── CommentService.java
│   ├── LikeService.java
│   ├── FollowService.java
│   ├── FeedService.java
│   ├── NotificationService.java
│   ├── FileStorageService.java
│   └── SearchService.java
├── repository/
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── LikeRepository.java
│   ├── FollowRepository.java
│   └── NotificationRepository.java
├── model/
│   ├── User.java
│   ├── Post.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Follow.java
│   └── Notification.java
├── spec/
│   ├── PostSpecification.java
│   └── UserSpecification.java
├── dto/
│   ├── UserRequest.java
│   ├── UserResponse.java
│   ├── PostRequest.java
│   ├── PostResponse.java
│   ├── CommentRequest.java
│   ├── CommentResponse.java
│   └── NotificationResponse.java
├── exception/
│   └── GlobalExceptionHandler.java
└── upload/
    └── FileStorageProperties.java

src/main/resources/
├── application.yml
└── application-prod.yml
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>social-media</artifactId>
    <version>1.0.0</version>
    <name>social-media</name>
    <description>Social Media Backend</description>

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
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
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
spring:
  datasource:
    url: jdbc:h2:mem:socialdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

app:
  file:
    upload-dir: ./uploads
    allowed-types: image/jpeg,image/png,image/gif

server:
  port: 8080
```

## SocialApplication.java

```java
package com.example.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocialApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
    }
}
```

## model/User.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "cover_picture_url")
    private String coverPictureUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public User() {}

    public User(String username, String email, String password, String displayName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public String getCoverPictureUrl() { return coverPictureUrl; }
    public void setCoverPictureUrl(String coverPictureUrl) { this.coverPictureUrl = coverPictureUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/Post.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public Post() {}

    public Post(User user, String content) {
        this.user = user;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }
    public Set<Like> getLikes() { return likes; }
    public void setLikes(Set<Like> likes) { this.likes = likes; }
    public int getLikeCount() { return likes.size(); }
    public int getCommentCount() { return comments.size(); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/Comment.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Comment() {}

    public Comment(Post post, User user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## model/Like.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Like() {}

    public Like(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## model/Follow.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Follow() {}

    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getFollower() { return follower; }
    public void setFollower(User follower) { this.follower = follower; }
    public User getFollowing() { return following; }
    public void setFollowing(User following) { this.following = following; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## model/Notification.java

```java
package com.example.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Notification() {}

    public Notification(User recipient, User actor, NotificationType type,
                        Long referenceId, String referenceType, String message) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.message = message;
    }

    public enum NotificationType {
        LIKE, COMMENT, FOLLOW
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

## spec/UserSpecification.java

```java
package com.example.social.spec;

import com.example.social.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Predicate username = cb.like(cb.lower(root.get("username")), pattern);
            Predicate displayName = cb.like(cb.lower(root.get("displayName")), pattern);
            Predicate email = cb.like(cb.lower(root.get("email")), pattern);
            Predicate bio = cb.like(cb.lower(root.get("bio")), pattern);
            return cb.or(username, displayName, email, bio);
        };
    }
}
```

## spec/PostSpecification.java

```java
package com.example.social.spec;

import com.example.social.model.Post;
import com.example.social.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Set;

public class PostSpecification {

    public static Specification<Post> byUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Post> byUsers(Set<User> users) {
        return (root, query, cb) -> root.get("user").in(users);
    }

    public static Specification<Post> byContentKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("content")), pattern);
        };
    }

    public static Specification<Post> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThan(root.get("createdAt"), date);
    }

    public static Specification<Post> withComments() {
        return (root, query, cb) -> cb.isNotEmpty(root.get("comments"));
    }

    public static Specification<Post> orderByNewest() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.conjunction();
        };
    }
}
```

## repository/UserRepository.java

```java
package com.example.social.repository;

import com.example.social.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    Set<User> findFollowing(@Param("userId") Long userId);

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    Set<User> findFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countFollowers(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowing(@Param("userId") Long userId);
}
```

## repository/PostRepository.java

```java
package com.example.social.repository;

import com.example.social.model.Post;
import com.example.social.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user IN :users ORDER BY p.createdAt DESC")
    List<Post> findByUsersOrderByCreatedAtDesc(@Param("users") Set<User> users, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
```

## repository/CommentRepository.java

```java
package com.example.social.repository;

import com.example.social.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    long countByPostId(Long postId);
}
```

## repository/LikeRepository.java

```java
package com.example.social.repository;

import com.example.social.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
```

## repository/FollowRepository.java

```java
package com.example.social.repository;

import com.example.social.model.Follow;
import com.example.social.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
    long countByFollowing(User user);
    long countByFollower(User user);
    void deleteByFollowerAndFollowing(User follower, User following);
}
```

## repository/NotificationRepository.java

```java
package com.example.social.repository;

import com.example.social.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);
}
```

## dto/UserRequest.java

```java
package com.example.social.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    String displayName,
    String bio
) {}
```

## dto/UserResponse.java

```java
package com.example.social.dto;

import com.example.social.model.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id, String username, String email, String displayName,
    String bio, String profilePictureUrl, String coverPictureUrl,
    long followerCount, long followingCount, long postCount,
    boolean isFollowedByCurrentUser,
    LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user, long followerCount,
                                          long followingCount, long postCount,
                                          boolean isFollowed) {
        return new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(),
            user.getDisplayName(), user.getBio(),
            user.getProfilePictureUrl(), user.getCoverPictureUrl(),
            followerCount, followingCount, postCount, isFollowed,
            user.getCreatedAt()
        );
    }
}
```

## dto/PostRequest.java

```java
package com.example.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
    @NotBlank @Size(max = 5000) String content,
    String imageUrl
) {}
```

## dto/PostResponse.java

```java
package com.example.social.dto;

import com.example.social.model.Post;
import java.time.LocalDateTime;

public record PostResponse(
    Long id, Long userId, String username, String displayName,
    String userProfilePictureUrl,
    String content, String imageUrl,
    int likeCount, int commentCount,
    boolean isLikedByCurrentUser,
    LocalDateTime createdAt
) {
    public static PostResponse fromEntity(Post post, boolean isLiked) {
        return new PostResponse(
            post.getId(), post.getUser().getId(),
            post.getUser().getUsername(),
            post.getUser().getDisplayName(),
            post.getUser().getProfilePictureUrl(),
            post.getContent(), post.getImageUrl(),
            post.getLikeCount(), post.getCommentCount(),
            isLiked, post.getCreatedAt()
        );
    }
}
```

## dto/CommentRequest.java

```java
package com.example.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotBlank @Size(max = 2000) String content
) {}
```

## dto/CommentResponse.java

```java
package com.example.social.dto;

import com.example.social.model.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id, Long postId, Long userId, String username,
    String displayName, String userProfilePictureUrl,
    String content, LocalDateTime createdAt
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
            comment.getId(), comment.getPost().getId(),
            comment.getUser().getId(),
            comment.getUser().getUsername(),
            comment.getUser().getDisplayName(),
            comment.getUser().getProfilePictureUrl(),
            comment.getContent(), comment.getCreatedAt()
        );
    }
}
```

## dto/NotificationResponse.java

```java
package com.example.social.dto;

import com.example.social.model.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id, Long actorId, String actorUsername,
    String actorDisplayName, String actorProfilePictureUrl,
    String type, Long referenceId, String referenceType,
    String message, boolean isRead, LocalDateTime createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
            notification.getId(), notification.getActor().getId(),
            notification.getActor().getUsername(),
            notification.getActor().getDisplayName(),
            notification.getActor().getProfilePictureUrl(),
            notification.getType().name(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.getMessage(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
```

## upload/FileStorageProperties.java

```java
package com.example.social.upload;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {
    private String uploadDir;
    private String allowedTypes;

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    public String getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(String allowedTypes) { this.allowedTypes = allowedTypes; }
}
```

## service/FileStorageService.java

```java
package com.example.social.service;

import com.example.social.upload.FileStorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final Set<String> allowedTypes;

    public FileStorageService(FileStorageProperties properties) {
        this.uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        this.allowedTypes = new HashSet<>(Arrays.asList(
            properties.getAllowedTypes().split(",")));
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + extension;

        try {
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + filename, e);
        }
    }
}
```

## service/UserService.java

```java
package com.example.social.service;

import com.example.social.dto.UserRequest;
import com.example.social.dto.UserResponse;
import com.example.social.model.User;
import com.example.social.repository.FollowRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import com.example.social.spec.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, FollowRepository followRepository,
                       PostRepository postRepository, PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email in use");
        }

        User user = new User(request.username(), request.email(),
            passwordEncoder.encode(request.password()),
            request.displayName() != null ? request.displayName() : request.username());
        user.setBio(request.bio());
        User saved = userRepository.save(user);

        return UserResponse.fromEntity(saved, 0, 0, 0, false);
    }

    public UserResponse getUser(Long id, Long currentUserId) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        long followers = followRepository.countByFollowing(user);
        long following = followRepository.countByFollower(user);
        long posts = postRepository.countByUserId(id);
        boolean isFollowed = currentUserId != null &&
            followRepository.existsByFollowerAndFollowing(
                userRepository.getReferenceById(currentUserId), user);
        return UserResponse.fromEntity(user, followers, following, posts, isFollowed);
    }

    public UserResponse updateProfile(Long id, String displayName, String bio) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (displayName != null) user.setDisplayName(displayName);
        if (bio != null) user.setBio(bio);
        User saved = userRepository.save(user);
        return getUser(saved.getId(), id);
    }

    public String uploadProfilePicture(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String url = fileStorageService.storeFile(file);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return url;
    }

    public String uploadCoverPicture(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String url = fileStorageService.storeFile(file);
        user.setCoverPictureUrl(url);
        userRepository.save(user);
        return url;
    }

    public Page<UserResponse> searchUsers(String keyword, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(UserSpecification.searchByKeyword(keyword), pageable)
            .map(user -> {
                long followers = followRepository.countByFollowing(user);
                long following = followRepository.countByFollower(user);
                long posts = postRepository.countByUserId(user.getId());
                boolean isFollowed = currentUserId != null &&
                    followRepository.existsByFollowerAndFollowing(
                        userRepository.getReferenceById(currentUserId), user);
                return UserResponse.fromEntity(user, followers, following, posts, isFollowed);
            });
    }
}
```

## service/PostService.java

```java
package com.example.social.service;

import com.example.social.dto.PostRequest;
import com.example.social.dto.PostResponse;
import com.example.social.model.Post;
import com.example.social.model.User;
import com.example.social.repository.LikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import com.example.social.spec.PostSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       LikeRepository likeRepository, FileStorageService fileStorageService,
                       NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    public PostResponse createPost(Long userId, PostRequest request, MultipartFile image) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Post post = new Post(user, request.content());
        if (image != null && !image.isEmpty()) {
            post.setImageUrl(fileStorageService.storeFile(image));
        } else if (request.imageUrl() != null) {
            post.setImageUrl(request.imageUrl());
        }

        Post saved = postRepository.save(post);
        return PostResponse.fromEntity(saved, false);
    }

    public PostResponse getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        boolean isLiked = currentUserId != null &&
            likeRepository.existsByPostIdAndUserId(postId, currentUserId);
        return PostResponse.fromEntity(post, isLiked);
    }

    public Page<PostResponse> getUserPosts(Long userId, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.getReferenceById(userId);
        return postRepository.findByUserOrderByCreatedAtDesc(user, pageable)
            .map(post -> PostResponse.fromEntity(post,
                currentUserId != null &&
                likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId)));
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete another user's post");
        }
        postRepository.delete(post);
    }

    public Page<PostResponse> searchPosts(String keyword, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Post> spec = Specification
            .where(PostSpecification.byContentKeyword(keyword));
        return postRepository.findAll(spec, pageable)
            .map(post -> PostResponse.fromEntity(post,
                currentUserId != null &&
                likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId)));
    }
}
```

## service/CommentService.java

```java
package com.example.social.service;

import com.example.social.dto.CommentRequest;
import com.example.social.dto.CommentResponse;
import com.example.social.model.Comment;
import com.example.social.model.Notification;
import com.example.social.model.Post;
import com.example.social.model.User;
import com.example.social.repository.CommentRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                          UserRepository userRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = new Comment(post, user, request.content());
        Comment saved = commentRepository.save(comment);

        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                post.getUser(), user, Notification.NotificationType.COMMENT,
                post.getId(), "Post",
                user.getUsername() + " commented on your post"
            );
        }

        return CommentResponse.fromEntity(saved);
    }

    public Page<CommentResponse> getPostComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
            .map(CommentResponse::fromEntity);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete another user's comment");
        }
        commentRepository.delete(comment);
    }
}
```

## service/LikeService.java

```java
package com.example.social.service;

import com.example.social.model.Like;
import com.example.social.model.Notification;
import com.example.social.model.Post;
import com.example.social.model.User;
import com.example.social.repository.LikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository,
                       UserRepository userRepository, NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public boolean toggleLike(Long postId, Long userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.deleteByPostIdAndUserId(postId, userId);
            return false;
        }

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        likeRepository.save(new Like(post, user));

        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                post.getUser(), user, Notification.NotificationType.LIKE,
                post.getId(), "Post",
                user.getUsername() + " liked your post"
            );
        }

        return true;
    }

    public long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
```

## service/FollowService.java

```java
package com.example.social.service;

import com.example.social.model.Follow;
import com.example.social.model.Notification;
import com.example.social.model.User;
import com.example.social.repository.FollowRepository;
import com.example.social.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FollowService(FollowRepository followRepository, UserRepository userRepository,
                         NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public boolean toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(
                userRepository.getReferenceById(followerId),
                userRepository.getReferenceById(followingId))) {
            followRepository.deleteByFollowerAndFollowing(
                userRepository.getReferenceById(followerId),
                userRepository.getReferenceById(followingId));
            return false;
        }

        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        followRepository.save(new Follow(follower, following));

        notificationService.createNotification(
            following, follower, Notification.NotificationType.FOLLOW,
            follower.getId(), "User",
            follower.getUsername() + " started following you"
        );

        return true;
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerAndFollowing(
            userRepository.getReferenceById(followerId),
            userRepository.getReferenceById(followingId));
    }
}
```

## service/FeedService.java

```java
package com.example.social.service;

import com.example.social.dto.PostResponse;
import com.example.social.model.User;
import com.example.social.repository.LikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository,
                       LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    public Page<PostResponse> getFeed(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Set<User> following = userRepository.findFollowing(userId);
        following.add(user);

        Pageable pageable = PageRequest.of(page, size);
        var posts = postRepository.findByUsersOrderByCreatedAtDesc(following, pageable);

        return posts.stream()
            .map(post -> PostResponse.fromEntity(post,
                likeRepository.existsByPostIdAndUserId(post.getId(), userId)))
            .collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toList(),
                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, posts.size())));
    }
}
```

## service/NotificationService.java

```java
package com.example.social.service;

import com.example.social.dto.NotificationResponse;
import com.example.social.model.Notification;
import com.example.social.model.User;
import com.example.social.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User recipient, User actor, Notification.NotificationType type,
                                    Long referenceId, String referenceType, String message) {
        if (recipient.getId().equals(actor.getId())) return;
        Notification notification = new Notification(recipient, actor, type,
            referenceId, referenceType, message);
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
            .map(NotificationResponse::fromEntity);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
```

## service/SearchService.java

```java
package com.example.social.service;

import com.example.social.dto.PostResponse;
import com.example.social.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final UserService userService;
    private final PostService postService;

    public SearchService(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    public Page<UserResponse> searchUsers(String query, int page, int size, Long currentUserId) {
        return userService.searchUsers(query, page, size, currentUserId);
    }

    public Page<PostResponse> searchPosts(String query, Long currentUserId, int page, int size) {
        return postService.searchPosts(query, currentUserId, page, size);
    }
}
```

## controller/UserController.java

```java
package com.example.social.controller;

import com.example.social.dto.UserRequest;
import com.example.social.dto.UserResponse;
import com.example.social.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id,
                                                @RequestParam(required = false) Long currentUserId) {
        return ResponseEntity.ok(userService.getUser(id, currentUserId));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String bio) {
        return ResponseEntity.ok(userService.updateProfile(id, displayName, bio));
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfilePicture(id, file));
    }

    @PostMapping("/{id}/cover-picture")
    public ResponseEntity<String> uploadCoverPicture(
            @PathVariable Long id,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(userService.uploadCoverPicture(id, file));
    }
}
```

## controller/PostController.java

```java
package com.example.social.controller;

import com.example.social.dto.PostRequest;
import com.example.social.dto.PostResponse;
import com.example.social.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) { this.postService = postService; }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestParam Long userId,
            @Valid @RequestPart("request") PostRequest request,
            @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(postService.createPost(userId, request, image));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId) {
        return ResponseEntity.ok(postService.getPost(postId, currentUserId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getUserPosts(userId, currentUserId, page, size));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @RequestParam Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/CommentController.java

```java
package com.example.social.controller;

import com.example.social.dto.CommentRequest;
import com.example.social.dto.CommentResponse;
import com.example.social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) { this.commentService = commentService; }

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.createComment(postId, userId, request));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentResponse>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getPostComments(postId, page, size));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/LikeController.java

```java
package com.example.social.controller;

import com.example.social.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) { this.likeService = likeService; }

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        boolean liked = likeService.toggleLike(postId, userId);
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(Map.of("liked", liked, "count", count));
    }
}
```

## controller/FollowController.java

```java
package com.example.social.controller;

import com.example.social.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) { this.followService = followService; }

    @PostMapping("/{followingId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @PathVariable Long followingId,
            @RequestParam Long followerId) {
        boolean following = followService.toggleFollow(followerId, followingId);
        return ResponseEntity.ok(Map.of("following", following));
    }

    @GetMapping("/{followerId}/is-following/{followingId}")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        return ResponseEntity.ok(Map.of("following",
            followService.isFollowing(followerId, followingId)));
    }
}
```

## controller/FeedController.java

```java
package com.example.social.controller;

import com.example.social.dto.PostResponse;
import com.example.social.service.FeedService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) { this.feedService = feedService; }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }
}
```

## controller/NotificationController.java

```java
package com.example.social.controller;

import com.example.social.dto.NotificationResponse;
import com.example.social.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, size));
    }

    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PostMapping("/{userId}/mark-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/SearchController.java

```java
package com.example.social.controller;

import com.example.social.dto.PostResponse;
import com.example.social.dto.UserResponse;
import com.example.social.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) { this.searchService = searchService; }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam String q,
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.searchUsers(q, page, size, currentUserId));
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String q,
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.searchPosts(q, currentUserId, page, size));
    }
}
```

## config/SecurityConfig.java

```java
package com.example.social.config;

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
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .anyRequest().denyAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## config/FileUploadConfig.java

```java
package com.example.social.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
```

## exception/GlobalExceptionHandler.java

```java
package com.example.social.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/users/register` | Register user |
| GET | `/api/users/{id}` | Get user profile |
| PUT | `/api/users/{id}/profile` | Update profile |
| POST | `/api/users/{id}/profile-picture` | Upload profile pic |
| POST | `/api/users/{id}/cover-picture` | Upload cover pic |
| POST | `/api/posts` | Create post |
| GET | `/api/posts/{postId}` | Get post |
| GET | `/api/posts/user/{userId}` | Get user's posts |
| DELETE | `/api/posts/{postId}` | Delete post |
| POST | `/api/comments/post/{postId}` | Add comment |
| GET | `/api/comments/post/{postId}` | Get post comments |
| DELETE | `/api/comments/{commentId}` | Delete comment |
| POST | `/api/likes/{postId}/toggle` | Toggle like |
| POST | `/api/follows/{followingId}/toggle` | Toggle follow |
| GET | `/api/follows/{followerId}/is-following/{followingId}` | Check follow |
| GET | `/api/feed/{userId}` | Get news feed |
| GET | `/api/notifications/{userId}` | Get notifications |
| GET | `/api/notifications/{userId}/unread-count` | Unread count |
| POST | `/api/notifications/{userId}/mark-read` | Mark all read |
| GET | `/api/search/users?q=` | Search users |
| GET | `/api/search/posts?q=` | Search posts |
