package org.digeplan.common.springgpt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("data-loader")
public class HackTechAtlasConfiguration {
    private final Resource hackTechData;
    private final VectorStore vectorStore;

    public HackTechAtlasConfiguration(
            @Value("classpath:/docs/digetron-1.json") Resource hackTechFile,
            VectorStore vectorStore) {
        this.hackTechData = hackTechFile;
        this.vectorStore = vectorStore;
    }

    @GetMapping
    void loadData() {
        log.info("Loading data");
        TextReader textReader = new TextReader(hackTechData);
        textReader.getCustomMetadata().put("filename", "digetron-1.json");
        List<Document> documents = textReader.get();
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }
}
