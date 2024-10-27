package org.digeplan.common.springgpt.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.MongoDBAtlasVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration
public class VectorConfig {
    private final String openAiKey;
    private final Boolean initSchema;

    public VectorConfig(
            @Value("${spring.ai.openai.api-key}")
            String openAiKey,
            @Value("${spring.ai.vectorstore.mongodb.initialize-schema}")
            Boolean initSchema) {
        this.openAiKey = openAiKey;
        this.initSchema = initSchema;
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi(openAiKey));
    }

    @Bean
    public VectorStore mongodbVectorStore(MongoTemplate mongoTemplate, EmbeddingModel embeddingModel) {
        return new MongoDBAtlasVectorStore(mongoTemplate, embeddingModel,
                MongoDBAtlasVectorStore.MongoDBVectorStoreConfig.builder().build(), initSchema);
    }

}
