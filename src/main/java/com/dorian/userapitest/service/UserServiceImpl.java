package com.dorian.userapitest.service;

import com.dorian.userapitest.dto.UserDto;
import com.dorian.userapitest.entity.User;
import com.dorian.userapitest.exception.ObjectNotFoundException;
import com.dorian.userapitest.exception.DataIntegrityViolationException;
import com.dorian.userapitest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Injection via constructeur
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /users
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // GET /users/{id}
    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Utilisateur introuvable avec l'id : " + id));
        return toDto(user);
    }

    // POST /users
    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DataIntegrityViolationException("L'email est déjà utilisé : " + userDto.getEmail());
        }

        User user = toEntity(userDto);
        // ⚠️ Tu dois gérer le mot de passe dans la vraie entité
        user.setPassword("motDePasseParDéfaut"); // à remplacer

        return toDto(userRepository.save(user));
    }

    // PUT /users/{id}
    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Utilisateur introuvable avec l'id : " + id));

        // Si l’email a changé, vérifier qu’il n’existe pas déjà
        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new DataIntegrityViolationException("L'email est déjà utilisé : " + userDto.getEmail());
        }

        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        // Ne pas modifier le mot de passe ici

        return toDto(userRepository.save(existingUser));
    }

    // DELETE /users/{id}
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException("Utilisateur introuvable avec l'id : " + id);
        }
        userRepository.deleteById(id);
    }

    // 🔁 Conversion : Entity → DTO
    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // 🔁 Conversion : DTO → Entity
    private User toEntity(UserDto dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                // le password est à ajouter ensuite
                .build();
    }
}