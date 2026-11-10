package com.arivubuilds.api.graphql;

import com.arivubuilds.api.graphql.config.GraphQLSecurityConfig;
import com.arivubuilds.api.graphql.controller.CatalogDataFetcher;
import com.arivubuilds.api.graphql.repository.CatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = GraphQLComplexityTest.TestConfig.class)
class GraphQLComplexityTest {

    @Configuration
    @EnableAutoConfiguration
    @Import({CatalogDataFetcher.class, CatalogRepository.class, GraphQLSecurityConfig.class})
    static class TestConfig {}

    private GraphQlTester graphQlTester;

    @Autowired
    private ExecutionGraphQlService executionGraphQlService;

    @Autowired
    private CatalogRepository repository;

    @BeforeEach
    void setUp() {
        this.graphQlTester = ExecutionGraphQlServiceTester.create(executionGraphQlService);
        repository.resetCounter();
    }

    @Test
    @DisplayName("BatchMapping resolves authors for all books in a single batch query (No N+1)")
    void shouldResolveAuthorsWithSingleBatchQuery() {
        String document = """
            query {
                books {
                    id
                    title
                    author {
                        name
                    }
                }
            }
            """;

        graphQlTester.document(document)
                .execute()
                .path("books")
                .entityList(Object.class)
                .hasSize(4);

        assertEquals(1, repository.getAuthorBatchQueryCount());
    }

    @Test
    @DisplayName("Queries exceeding max depth of 4 are blocked with execution error")
    void shouldBlockExcessiveQueryDepth() {
        String maliciousDocument = """
            query {
                books {
                    author {
                        books {
                            author {
                                name
                            }
                        }
                    }
                }
            }
            """;

        graphQlTester.document(maliciousDocument)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertEquals(1, errors.size());
                    assertTrue(errors.get(0).getMessage().toLowerCase().contains("maximum query depth exceeded"));
                });
    }
}