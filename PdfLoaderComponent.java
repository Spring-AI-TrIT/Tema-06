package com.springai.curriculums.init.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdfLoaderComponent {
	private final Path rootPath;
    private final TokenTextSplitter splitter;

    public PdfLoaderComponent(
            @Value("${app.cvs.path}") String rootPath) {
        this.rootPath = Path.of(rootPath);
		//creación y configuración del splitter
        this.splitter = TokenTextSplitter.builder()
        		.withChunkSize(80) //tamaño en tokens de cada chunk					
        		.withMaxNumChunks(10) //máximo número de chunks por documento
				.build();       
    }

    public List<Document> obtenerChunks() {
        try (Stream<Path> paths = Files.walk(rootPath)) {
        	return splitter.split(
	            paths
	                .filter(Files::isRegularFile)
	                .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
					//transforma el contenido de un archivo en un Document
	                .map(d->new Document(extraerTexto(d), Map.of("file_path", d.toString())))
	                .toList()
	          			);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo PDFs", e);
        }      
    }
	//devuelve el contenido de un pdf como texto
    private String extraerTexto(Path pdfPath) {
        try (PDDocument doc =  Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo PDF: " + pdfPath, e);
        }
    }
}
