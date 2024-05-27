package com.eazybytes.eazyschool.service;

import com.eazybytes.eazyschool.model.CourseDocument;
import com.eazybytes.eazyschool.model.Courses;
import com.eazybytes.eazyschool.repository.CourseDocumentRepository;
import com.eazybytes.eazyschool.repository.CoursesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class CourseDocumentService {

    private final CourseDocumentRepository courseDocumentRepository;
    private final CoursesRepository coursesRepository;
    private static final String UPLOAD_PATH = "src/main/resources/static/media/pdfs";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Value("${course.document.number}")
    private int numberOfDocuments;

    @Autowired
    public CourseDocumentService(CourseDocumentRepository courseDocumentRepository,
                                 CoursesRepository coursesRepository) {

        this.courseDocumentRepository = courseDocumentRepository;
        this.coursesRepository = coursesRepository;
    }

    public CourseDocument saveDocument(MultipartFile file, Courses courses) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file has been added.");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("File is not a PDF.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 10 MB!");
        }

        if (courses.getDocuments().size() >= numberOfDocuments) {
            throw new IllegalArgumentException("Cannot upload more than 8 files for this course.");
        }

        String courseDirName = courses.getName().toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        Path uploadDir = Paths.get(UPLOAD_PATH + "/" + courseDirName);

        if (Files.notExists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());

        CourseDocument courseDocument = new CourseDocument();
        courseDocument.setDocumentName(fileName);
        courseDocument.setDocumentPath(courseDirName + "/" + fileName);
        courseDocument.setCourse(courses);
        return courseDocumentRepository.save(courseDocument);
    }

    public Courses deleteDocument(int documentId, Courses courses) throws IOException {

        Optional<CourseDocument> optionalCourseDocument = courseDocumentRepository.findById(documentId);

        if (optionalCourseDocument.isPresent()) {

            CourseDocument courseDocument = optionalCourseDocument.get();

            Path filePath = Paths.get(UPLOAD_PATH + "/" + courseDocument.getDocumentPath());

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            courses.getDocuments().remove(courseDocument);
            return coursesRepository.save(courses);

        } else {
            throw new IllegalArgumentException("Document with ID " + documentId + " not found.");
        }
    }


    public CourseDocument getDocumentById(int id) {
        return courseDocumentRepository.findById(id).orElse(null);
    }

    public void incrementDownloadCount(int id) {
        CourseDocument document = courseDocumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid document Id: " + id));
        if (document.getDownloadCount() == null) {
            document.setDownloadCount(0);
        }
        document.setDownloadCount(document.getDownloadCount() + 1);
        courseDocumentRepository.save(document);
    }
}