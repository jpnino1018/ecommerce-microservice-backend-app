package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User user;
    private UserDto userDto;
    private Credential credential;
    private CredentialDto credentialDto;
    
    private static final String DEFAULT_FIRST_NAME = "John";
    private static final String DEFAULT_LAST_NAME = "Doe";
    private static final String DEFAULT_EMAIL = "john.doe@example.com";
    private static final String DEFAULT_PHONE = "1234567890";
    private static final String DEFAULT_IMAGE_URL = "http://example.com/image.jpg";
    private static final String DEFAULT_USERNAME = "johndoe";
    private static final String DEFAULT_PASSWORD = "password123";
    
    @BeforeEach
    void setup() {
        Instant now = Instant.now();
        
        credentialDto = CredentialDto.builder()
            .credentialId(1)
            .username(DEFAULT_USERNAME)
            .password(DEFAULT_PASSWORD)
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();

        credential = Credential.builder()
            .credentialId(1)
            .username(DEFAULT_USERNAME)
            .password(DEFAULT_PASSWORD)
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();

        user = User.builder()
            .userId(1)
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .imageUrl(DEFAULT_IMAGE_URL)
            .email(DEFAULT_EMAIL)
            .phone(DEFAULT_PHONE)
            .credential(credential)
            .build();
            
        userDto = UserDto.builder()
            .userId(1)
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .imageUrl(DEFAULT_IMAGE_URL)
            .email(DEFAULT_EMAIL)
            .phone(DEFAULT_PHONE)
            .credentialDto(credentialDto)
            .build();
            
        // Set up bidirectional relationship
        credential.setUser(user);
        
        // Set audit fields
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
    }
    
    @Test
    void findAll_shouldReturnListOfUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        
        // Act
        List<UserDto> result = userService.findAll();
        
        // Assert
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Should return one user");
        assertEquals(DEFAULT_FIRST_NAME, result.get(0).getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.get(0).getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.get(0).getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.get(0).getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.get(0).getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void findById_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.findById(1);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getUserId(), "User ID should match");
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findById(1);
    }
    
    @Test
    void findById_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UserObjectNotFoundException.class, () -> 
            userService.findById(999),
            "Should throw UserObjectNotFoundException"
        );
        verify(userRepository, times(1)).findById(999);
    }
    
    @Test
    void save_shouldReturnSavedUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // Act
        UserDto result = userService.save(userDto);
        
        // Assert
        assertNotNull(result, "Saved user should not be null");
        assertEquals(userDto.getUserId(), result.getUserId(), "User ID should match");
        assertEquals(userDto.getFirstName(), result.getFirstName(), "First name should match");
        assertEquals(userDto.getEmail(), result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findByCredentialUsername(DEFAULT_USERNAME)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.findByUsername(DEFAULT_USERNAME);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findByCredentialUsername(DEFAULT_USERNAME);
    }
    
    @Test 
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        Integer userId = 1;
        doNothing().when(userRepository).deleteById(userId);
        
        // Act
        userService.deleteById(userId);
        
        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
}






