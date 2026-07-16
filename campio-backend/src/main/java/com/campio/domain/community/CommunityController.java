package com.campio.domain.community;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommunityController {

  private final CommunityService communityService;

  @GetMapping
  public List<CommunityPostResponse> list(HttpSession session) {
    return communityService.listPosts(session);
  }

  @GetMapping("/mine")
  public List<CommunityPostResponse> mine(HttpSession session) {
    return communityService.myPosts(session);
  }

  @GetMapping("/{id}")
  public CommunityPostResponse detail(@PathVariable Long id, HttpSession session) {
    return communityService.detail(id, session);
  }

  @PostMapping
  public CommunityPostResponse create(@Valid @RequestBody CommunityPostRequest request, HttpSession session) {
    return communityService.create(request, session);
  }

  @PatchMapping("/{id}")
  public CommunityPostResponse update(
      @PathVariable Long id, @Valid @RequestBody CommunityPostRequest request, HttpSession session) {
    return communityService.update(id, request, session);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, HttpSession session) {
    communityService.delete(id, session);
  }

  @PostMapping("/{id}/comments")
  public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request, HttpSession session) {
    return communityService.addComment(id, request, session);
  }

  @GetMapping("/{id}/comments")
  public List<CommentResponse> comments(@PathVariable Long id, HttpSession session) {
    return communityService.comments(id, session);
  }

  @DeleteMapping("/{id}/comments/{commentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteComment(
      @PathVariable Long id, @PathVariable Long commentId, HttpSession session) {
    communityService.deleteComment(id, commentId, session);
  }

  @PostMapping("/{id}/save")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@PathVariable Long id, HttpSession session) {
    communityService.save(id, session);
  }

  @DeleteMapping("/{id}/save")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unsave(@PathVariable Long id, HttpSession session) {
    communityService.unsave(id, session);
  }
}
