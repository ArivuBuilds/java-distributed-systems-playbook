package com.arivubuilds.api.graphql.repository;

import com.arivubuilds.api.graphql.model.Author;
import com.arivubuilds.api.graphql.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class CatalogRepository {

    private final AtomicInteger authorBatchQueryCount = new AtomicInteger(0);

    private final Map<String, Author> authors = Map.of(
            "AUTH-1", new Author("AUTH-1", "Martin Fowler", "fowler@refactoring.com"),
            "AUTH-2", new Author("AUTH-2", "Robert C. Martin", "unclebob@cleancode.com")
    );

    private final List<Book> books = List.of(
            new Book("BOOK-1", "Patterns of Enterprise Application Architecture", 54.99, "AUTH-1"),
            new Book("BOOK-2", "Refactoring", 49.99, "AUTH-1"),
            new Book("BOOK-3", "Clean Architecture", 39.99, "AUTH-2"),
            new Book("BOOK-4", "Clean Code", 42.50, "AUTH-2")
    );

    public List<Book> findAllBooks() {
        return books;
    }

    public Optional<Book> findBookById(String id) {
        return books.stream().filter(b -> b.id().equals(id)).findFirst();
    }

    public Map<String, Author> findAuthorsByIds(Set<String> authorIds) {
        authorBatchQueryCount.incrementAndGet();
        Map<String, Author> result = new HashMap<>();
        for (String id : authorIds) {
            if (authors.containsKey(id)) {
                result.put(id, authors.get(id));
            }
        }
        return result;
    }

    public int getAuthorBatchQueryCount() {
        return authorBatchQueryCount.get();
    }

    public void resetCounter() {
        authorBatchQueryCount.set(0);
    }
}
