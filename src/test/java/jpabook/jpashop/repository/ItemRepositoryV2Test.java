package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.exception.NotEnoughStockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for ItemRepositoryV2
 * Testing Framework: JUnit 5 with Spring Boot Test
 * Assertion Library: AssertJ
 * JPA Provider: Hibernate with H2 in-memory database
 * 
 * This test class covers all standard JPA repository operations
 * as ItemRepositoryV2 extends JpaRepository<Item, Long>
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRepositoryV2Test {

    @Autowired
    private ItemRepositoryV2 itemRepositoryV2;

    @Autowired
    private EntityManager em;

    private Book testBook;
    private Album testAlbum;
    private Movie testMovie;

    @BeforeEach
    void setUp() {
        // Clear any existing data to ensure test isolation
        em.createQuery("DELETE FROM Item").executeUpdate();
        em.flush();
        em.clear();

        // Create test items with valid data
        testBook = new Book();
        testBook.setName("Test Book");
        testBook.setPrice(20000);
        testBook.setStockQuantity(100);
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-0123456789");

        testAlbum = new Album();
        testAlbum.setName("Test Album");
        testAlbum.setPrice(15000);
        testAlbum.setStockQuantity(50);
        testAlbum.setArtist("Test Artist");
        testAlbum.setEtc("Pop Music");

        testMovie = new Movie();
        testMovie.setName("Test Movie");
        testMovie.setPrice(25000);
        testMovie.setStockQuantity(30);
        testMovie.setDirector("Test Director");
        testMovie.setActor("Test Actor");
    }

    @Nested
    @DisplayName("Save Operation Tests")
    class SaveOperationTests {

        @Test
        @DisplayName("Should save a book item successfully")
        void shouldSaveBookItem() {
            // When
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem).isNotNull();
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getName()).isEqualTo("Test Book");
            assertThat(savedItem.getPrice()).isEqualTo(20000);
            assertThat(savedItem.getStockQuantity()).isEqualTo(100);
            assertThat(savedItem).isInstanceOf(Book.class);
            
            Book savedBook = (Book) savedItem;
            assertThat(savedBook.getAuthor()).isEqualTo("Test Author");
            assertThat(savedBook.getIsbn()).isEqualTo("978-0123456789");
        }

        @Test
        @DisplayName("Should save an album item successfully")
        void shouldSaveAlbumItem() {
            // When
            Item savedItem = itemRepositoryV2.save(testAlbum);

            // Then
            assertThat(savedItem).isNotNull();
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getName()).isEqualTo("Test Album");
            assertThat(savedItem.getPrice()).isEqualTo(15000);
            assertThat(savedItem.getStockQuantity()).isEqualTo(50);
            assertThat(savedItem).isInstanceOf(Album.class);
            
            Album savedAlbum = (Album) savedItem;
            assertThat(savedAlbum.getArtist()).isEqualTo("Test Artist");
            assertThat(savedAlbum.getEtc()).isEqualTo("Pop Music");
        }

        @Test
        @DisplayName("Should save a movie item successfully")
        void shouldSaveMovieItem() {
            // When
            Item savedItem = itemRepositoryV2.save(testMovie);

            // Then
            assertThat(savedItem).isNotNull();
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getName()).isEqualTo("Test Movie");
            assertThat(savedItem.getPrice()).isEqualTo(25000);
            assertThat(savedItem.getStockQuantity()).isEqualTo(30);
            assertThat(savedItem).isInstanceOf(Movie.class);
            
            Movie savedMovie = (Movie) savedItem;
            assertThat(savedMovie.getDirector()).isEqualTo("Test Director");
            assertThat(savedMovie.getActor()).isEqualTo("Test Actor");
        }

        @Test
        @DisplayName("Should save item with null optional fields")
        void shouldSaveItemWithNullOptionalFields() {
            // Given
            Book bookWithNulls = new Book();
            bookWithNulls.setName("Book with nulls");
            bookWithNulls.setPrice(10000);
            bookWithNulls.setStockQuantity(10);
            // Author and ISBN are left as null

            // When
            Item savedItem = itemRepositoryV2.save(bookWithNulls);

            // Then
            assertThat(savedItem).isNotNull();
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getName()).isEqualTo("Book with nulls");
            
            Book savedBook = (Book) savedItem;
            assertThat(savedBook.getAuthor()).isNull();
            assertThat(savedBook.getIsbn()).isNull();
        }

        @Test
        @DisplayName("Should generate ID automatically on save")
        void shouldGenerateIdAutomatically() {
            // Given
            assertThat(testBook.getId()).isNull();

            // When
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem.getId()).isNotNull();
            assertThat(savedItem.getId()).isPositive();
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find item by valid ID")
        void shouldFindItemByValidId() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // When
            Optional<Item> foundItem = itemRepositoryV2.findById(itemId);

            // Then
            assertThat(foundItem).isPresent();
            assertThat(foundItem.get().getId()).isEqualTo(itemId);
            assertThat(foundItem.get().getName()).isEqualTo("Test Book");
            assertThat(foundItem.get()).isInstanceOf(Book.class);
        }

        @Test
        @DisplayName("Should return empty optional for non-existent ID")
        void shouldReturnEmptyOptionalForNonExistentId() {
            // When
            Optional<Item> foundItem = itemRepositoryV2.findById(999L);

            // Then
            assertThat(foundItem).isEmpty();
        }

        @Test
        @DisplayName("Should return empty optional for null ID")
        void shouldReturnEmptyOptionalForNullId() {
            // When
            Optional<Item> foundItem = itemRepositoryV2.findById(null);

            // Then
            assertThat(foundItem).isEmpty();
        }

        @Test
        @DisplayName("Should find all items when items exist")
        void shouldFindAllItemsWhenItemsExist() {
            // Given
            itemRepositoryV2.save(testBook);
            itemRepositoryV2.save(testAlbum);
            itemRepositoryV2.save(testMovie);

            // When
            List<Item> allItems = itemRepositoryV2.findAll();

            // Then
            assertThat(allItems).hasSize(3);
            assertThat(allItems).extracting(Item::getName)
                    .containsExactlyInAnyOrder("Test Book", "Test Album", "Test Movie");
        }

        @Test
        @DisplayName("Should return empty list when no items exist")
        void shouldReturnEmptyListWhenNoItemsExist() {
            // When
            List<Item> allItems = itemRepositoryV2.findAll();

            // Then
            assertThat(allItems).isEmpty();
        }

        @Test
        @DisplayName("Should find all items by IDs")
        void shouldFindAllItemsByIds() {
            // Given
            Item savedBook = itemRepositoryV2.save(testBook);
            Item savedAlbum = itemRepositoryV2.save(testAlbum);
            Item savedMovie = itemRepositoryV2.save(testMovie);
            
            List<Long> ids = Arrays.asList(savedBook.getId(), savedAlbum.getId());

            // When
            List<Item> foundItems = itemRepositoryV2.findAllById(ids);

            // Then
            assertThat(foundItems).hasSize(2);
            assertThat(foundItems).extracting(Item::getId)
                    .containsExactlyInAnyOrder(savedBook.getId(), savedAlbum.getId());
        }

        @Test
        @DisplayName("Should return empty list when finding by empty ID list")
        void shouldReturnEmptyListWhenFindingByEmptyIdList() {
            // Given
            itemRepositoryV2.save(testBook);

            // When
            List<Item> foundItems = itemRepositoryV2.findAllById(Arrays.asList());

            // Then
            assertThat(foundItems).isEmpty();
        }

        @Test
        @DisplayName("Should find items by mixed valid and invalid IDs")
        void shouldFindItemsByMixedValidAndInvalidIds() {
            // Given
            Item savedBook = itemRepositoryV2.save(testBook);
            List<Long> mixedIds = Arrays.asList(savedBook.getId(), 999L, 888L);

            // When
            List<Item> foundItems = itemRepositoryV2.findAllById(mixedIds);

            // Then
            assertThat(foundItems).hasSize(1);
            assertThat(foundItems.get(0).getId()).isEqualTo(savedBook.getId());
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should update existing item successfully")
        void shouldUpdateExistingItem() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // When
            savedItem.setName("Updated Book Name");
            savedItem.setPrice(30000);
            savedItem.setStockQuantity(200);
            Item updatedItem = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(updatedItem.getId()).isEqualTo(itemId);
            assertThat(updatedItem.getName()).isEqualTo("Updated Book Name");
            assertThat(updatedItem.getPrice()).isEqualTo(30000);
            assertThat(updatedItem.getStockQuantity()).isEqualTo(200);

            // Verify persistence
            em.flush();
            em.clear();
            Optional<Item> retrievedItem = itemRepositoryV2.findById(itemId);
            assertThat(retrievedItem).isPresent();
            assertThat(retrievedItem.get().getName()).isEqualTo("Updated Book Name");
        }

        @Test
        @DisplayName("Should update item with zero values")
        void shouldUpdateItemWithZeroValues() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);

            // When
            savedItem.setPrice(0);
            savedItem.setStockQuantity(0);
            Item updatedItem = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(updatedItem.getPrice()).isEqualTo(0);
            assertThat(updatedItem.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should update specific fields of subclass")
        void shouldUpdateSpecificFieldsOfSubclass() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Book savedBook = (Book) savedItem;

            // When
            savedBook.setAuthor("Updated Author");
            savedBook.setIsbn("978-9876543210");
            Item updatedItem = itemRepositoryV2.save(savedBook);

            // Then
            Book updatedBook = (Book) updatedItem;
            assertThat(updatedBook.getAuthor()).isEqualTo("Updated Author");
            assertThat(updatedBook.getIsbn()).isEqualTo("978-9876543210");
            // Verify other fields remain unchanged
            assertThat(updatedBook.getName()).isEqualTo("Test Book");
            assertThat(updatedBook.getPrice()).isEqualTo(20000);
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete existing item by ID")
        void shouldDeleteExistingItemById() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // Verify item exists
            assertThat(itemRepositoryV2.findById(itemId)).isPresent();

            // When
            itemRepositoryV2.deleteById(itemId);

            // Then
            assertThat(itemRepositoryV2.findById(itemId)).isEmpty();
        }

        @Test
        @DisplayName("Should handle deletion of non-existent item gracefully")
        void shouldHandleDeletionOfNonExistentItem() {
            // When & Then - should not throw exception
            assertThatCode(() -> itemRepositoryV2.deleteById(999L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should delete item by entity")
        void shouldDeleteItemByEntity() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // When
            itemRepositoryV2.delete(savedItem);

            // Then
            assertThat(itemRepositoryV2.findById(itemId)).isEmpty();
        }

        @Test
        @DisplayName("Should delete all items")
        void shouldDeleteAllItems() {
            // Given
            itemRepositoryV2.save(testBook);
            itemRepositoryV2.save(testAlbum);
            itemRepositoryV2.save(testMovie);
            
            assertThat(itemRepositoryV2.count()).isEqualTo(3);

            // When
            itemRepositoryV2.deleteAll();

            // Then
            assertThat(itemRepositoryV2.count()).isEqualTo(0);
            assertThat(itemRepositoryV2.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should delete items in batch")
        void shouldDeleteItemsInBatch() {
            // Given
            Item savedBook = itemRepositoryV2.save(testBook);
            Item savedAlbum = itemRepositoryV2.save(testAlbum);
            itemRepositoryV2.save(testMovie);
            
            List<Item> itemsToDelete = Arrays.asList(savedBook, savedAlbum);

            // When
            itemRepositoryV2.deleteAll(itemsToDelete);

            // Then
            assertThat(itemRepositoryV2.count()).isEqualTo(1);
            assertThat(itemRepositoryV2.findById(savedBook.getId())).isEmpty();
            assertThat(itemRepositoryV2.findById(savedAlbum.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should handle deletion of empty collection")
        void shouldHandleDeletionOfEmptyCollection() {
            // Given
            itemRepositoryV2.save(testBook);
            long originalCount = itemRepositoryV2.count();

            // When
            itemRepositoryV2.deleteAll(Arrays.asList());

            // Then
            assertThat(itemRepositoryV2.count()).isEqualTo(originalCount);
        }
    }

    @Nested
    @DisplayName("Count and Existence Tests")
    class CountAndExistenceTests {

        @Test
        @DisplayName("Should count all items correctly")
        void shouldCountAllItemsCorrectly() {
            // Given
            itemRepositoryV2.save(testBook);
            itemRepositoryV2.save(testAlbum);

            // When
            long count = itemRepositoryV2.count();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return zero count when no items exist")
        void shouldReturnZeroCountWhenNoItemsExist() {
            // When
            long count = itemRepositoryV2.count();

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should check if item exists by ID")
        void shouldCheckIfItemExistsById() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // When & Then
            assertThat(itemRepositoryV2.existsById(itemId)).isTrue();
            assertThat(itemRepositoryV2.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should handle null ID in exists check")
        void shouldHandleNullIdInExistsCheck() {
            // When & Then
            assertThat(itemRepositoryV2.existsById(null)).isFalse();
        }

        @Test
        @DisplayName("Should count items after operations")
        void shouldCountItemsAfterOperations() {
            // Given
            itemRepositoryV2.save(testBook);
            itemRepositoryV2.save(testAlbum);
            assertThat(itemRepositoryV2.count()).isEqualTo(2);

            // When
            itemRepositoryV2.save(testMovie);

            // Then
            assertThat(itemRepositoryV2.count()).isEqualTo(3);

            // When
            itemRepositoryV2.deleteById(testBook.getId());

            // Then
            assertThat(itemRepositoryV2.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        @Test
        @DisplayName("Should save multiple items in batch")
        void shouldSaveMultipleItemsInBatch() {
            // Given
            List<Item> items = Arrays.asList(testBook, testAlbum, testMovie);

            // When
            List<Item> savedItems = itemRepositoryV2.saveAll(items);

            // Then
            assertThat(savedItems).hasSize(3);
            assertThat(savedItems).allMatch(item -> item.getId() != null);
            
            // Verify all items were saved
            assertThat(itemRepositoryV2.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle empty list in batch save")
        void shouldHandleEmptyListInBatchSave() {
            // When
            List<Item> savedItems = itemRepositoryV2.saveAll(Arrays.asList());

            // Then
            assertThat(savedItems).isEmpty();
        }

        @Test
        @DisplayName("Should save and update mixed items in batch")
        void shouldSaveAndUpdateMixedItemsInBatch() {
            // Given
            Item existingItem = itemRepositoryV2.save(testBook);
            existingItem.setName("Updated Book");
            
            List<Item> mixedItems = Arrays.asList(existingItem, testAlbum, testMovie);

            // When
            List<Item> savedItems = itemRepositoryV2.saveAll(mixedItems);

            // Then
            assertThat(savedItems).hasSize(3);
            
            // Check the updated item
            Optional<Item> updatedBook = savedItems.stream()
                    .filter(item -> item.getId().equals(existingItem.getId()))
                    .findFirst();
            assertThat(updatedBook).isPresent();
            assertThat(updatedBook.get().getName()).isEqualTo("Updated Book");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should add stock successfully")
        void shouldAddStockSuccessfully() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            int originalStock = savedItem.getStockQuantity();

            // When
            savedItem.addStock(50);
            Item updatedItem = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(updatedItem.getStockQuantity()).isEqualTo(originalStock + 50);
        }

        @Test
        @DisplayName("Should remove stock successfully when sufficient stock exists")
        void shouldRemoveStockSuccessfullyWhenSufficientStockExists() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook); // Stock: 100
            int originalStock = savedItem.getStockQuantity();

            // When
            savedItem.removeStock(30);
            Item updatedItem = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(updatedItem.getStockQuantity()).isEqualTo(originalStock - 30);
        }

        @Test
        @DisplayName("Should throw exception when removing more stock than available")
        void shouldThrowExceptionWhenRemovingMoreStockThanAvailable() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook); // Stock: 100

            // When & Then
            assertThatThrownBy(() -> savedItem.removeStock(150))
                    .isInstanceOf(NotEnoughStockException.class)
                    .hasMessage("need more stock");
        }

        @Test
        @DisplayName("Should handle exact stock removal")
        void shouldHandleExactStockRemoval() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook); // Stock: 100

            // When
            savedItem.removeStock(100);
            Item updatedItem = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(updatedItem.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle zero stock addition")
        void shouldHandleZeroStockAddition() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            int originalStock = savedItem.getStockQuantity();

            // When
            savedItem.addStock(0);

            // Then
            assertThat(savedItem.getStockQuantity()).isEqualTo(originalStock);
        }

        @Test
        @DisplayName("Should handle negative stock scenario in removeStock")
        void shouldHandleNegativeStockScenarioInRemoveStock() {
            // Given
            testBook.setStockQuantity(5);
            Item savedItem = itemRepositoryV2.save(testBook);

            // When & Then
            assertThatThrownBy(() -> savedItem.removeStock(10))
                    .isInstanceOf(NotEnoughStockException.class)
                    .hasMessage("need more stock");
            
            // Verify stock remains unchanged
            assertThat(savedItem.getStockQuantity()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle items with maximum integer values")
        void shouldHandleItemsWithMaximumIntegerValues() {
            // Given
            testBook.setPrice(Integer.MAX_VALUE);
            testBook.setStockQuantity(Integer.MAX_VALUE);

            // When
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem.getPrice()).isEqualTo(Integer.MAX_VALUE);
            assertThat(savedItem.getStockQuantity()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should handle items with minimum integer values")
        void shouldHandleItemsWithMinimumIntegerValues() {
            // Given
            testBook.setPrice(0);
            testBook.setStockQuantity(0);

            // When
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem.getPrice()).isEqualTo(0);
            assertThat(savedItem.getStockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle very long item names")
        void shouldHandleVeryLongItemNames() {
            // Given
            String longName = "A".repeat(255);
            testBook.setName(longName);

            // When
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem.getName()).isEqualTo(longName);
            assertThat(savedItem.getName()).hasSize(255);
        }

        @Test
        @DisplayName("Should handle concurrent modifications gracefully")
        void shouldHandleConcurrentModificationsGracefully() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            
            // Simulate concurrent access
            Optional<Item> item1 = itemRepositoryV2.findById(savedItem.getId());
            Optional<Item> item2 = itemRepositoryV2.findById(savedItem.getId());

            // When
            item1.get().setName("Updated by User 1");
            item2.get().setName("Updated by User 2");

            itemRepositoryV2.save(item1.get());
            
            // Then - Second save should work (last write wins)
            assertThatCode(() -> itemRepositoryV2.save(item2.get()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should maintain referential integrity after flush and clear")
        void shouldMaintainReferentialIntegrityAfterFlushAndClear() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long itemId = savedItem.getId();

            // When
            em.flush();
            em.clear(); // Clear first-level cache

            // Then
            Optional<Item> retrievedItem = itemRepositoryV2.findById(itemId);
            assertThat(retrievedItem).isPresent();
            assertThat(retrievedItem.get().getId()).isEqualTo(itemId);
            assertThat(retrievedItem.get().getName()).isEqualTo("Test Book");
        }
    }

    @Nested
    @DisplayName("Transaction and Persistence Tests")
    class TransactionAndPersistenceTests {

        @Test
        @DisplayName("Should persist changes within transaction")
        void shouldPersistChangesWithinTransaction() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            String originalName = savedItem.getName();

            // When
            savedItem.setName("Updated Name");
            itemRepositoryV2.save(savedItem);
            em.flush(); // Force synchronization

            // Then
            em.clear(); // Clear persistence context
            Optional<Item> retrievedItem = itemRepositoryV2.findById(savedItem.getId());
            assertThat(retrievedItem).isPresent();
            assertThat(retrievedItem.get().getName()).isEqualTo("Updated Name");
            assertThat(retrievedItem.get().getName()).isNotEqualTo(originalName);
        }

        @Test
        @DisplayName("Should handle entity state transitions")
        void shouldHandleEntityStateTransitions() {
            // Given - Transient state
            assertThat(testBook.getId()).isNull();

            // When - Persistent state
            Item savedItem = itemRepositoryV2.save(testBook);

            // Then
            assertThat(savedItem.getId()).isNotNull();
            assertThat(em.contains(savedItem)).isTrue();

            // When - Detached state
            em.detach(savedItem);

            // Then
            assertThat(em.contains(savedItem)).isFalse();
            
            // When - Merge back
            Item mergedItem = itemRepositoryV2.save(savedItem);
            
            // Then
            assertThat(em.contains(mergedItem)).isTrue();
        }

        @Test
        @DisplayName("Should handle multiple saves of same entity")
        void shouldHandleMultipleSavesOfSameEntity() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            Long originalId = savedItem.getId();

            // When
            savedItem.setName("First Update");
            Item firstUpdate = itemRepositoryV2.save(savedItem);
            
            savedItem.setName("Second Update");
            Item secondUpdate = itemRepositoryV2.save(savedItem);

            // Then
            assertThat(firstUpdate.getId()).isEqualTo(originalId);
            assertThat(secondUpdate.getId()).isEqualTo(originalId);
            assertThat(secondUpdate.getName()).isEqualTo("Second Update");
            
            // Verify only one entity exists
            assertThat(itemRepositoryV2.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should rollback transaction on exception")
        void shouldRollbackTransactionOnException() {
            // Given
            Item savedItem = itemRepositoryV2.save(testBook);
            long initialCount = itemRepositoryV2.count();

            // When & Then
            assertThatThrownBy(() -> {
                savedItem.removeStock(200); // This should throw NotEnoughStockException
                itemRepositoryV2.save(savedItem);
            }).isInstanceOf(NotEnoughStockException.class);

            // Verify no changes were persisted
            assertThat(itemRepositoryV2.count()).isEqualTo(initialCount);
            
            Optional<Item> retrievedItem = itemRepositoryV2.findById(savedItem.getId());
            assertThat(retrievedItem).isPresent();
            assertThat(retrievedItem.get().getStockQuantity()).isEqualTo(100); // Original value
        }
    }

    @Nested
    @DisplayName("Inheritance and Polymorphism Tests")
    class InheritanceAndPolymorphismTests {

        @Test
        @DisplayName("Should save and retrieve different item types correctly")
        void shouldSaveAndRetrieveDifferentItemTypesCorrectly() {
            // Given
            List<Item> items = Arrays.asList(testBook, testAlbum, testMovie);

            // When
            List<Item> savedItems = itemRepositoryV2.saveAll(items);

            // Then
            assertThat(savedItems).hasSize(3);
            
            // Verify polymorphism - all saved as Item but maintain their specific types
            assertThat(savedItems.stream().anyMatch(item -> item instanceof Book)).isTrue();
            assertThat(savedItems.stream().anyMatch(item -> item instanceof Album)).isTrue();
            assertThat(savedItems.stream().anyMatch(item -> item instanceof Movie)).isTrue();
        }

        @Test
        @DisplayName("Should handle mixed queries on inheritance hierarchy")
        void shouldHandleMixedQueriesOnInheritanceHierarchy() {
            // Given
            itemRepositoryV2.save(testBook);
            itemRepositoryV2.save(testAlbum);
            itemRepositoryV2.save(testMovie);

            // When
            List<Item> allItems = itemRepositoryV2.findAll();

            // Then
            assertThat(allItems).hasSize(3);
            
            // Verify each type maintains its specific properties
            Book foundBook = (Book) allItems.stream()
                    .filter(item -> item instanceof Book)
                    .findFirst().orElse(null);
            assertThat(foundBook).isNotNull();
            assertThat(foundBook.getAuthor()).isEqualTo("Test Author");

            Album foundAlbum = (Album) allItems.stream()
                    .filter(item -> item instanceof Album)
                    .findFirst().orElse(null);
            assertThat(foundAlbum).isNotNull();
            assertThat(foundAlbum.getArtist()).isEqualTo("Test Artist");

            Movie foundMovie = (Movie) allItems.stream()
                    .filter(item -> item instanceof Movie)
                    .findFirst().orElse(null);
            assertThat(foundMovie).isNotNull();
            assertThat(foundMovie.getDirector()).isEqualTo("Test Director");
        }

        @Test
        @DisplayName("Should maintain discriminator values correctly")
        void shouldMaintainDiscriminatorValuesCorrectly() {
            // Given & When
            Item savedBook = itemRepositoryV2.save(testBook);
            Item savedAlbum = itemRepositoryV2.save(testAlbum);
            Item savedMovie = itemRepositoryV2.save(testMovie);

            // Force flush to database and clear persistence context
            em.flush();
            em.clear();

            // Then - Retrieve and verify types are maintained
            Optional<Item> retrievedBook = itemRepositoryV2.findById(savedBook.getId());
            Optional<Item> retrievedAlbum = itemRepositoryV2.findById(savedAlbum.getId());
            Optional<Item> retrievedMovie = itemRepositoryV2.findById(savedMovie.getId());

            assertThat(retrievedBook).isPresent();
            assertThat(retrievedBook.get()).isInstanceOf(Book.class);

            assertThat(retrievedAlbum).isPresent();
            assertThat(retrievedAlbum.get()).isInstanceOf(Album.class);

            assertThat(retrievedMovie).isPresent();
            assertThat(retrievedMovie.get()).isInstanceOf(Movie.class);
        }
    }
}