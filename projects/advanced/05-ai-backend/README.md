# AI-Powered Backend

Complete Spring AI backend with chat completion, RAG (Retrieval Augmented Generation), document ingestion, image generation, semantic search, prompt templating, and structured output parsing.

## Architecture

```
                     ┌──────────────────┐
                     │   REST API       │
                     └───────┬──────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────▼─────┐ ┌──────▼──────┐ ┌────▼──────┐
        │   Chat    │ │   RAG /     │ │  Image    │
        │   Service │ │  Document   │ │  Service  │
        └─────┬─────┘ └──────┬──────┘ └────┬──────┘
              │              │              │
        ┌─────▼──────────────▼──────────────▼─────┐
        │   Vector Store (PGVector / In-Memory)   │
        └─────────────────────────────────────────┘
              │
        ┌─────▼─────┐
        │ Document  │
        │ Ingestion │
        └───────────┘
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
    <groupId>com.ai</groupId>
    <artifactId>ai-backend</artifactId>
    <version>1.0.0</version>
    <name>ai-backend</name>
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M2</spring-ai.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-pdf-document-reader</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-tika-document-reader</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
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
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
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
    name: ai-backend
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-placeholder}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
          max-tokens: 2000
      embedding:
        options:
          model: text-embedding-3-small
    vectorstore:
      pgvector:
        initialize-schema: true
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
  jackson:
    serialization:
      write-dates-as-timestamps: false

app:
  document:
    upload-dir: ./uploads
  image:
    size: 1024x1024
    quality: standard
```

## AiBackendApplication.java

```java
package com.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiBackendApplication.class, args);
    }
}
```

---

## Chat Completion Service

### ChatService.java
```java
package com.ai.service;

import com.ai.dto.ChatRequest;
import com.ai.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse as SpringChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatResponse chat(ChatRequest request) {
        Prompt prompt = new Prompt(new UserMessage(request.getMessage()));
        SpringChatResponse response = chatClient.call(prompt);
        String content = response.getResult().getOutput().getContent();

        return new ChatResponse(content, response.getResult().getMetadata().getId(),
            response.getResult().getMetadata().getModel());
    }

    public Flux<String> streamChat(ChatRequest request) {
        Prompt prompt = new Prompt(new UserMessage(request.getMessage()));
        return chatClient.stream(prompt)
            .map(resp -> resp.getResult().getOutput().getContent());
    }

    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        org.springframework.ai.chat.messages.SystemMessage sm =
            new org.springframework.ai.chat.messages.SystemMessage(systemPrompt);
        UserMessage um = new UserMessage(userMessage);
        Prompt prompt = new Prompt(java.util.List.of(sm, um));
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
```

### ChatController.java
```java
package com.ai.controller;

import com.ai.dto.ChatRequest;
import com.ai.dto.ChatResponse;
import com.ai.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }
}
```

### ChatRequest.java
```java
package com.ai.dto;

public class ChatRequest {
    private String message;
    private String conversationId;
    private Double temperature;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
```

### ChatResponse.java
```java
package com.ai.dto;

public class ChatResponse {
    private String content;
    private String messageId;
    private String model;

    public ChatResponse() {}

    public ChatResponse(String content, String messageId, String model) {
        this.content = content;
        this.messageId = messageId;
        this.model = model;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
```

---

## Document Ingestion

### DocumentEntity.java
```java
package com.ai.document.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column
    private String contentType;

    @Column(length = 10000)
    private String content;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "chunk_count")
    private int chunkCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### DocumentRepository.java
```java
package com.ai.document.repository;

import com.ai.document.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}
```

### DocumentIngestionService.java
```java
package com.ai.service;

import com.ai.document.entity.DocumentEntity;
import com.ai.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    @Value("${app.document.upload-dir}")
    private String uploadDir;

    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentIngestionService(DocumentRepository documentRepository,
                                     VectorStore vectorStore,
                                     TokenTextSplitter textSplitter) {
        this.documentRepository = documentRepository;
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    @Transactional
    public DocumentEntity ingestDocument(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(file.getOriginalFilename());
        file.transferTo(filePath.toFile());

        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(filePath.toFile()));
        List<Document> documents = reader.read();

        StringBuilder fullContent = new StringBuilder();
        for (Document doc : documents) {
            fullContent.append(doc.getContent()).append("\n");
        }

        String contentHash = computeHash(fullContent.toString());

        DocumentEntity docEntity = new DocumentEntity();
        docEntity.setFilename(file.getOriginalFilename());
        docEntity.setContentType(file.getContentType());
        docEntity.setContent(fullContent.toString());
        docEntity.setContentHash(contentHash);
        docEntity.setChunkCount(0);

        List<Document> chunks = textSplitter.apply(documents);
        docEntity.setChunkCount(chunks.size());

        vectorStore.accept(chunks);

        DocumentEntity saved = documentRepository.save(docEntity);
        log.info("Ingested document: {} with {} chunks", file.getOriginalFilename(), chunks.size());

        Files.deleteIfExists(filePath);
        return saved;
    }

    public String extractTextFromFile(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempFile.toFile()));
        List<Document> documents = reader.read();

        Files.deleteIfExists(tempFile);

        StringBuilder text = new StringBuilder();
        for (Document doc : documents) {
            text.append(doc.getContent()).append("\n");
        }
        return text.toString();
    }

    private String computeHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }
}
```

### DocumentIngestionController.java
```java
package com.ai.controller;

import com.ai.document.entity.DocumentEntity;
import com.ai.service.DocumentIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/documents")
public class DocumentIngestionController {

    private final DocumentIngestionService documentService;

    public DocumentIngestionController(DocumentIngestionService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentEntity> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.ingestDocument(file));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String text = documentService.extractTextFromFile(file);
            return ResponseEntity.ok(Map.of("text", text));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

---

## RAG (Retrieval Augmented Generation)

### RagService.java
```java
package com.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final String SYSTEM_TEMPLATE = """
        You are a helpful AI assistant. Answer the user's question based on the provided context.
        If the context doesn't contain enough information to answer, say so.
        Always cite your sources from the context when possible.
        
        Context:
        {context}
        
        Question: {question}
        
        Answer the question based on the context above. Be thorough and accurate.
        """;

    public RagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public String ask(String question) {
        List<Document> similarDocs = vectorStore.similaritySearch(question, 5);
        String context = similarDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            context = "No relevant context found.";
        }

        String systemPrompt = SYSTEM_TEMPLATE
            .replace("{context}", context)
            .replace("{question}", question);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        log.info("RAG query with {} relevant documents", similarDocs.size());
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

    public String askWithSources(String question, int topK) {
        List<Document> similarDocs = vectorStore.similaritySearch(question, topK);
        String context = similarDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n---\n\n"));

        SystemMessage systemMessage = new SystemMessage(SYSTEM_TEMPLATE
            .replace("{context}", context)
            .replace("{question}", question));
        UserMessage userMessage = new UserMessage(question);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        String answer = chatClient.call(prompt).getResult().getOutput().getContent();
        return answer + "\n\n---\n**Sources (" + similarDocs.size() + " documents retrieved)**";
    }
}
```

### RagController.java
```java
package com.ai.controller;

import com.ai.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }
        String answer = ragService.ask(question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @PostMapping("/ask-with-sources")
    public ResponseEntity<Map<String, String>> askWithSources(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        int topK = request.containsKey("topK") ? (int) request.get("topK") : 5;
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }
        String answer = ragService.askWithSources(question, topK);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
```

---

## Semantic Search

### SemanticSearchService.java
```java
package com.ai.service;

import com.ai.document.entity.DocumentEntity;
import com.ai.document.repository.DocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    public SemanticSearchService(VectorStore vectorStore, DocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    public List<Map<String, Object>> search(String query, int topK) {
        List<Document> results = vectorStore.similaritySearch(query, topK);
        return results.stream().map(doc -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", doc.getContent());
            result.put("score", doc.getMetadata().getOrDefault("distance", 0.0));
            result.put("source", doc.getMetadata().getOrDefault("source", "unknown"));
            return result;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> searchWithContext(String query, int topK) {
        List<Map<String, Object>> results = search(query, topK);
        String context = results.stream()
            .map(r -> (String) r.get("content"))
            .collect(Collectors.joining("\n\n---\n\n"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("resultCount", results.size());
        response.put("results", results);
        response.put("combinedContext", context);
        return response;
    }
}
```

### SemanticSearchController.java
```java
package com.ai.controller;

import com.ai.service.SemanticSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/search")
public class SemanticSearchController {

    private final SemanticSearchService searchService;

    public SemanticSearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int topK) {
        return ResponseEntity.ok(searchService.search(q, topK));
    }

    @GetMapping("/with-context")
    public ResponseEntity<Map<String, Object>> searchWithContext(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int topK) {
        return ResponseEntity.ok(searchService.searchWithContext(q, topK));
    }
}
```

---

## Image Generation

### ImageService.java
```java
package com.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImageClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    private final ImageClient imageClient;

    public ImageService(ImageClient imageClient) {
        this.imageClient = imageClient;
    }

    public String generateImage(String prompt, String size, String quality) {
        OpenAiImageOptions options = OpenAiImageOptions.builder()
            .withModel("dall-e-3")
            .withQuality(quality != null ? quality : "standard")
            .withHeight(parseSize(size)[1])
            .withWidth(parseSize(size)[0])
            .build();

        ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
        ImageResponse response = imageClient.call(imagePrompt);

        String url = response.getResult().getOutput().getUrl();
        log.info("Generated image for prompt: {}", prompt);
        return url;
    }

    public String generateVariation(String imageUrl) {
        return null;
    }

    private int[] parseSize(String size) {
        if (size == null) return new int[]{1024, 1024};
        String[] parts = size.split("x");
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            return new int[]{1024, 1024};
        }
    }
}
```

### ImageController.java
```java
package com.ai.controller;

import com.ai.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        String size = request.getOrDefault("size", "1024x1024");
        String quality = request.getOrDefault("quality", "standard");
        String url = imageService.generateImage(prompt, size, quality);
        return ResponseEntity.ok(Map.of("url", url, "prompt", prompt));
    }
}
```

---

## Prompt Templates

### PromptTemplateService.java
```java
package com.ai.service;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PromptTemplateService {

    private final ChatClient chatClient;

    private static final Map<String, String> TEMPLATES = Map.of(
        "summarize", """
            You are a professional summarizer. Summarize the following text in a clear and concise manner.
            Keep the summary to 2-3 paragraphs while preserving all key points.
            
            Text to summarize:
            {text}
            """,

        "translate", """
            You are a professional translator. Translate the following text from {source_language} to {target_language}.
            Maintain the original tone and style of the text.
            
            Text:
            {text}
            """,

        "analyze-sentiment", """
            Analyze the sentiment of the following text. Classify it as POSITIVE, NEGATIVE, or NEUTRAL.
            Provide a confidence score (0-1) and a brief explanation.
            
            Text:
            {text}
            
            Respond in JSON format with keys: sentiment, confidence, explanation
            """,

        "extract-entities", """
            Extract all named entities (people, organizations, locations, dates, etc.) from the following text.
            Categorize each entity by type.
            
            Text:
            {text}
            
            Respond in JSON format with entity categories as arrays.
            """,

        "code-review", """
            You are an expert code reviewer. Review the following code for:
            1. Bugs and potential issues
            2. Security vulnerabilities
            3. Performance improvements
            4. Code style and best practices
            5. Suggested fixes
            
            Code to review:
            ```{language}
            {code}
            ```
            
            Provide a detailed code review with specific recommendations.
            """
    );

    public PromptTemplateService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String executeTemplate(String templateName, Map<String, String> variables) {
        String template = TEMPLATES.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Unknown template: " + templateName);
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return chatClient.call(new Prompt(new UserMessage(template)))
            .getResult().getOutput().getContent();
    }

    public String chainTemplates(List<String> templateNames, Map<String, String> initialVariables) {
        String result = null;
        Map<String, String> variables = new java.util.HashMap<>(initialVariables);

        for (String templateName : templateNames) {
            String template = TEMPLATES.get(templateName);
            if (template == null) continue;

            String filled = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                filled = filled.replace("{" + entry.getKey() + "}", entry.getValue());
            }

            result = chatClient.call(new Prompt(new UserMessage(filled)))
                .getResult().getOutput().getContent();
            variables.put("result", result);
        }
        return result;
    }
}
```

### PromptController.java
```java
package com.ai.controller;

import com.ai.service.PromptTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/prompts")
public class PromptController {

    private final PromptTemplateService promptService;

    public PromptController(PromptTemplateService promptService) {
        this.promptService = promptService;
    }

    @PostMapping("/execute/{templateName}")
    public ResponseEntity<Map<String, String>> executeTemplate(
            @PathVariable String templateName,
            @RequestBody Map<String, String> variables) {
        String result = promptService.executeTemplate(templateName, variables);
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/chain")
    public ResponseEntity<Map<String, String>> chainTemplates(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> templates = (List<String>) request.get("templates");
        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request.get("variables");
        String result = promptService.chainTemplates(templates, variables);
        return ResponseEntity.ok(Map.of("result", result));
    }
}
```

---

## Output Parsing (Structured AI Responses)

### OutputParserService.java
```java
package com.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutputParserService {

    private static final Logger log = LoggerFactory.getLogger(OutputParserService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OutputParserService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public SentimentResult analyzeSentiment(String text) {
        String prompt = """
            Analyze the sentiment of the following text and return ONLY valid JSON.
            Do not include any other text or markdown formatting.
            
            Text: %s
            
            Respond with JSON in format: {"sentiment": "POSITIVE|NEGATIVE|NEUTRAL", "confidence": 0.0-1.0, "explanation": "..."}
            """.formatted(text);

        String response = chatClient.call(new Prompt(new UserMessage(prompt)))
            .getResult().getOutput().getContent();

        try {
            JsonNode json = objectMapper.readTree(response);
            SentimentResult result = new SentimentResult();
            result.setSentiment(json.get("sentiment").asText());
            result.setConfidence(json.get("confidence").asDouble());
            result.setExplanation(json.get("explanation").asText());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse sentiment response", e);
            return null;
        }
    }

    public List<ExtractedEntity> extractEntities(String text) {
        String prompt = """
            Extract all named entities from the following text.
            Return ONLY valid JSON array. Do not include any other text.
            
            Text: %s
            
            Respond with JSON format: [{"name": "...", "type": "PERSON|ORGANIZATION|LOCATION|DATE|OTHER", "context": "..."}]
            """.formatted(text);

        String response = chatClient.call(new Prompt(new UserMessage(prompt)))
            .getResult().getOutput().getContent();

        try {
            JsonNode arr = objectMapper.readTree(response);
            List<ExtractedEntity> entities = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    ExtractedEntity entity = new ExtractedEntity();
                    entity.setName(node.get("name").asText());
                    entity.setType(node.get("type").asText());
                    entity.setContext(node.has("context") ? node.get("context").asText() : "");
                    entities.add(entity);
                }
            }
            return entities;
        } catch (Exception e) {
            log.error("Failed to parse entities", e);
            return List.of();
        }
    }

    public SummaryResult summarize(String text, int maxWords) {
        String prompt = """
            Summarize the following text and return ONLY valid JSON.
            Do not include any other text.
            
            Text: %s
            
            Respond with JSON format: {"summary": "...", "keyPoints": ["...", "..."], "wordCount": %d}
            """.formatted(text, maxWords);

        String response = chatClient.call(new Prompt(new UserMessage(prompt)))
            .getResult().getOutput().getContent();

        try {
            JsonNode json = objectMapper.readTree(response);
            SummaryResult result = new SummaryResult();
            result.setSummary(json.get("summary").asText());
            List<String> keyPoints = new ArrayList<>();
            if (json.has("keyPoints") && json.get("keyPoints").isArray()) {
                for (JsonNode point : json.get("keyPoints")) {
                    keyPoints.add(point.asText());
                }
            }
            result.setKeyPoints(keyPoints);
            result.setWordCount(json.has("wordCount") ? json.get("wordCount").asInt() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to parse summary", e);
            return null;
        }
    }

    public ClassificationResult classify(String text, List<String> categories) {
        String categoriesStr = String.join(", ", categories);
        String prompt = """
            Classify the following text into one of these categories: [%s]
            Return ONLY valid JSON.
            
            Text: %s
            
            Respond with JSON format: {"category": "...", "confidence": 0.0-1.0, "reasoning": "..."}
            """.formatted(categoriesStr, text);

        String response = chatClient.call(new Prompt(new UserMessage(prompt)))
            .getResult().getOutput().getContent();

        try {
            JsonNode json = objectMapper.readTree(response);
            ClassificationResult result = new ClassificationResult();
            result.setCategory(json.get("category").asText());
            result.setConfidence(json.get("confidence").asDouble());
            result.setReasoning(json.get("reasoning").asText());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse classification", e);
            return null;
        }
    }
}
```

### OutputParserController.java
```java
package com.ai.controller;

import com.ai.dto.*;
import com.ai.service.OutputParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/parse")
public class OutputParserController {

    private final OutputParserService parserService;

    public OutputParserController(OutputParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/sentiment")
    public ResponseEntity<SentimentResult> analyzeSentiment(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(parserService.analyzeSentiment(request.get("text")));
    }

    @PostMapping("/entities")
    public ResponseEntity<List<ExtractedEntity>> extractEntities(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(parserService.extractEntities(request.get("text")));
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummaryResult> summarize(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        int maxWords = request.containsKey("maxWords") ? (int) request.get("maxWords") : 100;
        return ResponseEntity.ok(parserService.summarize(text, maxWords));
    }

    @PostMapping("/classify")
    public ResponseEntity<ClassificationResult> classify(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) request.get("categories");
        return ResponseEntity.ok(parserService.classify(text, categories));
    }
}
```

### DTOs Package

### SentimentResult.java
```java
package com.ai.dto;

public class SentimentResult {
    private String sentiment;
    private double confidence;
    private String explanation;

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
```

### ExtractedEntity.java
```java
package com.ai.dto;

public class ExtractedEntity {
    private String name;
    private String type;
    private String context;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
```

### SummaryResult.java
```java
package com.ai.dto;

import java.util.List;

public class SummaryResult {
    private String summary;
    private List<String> keyPoints;
    private int wordCount;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }
    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
}
```

### ClassificationResult.java
```java
package com.ai.dto;

public class ClassificationResult {
    private String category;
    private double confidence;
    private String reasoning;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
```

---

## AI Configuration

### AiConfig.java
```java
package com.ai.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(500, 100, 5, 1000, true);
    }
}
```

### WebConfig.java
```java
package com.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }
}
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/ai/chat` | Chat completion |
| POST | `/api/ai/chat/stream` | Streaming chat (SSE) |
| POST | `/api/ai/documents/upload` | Upload and ingest document |
| POST | `/api/ai/documents/extract` | Extract text from document |
| POST | `/api/ai/rag/ask` | Ask with RAG context |
| POST | `/api/ai/rag/ask-with-sources` | Ask with sources |
| GET | `/api/ai/search?q=query` | Semantic search |
| GET | `/api/ai/search/with-context?q=query` | Search with combined context |
| POST | `/api/ai/images/generate` | Generate image |
| POST | `/api/ai/prompts/execute/{template}` | Execute prompt template |
| POST | `/api/ai/prompts/chain` | Chain multiple templates |
| POST | `/api/ai/parse/sentiment` | Analyze sentiment |
| POST | `/api/ai/parse/entities` | Extract entities |
| POST | `/api/ai/parse/summarize` | Summarize text |
| POST | `/api/ai/parse/classify` | Classify text |

## Running the Application

```bash
# Start PostgreSQL with pgvector
docker run -d --name ai-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=ai_db \
  -p 5432:5432 \
  pgvector/pgvector:pg16

# Set your OpenAI API key
export OPENAI_API_KEY=sk-your-key-here

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Chat
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"What is the capital of France?"}'

# Upload a document
curl -X POST http://localhost:8080/api/ai/documents/upload \
  -F "file=@/path/to/document.pdf"

# Ask RAG question
curl -X POST http://localhost:8080/api/ai/rag/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What does the document say about microservices?"}'

# Semantic search
curl "http://localhost:8080/api/ai/search?q=cloud+architecture&topK=5"

# Generate image
curl -X POST http://localhost:8080/api/ai/images/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt":"A futuristic city skyline at sunset","size":"1024x1024","quality":"standard"}'

# Summarize text
curl -X POST http://localhost:8080/api/ai/parse/summarize \
  -H "Content-Type: application/json" \
  -d '{"text":"Long text to summarize...","maxWords":100}'

# Analyze sentiment
curl -X POST http://localhost:8080/api/ai/parse/sentiment \
  -H "Content-Type: application/json" \
  -d '{"text":"I absolutely love this product! It works perfectly."}'

# Use prompt template
curl -X POST http://localhost:8080/api/ai/prompts/execute/translate \
  -H "Content-Type: application/json" \
  -d '{"text":"Hello world","source_language":"English","target_language":"Spanish"}'

# Stream chat
curl -N -X POST http://localhost:8080/api/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Tell me a story"}'
```
