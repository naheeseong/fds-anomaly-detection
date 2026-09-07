package com.fds.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final Pagination pagination;
    private final LocalDateTime timestamp;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pagination = new Pagination(page);
        this.timestamp = LocalDateTime.now();
    }

    @Getter
    public static class Pagination {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public Pagination(Page<?> page) {
            this.page = page.getNumber();
            this.size = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
        }
    }
}
