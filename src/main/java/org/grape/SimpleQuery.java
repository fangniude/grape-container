package org.grape;

import com.google.common.base.Strings;
import io.ebean.Expression;
import io.ebean.ExpressionFactory;
import io.ebean.Junction;
import io.ebean.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleQuery implements Serializable {
    private List<String> columns;
    private List<String> searchColumns;
    private String searchText;
    private WhereCondition whereCondition;
    private List<OrderBy> orderByList;
    private int pageSize = Integer.MAX_VALUE;

    /**
     * start with 1
     */
    private int pageIndex = 1;

    public int offSet() {
        return (pageIndex - 1) * pageSize;
    }

    public <T extends BaseDomain> io.ebean.OrderBy<T> orderBy() {
        io.ebean.OrderBy<T> result = new io.ebean.OrderBy<>();
        if (orderByList != null) {
            for (OrderBy orderBY : orderByList) {
                switch (orderBY.getOrder()) {
                    case DESC:
                        result.desc(orderBY.getColumn());
                        break;
                    case ASC:
                    default:
                        result.asc(orderBY.getColumn());
                }
            }
        }
        return result;
    }

    public <T extends BaseDomain> Expression whereExpression(Query<T> query) {
        if (searchColumns != null && !searchColumns.isEmpty() && !Strings.isNullOrEmpty(searchText)) {
            if (whereCondition != null) {
                return query.getExpressionFactory().and(searchExpression(query), whereCondition.whereExpression(query));
            } else {
                return searchExpression(query);
            }
        } else {
            if (whereCondition != null) {
                return whereCondition.whereExpression(query);
            } else {
                return null;
            }
        }
    }

    private <T extends BaseDomain> Expression searchExpression(Query<T> query) {
        ExpressionFactory factory = query.getExpressionFactory();
        Junction<T> disjunction = factory.disjunction(query);
        for (String sc : searchColumns) {
            disjunction.icontains(sc, searchText);
        }
        return disjunction;
    }
}
