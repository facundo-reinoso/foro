package com.example.demo.services;

import com.example.demo.dtos.CommentsResponseDto;
import com.example.demo.exceptions.CommentNotFoundException;
import com.example.demo.exceptions.RepositoryException;
import com.example.demo.models.CommentsEntity;
import com.example.demo.repository.CommentsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentsService {

    private static final Logger log = LoggerFactory.getLogger(CommentsService.class);

    private final CommentsRepository commentsRepository;

    @Autowired
    public CommentsService(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    public void createComment(CommentsEntity comment) {
        log.debug("Comments Service: create a comment");
        try {
            if( (!comment.getParentId().equals(0L)) && commentsRepository.getById(comment.getParentId()).getStatus().equals(0L)){
                throw new CommentNotFoundException();
            }
            commentsRepository.save(comment);
        }catch(CommentNotFoundException e){
            log.error("Comments Service: Parent comment not found");
            throw new CommentNotFoundException();
        }catch(RuntimeException e){
            log.error("Comments Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public List<CommentsResponseDto> listComments(Long parentId) {
        log.debug("Comments Service: List comments");
        try{
            if(parentId==null){
                parentId=0L;
            }

            List<CommentsEntity> comments = commentsRepository.getComments(parentId);

            if(parentId!=0){
                CommentsEntity parent = commentsRepository.getById(parentId);
                if(parent.getStatus().equals(1L)){
                    comments.add(0, parent);
                }else{
                    throw new EntityNotFoundException();
                }
            }

            List<CommentsResponseDto> foundedComments = new ArrayList<>();

            for (CommentsEntity i : comments) {
                CommentsResponseDto responseComment = new CommentsResponseDto();
                responseComment.commentId = i.getCommentId();
                responseComment.userId = i.getUserId();
                responseComment.parentId = i.getParentId();
                responseComment.message = i.getMessage();
                responseComment.creationDate = i.getCreationDate();
                responseComment.status = i.getStatus();

                foundedComments.add(responseComment);
            }

            return foundedComments;
        }catch(EntityNotFoundException e) {
            log.error("Comments Service: The comment with id: "+ parentId +" does not exist");
            throw new CommentNotFoundException();
        }catch(RuntimeException e){
            log.error("Comments Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public Optional<CommentsEntity> getCommentById(Long id){
        log.debug("Comments Service: Find a comment with id: "+ id);
        try{
            Optional<CommentsEntity> founded = commentsRepository.findById(id);

            if(founded.isEmpty()){
                throw new CommentNotFoundException();
            }

            return founded;
        }catch(CommentNotFoundException e){
            log.error("Comments Service: The comment with id: "+ id +"does not exist");
            throw new CommentNotFoundException();
        }catch(RuntimeException e){
            log.error("Comments Service: Error from Repository");
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void deleteComment(@PathVariable("id") Long id) {
        log.debug("Comments Service: Delete a comment with id: "+ id);
        Optional<CommentsEntity> founded = commentsRepository.findById(id);

        if(founded.isPresent() && founded.get().getStatus().equals(1L)) {
            commentsRepository.deleteCommentsById(id);
        }else{
            throw new CommentNotFoundException();
        }
    }

    public void modifyComment(Long id, String message) {
        log.debug("Comments Service: Modify a comment with id: "+ id);
        Optional<CommentsEntity> foundedComment = commentsRepository.findById(id);

        if (foundedComment.isPresent() && foundedComment.get().getStatus().equals(1L)) {
            CommentsEntity commentToModify = foundedComment.get();
            commentToModify.setMessage(message);
            commentsRepository.save(commentToModify);
        }else{
            throw new CommentNotFoundException();
        }
    }
}
