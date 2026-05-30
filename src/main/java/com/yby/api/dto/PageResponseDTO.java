package com.yby.api.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponseDTO<T>(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<T> content
) {

    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getContent()
        );
    }
}
