package com.vasav.ecom.searchservice.service;

import com.vasav.ecom.searchservice.dto.SearchFilter;
import com.vasav.ecom.searchservice.dto.SearchResult;
import com.vasav.ecom.searchservice.entity.SearchProduct;
import com.vasav.ecom.searchservice.events.ProductRatingChanged;
import com.vasav.ecom.searchservice.events.ProductUpserted;
import com.vasav.ecom.searchservice.events.StockLevelChanged;
import com.vasav.ecom.searchservice.repository.SearchProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Set<String> SORTABLE = Set.of("name", "price", "averageRating", "reviewCount");

    private final SearchProductRepository repository;

    @Transactional(readOnly = true)
    public Page<SearchResult> search(SearchFilter filter, Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort must be one of " + SORTABLE);
            }
        }
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("name");
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by("productId")));
        return repository.findAll(matching(filter), page).map(SearchResult::from);
    }

    @Transactional
    public void onProduct(ProductUpserted event) {
        repository.upsertProduct(event.productId(), event.name(), event.description(), event.price(),
                event.imageUrl(), event.status());
    }

    @Transactional
    public void onStock(StockLevelChanged event) {
        repository.upsertStock(event.productId(), event.inStock());
    }

    @Transactional
    public void onRating(ProductRatingChanged event) {
        repository.upsertRating(event.productId(), event.averageRating(), event.reviewCount());
    }

    private static Specification<SearchProduct> matching(SearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));
            if (filter.q() != null && !filter.q().isBlank()) {
                String pattern = "%" + filter.q().trim().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, '\\'),
                        cb.like(cb.lower(root.get("description")), pattern, '\\')));
            }
            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.inStock() != null) {
                predicates.add(cb.equal(root.get("inStock"), filter.inStock()));
            }
            if (filter.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), filter.minRating()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
