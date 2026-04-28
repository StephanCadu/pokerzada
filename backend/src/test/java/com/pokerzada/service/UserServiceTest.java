package com.pokerzada.service;

import com.pokerzada.entity.User;
import com.pokerzada.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "password123");
        testUser.setId(1L);
        testUser.setActive(true);
        testUser.setDeleted(false);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateUserSuccess() {
        User newUser = new User("Jane Doe", "jane@example.com", "password456");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User createdUser = userService.create(newUser);

        assertNotNull(createdUser);
        assertEquals("Jane Doe", createdUser.getName());
        assertEquals("jane@example.com", createdUser.getEmail());
        assertTrue(createdUser.getActive());
        assertFalse(createdUser.getDeleted());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUserWithBlankNameThrowsException() {
        User newUser = new User("", "jane@example.com", "password456");

        assertThrows(IllegalArgumentException.class, () -> userService.create(newUser));
    }

    @Test
    void testCreateUserWithBlankEmailThrowsException() {
        User newUser = new User("Jane Doe", "", "password456");

        assertThrows(IllegalArgumentException.class, () -> userService.create(newUser));
    }

    @Test
    void testCreateUserWithInvalidEmailThrowsException() {
        User newUser = new User("Jane Doe", "invalid-email", "password456");

        assertThrows(IllegalArgumentException.class, () -> userService.create(newUser));
    }

    @Test
    void testCreateUserWithExistingEmailThrowsException() {
        User newUser = new User("Jane Doe", "john@example.com", "password456");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.create(newUser));
    }

    @Test
    void testCreateUserWithBlankPasswordThrowsException() {
        User newUser = new User("Jane Doe", "jane@example.com", "");

        assertThrows(IllegalArgumentException.class, () -> userService.create(newUser));
    }

    @Test
    void testUpdateUserSuccess() {
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setEmail("newemail@example.com");
        updateData.setPassword("newpassword");

        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.update(1L, updateData);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("newemail@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserNotFoundThrowsException() {
        User updateData = new User();
        updateData.setName("Updated Name");

        when(userRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.update(999L, updateData));
    }

    @Test
    void testUpdateUserWithInvalidEmailThrowsException() {
        User updateData = new User();
        updateData.setEmail("invalid-email");

        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.update(1L, updateData));
    }

    @Test
    void testUpdateUserWithExistingEmailThrowsException() {
        User otherUser = new User("Other User", "other@example.com", "password");
        User updateData = new User();
        updateData.setEmail("other@example.com");

        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(IllegalArgumentException.class, () -> userService.update(1L, updateData));
    }

    @Test
    void testGetUserSuccess() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));

        Optional<User> user = userService.get(1L);

        assertTrue(user.isPresent());
        assertEquals("John Doe", user.get().getName());
        assertEquals("john@example.com", user.get().getEmail());
    }

    @Test
    void testGetUserNotFound() {
        when(userRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

        Optional<User> user = userService.get(999L);

        assertFalse(user.isPresent());
    }

    @Test
    void testListUsersSuccess() {
        User user2 = new User("Jane Doe", "jane@example.com", "password456");
        user2.setId(2L);
        user2.setActive(true);
        user2.setDeleted(false);

        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAllActive()).thenReturn(users);

        List<User> result = userService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Doe", result.get(1).getName());
    }

    @Test
    void testListUsersEmpty() {
        when(userRepository.findAllActive()).thenReturn(Arrays.asList());

        List<User> result = userService.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToggleActiveUserFromActiveToInactive() {
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        testUser.setActive(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User toggledUser = userService.toggleActive(1L);

        assertNotNull(toggledUser);
        assertFalse(toggledUser.getActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testToggleActiveUserFromInactiveToActive() {
        testUser.setActive(false);
        when(userRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User toggledUser = userService.toggleActive(1L);

        assertNotNull(toggledUser);
        assertTrue(toggledUser.getActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testToggleActiveUserNotFoundThrowsException() {
        when(userRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.toggleActive(999L));
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.delete(1L);

        assertTrue(testUser.getDeleted());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUserNotFoundThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.delete(999L));
    }

}
