package org.grape;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderBy implements Serializable {
    private String column;
    private Order order;

    public String toSql() {
        return String.format(" %s %s ", column, order.name());
    }

    public enum Order implements Serializable {
        /**
         * 升序
         */
        ASC,
        /**
         * 降序
         */
        DESC;
    }
}
