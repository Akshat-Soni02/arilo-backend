package com.project_x.project_x_backend.dto.authDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleLoginRequest {
    private String idToken;
    private boolean bypassAuth;
}
