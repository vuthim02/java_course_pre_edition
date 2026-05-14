package com.aibackend.service;

import com.aibackend.model.Document;
import com.aibackend.model.VectorEmbedding;
import com.aibackend.repository.DocumentRepository;
import com.aibackend.repository.VectorEmbeddingRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {
    private final DocumentRepository documentRepository;
    private final VectorEmbeddingRepository vectorEmbeddingRepository;
    private final EmbeddingModel embeddingModel;

    public IngestionService(DocumentRepository documentRepository,
                            VectorEmbeddingRepository vectorEmbeddingRepository,
                            EmbeddingModel embeddingModel) {
        this.documentRepository = documentRepository;
        this.vectorEmbeddingRepository = vectorEmbeddingRepository;
        this.embeddingModel = embeddingModel;
    }

    public Document ingest(MultipartFile file) {
        try {
            var content = extractText(file);
            var doc = new Document();
            doc.setFilename(file.getOriginalFilename());
            doc.setContent(content);
            doc.setContentType(file.getContentType());
            doc = documentRepository.save(doc);

            var chunks = chunk(content, 500);
            var embeddings = embeddingModel.embed(chunks);

            for (int i = 0; i < chunks.size(); i++) {
                var vector = new VectorEmbedding();
                vector.setDocumentId(doc.getId());
                vector.setChunk(chunks.get(i));
                float[] emb = embeddings.get(i);
                float[] embedding = emb.length > 0 ? emb : new float[384];
                vector.setEmbedding(embedding);
                vectorEmbeddingRepository.save(vector);
            }

            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest document", e);
        }
    }

    public List<VectorEmbedding> searchSimilar(float[] queryVector, int limit) {
        return vectorEmbeddingRepository.findSimilar(queryVector, limit);
    }

    private String extractText(MultipartFile file) throws Exception {
        var contentType = file.getContentType();
        if (contentType != null && contentType.equals("application/pdf")) {
            try (var pdfDoc = Loader.loadPDF(file.getBytes())) {
                var stripper = new PDFTextStripper();
                return stripper.getText(pdfDoc);
            }
        }
        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return String.join("\n", reader.lines().toList());
        }
    }

    private List<String> chunk(String text, int chunkSize) {
        var chunks = new ArrayList<String>();
        var words = text.split("\\s+");
        var sb = new StringBuilder();
        for (var word : words) {
            if (sb.length() + word.length() > chunkSize && !sb.isEmpty()) {
                chunks.add(sb.toString().trim());
                sb = new StringBuilder();
            }
            sb.append(word).append(" ");
        }
        if (!sb.isEmpty()) {
            chunks.add(sb.toString().trim());
        }
        return chunks;
    }
}
