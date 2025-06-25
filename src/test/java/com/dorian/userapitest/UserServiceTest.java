package com.dorian.userapitest;

import com.dorian.userapitest.dto.UserDto;
import com.dorian.userapitest.entity.User;
import com.dorian.userapitest.exception.DataIntegrityViolationException;
import com.dorian.userapitest.exception.ObjectNotFoundException;
import com.dorian.userapitest.repository.UserRepository;
import com.dorian.userapitest.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User anotherUser;
    private UserDto userDto;
    private UserDto anotherUserDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("1234")
                .build();

        anotherUser = User.builder()
                .id(2L)
                .name("Jane Roe")
                .email("jane@example.com")
                .password("abcd")
                .build();

        userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        anotherUserDto = UserDto.builder()
                .name("Jane Roe")
                .email("jane@example.com")
                .build();
    }

    @Test
    void createUser_shouldSucceed_whenEmailIsNew() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.createUser(userDto);

        assertEquals(userDto.getName(), created.getName());
        assertEquals(userDto.getEmail(), created.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyUsed() {
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getUserById(1L);

        assertEquals("John Doe", found.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_shouldSucceed_whenDataValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anotherUserDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(anotherUser);

        UserDto updated = userService.updateUser(1L, anotherUserDto);

        assertEquals("Jane Roe", updated.getName());
        assertEquals("jane@example.com", updated.getEmail());
    }

    @Test
    void updateUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> userService.updateUser(99L, anotherUserDto));
    }

    @Test
    void updateUser_shouldThrow_whenNewEmailAlreadyUsed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anotherUserDto.getEmail())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.updateUser(1L, anotherUserDto));
    }

    @Test
    void deleteUser_shouldSucceed_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrow_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user, anotherUser));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("john@example.com")));
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("jane@example.com")));
    }
}