package ru.practicum.repository.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.comment.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    List<Comment> findAllByAuthorIdAndEventId(Long userId, Long eventId, Pageable pageable);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);
}
