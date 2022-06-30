package com.example.demo.controllers;

import com.example.demo.context.RequestContext;
import com.example.demo.dtos.CommentsCreateDto;
import com.example.demo.dtos.CommentsEditDto;
import com.example.demo.dtos.CommentsResponseDto;
import com.example.demo.exceptions.CommentNotFoundException;
import com.example.demo.exceptions.RepositoryException;
import com.example.demo.models.CommentsEntity;
import com.example.demo.services.CommentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/comments")
@Validated
public class CommentsController {

    private static final Logger log = LoggerFactory.getLogger(CommentsController.class);
    private final RequestContext context;
    private final CommentsService commentsService;

    @Autowired
    public CommentsController(CommentsService commentsService, RequestContext context) {
        this.commentsService = commentsService;
        this.context = context;
    }

    @PostMapping("")
    public ResponseEntity<String> createComment(@Valid @RequestBody CommentsCreateDto commentsDto) {
        log.info("Request from user: "+ context.getUserId() +" to create a new comment");

        try {
            if(commentsDto.parentId == null){
                commentsDto.parentId=0L;
            }

            CommentsEntity comment = new CommentsEntity();

            comment.setUserId(Long.valueOf(context.getUserId()));
            comment.setParentId(commentsDto.parentId);
            comment.setMessage(commentsDto.message);
            comment.setCreationDate(LocalDateTime.now());
            comment.setStatus(1L);

            commentsService.createComment(comment);
            return new ResponseEntity<>(HttpStatus.OK);

        }catch(CommentNotFoundException e) {
            log.error("The parent comment with id: "+ commentsDto.parentId +" does not exist");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch(RepositoryException e) {
            log.error("Error from repository");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("")
    public ResponseEntity<?> listComments(@RequestParam(value = "parentId", required = false) Long parentId) {
        log.info("Request from user: "+ context.getUserId() +" to list comments");

        try {
            List<CommentsResponseDto> list = commentsService.listComments(parentId);

            if(!list.isEmpty()){
                return new ResponseEntity<>(list, HttpStatus.OK);
            }else{
                log.info("Empty list");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

        }catch(CommentNotFoundException e){
            log.error("The comment with id: "+ parentId +" does not exist");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (RepositoryException e) {
            log.error("Error from repository");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable("id") Long id) {
        log.info("Request from user: "+ context.getUserId() +" to delete the comment with id: "+ id);

        try {
            Long commentUserId = commentsService.getCommentById(id).get().getUserId();

            if( (commentUserId.equals(Long.valueOf(context.getUserId()))) || context.hasRole("Admin") ){
                commentsService.deleteComment(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        }catch(CommentNotFoundException e){
            log.error("The comment with id: "+ id +"does not exist");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (RepositoryException e) {
            log.error("Error from repository");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> modifyComment(@PathVariable("id") Long id, @Valid @RequestBody CommentsEditDto commentsDto) {
        log.info("Request from user: "+ context.getUserId() +" to modify the comment with id: "+ id);

        try {
            Long commentUserId = commentsService.getCommentById(id).get().getUserId();

            if(commentUserId.equals(Long.valueOf(context.getUserId()))){
                commentsService.modifyComment(id, commentsDto.message);
                return new ResponseEntity<>(HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        }catch(CommentNotFoundException e){
            log.error("The comment with id: "+ id +"does not exist");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (RepositoryException e) {
            log.error("Error from repository");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
