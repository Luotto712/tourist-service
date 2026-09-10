package com.tourist.service;

import com.tourist.common.BusinessException;
import com.tourist.dto.request.RegisterRequest;
import com.tourist.entity.User;
import com.tourist.mapper.UserMapper;
import com.tourist.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 注册：游客身份、密码加密、用户名查重。 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserMapper userMapper;

    @InjectMocks private AuthServiceImpl service;

    private RegisterRequest req(String username) {
        RegisterRequest r = new RegisterRequest();
        r.setUsername(username);
        r.setPassword("123456");
        r.setPhone("13800000000");
        r.setEmail("a@b.com");
        return r;
    }

    @Test
    void register_createsTouristWithHashedPassword() {
        when(userMapper.findByUsernameIncludeDisabled("newbie")).thenReturn(null);

        service.register(req("newbie"));

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(cap.capture());
        User u = cap.getValue();
        assertEquals("newbie", u.getUsername());
        assertEquals("TOURIST", u.getRole());
        assertEquals("13800000000", u.getPhone());
        assertEquals("a@b.com", u.getEmail());
        assertEquals(1, u.getStatus());
        assertNotEquals("123456", u.getPassword());
    }

    @Test
    void register_duplicateUsername_throws() {
        User existing = new User();
        existing.setId(1L);
        when(userMapper.findByUsernameIncludeDisabled("dup")).thenReturn(existing);

        assertThrows(BusinessException.class, () -> service.register(req("dup")));
        verify(userMapper, never()).insert(any());
    }
}
