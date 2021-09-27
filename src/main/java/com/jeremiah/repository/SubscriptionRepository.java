package com.jeremiah.repository;

import com.jeremiah.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    @Query(value = "SELECT * FROM subscriptions WHERE topic LIKE '%' || :topic || '%' AND url LIKE '%' || :url || '%' ",
            countQuery = "SELECT count(*) FROM subscriptions WHERE topic LIKE '%' || :topic || '%' AND url LIKE '%' || :url || '%' ",
            nativeQuery = true)
    Page<Subscription> findAllByTopicAndUrl(String topic, String url, Pageable pageable);

    @Query(value = "SELECT * FROM subscriptions WHERE topic = :topic ",
            nativeQuery = true)
    List<Subscription> findAllByTopicUnPaged(String topic);

    @Query(value = "SELECT * FROM subscriptions WHERE topic = :topic AND url = :url LIMIT 1 ",
            nativeQuery = true)
    Optional<Subscription> getByTopicAndUrl(String topic, String url);

}
