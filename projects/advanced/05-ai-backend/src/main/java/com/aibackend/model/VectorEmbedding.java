package com.aibackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vector_embeddings")
public class VectorEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(nullable = false, length = 2048)
    private String chunk;

    @Column(name = "embedding", columnDefinition = "float8[]")
    private float[] embedding;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getChunk() { return chunk; }
    public void setChunk(String chunk) { this.chunk = chunk; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
