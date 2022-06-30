package com.example.demo.services;

import com.example.demo.exceptions.CommentNotFoundException;
import com.example.demo.models.CommentsEntity;
import com.example.demo.repository.CommentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentsServiceTest {
    private static CommentsRepository repository;
    private static CommentsService service;

    @BeforeEach
    void setUp() {
        repository = mock(CommentsRepository.class);
        service = new CommentsService(repository);
    }

    @Test
    void getCommentByIdTest(){
        CommentsEntity comment = new CommentsEntity();
        comment.setCommentId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(comment));
        Optional<CommentsEntity> commentFounded = service.getCommentById(1L);

        assertEquals(comment.getCommentId(), commentFounded.get().getCommentId());
    }

    @Test
    void getCommentByIdNotFoundTest(){
        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CommentNotFoundException.class, ()-> service.getCommentById(2L));
    }

    @Test
    void getCommentByIdException(){
        when(repository.findById(3L)).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, ()-> service.getCommentById(3L));
    }

    @Test
    void createCommentTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setParentId(0L);

        service.createComment(comment);
        verify(repository).save(comment);
    }

    @Test
    void createCommentFailTest(){
        CommentsEntity comment = new CommentsEntity();

        when(repository.save(comment)).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, ()->service.createComment(comment));
    }

    @Test
    void createCommentWithNotFoundParentTest(){
        CommentsEntity deletedComment = new CommentsEntity();
        deletedComment.setCommentId(1L);
        deletedComment.setStatus(0L);

        CommentsEntity comment = new CommentsEntity();
        comment.setParentId(1L);

        when(repository.getById(1L)).thenReturn(deletedComment);
        assertThrows(CommentNotFoundException.class, ()-> service.createComment(comment));
    }

    @Test
    void listCommentsTest() {
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity comment = new CommentsEntity();
        comment.setCommentId(1L);
        comment.setUserId(1L);
        comment.setParentId(0L);
        comment.setMessage("Hello!");
        comment.setCreationDate(LocalDateTime.of(2022, 5, 24, 17, 15));
        comments.add(comment);

        when(repository.getComments(0L)).thenReturn(comments);
        assertEquals(1, service.listComments(0L).size());
        verify(repository).getComments(0L);
    }

    @Test
    void listCommentsParentWithNestedTest() {
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity comment1 = new CommentsEntity();
        comment1.setCommentId(1L);
        comment1.setParentId(0L);
        comment1.setStatus(1L);

        CommentsEntity comment2 = new CommentsEntity();
        comment2.setCommentId(2L);
        comment2.setParentId(1L);
        comment2.setStatus(1L);
        comments.add(comment2);

        when(repository.getComments(1L)).thenReturn(comments);
        when(repository.getById(1L)).thenReturn(comment1);
        assertEquals(2, service.listComments(1L).size());
        verify(repository).getComments(1L);
    }

    @Test
    void listCommentsEmptyTest(){
        List<CommentsEntity> comments = new ArrayList<>();

        when(repository.getComments(null)).thenReturn(comments);
        assertEquals(0, service.listComments(null).size());
    }

    @Test
    void listCommentsEntityNotFoundExceptionTest() {
        when(repository.getById(3L)).thenThrow(new EntityNotFoundException());
        assertThrows(RuntimeException.class, ()->service.listComments(3L));
    }

    @Test
    void listCommentsExceptionTest() {
        when(repository.getComments(4L)).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, ()->service.listComments(4L));
    }

    @Test
    void listCommentsDeletedParentTest(){
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity comment1 = new CommentsEntity();
        comment1.setCommentId(1L);
        comment1.setParentId(0L);
        comment1.setStatus(0L);

        CommentsEntity comment2 = new CommentsEntity();
        comment2.setCommentId(2L);
        comment2.setParentId(1L);
        comment2.setStatus(0L);
        comments.add(comment2);

        when(repository.getComments(1L)).thenReturn(comments);
        when(repository.getById(1L)).thenReturn(comment1);
        assertThrows(CommentNotFoundException.class, ()-> service.listComments(1L));
    }

    @Test
    void deleteCommentNotFoundTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setStatus(0L);

        when(repository.findById(1L)).thenReturn(Optional.of(comment));
        assertThrows(CommentNotFoundException.class, ()-> service.deleteComment(1L));
    }

    @Test
    void modifyCommentTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setStatus(1L);
        String message = "Modify comment";

        when(repository.findById(1L)).thenReturn(Optional.of(comment));
        service.modifyComment(1L, "Modify comment");
        assertEquals(message, comment.getMessage());
        verify(repository).save(comment);
    }

    @Test
    void modifyCommentNotFoundTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setStatus(0L);
        String message = "Modify comment";

        when(repository.findById(1L)).thenReturn(Optional.of(comment));
        assertThrows(CommentNotFoundException.class, ()-> service.modifyComment(1L, message));
    }

}
