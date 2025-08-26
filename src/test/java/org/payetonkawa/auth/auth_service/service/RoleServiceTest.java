package org.payetonkawa.auth.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.payetonkawa.auth.auth_service.repository.UserRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 🔸 createRole - succès
    @Test
    void createRole_shouldCreateNewRole_whenNameIsUnique() {
        String roleName = "NEW_ROLE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        Role savedRole = new Role();
        savedRole.setId(1L);
        savedRole.setName(roleName);

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleName);

        assertThat(result.getName()).isEqualTo(roleName);
        verify(roleRepository).save(any(Role.class));
    }

    // 🔸 createRole - échec si déjà existant
    @Test
    void createRole_shouldThrow_whenRoleAlreadyExists() {
        String roleName = "EXISTING_ROLE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(new Role()));

        assertThatThrownBy(() -> roleService.createRole(roleName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role already exists");
    }

    // 🔸 getAllRoles
    @Test
    void getAllRoles_shouldReturnListOfRoles() {
        List<Role> roles = List.of(new Role(), new Role());
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertThat(result).hasSize(2);
    }

    // 🔸 getRoleById - succès
    @Test
    void getRoleById_shouldReturnRole_whenFound() {
        Role role = new Role();
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        Role result = roleService.getRoleById(1L);

        assertThat(result).isEqualTo(role);
    }

    // 🔸 getRoleById - échec
    @Test
    void getRoleById_shouldThrow_whenNotFound() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role not found");
    }

    // 🔸 updateRole
    @Test
    void updateRole_shouldUpdateRoleName() {
        Role role = new Role();
        role.setId(1L);
        role.setName("OLD");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).then(AdditionalAnswers.returnsFirstArg());

        Role updated = roleService.updateRole(1L, "NEW");

        assertThat(updated.getName()).isEqualTo("NEW");
    }

    // 🔸 deleteRole - succès
    @Test
    void deleteRole_shouldDeleteRole_whenNoUserAssigned() {
        Role role = new Role();
        role.setId(1L);
        role.setName("TO_DELETE");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoles_Name("TO_DELETE")).thenReturn(false);

        roleService.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    // 🔸 deleteRole - échec si utilisateurs assignés
    @Test
    void deleteRole_shouldThrow_whenRoleAssignedToUsers() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ASSIGNED_ROLE");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByRoles_Name("ASSIGNED_ROLE")).thenReturn(true);

        assertThatThrownBy(() -> roleService.deleteRole(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Impossible de supprimer le rôle");
    }
}
