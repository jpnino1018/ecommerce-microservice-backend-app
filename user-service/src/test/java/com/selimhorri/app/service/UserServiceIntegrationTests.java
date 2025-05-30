package com.selimhorri.app.service;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;
    private User user;

    private static final String DEFAULT_FIRST_NAME = "Lucas";
    private static final String DEFAULT_LAST_NAME = "Mendoza";
    private static final String DEFAULT_EMAIL = "lucas.mendoza@demo.com";
    private static final String DEFAULT_PHONE = "5551234567";
    private static final String DEFAULT_IMAGE_URL = "http://demo.com/lucas.jpg";
    private static final String DEFAULT_USERNAME = "lucasm";
    private static final String DEFAULT_PASSWORD = "lucasSecure123";

    private static final String UPDATED_FIRST_NAME = "Camila";
    private static final String UPDATED_LAST_NAME = "Ramirez";
    private static final String UPDATED_EMAIL = "camila.ramirez@demo.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        CredentialDto credentialDto = CredentialDto.builder()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userDto = UserDto.builder()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .credentialDto(credentialDto)
                .build();

        Credential credential = Credential.builder()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user = User.builder()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .email(DEFAULT_EMAIL)
                .phone(DEFAULT_PHONE)
                .imageUrl(DEFAULT_IMAGE_URL)
                .credential(credential)
                .build();

        if (credential != null) {
            credential.setUser(user);
            user.setCredential(credential);
        }
    }

    @Test
    void saveUser_shouldPersistUser() {
        UserDto savedUserDto = userService.save(userDto);

        assertThat(savedUserDto).isNotNull();
        assertThat(savedUserDto.getUserId()).isNotNull();
        assertThat(savedUserDto.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(savedUserDto.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(savedUserDto.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(savedUserDto.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(savedUserDto.getImageUrl()).isEqualTo(DEFAULT_IMAGE_URL);
        assertThat(savedUserDto.getCredentialDto().getUsername()).isEqualTo(DEFAULT_USERNAME);

        Optional<User> repoUser = userRepository.findById(savedUserDto.getUserId());
        assertThat(repoUser).isPresent();
        assertThat(repoUser.get().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void updateUser_shouldModifyExistingUser() {
        User savedEntity = userRepository.saveAndFlush(user);

        UserDto dtoToUpdate = UserDto.builder()
                .userId(savedEntity.getUserId())
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .email(UPDATED_EMAIL)
                .phone(savedEntity.getPhone())
                .imageUrl(savedEntity.getImageUrl())
                .credentialDto(CredentialDto.builder()
                        .credentialId(savedEntity.getCredential().getCredentialId())
                        .username(savedEntity.getCredential().getUsername())
                        .password(savedEntity.getCredential().getPassword())
                        .isEnabled(savedEntity.getCredential().getIsEnabled())
                        .isAccountNonExpired(savedEntity.getCredential().getIsAccountNonExpired())
                        .isAccountNonLocked(savedEntity.getCredential().getIsAccountNonLocked())
                        .isCredentialsNonExpired(savedEntity.getCredential().getIsCredentialsNonExpired())
                        .build())
                .build();

        UserDto updatedDto = userService.update(dtoToUpdate);

        assertThat(updatedDto.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(updatedDto.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(updatedDto.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    void deleteUserById_shouldRemoveUserFromDatabase() {
        User savedEntity = userRepository.saveAndFlush(user);
        Integer userIdToDelete = savedEntity.getUserId();

        assertThat(userRepository.findById(userIdToDelete)).isPresent();

        userService.deleteById(userIdToDelete);

        assertThat(userRepository.findById(userIdToDelete)).isNotPresent();
    }

    @Test
    void findUserById_whenExists_shouldReturnUser() {
        User savedEntity = userRepository.saveAndFlush(user);
        Integer userIdToFind = savedEntity.getUserId();

        UserDto foundDto = userService.findById(userIdToFind);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getUserId()).isEqualTo(userIdToFind);
        assertThat(foundDto.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
    }

    @Test
    void findUserById_whenNotExists_shouldThrowUserObjectNotFoundException() {
        Integer nonExistentId = -999;
        Exception exception = assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findById(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("User with id: " + nonExistentId + " not found");
    }

    @Test
    void findByUsername_whenExists_shouldReturnUser() {
        userRepository.saveAndFlush(user);

        UserDto foundDto = userService.findByUsername(DEFAULT_USERNAME);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getCredentialDto().getUsername()).isEqualTo(DEFAULT_USERNAME);
    }

    @Test
    void findByUsername_whenNotExists_shouldThrowUserObjectNotFoundException() {
        String invalidUsername = "unknown_user";
        Exception exception = assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findByUsername(invalidUsername);
        });

        assertThat(exception.getMessage()).contains("User with username: " + invalidUsername + " not found");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userRepository.saveAndFlush(user); // User 1

        Credential credential2 = Credential.builder()
                .username("carlap")
                .password("pw")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        User user2 = User.builder()
                .firstName("Carla")
                .lastName("Perez")
                .email("carla.perez@example.com")
                .phone("777888999")
                .imageUrl("http://example.com/carla.jpg")
                .credential(credential2)
                .build();

        credential2.setUser(user2);
        userRepository.saveAndFlush(user2); // User 2

        List<UserDto> users = userService.findAll();

        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.stream().anyMatch(u -> u.getFirstName().equals(DEFAULT_FIRST_NAME))).isTrue();
        assertThat(users.stream().anyMatch(u -> u.getFirstName().equals("Carla"))).isTrue();
    }
}
