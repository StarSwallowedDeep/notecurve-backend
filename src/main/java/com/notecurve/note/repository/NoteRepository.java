package com.notecurve.note.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.notecurve.note.domain.Note;
import com.notecurve.user.domain.User;
import com.notecurve.category.domain.Category;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("""
           SELECT DISTINCT n FROM Note n
           LEFT JOIN FETCH n.category c
           LEFT JOIN FETCH c.user
           WHERE n.user = :user
           """)
    List<Note> findAllByUserWithCategoryAndUser(@Param("user") User user);

    @Query("""
           SELECT DISTINCT n FROM Note n
           LEFT JOIN FETCH n.category c
           LEFT JOIN FETCH c.user
           WHERE n.id = :id AND n.user = :user
           """)
    Optional<Note> findByIdAndUserWithCategoryAndUser(
            @Param("id") Long id,
            @Param("user") User user
    );

    @Query("""
           SELECT DISTINCT n FROM Note n
           LEFT JOIN FETCH n.category c
           LEFT JOIN FETCH c.user
           WHERE n.category = :category
           """)
    List<Note> findByCategoryWithFetch(@Param("category") Category category);
}
