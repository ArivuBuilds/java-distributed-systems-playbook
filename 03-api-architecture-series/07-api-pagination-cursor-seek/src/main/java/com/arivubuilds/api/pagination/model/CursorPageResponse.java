package com.arivubuilds.api.pagination.model;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> data,
        PageMetadata pagination
) {
    public record PageMetadata(
            int pageSize,
            boolean hasNextPage,
            String nextCursor
    ) {}
}