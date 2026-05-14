-- pgvector extension setup for PostgreSQL
-- Run this on your PostgreSQL database before using vector search

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS vector_embeddings (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk TEXT NOT NULL,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS idx_vector_embeddings_document_id
    ON vector_embeddings(document_id);

CREATE INDEX IF NOT EXISTS idx_vector_embeddings_embedding
    ON vector_embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- Cosine similarity search query:
-- SELECT * FROM vector_embeddings
-- ORDER BY embedding <=> '[0.1, 0.2, ...]'
-- LIMIT 5;
