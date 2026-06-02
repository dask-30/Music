package musicApp.services;

import musicApp.model.structural.Score;
import musicApp.model.user.User;
import musicApp.repository.ScoreRepository;
import musicApp.repository.UserRepository;
import musicApp.utils.dto.ScoreRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ScoreService {

    private static final int MAX_XML_SIZE = 5 * 1024 * 1024;

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public ScoreService(
            ScoreRepository scoreRepository,
            UserRepository userRepository
    ) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Score processAndSaveMxl(
            ScoreRequestDTO request,
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        byte[] binaryData = request.data();

        if (binaryData == null || binaryData.length == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File is empty"
            );
        }

        try {

            InputStream xmlStream = extractXmlStreamFromMxl(binaryData);

            Document document = parseXmlDocument(xmlStream);

            String title = extractMetadata(
                    document,
                    "work-title",
                    "Untitled"
            );

            String author = extractMetadata(
                    document,
                    "creator",
                    "Unknown author"
            );

            Score score = new Score();

            score.setUser(user);
            score.setTitle(title);
            score.setAuthor(author);
            score.setData(binaryData);
            System.out.println(score.getData().getClass());
            System.out.println(score.getData().length);
            return scoreRepository.save(score);

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid MXL/XML file: " + e.getMessage()
            );
        }
    }

    public Score getScoreById(Long id) {

        return scoreRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Score not found"
                        )
                );
    }

    public List<Score> getAllScoresByUser(Long userId) {

        return scoreRepository.findAllByUserId(userId);
    }

    @Transactional
    public Score updateScore(
            Long id,
            ScoreRequestDTO request,
            Long userId
    ) {

        Score existing = scoreRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Score not found"
                        )
                );

        if (!existing.getUser().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update this score"
            );
        }

        byte[] binaryData = request.data();

        if (binaryData == null || binaryData.length == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File is empty"
            );
        }

        try {

            InputStream xmlStream = extractXmlStreamFromMxl(binaryData);

            Document document = parseXmlDocument(xmlStream);

            String title = extractMetadata(
                    document,
                    "work-title",
                    existing.getTitle()
            );

            String author = extractMetadata(
                    document,
                    "creator",
                    existing.getAuthor()
            );

            existing.setTitle(title);
            existing.setAuthor(author);
            existing.setData(binaryData);

            return scoreRepository.save(existing);

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid MXL/XML file"
            );
        }
    }

    @Transactional
    public void deleteScore(Long id, Long userId) {

        Score score = getScoreById(id);

        if (!score.getUser().getId().equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this score"
            );
        }

        scoreRepository.delete(score);
    }

    private InputStream extractXmlStreamFromMxl(byte[] binaryData)
            throws Exception {

        boolean isZip =
                binaryData.length >= 2 &&
                        binaryData[0] == 0x50 &&
                        binaryData[1] == 0x4B;

        if (!isZip) {
            return new ByteArrayInputStream(binaryData);
        }

        ZipInputStream zis =
                new ZipInputStream(new ByteArrayInputStream(binaryData));

        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {

            String name = entry.getName().toLowerCase();

            if (
                    !entry.isDirectory()
                            && name.endsWith(".xml")
                            && !name.contains("container.xml")
            ) {

                ByteArrayOutputStream baos =
                        new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];

                int len;
                int total = 0;

                while ((len = zis.read(buffer)) > 0) {

                    total += len;

                    if (total > MAX_XML_SIZE) {

                        throw new RuntimeException(
                                "XML file too large"
                        );
                    }

                    baos.write(buffer, 0, len);
                }

                zis.close();

                return new ByteArrayInputStream(
                        baos.toByteArray()
                );
            }

            zis.closeEntry();
        }

        zis.close();

        throw new RuntimeException(
                "No valid XML found inside MXL"
        );
    }

    private Document parseXmlDocument(InputStream xmlStream)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setValidating(false);

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );


        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        builder.setEntityResolver((publicId, systemId) ->
                new org.xml.sax.InputSource(new java.io.StringReader(""))
        );

        Document document =
                builder.parse(xmlStream);

        document.getDocumentElement().normalize();

        return document;
    }

    private String extractMetadata(
            Document document,
            String tagName,
            String defaultValue
    ) {

        NodeList list = document.getElementsByTagName(tagName);

        if (list.getLength() == 0) {
            return defaultValue;
        }

        String value = list.item(0)
                .getTextContent()
                .trim();

        return value.isBlank()
                ? defaultValue
                : value;
    }
}