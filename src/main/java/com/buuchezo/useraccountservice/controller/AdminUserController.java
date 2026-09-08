package com.buuchezo.useraccountservice.controller;

import com.buuchezo.useraccountservice.dto.ApiResponse;
import com.buuchezo.useraccountservice.dto.UserDto;
import com.buuchezo.useraccountservice.dto.UserStatisticsDto;
import com.buuchezo.useraccountservice.dto.UserWithAccountDto;
import com.buuchezo.useraccountservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserWithAccountDto>> search(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String accountNumber) {
        return ResponseEntity.ok(userService.searchUser(email, accountNumber));
    }


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @RequestParam(required = false) String roleName,
            @PageableDefault(page = 0, size = 100) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getAllUsers(roleName, pageable));
    }


    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatisticsDto>> getStatistics() {
        return ResponseEntity.ok(userService.getEntireUserStatistics());
    }

    @PatchMapping("/toggle-status/{id}")
    public ResponseEntity<ApiResponse<String>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));

    }


}
