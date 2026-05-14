# Domain Specialization — Lesson 4: AI/ML Integration (LangChain4j, Spring AI)

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why AI in Java?

Large Language Models (LLMs) like GPT-4, Claude, and Llama have made AI accessible to every developer. Java frameworks now provide first-class support for LLM integration — enabling you to build AI-powered features without leaving the JVM ecosystem.

```
┌─────────────────────────────────────────────────────────────┐
│                   AI-POWERED JAVA APP                         │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Your Spring Boot Application                            │ │
│  │                                                          │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │ │
│  │  │ Chat Bot     │  │ RAG System   │  │ AI Agent     │  │ │
│  │  │ (Question    │  │ (Question    │  │ (Autonomous  │  │ │
│  │  │  Answering)  │  │  + Your Docs)│  │  Actions)     │  │ │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │ │
│  │         │                 │                 │           │ │
│  │         └────────┬────────┴────────┬────────┘           │ │
│  │                  │                 │                    │ │
│  │         ┌────────▼────────┐  ┌─────▼──────┐            │ │
│  │         │  LangChain4j    │  │ Spring AI  │            │ │
│  │         └────────┬────────┘  └─────┬──────┘            │ │
│  └──────────────────┼──────────────────┼───────────────────┘ │
│                     │                  │                      │
│  ┌──────────────────▼──────────────────▼───────────────────┐ │
│  │              LLM Providers                               │ │
│  │  OpenAI │ Anthropic │ Google │ AWS Bedrock │ Ollama     │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Spring AI

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>1.0.0-M2</version>
</dependency>
```

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.model=gpt-4o
spring.ai.openai.chat.temperature=0.7
```

### Chat Completion

```java
@Service
public class ChatService {

    @Autowired
    private ChatClient chatClient;

    public String ask(String question) {
        return chatClient.call(question);
    }

    public String askWithContext(String question, String context) {
        return chatClient.call(new Prompt(
            "Context: " + context + "\nQuestion: " + question
        )).getResult().getOutput().getContent();
    }
}

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String response = chatService.ask(request.get("question"));
        return ResponseEntity.ok(Map.of("response", response));
    }
}
```

### Structured Output

```java
public record SentimentAnalysis(String sentiment, double confidence, String explanation) {}

@Service
public class SentimentService {

    @Autowired
    private ChatClient chatClient;

    public SentimentAnalysis analyze(String text) {
        return chatClient.prompt()
            .user(u -> u.text("Analyze the sentiment of: {text}")
                .param("text", text))
            .call()
            .entity(SentimentAnalysis.class);
    }
}
```

### RAG (Retrieval-Augmented Generation)

RAG lets your AI answer questions based on YOUR documents.

```
User: "What is our return policy?"

Without RAG:                        With RAG:
"Sorry, I don't know."              "Our return policy allows returns
                                     within 30 days of purchase with
                                     original receipt. Exclusions: ..."
                                     ↑
                                  Retrieved from YOUR documents
```

```java
@Service
public class RagService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    public String ask(String question) {
        // 1. Search relevant documents
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(3)
        );

        // 2. Build context from documents
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        // 3. Ask LLM with context
        return chatClient.prompt()
            .user(u -> u.text("""
                Answer the question based on the context below.
                If the context doesn't contain the answer, say so.

                Context:
                {context}

                Question: {question}
                """)
                .param("context", context)
                .param("question", question))
            .call()
            .content();
    }
}
```

## LangChain4j

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>0.33.0</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.33.0</version>
</dependency>
```

### AI Service (Declarative)

```java
interface Assistant {
    String chat(String userMessage);
}

Assistant assistant = AiServices.create(Assistant.class, model);

String answer = assistant.chat("What is the capital of France?");
```

### Tools (Function Calling)

```java
public class OrderTools {

    private final OrderRepository orderRepository;

    @Tool("Get order status by order ID")
    public String getOrderStatus(@ToolParam("The order ID") String orderId) {
        return orderRepository.findById(orderId)
            .map(order -> "Order " + orderId + " is " + order.getStatus())
            .orElse("Order not found");
    }

    @Tool("Cancel an order by order ID")
    public String cancelOrder(@ToolParam("The order ID") String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });
        return "Order " + orderId + " has been cancelled";
    }
}

// AI can now CALL your Java methods automatically!
Assistant assistant = AiServices.builder(Assistant.class)
    .chatLanguageModel(model)
    .tools(new OrderTools())
    .build();

assistant.chat("Cancel order ORD-123");  // AI calls cancelOrder() !
```

## Embeddings & Vector Stores

```java
// Create embeddings (vector representation of text)
@Autowired
private EmbeddingClient embeddingClient;

float[] embedding = embeddingClient.embed("Your text here");

// Store in vector database
VectorStore vectorStore = new PgVectorStore(jdbcTemplate, embeddingClient);
vectorStore.add(List.of(
    new Document("Our return policy allows returns within 30 days..."),
    new Document("Shipping takes 3-5 business days...")
));
```

## AI Agent Example

```java
@Service
public class CustomerSupportAgent {

    @Autowired private ChatClient chatClient;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ReturnService returnService;

    public String handleRequest(String customerMessage) {
        String systemPrompt = """
            You are a customer support agent for an e-commerce store.
            You have access to these tools:
            - Look up orders
            - Check product availability
            - Process returns
            - Issue refunds

            Be helpful, concise, and professional.
            If you can't help, escalate to a human agent.
            """;

        return chatClient.prompt()
            .system(systemPrompt)
            .user(customerMessage)
            .functions("getOrderStatus", "checkStock", "processReturn")
            .call()
            .content();
    }
}
```

## Exercises

1. Set up Spring AI with OpenAI and create a simple chat endpoint.
2. Implement a RAG pipeline that answers questions from a PDF document.
3. Create an AI service with LangChain4j that uses function calling (tools).
4. Build a vector store and store document embeddings for semantic search.
5. Create a customer support agent that can look up orders and process returns.
