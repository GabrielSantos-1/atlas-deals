package com.atlasdeals.common.application.dto;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record PagedResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean isEmpty
) {
    public PagedResult {
        content = content != null ? Collections.unmodifiableList(content) : List.of();
    }

    public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResult<>(
            content, page, size, totalElements, totalPages,
            page == 0, page >= totalPages - 1, content.isEmpty()
        );
    }

    public static <T> PagedResult<T> empty() {
        return new PagedResult<>(List.of(), 0, 0, 0, 0, true, true, true);
    }

    public <U> PagedResult<U> map(Function<? super T, ? extends U> converter) {
        List<U> mapped = new java.util.ArrayList<>(content.size());
        for (T item : content) {
            mapped.add(converter.apply(item));
        }
        return new PagedResult<>(
            mapped,
            page, size, totalElements, totalPages, first, last, isEmpty
        );
    }
}
