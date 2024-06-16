package org.miaixz.bus.notify.magic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.core.lang.Symbol;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(Symbol.ZERO, "Success"),
    FAILURE("-1", "Failure"),
    UNSUPPORTED("5003", "Unsupported operation");

    private String code;
    private String desc;

}
