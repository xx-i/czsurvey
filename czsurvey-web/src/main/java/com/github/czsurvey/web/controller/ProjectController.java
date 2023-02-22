package com.github.czsurvey.web.controller;

import com.github.czsurvey.common.payload.ApiResponse;
import com.github.czsurvey.common.util.HeaderUtil;
import com.github.czsurvey.common.util.PaginationUtil;
import com.github.czsurvey.project.entity.Project;
import com.github.czsurvey.project.request.MoveProjectRequest;
import com.github.czsurvey.project.request.ProjectRequest;
import com.github.czsurvey.project.response.ProjectResponse;
import com.github.czsurvey.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author YanYu
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/folder")
    public ResponseEntity<?> createFolder(@Valid @RequestBody FolderNameRequest folderNameRequest) throws URISyntaxException {
        Project result = projectService.createFolder(folderNameRequest.folderName());
        return ResponseEntity.created(new URI("/api/project/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(true, Project.entityName(), result.getId().toString()))
            .body(new ApiResponse(true, "文件夹创建成功"));
    }

    @PutMapping("/folder/{folderId}/name")
    public ResponseEntity<?> renameFolder(@PathVariable Long folderId, @Valid @RequestBody FolderNameRequest folderNameRequest) {
        Project project = projectService.renameFolder(folderId, folderNameRequest.folderName());
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(true, project.getName(), project.getId().toString()))
            .body(new ApiResponse(true, "文件夹名修改成功"));

    }

    @GetMapping("/page/mine")
    public ResponseEntity<?> pageProject(ProjectRequest projectRequest, Pageable pageable) {
        Page<ProjectResponse> page = projectService.pageMyProject(projectRequest, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/myFolder")
    public List<Project> getMyFolder() {
        return projectService.listMyFolder();
    }

    @GetMapping("/myAllFolder")
    public List<Project> getMyAllFolder() {
        return projectService.listMyAllFolder();
    }

    @DeleteMapping("/{projectId}/moveToTrash")
    public ResponseEntity<?> moveToTrash(@PathVariable Long projectId) {
        projectService.moveToTrash(projectId);
        return ResponseEntity.ok(new ApiResponse(true, "操作成功"));
    }

    @PutMapping("/move")
    public ResponseEntity<?> moveProject(@Valid @RequestBody MoveProjectRequest moveProjectRequest) {
        projectService.moveProject(moveProjectRequest);
        return ResponseEntity.ok(new ApiResponse(true, "操作成功"));
    }

    @PutMapping("/recover/{projectId}")
    public ResponseEntity<?> recoverProject(@PathVariable Long projectId) {
        projectService.recoverProject(projectId);
        return ResponseEntity.ok(new ApiResponse(true, "操作成功"));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityDeletionAlert(true, Project.entityName(), projectId.toString()))
            .build();
    }

    public record FolderNameRequest(@NotBlank @Size(min = 1, max = 20) String folderName) {}
}
