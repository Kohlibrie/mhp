package com.mhp.coding.challenges.retry.core.entities;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class EmailNotification {

    @NotBlank
    private String recipient;

    @NotBlank
    private String subject;

    @NotBlank
    private String text;
}
