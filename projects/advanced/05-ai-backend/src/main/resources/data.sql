-- Seed data for AI backend
-- Documents for RAG (Retrieval-Augmented Generation) with pgvector embeddings
-- Embedding vectors are truncated to 4 dimensions for seed data (placeholder values)

INSERT INTO documents (title, content, content_type, source, author, embedding, token_count, chunk_index, is_active)
VALUES
    ('Java Virtual Threads Overview',
     'Virtual threads are lightweight threads that dramatically reduce the effort of writing, maintaining, and debugging high-throughput concurrent applications. Introduced as a preview feature in Java 19 and finalized in Java 21, virtual threads are managed by the JVM rather than the OS, allowing millions of concurrent threads with minimal memory overhead. They are best suited for I/O-bound tasks where threads spend most of their time waiting. Virtual threads use a small number of carrier platform threads via a work-stealing ForkJoinPool.',
     'TEXT', 'course-materials', 'Bro Code', '[0.0123, -0.0456, 0.0789, -0.0234]', 342, 0, true),
    ('Spring Boot Auto-Configuration Guide',
     'Spring Boot auto-configuration attempts to automatically configure your Spring application based on the jar dependencies you have added. For example, if H2 database is on your classpath and you have not configured any DataSource beans manually, Spring Boot auto-configures an in-memory database. Auto-configuration is implemented with @Conditional annotations, including @ConditionalOnClass, @ConditionalOnMissingBean, and @ConditionalOnProperty. Understanding auto-configuration is key to debugging Spring Boot applications and writing your own auto-configuration starters.',
     'TEXT', 'course-materials', 'Bro Code', '[0.0567, -0.0123, 0.0912, -0.0678]', 456, 0, true);
