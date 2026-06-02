package musicApp.utils.dto;

public record ScoreResponseDTO(
        Long id,
        String title,
        String subtitle,
        String author,
        byte[] data
) {}