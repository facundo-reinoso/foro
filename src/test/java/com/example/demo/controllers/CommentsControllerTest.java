package com.example.demo.controllers;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.CommentsCreateDto;
import com.example.demo.dtos.CommentsEditDto;
import com.example.demo.exceptions.CommentNotFoundException;
import com.example.demo.models.CommentsEntity;
import com.example.demo.repository.CommentsRepository;
import com.example.demo.services.CommentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentsControllerTest {
    private static CommentsRepository repository;
    private static CommentsService service;
    private static CommentsController controller;

    private static RequestContext context;
    private static CommentsCreateDto comment;

    @BeforeEach
    void setUpAll() {
        LocalDateTime dateCreated = LocalDateTime.now();
        String userIdContext = "1";
        Collection roles = new ArrayList();
        roles.add("Admin");

        repository = mock(CommentsRepository.class);
        service = new CommentsService(repository);
        context = new RequestContext(dateCreated, userIdContext, roles);
        controller = new CommentsController(service, context);

        comment = new CommentsCreateDto();
        comment.message = "Generic Message";
    }

    @Test
    void createCommentTest() {
        ResponseEntity<String> response = controller.createComment(comment);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createCommentWithDeletedParentTest(){
        CommentsEntity deletedParent = new CommentsEntity();
        deletedParent.setCommentId(1L);
        deletedParent.setParentId(0L);
        deletedParent.setStatus(0L);
        comment.parentId = 1L;

        when(repository.getById(1L)).thenReturn(deletedParent);
        ResponseEntity<String> response = controller.createComment(comment);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createCommentFailTest() {
        comment.message = "";

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CommentsCreateDto>> violations = validator.validate(comment);

        ResponseEntity<String> response;

        if(violations.isEmpty()){
            response = controller.createComment(comment);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createCommentExceptionTest(){
        when(repository.save(any())).thenThrow(new RuntimeException());

        ResponseEntity<String> response = controller.createComment(comment);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }


    @Test
    void listCommentsTest() {
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity comentarioEntity = new CommentsEntity();
        comments.add(comentarioEntity);

        when(repository.getComments(0L)).thenReturn(comments);

        ResponseEntity<?> response = controller.listComments(0L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listCommentsParentWithNestedTest() {
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity commentEntity = new CommentsEntity();
        commentEntity.setCommentId(1L);
        commentEntity.setParentId(0L);
        commentEntity.setStatus(1L);

        CommentsEntity commentEntity2 = new CommentsEntity();
        commentEntity2.setCommentId(2L);
        commentEntity2.setParentId(1L);
        commentEntity2.setStatus(1L);
        comments.add(commentEntity2);

        when(repository.getComments(1L)).thenReturn(comments);
        when(repository.getById(1L)).thenReturn(commentEntity);
        ResponseEntity<?> response = controller.listComments(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listCommentsDeletedParentWithNestedTest() {
        List<CommentsEntity> comments = new ArrayList<>();
        CommentsEntity commentEntity = new CommentsEntity();
        commentEntity.setCommentId(1L);
        commentEntity.setParentId(0L);
        commentEntity.setStatus(0L);

        CommentsEntity commentEntity2 = new CommentsEntity();
        commentEntity2.setCommentId(2L);
        commentEntity2.setParentId(1L);
        commentEntity2.setStatus(1L);
        comments.add(commentEntity2);

        when(repository.getComments(1L)).thenReturn(comments);
        when(repository.getById(1L)).thenReturn(commentEntity);
        ResponseEntity<?> response = controller.listComments(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listCommentsEmptyTest(){
        ResponseEntity<?> response = controller.listComments(null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void listCommentsCommentNotFoundExceptionExceptionTest(){
        when(repository.getById(3L)).thenThrow(new EntityNotFoundException());

        ResponseEntity<?> response = controller.listComments(3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listCommentsExceptionTest(){
        when(repository.getComments(4L)).thenThrow(new RuntimeException());

        ResponseEntity<?> response = controller.listComments(4L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteCommentTest() {
        CommentsEntity commentsEntity = new CommentsEntity();
        commentsEntity.setUserId(1L);
        commentsEntity.setStatus(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(commentsEntity));

        ResponseEntity<String> response = controller.deleteComment(1L);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void deleteDeletedCommentTest() {
        CommentsEntity commentsEntity = new CommentsEntity();
        commentsEntity.setUserId(1L);
        commentsEntity.setStatus(0L);

        when(repository.findById(1L)).thenReturn(Optional.of(commentsEntity));

        ResponseEntity<String> response = controller.deleteComment(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteCommentForbiddenTest() {
        CommentsEntity commentsEntity = new CommentsEntity();
        commentsEntity.setCommentId(1L);
        commentsEntity.setUserId(5L);

        when(repository.findById(1L)).thenReturn(Optional.of(commentsEntity));

        ResponseEntity<String> response = controller.deleteComment(1L);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCodeValue());
    }

    @Test
    void deleteCommentNotFoundTest() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.deleteComment(2L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteCommentExceptionTest(){
        when(repository.findById(3L)).thenThrow(new RuntimeException());

        ResponseEntity<String> response = controller.deleteComment(3L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void modifyCommentTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setUserId(1L);
        comment.setStatus(1L);
        CommentsEditDto editedComment = new CommentsEditDto();

        when(repository.findById(1L)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = controller.modifyComment(1L, editedComment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void modifyDeletedCommentTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setUserId(1L);
        comment.setStatus(0L);
        CommentsEditDto editedComment = new CommentsEditDto();

        when(repository.findById(1L)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = controller.modifyComment(1L, editedComment);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void modifyCommentForbiddenTest() {
        CommentsEntity comment = new CommentsEntity();
        comment.setCommentId(1L);
        comment.setUserId(5L);

        CommentsEditDto editedComment = new CommentsEditDto();

        when(repository.findById(1L)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response = controller.modifyComment(1L, editedComment);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void modifyCommentWithEmptyMessageTest() {
        CommentsEditDto editedComment = new CommentsEditDto();
        editedComment.message = "";

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CommentsEditDto>> violations = validator.validate(editedComment);

        ResponseEntity<String> response;

        if(violations.isEmpty()){
            response = controller.modifyComment(1L, editedComment);
        }else {
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    @Test
    void modifyCommentNotFoundTest() {
        CommentsEditDto editedComment = new CommentsEditDto();
        editedComment.message = "Generic Message 2";

        when(repository.findById(2L)).thenThrow(new CommentNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, controller.modifyComment(2L, editedComment).getStatusCode());
    }

    @Test
    void modifyCommentExceptionTest(){
        CommentsEditDto editedComment = new CommentsEditDto();
        editedComment.message = "Generic Message 2";

        when(repository.findById(3L)).thenThrow(new RuntimeException());
        ResponseEntity<String> response = controller.modifyComment(3L, editedComment);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
