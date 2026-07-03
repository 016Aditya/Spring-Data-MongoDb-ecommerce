package learnMongoDb.learnSpringMongoDb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * Helper to map a Spring Data Page into our custom paginated DTO
     * @param mappedContent The transformed list of DTOs (e.g. ProductDto.Response)
     * @param pageData The original Spring Data Page containing the metadata
     */
    public static <T> PagedResponse<T> of(List<T> mappedContent, Page<?> pageData) {
        return new PagedResponse<>(
                mappedContent,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.hasNext(),
                pageData.hasPrevious()
        );
    }
}