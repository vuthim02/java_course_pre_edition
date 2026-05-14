package com.aibackend.repository;

import com.aibackend.model.VectorEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, Long> {
    List<VectorEmbedding> findByDocumentId(Long documentId);

    @Query(value = "SELECT * FROM vector_embeddings ORDER BY embedding <-> cast(:queryVector as float8[]) LIMIT :limit", nativeQuery = true)
    List<VectorEmbedding> findSimilar(float[] queryVector, int limit);
}
