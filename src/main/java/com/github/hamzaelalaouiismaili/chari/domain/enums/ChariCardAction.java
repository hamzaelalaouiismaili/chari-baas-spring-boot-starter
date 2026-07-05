package com.github.hamzaelalaouiismaili.chari.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Supported lifecycle actions for an issued card. */
@Getter
@RequiredArgsConstructor
public enum ChariCardAction {
    ACTIVATE("activate"),
    BLOCK("block"),
    SUSPEND("suspend"),
    REACTIVATE("reactivate"),
    CANCEL("cancel"),
    UNBLOCK_PIN("unblock-pin"),
    RESET_PIN("reset-pin");

    private final String path;
}
