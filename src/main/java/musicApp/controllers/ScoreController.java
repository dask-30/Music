package musicApp.controllers;

import musicApp.model.structural.Score;
import musicApp.services.ScoreService;
import musicApp.utils.dto.ScoreRequestDTO;
import musicApp.utils.dto.ScoreResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/music")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE
        }
)
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/users/{userId}/scores")
    public ResponseEntity<?> createScore(
            @RequestBody ScoreRequestDTO request,
            @PathVariable Long userId
    ) {

        Score saved = scoreService.processAndSaveMxl(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(saved));
    }

    @GetMapping("/users/{userId}/scores")
    public ResponseEntity<?> getUserScores(@PathVariable Long userId) {

        return ResponseEntity.ok(
                scoreService.getAllScoresByUser(userId)
                        .stream()
                        .map(score -> new ScoreResponseDTO(
                                score.getId(),
                                score.getTitle(),
                                score.getSubtitle(),
                                score.getAuthor(),
                                null // IMPORTANT: don't send binary in list
                        ))
                        .toList()
        );
    }

    @GetMapping("/scores/{id}")
    public ResponseEntity<?> getScore(@PathVariable Long id) {

        Score score = scoreService.getScoreById(id);

        return ResponseEntity.ok(toDto(score));
    }

    @PutMapping("/users/{userId}/scores/{id}")
    public ResponseEntity<?> updateScore(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody ScoreRequestDTO request
    ) {

        Score updated = scoreService.updateScore(id, request, userId);

        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/users/{userId}/scores/{id}")
    public ResponseEntity<?> deleteScore(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {



        scoreService.deleteScore(id, userId);

        return ResponseEntity.noContent().build();
    }

    private ScoreResponseDTO toDto(Score score) {

        return new ScoreResponseDTO(
                score.getId(),
                score.getTitle(),
                score.getSubtitle(),
                score.getAuthor(),
                score.getData()
        );
    }
}