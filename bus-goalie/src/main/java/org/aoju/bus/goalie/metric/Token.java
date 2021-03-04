package org.aoju.bus.goalie.metric;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * token参数
 *
 * @author Justubborn
 * @since 2021/3/4
 */
@AllArgsConstructor
@Data
public class Token {

    String token;

    int channel;
}
