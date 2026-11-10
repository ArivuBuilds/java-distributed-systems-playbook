package com.arivubuilds.api.graphql.controller;

import com.arivubuilds.api.graphql.model.Author;
import com.arivubuilds.api.graphql.model.Book;
import com.arivubuilds.api.graphql.repository.CatalogRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CatalogDataFetcher {

    private final CatalogRepository repository;

    public CatalogDataFetcher(CatalogRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public List<Book> books() {
        return repository.findAllBooks();
    }

    @QueryMapping
    public Optional<Book> bookById(@Argument String id) {
        return repository.findBookById(id);
    }

    /**
     * @BatchMapping intercepts field resolution for Author across all Books in the query.
     * Collapses N individual queries into exactly 1 batch execution.
     */
    @BatchMapping(typeName = "Book", field = "author")
    public Map<Book, Author> author(List<Book> books) {
        Set<String> authorIds = books.stream()
                .map(Book::authorId)
                .collect(Collectors.toSet());

        Map<String, Author> authorMap = repository.findAuthorsByIds(authorIds);

        Map<Book, Author> result = new HashMap<>();
        for (Book book : books) {
            result.put(book, authorMap.get(book.authorId()));
        }
        return result;
    }
}