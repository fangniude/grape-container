package org.grape;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("排序")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderBy implements Serializable {
    @ApiModelProperty("列名")
    private String column;
    @ApiModelProperty("升序降序")
    private Order order;

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
